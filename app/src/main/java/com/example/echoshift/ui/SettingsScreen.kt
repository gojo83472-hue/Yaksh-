package com.example.echoshift.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.data.SettingsEntity

@Composable
fun SettingsScreen(
    currentSettings: SettingsEntity,
    onSaveSettings: (SettingsEntity) -> Unit,
    onResetAllProgress: () -> Unit,
    onBack: () -> Unit
) {
    var sfxVol by remember { mutableFloatStateOf(currentSettings.sfxVolume) }
    var musicVol by remember { mutableFloatStateOf(currentSettings.musicVolume) }
    var haptics by remember { mutableStateOf(currentSettings.hapticsEnabled) }
    var screenShake by remember { mutableStateOf(currentSettings.screenShakeEnabled) }
    var showTimer by remember { mutableStateOf(currentSettings.showTimer) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080C16))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onSaveSettings(
                        currentSettings.copy(
                            sfxVolume = sfxVol,
                            musicVolume = musicVol,
                            hapticsEnabled = haptics,
                            screenShakeEnabled = screenShake,
                            showTimer = showTimer
                        )
                    )
                    onBack()
                },
                modifier = Modifier.testTag("settings_back_btn")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM SETTINGS",
                color = Color(0xFF00F0FF),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Body in Cards
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SFX Volume
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound FX Volume", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("${(sfxVol * 100).toInt()}%", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = sfxVol,
                        onValueChange = { sfxVol = it },
                        modifier = Modifier.testTag("sfx_slider")
                    )
                }

                // Music Volume
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Music Volume", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("${(musicVol * 100).toInt()}%", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = musicVol,
                        onValueChange = { musicVol = it },
                        modifier = Modifier.testTag("music_slider")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Haptics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Tactile Haptics", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Vibration on dash, jump & impact", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Switch(
                        checked = haptics,
                        onCheckedChange = { haptics = it },
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                // Screen Shake Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dynamic Screen Shake", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Visual rumble on explosions & damage", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Switch(
                        checked = screenShake,
                        onCheckedChange = { screenShake = it },
                        modifier = Modifier.testTag("shake_switch")
                    )
                }

                // Speedrun Timer Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Chrono Speedrun Timer", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Display precision stopwatch in HUD", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                    Switch(
                        checked = showTimer,
                        onCheckedChange = { showTimer = it },
                        modifier = Modifier.testTag("timer_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Reset Progress
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth().testTag("reset_data_btn")
                ) {
                    Text("RESET ALL GAME PROGRESS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?", color = Color.White) },
            text = { Text("This will wipe all 50 level scores, stars, and trophies. This action cannot be reversed.", color = Color(0xFFCBD5E1)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetAllProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("YES, WIPE DATA")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL")
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
