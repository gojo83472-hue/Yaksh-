package com.example.echoshift.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.echoshift.game.GameEngine
import com.example.echoshift.model.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate dynamic scale to fit 900x550 virtual game coordinate space nicely
        val scaleX = canvasWidth / 900f
        val scaleY = canvasHeight / 550f
        val scale = minOf(scaleX, scaleY).coerceAtLeast(0.8f)

        // Screen shake
        val shakeX = if (engine.screenShakeAmount > 0) ((Math.random() - 0.5) * engine.screenShakeAmount * 6f).toFloat() else 0f
        val shakeY = if (engine.screenShakeAmount > 0) ((Math.random() - 0.5) * engine.screenShakeAmount * 6f).toFloat() else 0f

        val camX = engine.cameraX - shakeX
        val camY = engine.cameraY - shakeY

        val biome = engine.level.biome

        // 1. Draw Biome Background
        drawBackground(biome, camX, camY)

        // 2. Draw Game World with Camera Transformation
        withTransform({
            scale(scale, scale, Offset.Zero)
            translate(-camX, -camY)
        }) {
            // Level Goal / Quantum Rift
            drawGoal(engine.level.goalX, engine.level.goalY, engine.level.goalWidth, engine.level.goalHeight, engine.elapsedTimeSeconds)

            // Platforms
            for (p in engine.platforms) {
                if (!p.isCrumbled) {
                    drawPlatform(p, biome, engine.elapsedTimeSeconds)
                }
            }

            // Hazards
            for (h in engine.hazards) {
                drawHazard(h, biome, engine.elapsedTimeSeconds)
            }

            // Switches & Pressure Plates
            for (sw in engine.switches) {
                drawSwitch(sw)
            }

            // Doors
            for (d in engine.doors) {
                drawDoor(d, biome)
            }

            // Lasers
            for (l in engine.lasers) {
                drawLaser(l, engine.elapsedTimeSeconds)
            }

            // Teleporters
            for (tp in engine.teleporters) {
                drawTeleporter(tp, engine.elapsedTimeSeconds)
            }

            // Collectibles
            for (c in engine.collectibles) {
                if (!c.isCollected) {
                    drawCollectible(c, engine.elapsedTimeSeconds)
                }
            }

            // Enemies
            for (e in engine.enemies) {
                if (!e.isDefeated) {
                    drawEnemy(e, engine.elapsedTimeSeconds)
                }
            }

            // Projectiles
            for (proj in engine.projectiles) {
                drawProjectile(proj)
            }

            // Echo Copy (Holographic duplicate)
            if (engine.echoState.isActive) {
                drawCharacter(
                    x = engine.echoState.x,
                    y = engine.echoState.y,
                    width = engine.playerWidth,
                    height = engine.playerHeight,
                    facingRight = engine.echoState.facingRight,
                    isDashing = engine.echoState.isDashing,
                    isEcho = true,
                    timeSeconds = engine.elapsedTimeSeconds
                )
            }

            // Player Character
            drawCharacter(
                x = engine.playerX,
                y = engine.playerY,
                width = engine.playerWidth,
                height = engine.playerHeight,
                facingRight = engine.facingRight,
                isDashing = engine.isDashing,
                isEcho = false,
                isInvulnerable = engine.invulnerableTimerMs > 0,
                isRecording = engine.isRecording,
                timeSeconds = engine.elapsedTimeSeconds
            )

            // Particles
            for (p in engine.particles) {
                drawCircle(
                    color = Color(p.colorHex).copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}

private fun DrawScope.drawBackground(biome: Biome, camX: Float, camY: Float) {
    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(biome.bgGradientTopHex),
                Color(biome.bgGradientBottomHex)
            )
        )
    )

    // Parallax background grid / silhouettes
    val gridOffset = -(camX * 0.2f) % 80f
    for (gx in -80..size.width.toInt() + 80 step 80) {
        drawLine(
            color = Color(biome.primaryColorHex).copy(alpha = 0.08f),
            start = Offset(gx + gridOffset, 0f),
            end = Offset(gx + gridOffset, size.height),
            strokeWidth = 1f
        )
    }

    // Distant horizon skyline blocks
    val skylineY = size.height * 0.75f - (camY * 0.1f)
    for (i in 0..12) {
        val bx = (i * 120f - (camX * 0.15f)) % (size.width + 120f)
        val bh = 70f + ((i * 37) % 110)
        drawRect(
            color = Color(biome.primaryColorHex).copy(alpha = 0.06f),
            topLeft = Offset(bx - 60f, skylineY - bh),
            size = Size(80f, bh + 150f)
        )
    }
}

