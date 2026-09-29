package com.example.echoshift.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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

@Composable
fun GameOverDialog(
    hint: String,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B14)),
            modifier = Modifier
                .width(340.dp)
                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "TEMPORAL DESYNC",
                    color = Color(0xFFEF4444),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = "Chrono Runner Terminated",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )

                // Tactical Hint
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1F1322),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "TACTICAL ADVICE:",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hint,
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth().testTag("game_over_retry_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RETRY SECTOR", fontWeight = FontWeight.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onLevelSelect,
                        modifier = Modifier.weight(1f).testTag("game_over_levels_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SECTORS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onMainMenu,
                        modifier = Modifier.weight(1f).testTag("game_over_menu_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("MENU", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
