package com.example.data.search

import com.example.data.local.dao.SearchHistoryDao
import com.example.data.local.dao.SettingDao
import com.example.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

interface SearchProvider {
    val name: String
    suspend fun isConfigured(): Boolean
    suspend fun search(query: String, category: String): SearchState
}

class SearchService(
    private val settingDao: SettingDao,
    private val searchHistoryDao: SearchHistoryDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun isConfigured(): Boolean {
        val apiKey = settingDao.getSetting("search_api_key")
        return !apiKey.isNullOrBlank()
    }

    suspend fun search(query: String, category: String = "web"): SearchState = withContext(Dispatchers.IO) {
        val (isValid, errorReason) = SearchSecurityValidator.validateQuery(query)
        if (!isValid) {
            return@withContext SearchState.SecurityBlocked(errorReason ?: "Invalid query")
        }

        val apiKey = settingDao.getSetting("search_api_key")
        val endpoint = settingDao.getSetting("search_endpoint") ?: "https://api.searchprovider.io/v1"

        if (apiKey.isNullOrBlank()) {
            return@withContext SearchState.NotConfigured(
                "Web search is not configured. To search live $category, configure your Search Provider & API Key in Amanix Settings."
            )
        }

        // Save real query to local search history
        searchHistoryDao.insertSearch(SearchHistoryEntity(query = query, category = category))

        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val requestUrl = "$endpoint/search?q=$encodedQuery&category=$category&key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .header("User-Agent", "Amanix-AI-Search/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext SearchState.Error("Search failed — HTTP error ${response.code}. Try again.")
            }

            val body = response.body?.string().orEmpty()
            if (body.isBlank()) {
                return@withContext SearchState.Empty
            }

            val json = JSONObject(body)
            val items = json.optJSONArray("results") ?: json.optJSONArray("items")
            if (items == null || items.length() == 0) {
                return@withContext SearchState.Empty
            }

            val results = mutableListOf<SearchResult>()
            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val rawUrl = item.optString("url", item.optString("link", ""))
                if (!SearchSecurityValidator.isSafeUrl(rawUrl)) {
                    continue // Skip unsafe or SSRF-prone URLs
                }

                val title = SearchSecurityValidator.sanitizeText(item.optString("title", "Untitled"))
                val snippet = SearchSecurityValidator.sanitizeText(item.optString("snippet", item.optString("description", "")))
                val domain = try {
                    java.net.URI(rawUrl).host ?: "web"
                } catch (e: Exception) {
                    "web"
                }

                results.add(
                    SearchResult(
                        title = title,
                        url = rawUrl,
                        domain = domain,
                        snippet = snippet,
                        publishedDate = if (item.has("publishedDate")) item.optString("publishedDate") else null,
                        source = item.optString("source", domain),
                        thumbnail = if (item.has("thumbnail")) item.optString("thumbnail") else null,
                        category = category
                    )
                )
            }

            if (results.isEmpty()) {
                SearchState.Empty
            } else {
                SearchState.Success(results, query)
            }
        } catch (e: Exception) {
            SearchState.Error("Search failed — ${e.localizedMessage ?: "Unable to connect to search service"}. Try again.")
        }
    }
}