private fun DrawScope.drawPlatform(p: Platform, biome: Biome, time: Float) {
    val baseColor = when (p.type) {
        PlatformType.ICE -> Color(0xFF67E8F9)
        PlatformType.CRUMBLING -> Color(0xFF94A3B8)
        PlatformType.CONVEYOR_RIGHT, PlatformType.CONVEYOR_LEFT -> Color(0xFFEAB308)
        PlatformType.MOVING_H, PlatformType.MOVING_V -> Color(0xFF38BDF8)
        else -> Color(0xFF1E293B)
    }

    // Platform body
    drawRoundRect(
        color = baseColor,
        topLeft = Offset(p.x, p.y),
        size = Size(p.width, p.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Glowing top rim
    val rimColor = if (p.type == PlatformType.ICE) Color(0xFFE0F2FE) else Color(biome.primaryColorHex)
    drawLine(
        color = rimColor,
        start = Offset(p.x, p.y),
        end = Offset(p.x + p.width, p.y),
        strokeWidth = 3f
    )

    // Conveyor animated chevrons
    if (p.type == PlatformType.CONVEYOR_RIGHT || p.type == PlatformType.CONVEYOR_LEFT) {
        val dir = if (p.type == PlatformType.CONVEYOR_RIGHT) 1f else -1f
        val offset = (time * 60f * dir) % 24f
        for (cx in 0 until (p.width / 24).toInt() + 1) {
            val arrowX = p.x + (cx * 24f) + offset
            if (arrowX in p.x..(p.x + p.width - 12f)) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.5f),
                    start = Offset(arrowX, p.y + 4f),
                    end = Offset(arrowX + 8f * dir, p.y + p.height / 2f),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}

private fun DrawScope.drawHazard(h: Hazard, biome: Biome, time: Float) {
    if (h.type == HazardType.SPIKES) {
        val spikeWidth = 14f
        val count = (h.width / spikeWidth).toInt()
        val path = Path()
        for (i in 0 until count) {
            val sx = h.x + i * spikeWidth
            path.moveTo(sx, h.y + h.height)
            path.lineTo(sx + spikeWidth / 2, h.y)
            path.lineTo(sx + spikeWidth, h.y + h.height)
        }
        drawPath(path, Color(biome.hazardColorHex))
    } else {
        // Plasma pool with pulsating glow
        val pulse = (sin(time * 6f) * 0.15f + 0.85f)
        drawRect(
            color = Color(biome.hazardColorHex).copy(alpha = pulse),
            topLeft = Offset(h.x, h.y),
            size = Size(h.width, h.height)
        )
    }
}

private fun DrawScope.drawSwitch(sw: PuzzleSwitch) {
    val plateColor = if (sw.isActivated) Color(0xFF10B981) else Color(0xFFEF4444)
    val plateHeight = if (sw.isActivated) 8f else 16f
    val plateY = sw.y + (16f - plateHeight)

    // Base bracket
    drawRect(
        color = Color(0xFF334155),
        topLeft = Offset(sw.x - 4f, sw.y + 12f),
        size = Size(sw.width + 8f, 6f)
    )

    // Pressure plate
    drawRoundRect(
        color = plateColor,
        topLeft = Offset(sw.x, plateY),
        size = Size(sw.width, plateHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
    )

    // Center indicator light
    drawCircle(
        color = Color.White,
        radius = 3.5f,
        center = Offset(sw.x + sw.width / 2, plateY + plateHeight / 2)
    )
}

private fun DrawScope.drawDoor(d: Door, biome: Biome) {
    if (d.isOpen) {
        // Retracted / glowing outline
        drawRect(
            color = Color(biome.primaryColorHex).copy(alpha = 0.2f),
            topLeft = Offset(d.x, d.y),
            size = Size(d.width, d.height),
            style = Stroke(width = 2f)
        )
    } else {
        // Solid energy door barrier with lock icon
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFDC2626), Color(0xFF991B1B))
            ),
            topLeft = Offset(d.x, d.y),
            size = Size(d.width, d.height)
        )
        // Energy lines
        for (i in 1..4) {
            val ly = d.y + i * (d.height / 5)
            drawLine(
                color = Color(0xFFFCA5A5),
                start = Offset(d.x + 2f, ly),
                end = Offset(d.x + d.width - 2f, ly),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawLaser(l: LaserBarrier, time: Float) {
    if (!l.isActive) return
    val pulse = (sin(time * 15f) * 0.2f + 0.8f)
    val color = Color(0xFFFF0055).copy(alpha = pulse)

    // Core beam
    drawRect(
        color = color,
        topLeft = Offset(l.x, l.y),
        size = Size(l.width, l.height)
    )
    // Inner white hot center
    val innerMargin = if (l.isHorizontal) 0f else l.width * 0.3f
    val innerMarginY = if (l.isHorizontal) l.height * 0.3f else 0f
    drawRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(l.x + innerMargin, l.y + innerMarginY),
        size = Size(l.width - innerMargin * 2, l.height - innerMarginY * 2)
    )
}

private fun DrawScope.drawTeleporter(tp: Teleporter, time: Float) {
    val cx = tp.x + 18f
    val cy = tp.y + 30f
    val pulse = sin(time * 4f) * 4f

    drawCircle(
        color = Color(0xFF00F0FF).copy(alpha = 0.3f),
        radius = 22f + pulse,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFFA855F7).copy(alpha = 0.7f),
        radius = 16f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White,
        radius = 8f,
        center = Offset(cx, cy)
    )
}

private fun DrawScope.drawCollectible(c: Collectible, time: Float) {
    val floatOffset = sin(time * 5f) * 5f
    val cy = c.y + floatOffset

    // Diamond rotating core
    withTransform({
        rotate(time * 90f, Offset(c.x, cy))
    }) {
        drawRect(
            color = Color(0xFFFFD700),
            topLeft = Offset(c.x - 9f, cy - 9f),
            size = Size(18f, 18f)
        )
        drawRect(
            color = Color.White,
            topLeft = Offset(c.x - 4f, cy - 4f),
            size = Size(8f, 8f)
        )
    }

    // Outer glow ring
    drawCircle(
        color = Color(0xFFFBBF24).copy(alpha = 0.4f),
        radius = 16f,
        center = Offset(c.x, cy),
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawGoal(x: Float, y: Float, width: Float, height: Float, time: Float) {
    val cx = x + width / 2
    val cy = y + height / 2

    // Quantum Rift Portal
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFF00F0FF), Color(0xFFA855F7), Color.Transparent),
            center = Offset(cx, cy),
            radius = 45f
        ),
        radius = 42f + sin(time * 4f) * 4f,
        center = Offset(cx, cy)
    )

    // Rotating temporal portal frame
    withTransform({
        rotate(time * 45f, Offset(cx, cy))
    }) {
        drawRoundRect(
            color = Color(0xFF00F0FF),
            topLeft = Offset(x, y),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
            style = Stroke(width = 2.5f)
        )
    }
}

