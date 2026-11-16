package com.al9yser.mufakkik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Channel(
    val id: String,
    val name: String,
    val category: String,
    val quality: String,
    val url: String,
    val isLive: Boolean = true,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MipxTheme {
                MipxApp()
            }
        }
    }
}

@Composable
private fun MipxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4CC9F0),
            secondary = Color(0xFF7C4DFF),
            background = Color(0xFF07111C),
            surface = Color(0xFF101B2C),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MipxApp() {
    val categories = listOf("الكل", "الأفلام", "الرياضة", "الأخبار", "الأطفال", "وثائقي")
    val channels = remember {
        listOf(
            Channel("1", "OSN Movies", "الأفلام", "1080p", "https://example.com/stream1.m3u8"),
            Channel("2", "beIN Sports", "الرياضة", "1080p", "https://example.com/stream2.m3u8"),
            Channel("3", "BBC News", "الأخبار", "720p", "https://example.com/stream3.m3u8"),
            Channel("4", "Cartoon Network", "الأطفال", "720p", "https://example.com/stream4.m3u8"),
            Channel("5", "National Geographic", "وثائقي", "1080p", "https://example.com/stream5.m3u8"),
            Channel("6", "MBC Action", "الأفلام", "1080p", "https://example.com/stream6.m3u8"),
            Channel("7", "Al Jazeera", "الأخبار", "720p", "https://example.com/stream7.m3u8"),
            Channel("8", "Sky Sport", "الرياضة", "4K", "https://example.com/stream8.m3u8"),
        )
    }

    var selectedCategory by remember { mutableStateOf("الكل") }
    var query by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf<Channel?>(null) }

    val filteredChannels = channels.filter {
        val matchesCategory = selectedCategory == "الكل" || it.category == selectedCategory
        val matchesQuery = query.isBlank() || it.name.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Scaffold(
        containerColor = Color(0xFF07111C),
        topBar = {
            TopAppBar(
                title = {
                    Text("Mipx", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF07111C),
                    titleContentColor = Color.White,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0E1A2B)) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("الرئيسية") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.LiveTv, null) },
                    label = { Text("قنوات") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Movie, null) },
                    label = { Text("أفلام") },
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("إعدادات") },
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4CC9F0),
                                Color(0xFF7C4DFF),
                                Color(0xFF0B1F33),
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column {
                    Text("IPTV Streaming", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Browse channels and enjoy live TV", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { selectedChannel = filteredChannels.firstOrNull() ?: channels.first() }) {
                        Text("Open Player")
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("بحث عن قناة أو فئة") },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredChannels) { channel ->
                    ChannelCard(channel = channel) {
                        selectedChannel = channel
                    }
                }
            }

            selectedChannel?.let { channel ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101E30)),
                    shape = RoundedCornerShape(22.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CC9F0))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${channel.category} • ${channel.quality}", color = Color(0xFFB7D0E2), fontSize = 12.sp)
                        }
                        Button(onClick = { }) {
                            Text("تشغيل")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(channel: Channel, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1D2D)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4CC9F0)),
                contentAlignment = Alignment.Center,
            ) {
                Text(channel.name.firstOrNull()?.toString() ?: "?", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(channel.category, color = Color(0xFF9EB7C8), fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(channel.quality, color = Color(0xFF9BE9C9), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Text(if (channel.isLive) "Live" else "VOD", color = Color(0xFF9DB5C8), fontSize = 10.sp)
            }
        }
    }
}
