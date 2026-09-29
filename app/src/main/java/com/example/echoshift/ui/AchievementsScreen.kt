package com.example.echoshift.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.data.AchievementEntity

@Composable
fun AchievementsScreen(
    achievements: List<AchievementEntity>,
    onBack: () -> Unit
) {
    val unlockedCount = achievements.count { it.isUnlocked }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C16))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("achievements_back_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "CHRONO TROPHIES",
                        color = Color(0xFFFFD700),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$unlockedCount / ${achievements.size} Unlocked",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            // Progress Bar
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${((unlockedCount.toFloat() / maxOf(1, achievements.size)) * 100).toInt()}% COMPLETE",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(achievements) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isUnlocked) Color(0xFF0F172A) else Color(0xFF0A0F1D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (item.isUnlocked) Color(0xFFFFD700) else Color(0xFF1E293B),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge Icon
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (item.isUnlocked) Color(0x33FFD700) else Color(0xFF1E293B),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (item.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (item.isUnlocked) Color(0xFFFFD700) else Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = if (item.isUnlocked) Color.White else Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                color = if (item.isUnlocked) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 12.sp
                            )
                        }

                        if (item.isUnlocked) {
                            Text(
                                text = "UNLOCKED",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
