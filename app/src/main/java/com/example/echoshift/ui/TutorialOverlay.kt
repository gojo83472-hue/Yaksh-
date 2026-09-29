package com.example.echoshift.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun TutorialOverlay(
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B18)),
            modifier = Modifier
                .width(420.dp)
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with step dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ECHO PROTOCOL ARCHIVE",
                        color = Color(0xFF00F0FF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "STEP $step / 4",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Step specific content
                when (step) {
                    1 -> {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Agility & Navigation",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Use Left/Right buttons (or A/D keys) to run. Tap Jump (or Space) to vault over gaps and obstacles. Caelen has high-friction cybernetics for crisp momentum.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    2 -> {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Temporal Echo Recording",
                            color = Color(0xFFE9D5FF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the ECHO button (or 'E' key) to begin recording your timeline. Run, jump, and step onto pressure plates. Tap DEPLOY to manifest a holographic duplicate that loops those exact actions!",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    3 -> {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Switches, Doors & Lasers",
                            color = Color(0xFFA7F3D0),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Momentary pressure plates release when stepped off. Use your Echo to hold one switch while you navigate to the second plate or run through the opened blast door!",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Echo Swap & Combat",
                            color = Color(0xFFBAE6FD),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap SWAP (or 'Q') to trade places with your active Echo clone, stunning nearby hazards! Use DASH (Shift) to phase through laser grids, and PULSE (F) to discharge EMP bursts!",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier.weight(1f).testTag("tut_prev_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("BACK")
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 4) step++ else onDismiss()
                        },
                        modifier = Modifier.weight(1f).testTag("tut_next_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (step < 4) "NEXT" else "START MISSION",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