private fun DrawScope.drawEnemy(e: Enemy, time: Float) {
    val cx = e.x + e.width / 2
    val cy = e.y + e.height / 2

    when (e.type) {
        EnemyType.SENTRY_DRONE -> {
            // Drone body
            val bob = sin(time * 6f) * 4f
            drawCircle(
                color = Color(0xFF334155),
                radius = e.width / 2,
                center = Offset(cx, cy + bob)
            )
            // Scanner eye (red/yellow)
            val eyeColor = if (e.isStunned) Color(0xFF38BDF8) else Color(0xFFFF0055)
            val eyeX = cx + (if (e.facingRight) 6f else -6f)
            drawCircle(
                color = eyeColor,
                radius = 6f,
                center = Offset(eyeX, cy + bob)
            )
            // Stun effect
            if (e.isStunned) {
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                    radius = e.width * 0.7f,
                    center = Offset(cx, cy + bob),
                    style = Stroke(width = 2f)
                )
            }
        }
        EnemyType.STALKER_HOUND -> {
            // Agile quadruped body
            drawRoundRect(
                color = if (e.isStunned) Color(0xFF64748B) else Color(0xFFB91C1C),
                topLeft = Offset(e.x, e.y + 10f),
                size = Size(e.width, e.height - 10f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            // Head
            val headX = if (e.facingRight) e.x + e.width - 12f else e.x
            drawCircle(
                color = Color(0xFFDC2626),
                radius = 10f,
                center = Offset(headX + 6f, e.y + 12f)
            )
        }
        EnemyType.SHIELDED_ENFORCER -> {
            // Heavy cyber armored soldier
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(e.x, e.y),
                size = Size(e.width, e.height)
            )
            // Glowing frontal shield
            val shieldX = if (e.facingRight) e.x + e.width else e.x - 6f
            drawRoundRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(shieldX, e.y - 4f),
                size = Size(6f, e.height + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
        }
        EnemyType.GLITCH_PHANTOM -> {
            // Glitch shifted silhouette
            val glitchOff = sin(time * 30f) * 6f
            drawRoundRect(
                color = Color(0xFFEC4899).copy(alpha = 0.85f),
                topLeft = Offset(e.x + glitchOff, e.y),
                size = Size(e.width, e.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
        EnemyType.BOSS -> {
            // Grand Boss Automaton / Core
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF0055), Color(0xFF881337), Color(0xFF1E1B4B)),
                    center = Offset(cx, cy),
                    radius = e.width * 0.7f
                ),
                topLeft = Offset(e.x, e.y),
                size = Size(e.width, e.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Boss glowing core eye
            drawCircle(
                color = Color.White,
                radius = 14f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFFFF0055),
                radius = 8f,
                center = Offset(cx, cy)
            )

            // Boss Health Bar above head
            val healthPercent = (e.health / e.maxHealth).coerceIn(0f, 1f)
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                topLeft = Offset(e.x - 10f, e.y - 18f),
                size = Size(e.width + 20f, 8f)
            )
            drawRect(
                color = Color(0xFFFF0055),
                topLeft = Offset(e.x - 10f, e.y - 18f),
                size = Size((e.width + 20f) * healthPercent, 8f)
            )
        }
    }
}

