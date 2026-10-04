package com.example.ui.screens.search

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.SearchHistoryDao
import com.example.data.search.SearchResult
import com.example.data.search.SearchService
import com.example.data.search.SearchState
import com.example.ui.components.AmanixTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentAmber
import com.example.ui.theme.AmanixAccentGreen
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixCyanContainer
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    searchService: SearchService,
    searchHistoryDao: SearchHistoryDao,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val searchTabs = listOf("Web", "Images", "Videos", "News")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchState by remember { mutableStateOf<SearchState>(SearchState.Idle) }

    val recentSearches by searchHistoryDao.getRecentSearches().collectAsState(initial = emptyList())

    fun executeSearch(query: String) {
        if (query.isBlank()) return
        coroutineScope.launch {
            searchState = SearchState.Loading
            searchState = searchService.search(query, searchTabs[selectedTabIndex].lowercase())
        }
    }

    Scaffold(
        topBar = {
            AmanixTopBar(
                title = "Web Search",
                onMenuClick = onOpenDrawer,
                onProfileClick = onOpenSettings,
                statusText = "Security Shield Active",
                isStatusOk = true
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = keyboardAndNavigationBottomPadding())
        ) {
            // Category Tabs in Frosted Glass Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .glassmorphic(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color(0x350A1424),
                        borderColor = Color(0x3500D2FF)
                    )
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = AmanixCyanPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AmanixCyanPrimary
                        )
                    }
                ) {
                    searchTabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                if (searchQuery.isNotBlank()) executeSearch(searchQuery)
                            },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) AmanixCyanPrimary else AmanixTextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Security Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AmanixAccentGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SSRF, Private IP & Dangerous URL Filter Active",
                    fontSize = 11.sp,
                    color = AmanixTextSecondary
                )
            }

            // Content Area (Results / Status / History)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                when (val state = searchState) {
                    is SearchState.Idle -> {
                        if (recentSearches.isNotEmpty()) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = AmanixCyanPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Recent Searches", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AmanixTextSecondary)
                                    }
                                    IconButton(
                                        onClick = { coroutineScope.launch { searchHistoryDao.clearSearchHistory() } }
                                    ) {
                                        Text("Clear", fontSize = 11.sp, color = AmanixTextMuted)
                                    }
                                }
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(recentSearches) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .glassmorphic(
                                                    shape = RoundedCornerShape(10.dp),
                                                    backgroundColor = Color(0x2514243A),
                                                    borderColor = Color(0x20FFFFFF)
                                                )
                                                .clickable {
                                                    searchQuery = item.query
                                                    executeSearch(item.query)
                                                }
                                                .padding(vertical = 10.dp, horizontal = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = AmanixTextMuted, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(item.query, color = AmanixTextPrimary, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.weight(1f))
                                            StatusBadge(status = item.category.uppercase())
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = Color(0x3500D2FF), modifier = Modifier.size(52.dp))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Amanix Real Search Engine", fontWeight = FontWeight.Bold, color = AmanixTextSecondary, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Type your query below to retrieve live verified web information.",
                                    fontSize = 12.sp,
                                    color = AmanixTextMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    is SearchState.Loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = AmanixCyanPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Querying configured search provider...", color = AmanixCyanPrimary, fontSize = 13.sp)
                        }
                    }

                    is SearchState.NotConfigured -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphic(
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = Color(0x351F2433),
                                    borderColor = AmanixAccentAmber.copy(alpha = 0.5f)
                                )
                                .padding(20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                StatusBadge(status = "NOT CONFIGURED")
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Web Search Provider Offline",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AmanixTextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 13.sp,
                                    color = AmanixTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 19.sp
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = onOpenSettings,
                                    colors = ButtonDefaults.buttonColors(containerColor = AmanixCyanPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Configure Search Provider", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    is SearchState.SecurityBlocked -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphic(
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = Color(0x352A1218),
                                    borderColor = AmanixAccentRed.copy(alpha = 0.6f)
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                StatusBadge(status = "SECURITY BLOCKED")
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Security Violation Blocked",
                                    color = AmanixAccentRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(state.reason, color = AmanixTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    is SearchState.Empty -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("No results found.", fontWeight = FontWeight.Bold, color = AmanixTextPrimary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Try refining your search terms or selecting another category.", color = AmanixTextMuted, fontSize = 12.sp)
                        }
                    }

                    is SearchState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .glassmorphic(
                                    shape = RoundedCornerShape(14.dp),
                                    backgroundColor = Color(0x352E141B),
                                    borderColor = AmanixAccentRed.copy(alpha = 0.5f)
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                StatusBadge(status = "FAILED")
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(state.message, color = AmanixAccentRed, fontSize = 13.sp)
                            }
                        }
                    }

                    is SearchState.Success -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(state.results) { result ->
                                SearchResultCard(
                                    result = result,
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Safe fallback
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Glassmorphic Search Composer (Anchored right above the keyboard)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .glassmorphic(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0x450C1628),
                        borderColor = Color(0x5500D2FF),
                        borderWidth = 1.dp
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AmanixCyanPrimary,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .size(20.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("What do you want to search?", color = AmanixTextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_query_input")
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = ""; searchState = SearchState.Idle },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AmanixTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { executeSearch(searchQuery) },
                        enabled = searchQuery.isNotBlank(),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AmanixCyanPrimary, Color(0xFF0099CC))
                                )
                            )
                            .border(1.dp, Color(0x80FFFFFF), CircleShape)
                            .testTag("search_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Search",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                shape = RoundedCornerShape(14.dp),
                backgroundColor = Color(0x35101E35),
                borderColor = Color(0x3500D2FF)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.domain,
                    fontSize = 11.sp,
                    color = AmanixCyanPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Link",
                    tint = AmanixTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AmanixTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.snippet,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = AmanixTextSecondary
            )
        }
    }
}
