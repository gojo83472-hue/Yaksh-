package com.example.echoshift.ui

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echoshift.game.GameEngine

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ControlsOverlay(
    engine: GameEngine,
    onMove: (Float) -> Unit,
    onJump: (Boolean) -> Unit,
    onDash: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Left Side: Directional Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp, start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var leftPressed by remember { mutableStateOf(false) }
            var rightPressed by remember { mutableStateOf(false) }

            // Move Left Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (leftPressed) Color(0x9900F0FF) else Color(0x660F172A))
                    .border(2.dp, if (leftPressed) Color(0xFF00F0FF) else Color(0xFF334155), CircleShape)
                    .testTag("btn_left")
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                leftPressed = true
                                onMove(-1f)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                leftPressed = false
                                onMove(if (rightPressed) 1f else 0f)
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Move Left",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Move Right Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (rightPressed) Color(0x9900F0FF) else Color(0x660F172A))
                    .border(2.dp, if (rightPressed) Color(0xFF00F0FF) else Color(0xFF334155), CircleShape)
                    .testTag("btn_right")
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                rightPressed = true
                                onMove(1f)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                rightPressed = false
                                onMove(if (leftPressed) -1f else 0f)
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Move Right",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Right Side: Action Cluster (Jump, Dash, Echo Record, Echo Swap, Pulse)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Echo Swap Button (Q)
            if (engine.level.levelNumber >= 6) {
                val canSwap = engine.echoState.isActive && engine.echoSwapCooldownMs == 0L
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (canSwap) Color(0x997E22CE) else Color(0x44334155))
                        .border(1.5.dp, if (canSwap) Color(0xFFA855F7) else Color(0xFF475569), CircleShape)
                        .testTag("btn_swap")
                        .pointerInteropFilter { motionEvent ->
                            if (motionEvent.action == MotionEvent.ACTION_DOWN && canSwap) {
                                engine.performEchoSwap()
                                true
                            } else false
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Echo Swap",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("SWAP", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Phase Pulse Attack Button (F)
            if (engine.level.levelNumber >= 26) {
                val canPulse = engine.pulseCooldownMs == 0L && engine.playerEnergy >= 35f
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (canPulse) Color(0x990284C7) else Color(0x44334155))
                        .border(1.5.dp, if (canPulse) Color(0xFF38BDF8) else Color(0xFF475569), CircleShape)
                        .testTag("btn_pulse")
                        .pointerInteropFilter { motionEvent ->
                            if (motionEvent.action == MotionEvent.ACTION_DOWN && canPulse) {
                                engine.triggerPhasePulse()
                                true
                            } else false
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Phase Pulse",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("PULSE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Echo Record Button (E)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (engine.isRecording) Color(0xCCEF4444)
                        else Color(0x998B5CF6)
                    )
                    .border(
                        2.dp,
                        if (engine.isRecording) Color(0xFFFF4D4D) else Color(0xFFA78BFA),
                        CircleShape
                    )
                    .testTag("btn_echo")
                    .pointerInteropFilter { motionEvent ->
                        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                            engine.toggleEchoRecord()
                            true
                        } else false
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (engine.isRecording) Icons.Default.FiberManualRecord else Icons.Default.GraphicEq,
                        contentDescription = "Echo Record",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (engine.isRecording) "DEPLOY" else "ECHO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Dash Button (Shift)
            if (engine.level.levelNumber >= 16) {
                val canDash = engine.dashCooldownMs == 0L && engine.playerEnergy >= 25f
                var dashPressed by remember { mutableStateOf(false) }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (canDash) Color(0x9906B6D4) else Color(0x44334155))
                        .border(1.5.dp, if (canDash) Color(0xFF22D3EE) else Color(0xFF475569), CircleShape)
                        .testTag("btn_dash")
                        .pointerInteropFilter { motionEvent ->
                            when (motionEvent.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    if (canDash) {
                                        dashPressed = true
                                        onDash(true)
                                    }
                                    true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    dashPressed = false
                                    onDash(false)
                                    true
                                }
                                else -> false
                            }
                        }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Dash",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("DASH", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Jump Button (Space)
            var jumpPressed by remember { mutableStateOf(false) }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (jumpPressed) listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            else listOf(Color(0xBB0284C7), Color(0xBB0F172A))
                        )
                    )
                    .border(2.dp, Color(0xFF38BDF8), CircleShape)
                    .testTag("btn_jump")
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                jumpPressed = true
                                onJump(true)
                                true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                jumpPressed = false
                                onJump(false)
                                true
                            }
                            else -> false
                        }
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Jump",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text("JUMP", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}
