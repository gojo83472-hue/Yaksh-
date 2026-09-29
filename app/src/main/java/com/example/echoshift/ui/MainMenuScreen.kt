package com.example.echoshift.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.data.LevelProgressEntity

@Composable
fun MainMenuScreen(
    levelProgressList: List<LevelProgressEntity>,
    onPlay: () -> Unit,
    onLevelSelect: () -> Unit,
    onTutorial: () -> Unit,
    onAchievements: () -> Unit,
    onSettings: () -> Unit
) {
    val totalStars = levelProgressList.sumOf { it.stars }
    val totalCores = levelProgressList.sumOf { it.quantumCoresCollected }
    val completedCount = levelProgressList.count { it.isCompleted }

    // Subtle neon pulse animation for title
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060913),
                        Color(0xFF0F172A),
                        Color(0xFF0A0E1A)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stats Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$totalStars / 150", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Adjust, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$totalCores Cores", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                IconButton(
                    onClick = onSettings,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .testTag("menu_settings_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF38BDF8))
                }
            }

            // Title & Futuristic Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // Futuristic Echo Brand Subtitle
                Text(
                    text = "CHRONO - RECURSION PROTOCOL",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Brand Title with Neon Shadow
                Text(
                    text = "ECHO SHIFT",
                    color = Color(0xFF00F0FF).copy(alpha = glowAlpha),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Record the Past. Conquer the Present.",
                    color = Color(0xFFA855F7),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }

            // Main Menu Action Buttons (Landscape / Adaptive layout)
            Column(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // START / CONTINUE
                Button(
                    onClick = onPlay,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("menu_play_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (completedCount > 0) "CONTINUE RUN" else "START MISSION",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                // SELECT SECTOR (50 LEVELS)
                Button(
                    onClick = onLevelSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("menu_levels_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "50 SECTORS & BIOMES",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // TUTORIAL
                    OutlinedButton(
                        onClick = onTutorial,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("menu_tut_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFA855F7)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFA855F7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TUTORIAL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // ACHIEVEMENTS
                    OutlinedButton(
                        onClick = onAchievements,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("menu_achieve_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TROPHIES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Footer info
            Text(
                text = "Version 1.0 • 12 Worlds • 50 Unique Levels • Keyboard & Touch Ready",
                color = Color(0xFF475569),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
