package com.example.echoshift.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.data.LevelProgressEntity
import com.example.echoshift.model.Biome
import java.util.Locale

@Composable
fun LevelSelectScreen(
    levels: List<LevelProgressEntity>,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    var selectedBiomeFilter by remember { mutableStateOf<Biome?>(null) }

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
                    modifier = Modifier.testTag("level_select_back_btn")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "TEMPORAL SECTORS",
                        color = Color(0xFF00F0FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "50 Chambers Across 12 Biomes",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Stats
            val totalUnlocked = levels.count { it.isUnlocked }
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "UNLOCKED: $totalUnlocked / 50",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid of 50 Levels
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 135.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val all50 = (1..50).map { lvlNum ->
                levels.find { it.levelNumber == lvlNum } ?: LevelProgressEntity(
                    levelNumber = lvlNum,
                    isUnlocked = lvlNum == 1
                )
            }

            items(all50) { progress ->
                val lvlNum = progress.levelNumber
                val biome = Biome.forLevel(lvlNum)
                val isBoss = lvlNum in listOf(10, 20, 25, 30, 35, 40, 50)
                val unlocked = progress.isUnlocked

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked) Color(0xFF0F172A) else Color(0xFF090D18)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(
                            width = if (isBoss && unlocked) 2.dp else 1.dp,
                            color = when {
                                !unlocked -> Color(0xFF1E293B)
                                isBoss -> Color(0xFFFF0055)
                                progress.isCompleted -> Color(biome.primaryColorHex)
                                else -> Color(0xFF38BDF8)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = unlocked) {
                            onSelectLevel(lvlNum)
                        }
                        .testTag("level_item_$lvlNum")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row: Level num + Biome label or Lock icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SEC $lvlNum",
                                color = if (unlocked) Color.White else Color(0xFF64748B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )

                            if (!unlocked) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isBoss) {
                                Surface(
                                    color = Color(0xFFFF0055),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "BOSS",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = biome.displayName.split(" ").first(),
                                    color = Color(biome.primaryColorHex),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Middle: Stars
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (s in 1..3) {
                                val hasStar = s <= progress.stars
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (hasStar) Color(0xFFFFD700) else Color(0xFF334155),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Bottom: Best time & Cores
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (progress.bestTimeSeconds > 0) String.format(Locale.US, "%.1fs", progress.bestTimeSeconds) else "--",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Adjust,
                                    contentDescription = null,
                                    tint = if (progress.quantumCoresCollected > 0) Color(0xFF00F0FF) else Color(0xFF475569),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${progress.quantumCoresCollected}/3",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
