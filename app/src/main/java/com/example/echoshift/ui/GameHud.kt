package com.example.echoshift.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.game.GameEngine
import java.util.Locale

@Composable
fun GameHud(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Left: Player Status (Health & Energy & Level Title)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(
                    Color(0xCC090D1A),
                    RoundedCornerShape(10.dp)
                )
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Level Title & Biome
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LVL ${engine.level.levelNumber}: ${engine.level.title}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = engine.level.biome.displayName,
                    color = Color(engine.level.biome.primaryColorHex),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Health Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HP",
                    color = Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(22.dp)
                )
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    val hpRatio = (engine.playerHealth / engine.maxHealth).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(hpRatio)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFEF4444), Color(0xFF10B981))
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Energy Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "EN",
                    color = Color(0xFF00F0FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(22.dp)
                )
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    val energyRatio = (engine.playerEnergy / engine.maxEnergy).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(energyRatio)
                            .background(Color(0xFF00F0FF))
                    )
                }
            }
        }

        // Top Center: Echo State Banner
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(
                    if (engine.isRecording) Color(0xDD7E22CE)
                    else if (engine.echoState.isActive) Color(0xDD0C4A6E)
                    else Color(0x990F172A),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    if (engine.isRecording) Color(0xFFA855F7)
                    else if (engine.echoState.isActive) Color(0xFF00F0FF)
                    else Color(0xFF334155),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            val echoText = when {
                engine.isRecording -> {
                    val secLeft = ((engine.maxRecordTimeMs - engine.recordTimeMs) / 1000f).coerceAtLeast(0f)
                    "RECORDING ECHO (${String.format(Locale.US, "%.1fs", secLeft)})"
                }
                engine.echoState.isActive -> "ECHO ACTIVE (LOOPING)"
                else -> "ECHO READY"
            }
            Text(
                text = echoText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Top Right: Timer, Cores, Pause
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantum Cores collected
            Row(
                modifier = Modifier
                    .background(Color(0xCC090D1A), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val collected = i <= engine.coresCollectedCount
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Core $i",
                        tint = if (collected) Color(0xFFFFD700) else Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Elapsed time
                Text(
                    text = String.format(Locale.US, "%02d:%02d", (engine.elapsedTimeSeconds / 60).toInt(), (engine.elapsedTimeSeconds % 60).toInt()),
                    color = if (engine.elapsedTimeSeconds <= engine.level.parTimeSeconds) Color(0xFF10B981) else Color(0xFFF59E0B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Pause Button
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xCC090D1A), CircleShape)
                    .border(1.dp, Color(0xFF38BDF8), CircleShape)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