private fun DrawScope.drawProjectile(p: Projectile) {
    val color = if (p.isPlayerOwned) Color(0xFF00F0FF) else Color(0xFFFF0055)
    drawCircle(
        color = color,
        radius = p.radius,
        center = Offset(p.x, p.y)
    )
    drawCircle(
        color = Color.White,
        radius = p.radius * 0.5f,
        center = Offset(p.x, p.y)
    )
}

private fun DrawScope.drawCharacter(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    facingRight: Boolean,
    isDashing: Boolean,
    isEcho: Boolean,
    isInvulnerable: Boolean = false,
    isRecording: Boolean = false,
    timeSeconds: Float
) {
    if (isInvulnerable && (timeSeconds * 20f).toInt() % 2 == 0) {
        return // Blinking during invulnerability
    }

    val alpha = if (isEcho) 0.85f else 1f
    val primaryColor = if (isEcho) Color(0xFFA855F7).copy(alpha = alpha) else Color(0xFF00F0FF).copy(alpha = alpha)
    val bodyColor = if (isEcho) Color(0xFF7E22CE).copy(alpha = alpha) else Color(0xFF0F172A).copy(alpha = alpha)
    val visorColor = if (isEcho) Color(0xFFF472B6).copy(alpha = alpha) else Color(0xFF38BDF8).copy(alpha = alpha)

    val cx = x + width / 2
    val cy = y + height / 2

    // Recording aura
    if (isRecording) {
        drawCircle(
            color = Color(0xFFA855F7).copy(alpha = 0.4f),
            radius = width * 1.1f,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )
    }

    // Dash trail
    if (isDashing) {
        val trailX = if (facingRight) x - 18f else x + 18f
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.35f),
            topLeft = Offset(trailX, y + 4f),
            size = Size(width, height - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
    }

    // Torso / Suit
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(x + 4f, y + 14f),
        size = Size(width - 8f, height - 24f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
    )

    // Cyber Head / Helmet
    drawRoundRect(
        color = Color(0xFF1E293B).copy(alpha = alpha),
        topLeft = Offset(x + 5f, y),
        size = Size(width - 10f, 16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Visor Glow
    val visorOffset = if (facingRight) width - 13f else 4f
    drawRect(
        color = visorColor,
        topLeft = Offset(x + visorOffset, y + 4f),
        size = Size(8f, 4f)
    )

    // Cybernetic Limbs / Legs
    val legOffset1 = if (facingRight) 6f else width - 12f
    val legOffset2 = if (facingRight) width - 12f else 6f
    drawLine(
        color = primaryColor,
        start = Offset(x + legOffset1, y + height - 12f),
        end = Offset(x + legOffset1, y + height),
        strokeWidth = 3f
    )
    drawLine(
        color = primaryColor,
        start = Offset(x + legOffset2, y + height - 12f),
        end = Offset(x + legOffset2, y + height),
        strokeWidth = 3f
    )

    // Core emblem on chest
    drawCircle(
        color = primaryColor,
        radius = 3.5f,
        center = Offset(cx, y + 22f)
    )
}
