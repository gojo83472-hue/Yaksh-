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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Locale

@Composable
fun VictoryDialog(
    levelNumber: Int,
    timeSeconds: Float,
    parTimeSeconds: Int,
    coresCollected: Int,
    starsEarned: Int,
    unlockedAbility: String?,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF090E17)),
            modifier = Modifier
                .width(360.dp)
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "QUANTUM RIFT STABILIZED",
                    color = Color(0xFF00F0FF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Sector $levelNumber Cleared!",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Star rating row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val active = i <= starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (active) Color(0xFFFFD700) else Color(0xFF334155),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Stats breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Clear Time:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text(
                            text = String.format(Locale.US, "%.2fs (Par: %ds)", timeSeconds, parTimeSeconds),
                            color = if (timeSeconds <= parTimeSeconds) Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quantum Cores:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Text(
                            text = "$coresCollected / 3",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Unlocked Ability notification
                if (unlockedAbility != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33A855F7), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NEW ABILITY UNLOCKED: $unlockedAbility!",
                            color = Color(0xFFE9D5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth().testTag("victory_next_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("PROCEED TO NEXT SECTOR", color = Color.Black, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier.weight(1f).testTag("victory_replay_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REPLAY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onLevelSelect,
                        modifier = Modifier.weight(1f).testTag("victory_levels_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SECTORS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
