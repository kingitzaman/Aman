package com.example.data.search

data class SearchResult(
    val title: String,
    val url: String,
    val domain: String,
    val snippet: String,
    val publishedDate: String? = null,
    val source: String,
    val thumbnail: String? = null,
    val category: String = "web" // "web", "images", "videos", "news"
)

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<SearchResult>, val query: String) : SearchState()
    object Empty : SearchState()
    data class Error(val message: String) : SearchState()
    data class NotConfigured(val message: String) : SearchState()
    data class SecurityBlocked(val reason: String) : SearchState()
}

object SearchSecurityValidator {
    private val blockedDomains = setOf(
        "localhost", "127.0.0.1", "0.0.0.0", "169.254.169.254", "internal", "local"
    )

    fun validateQuery(query: String): Pair<Boolean, String?> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return false to "Search query cannot be empty"
        if (trimmed.length > 500) return false to "Query exceeds safety character limit (500 chars)"
        return true to null
    }

    fun isSafeUrl(url: String): Boolean {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host?.lowercase() ?: return false
            val scheme = uri.scheme?.lowercase() ?: return false

            if (scheme != "http" && scheme != "https") return false
            if (blockedDomains.any { host == it || host.endsWith(".$it") }) return false
            // Block private IP ranges (SSRF protection)
            if (host.matches(Regex("""^(10\.|172\.(1[6-9]|2[0-9]|3[0-1])\.|192\.168\.).*"""))) return false

            true
        } catch (e: Exception) {
            false
        }
    }

    fun sanitizeText(input: String): String {
        return input.replace("<script.*?>.*?</script>".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("<.*?>".toRegex(), "")
            .trim()
    }
}
