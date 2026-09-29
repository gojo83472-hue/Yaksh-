package com.example.echoshift.game

import android.content.Context
import com.example.echoshift.audio.AudioEngine
import com.example.echoshift.audio.HapticsManager
import com.example.echoshift.model.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GameEngine(
    val level: LevelDefinition,
    private val context: Context? = null
) {
    // Player
    var playerX = level.playerStartX
    var playerY = level.playerStartY
    var playerVx = 0f
    var playerVy = 0f
    val playerWidth = 28f
    val playerHeight = 46f
    var playerHealth = 100f
    val maxHealth = 100f
    var playerEnergy = 100f
    val maxEnergy = 100f
    var isGrounded = false
    var facingRight = true
    var isDashing = false
    var dashTimerMs = 0L
    var dashCooldownMs = 0L
    var invulnerableTimerMs = 0L
    var tookDamageInLevel = false

    // Echo System
    var isRecording = false
    var recordTimeMs = 0L
    val maxRecordTimeMs = 7000L
    private val recordedFrames = mutableListOf<EchoFrame>()

    var echoState = EchoState()
    var echoSwapCooldownMs = 0L
    var pulseCooldownMs = 0L

    // Level state
    val platforms = level.platforms.map { it.copy() }.toMutableList()
    val switches = level.switches.map { it.copy() }.toMutableList()
    val doors = level.doors.map { it.copy() }.toMutableList()
    val lasers = level.lasers.map { it.copy() }.toMutableList()
    val hazards = level.hazards.map { it.copy() }.toMutableList()
    val collectibles = level.collectibles.map { it.copy() }.toMutableList()
    val enemies = level.enemies.map { it.copy() }.toMutableList()
    val teleporters = level.teleporters.map { it.copy() }.toMutableList()
    val projectiles = mutableListOf<Projectile>()
    val particles = mutableListOf<Particle>()

    // Game progress & stats
    var elapsedTimeSeconds = 0f
    var isGameOver = false
    var isVictory = false
    var coresCollectedCount = 0

    // Camera
    var cameraX = 0f
    var cameraY = 0f
    var screenShakeAmount = 0f

    // Physics Constants
    private val gravity = 900f
    private val moveSpeed = 260f
    private val jumpVelocity = -450f
    private val dashVelocity = 600f

    val playerBounds: Rect2D
        get() = Rect2D(playerX, playerY, playerWidth, playerHeight)

    val echoBounds: Rect2D?
        get() = if (echoState.isActive) Rect2D(echoState.x, echoState.y, playerWidth, playerHeight) else null

    fun update(deltaSeconds: Float, moveX: Float, jumpPressed: Boolean, dashPressed: Boolean) {
        if (isGameOver || isVictory) return

        val deltaMs = (deltaSeconds * 1000f).toLong().coerceIn(1L, 50L)
        elapsedTimeSeconds += deltaSeconds

        // Update Timers
        if (dashCooldownMs > 0) dashCooldownMs = max(0L, dashCooldownMs - deltaMs)
        if (echoSwapCooldownMs > 0) echoSwapCooldownMs = max(0L, echoSwapCooldownMs - deltaMs)
        if (pulseCooldownMs > 0) pulseCooldownMs = max(0L, pulseCooldownMs - deltaMs)
        if (invulnerableTimerMs > 0) invulnerableTimerMs = max(0L, invulnerableTimerMs - deltaMs)
        if (screenShakeAmount > 0) screenShakeAmount = max(0f, screenShakeAmount - deltaSeconds * 12f)

        // Recharge Energy
        if (playerEnergy < maxEnergy) {
            playerEnergy = min(maxEnergy, playerEnergy + deltaSeconds * 20f)
        }

        // Handle Dash
        if (dashPressed && dashCooldownMs == 0L && playerEnergy >= 25f) {
            isDashing = true
            dashTimerMs = 180L
            dashCooldownMs = 800L
            playerEnergy -= 25f
            invulnerableTimerMs = 250L
            AudioEngine.playSound(AudioEngine.SoundType.DASH)
            context?.let { HapticsManager.vibrate(it, 30L) }
            screenShakeAmount = 3f

            // Dash particles
            spawnParticles(playerX + playerWidth / 2, playerY + playerHeight / 2, 8, 0xFF00F0FF)
        }

        if (dashTimerMs > 0) {
            dashTimerMs = max(0L, dashTimerMs - deltaMs)
            if (dashTimerMs == 0L) isDashing = false
        }

        // Horizontal Movement
        if (isDashing) {
            playerVx = if (facingRight) dashVelocity else -dashVelocity
        } else {
            val targetVx = moveX * moveSpeed
            val friction = if (isGrounded) 0.82f else 0.90f
            playerVx = playerVx * friction + targetVx * (1f - friction)

            if (moveX > 0.1f) facingRight = true
            else if (moveX < -0.1f) facingRight = false
        }

        // Jump
        if (jumpPressed && isGrounded && !isDashing) {
            playerVy = jumpVelocity
            isGrounded = false
            AudioEngine.playSound(AudioEngine.SoundType.JUMP)
            context?.let { HapticsManager.vibrate(it, 20L) }
            spawnParticles(playerX + playerWidth / 2, playerY + playerHeight, 5, 0xFFFFFFFF)
        }

        // Gravity
        if (!isDashing) {
            playerVy += gravity * deltaSeconds
            playerVy = playerVy.coerceIn(-600f, 750f)
        } else {
            playerVy = 0f
        }

        // Move Player and Collide with Platforms & Closed Doors
        movePlayerAndCollide(deltaSeconds)

        // Update Echo Recording
        if (isRecording) {
            recordTimeMs += deltaMs
            recordedFrames.add(
                EchoFrame(
                    timeOffsetMs = recordTimeMs,
                    x = playerX,
                    y = playerY,
                    facingRight = facingRight,
                    isJumping = !isGrounded,
                    isDashing = isDashing,
                    isAttacking = false,
                    isInteracting = false
                )
            )
            // Spawn recording aura particles
            if (recordTimeMs % 200L < deltaMs) {
                spawnParticles(playerX + playerWidth / 2, playerY + playerHeight / 2, 2, 0xFFA855F7)
            }
            if (recordTimeMs >= maxRecordTimeMs) {
                deployEcho()
            }
        }

        // Update Echo Playback
        updateEchoPlayback(deltaMs)

        // Update Moving & Crumbling Platforms
        updatePlatforms(deltaSeconds, deltaMs)

        // Update Switches & Doors
        updateSwitchesAndDoors(deltaMs)

        // Update Lasers
        updateLasers(deltaMs)

        // Update Hazards & Laser Collision
        checkHazardCollisions()

        // Update Collectibles
        checkCollectibleCollisions()

        // Update Teleporters
        updateTeleporters(deltaMs)

        // Update Enemies & Bosses
        updateEnemies(deltaSeconds, deltaMs)

        // Update Projectiles
        updateProjectiles(deltaSeconds, deltaMs)

        // Update Particles
        updateParticles(deltaMs)

        // Check Goal
        if (playerBounds.intersects(level.goalBounds)) {
            triggerVictory()
        }

        // Camera Tracking
        updateCamera()
    }

    private fun movePlayerAndCollide(deltaSeconds: Float) {
        val oldX = playerX
        val oldY = playerY

        // Horizontal step
        playerX += playerVx * deltaSeconds
        var pBounds = playerBounds
        for (platform in platforms) {
            if (platform.isCrumbled) continue
            if (pBounds.intersects(platform.bounds)) {
                if (playerVx > 0) playerX = platform.bounds.left - playerWidth
                else if (playerVx < 0) playerX = platform.bounds.right
                playerVx = 0f
                pBounds = playerBounds
            }
        }
        for (door in doors) {
            if (!door.isOpen && pBounds.intersects(door.bounds)) {
                if (playerVx > 0) playerX = door.bounds.left - playerWidth
                else if (playerVx < 0) playerX = door.bounds.right
                playerVx = 0f
                pBounds = playerBounds
            }
        }

        // Vertical step
        playerY += playerVy * deltaSeconds
        pBounds = playerBounds
        isGrounded = false

        for (platform in platforms) {
            if (platform.isCrumbled) continue
            if (pBounds.intersects(platform.bounds)) {
                if (playerVy > 0) { // Landing
                    playerY = platform.bounds.top - playerHeight
                    playerVy = 0f
                    isGrounded = true

                    // Handle platform conveyor or crumble
                    if (platform.type == PlatformType.CRUMBLING && platform.crumbleTimerMs == 0L) {
                        platform.crumbleTimerMs = 800L
                    } else if (platform.type == PlatformType.CONVEYOR_RIGHT) {
                        playerX += platform.speed * deltaSeconds
                    } else if (platform.type == PlatformType.CONVEYOR_LEFT) {
                        playerX -= platform.speed * deltaSeconds
                    }
                } else if (playerVy < 0) { // Hitting ceiling
                    playerY = platform.bounds.bottom
                    playerVy = 0f
                }
                pBounds = playerBounds
            }
        }
        for (door in doors) {
            if (!door.isOpen && pBounds.intersects(door.bounds)) {
                if (playerVy > 0) {
                    playerY = door.bounds.top - playerHeight
                    playerVy = 0f
                    isGrounded = true
                } else if (playerVy < 0) {
                    playerY = door.bounds.bottom
                    playerVy = 0f
                }
                pBounds = playerBounds
            }
        }
    }

    fun toggleEchoRecord() {
        if (!isRecording) {
            // Start recording
            isRecording = true
            recordTimeMs = 0L
            recordedFrames.clear()
            AudioEngine.playSound(AudioEngine.SoundType.ECHO_START)
            context?.let { HapticsManager.vibrate(it, 35L) }
        } else {
            // End recording and deploy
            deployEcho()
        }
    }

    private fun deployEcho() {
        isRecording = false
        if (recordedFrames.isNotEmpty()) {
            val firstFrame = recordedFrames.first()
            echoState = EchoState(
                isActive = true,
                frames = recordedFrames.toList(),
                currentFrameIndex = 0,
                x = firstFrame.x,
                y = firstFrame.y,
                facingRight = firstFrame.facingRight,
                isDashing = firstFrame.isDashing,
                isAttacking = firstFrame.isAttacking
            )
            AudioEngine.playSound(AudioEngine.SoundType.ECHO_SPAWN)
            context?.let { HapticsManager.vibrate(it, 45L) }
            spawnParticles(firstFrame.x + playerWidth / 2, firstFrame.y + playerHeight / 2, 12, 0xFFA855F7)
        }
    }

    private fun updateEchoPlayback(deltaMs: Long) {
        if (!echoState.isActive || echoState.frames.isEmpty()) return

        var nextIndex = echoState.currentFrameIndex + 1
        if (nextIndex >= echoState.frames.size) {
            // Loop sequence so echo can sustain pressure plates and behaviors!
            nextIndex = 0
        }

        val frame = echoState.frames[nextIndex]
        echoState = echoState.copy(
            currentFrameIndex = nextIndex,
            x = frame.x,
            y = frame.y,
            facingRight = frame.facingRight,
            isDashing = frame.isDashing,
            isAttacking = frame.isAttacking
        )
    }

    fun performEchoSwap() {
        if (!echoState.isActive || echoSwapCooldownMs > 0) return
        echoSwapCooldownMs = 1200L

        // Swap positions
        val tempX = playerX
        val tempY = playerY

        playerX = echoState.x
        playerY = echoState.y

        echoState = echoState.copy(x = tempX, y = tempY)

        AudioEngine.playSound(AudioEngine.SoundType.ECHO_SWAP)
        context?.let { HapticsManager.vibrate(it, 60L) }
        screenShakeAmount = 5f

        // Stun nearby enemies in radius 150px
        for (enemy in enemies) {
            val dist = abs(enemy.x - playerX) + abs(enemy.y - playerY)
            if (dist < 150f) {
                enemy.isStunned = true
                enemy.stunTimerMs = 2500L
                spawnParticles(enemy.x, enemy.y, 8, 0xFF00F0FF)
            }
        }

        spawnParticles(playerX + playerWidth / 2, playerY + playerHeight / 2, 14, 0xFF00F0FF)
        spawnParticles(tempX + playerWidth / 2, tempY + playerHeight / 2, 14, 0xFFA855F7)
    }

    fun triggerPhasePulse() {
        if (pulseCooldownMs > 0 || playerEnergy < 35f) return
        playerEnergy -= 35f
        pulseCooldownMs = 1500L
        screenShakeAmount = 6f
        AudioEngine.playSound(AudioEngine.SoundType.ENERGY_PULSE)
        context?.let { HapticsManager.vibrate(it, 50L) }

        // Blast radius 160px
        val pulseRadius = 160f
        spawnParticles(playerX + playerWidth / 2, playerY + playerHeight / 2, 20, 0xFF38BDF8)

        for (enemy in enemies) {
            val dx = (enemy.x + enemy.width / 2) - (playerX + playerWidth / 2)
            val dy = (enemy.y + enemy.height / 2) - (playerY + playerHeight / 2)
            val dist = kotlin.math.sqrt(dx * dx + dy * dy)
            if (dist <= pulseRadius) {
                enemy.health -= 60f
                enemy.isStunned = true
                enemy.stunTimerMs = 1500L
                if (enemy.health <= 0) {
                    enemy.isDefeated = true
                    spawnParticles(enemy.x, enemy.y, 16, 0xFFFF0055)
                }
            }
        }
    }

    private fun updatePlatforms(deltaSeconds: Float, deltaMs: Long) {
        for (platform in platforms) {
            if (platform.type == PlatformType.MOVING_H) {
                platform.x += platform.moveDir * platform.speed * deltaSeconds
                if (platform.x <= platform.minX) {
                    platform.x = platform.minX
                    platform.moveDir = 1f
                } else if (platform.x >= platform.maxX) {
                    platform.x = platform.maxX
                    platform.moveDir = -1f
                }
            } else if (platform.type == PlatformType.MOVING_V) {
                platform.y += platform.moveDir * platform.speed * deltaSeconds
                if (platform.y <= platform.minY) {
                    platform.y = platform.minY
                    platform.moveDir = 1f
                } else if (platform.y >= platform.maxY) {
                    platform.y = platform.maxY
                    platform.moveDir = -1f
                }
            } else if (platform.type == PlatformType.CRUMBLING && platform.crumbleTimerMs > 0L) {
                platform.crumbleTimerMs -= deltaMs
                if (platform.crumbleTimerMs <= 0L) {
                    platform.isCrumbled = true
                    spawnParticles(platform.x + platform.width / 2, platform.y + platform.height / 2, 10, 0xFF888888)
                }
            }
        }
    }

    private fun updateSwitchesAndDoors(deltaMs: Long) {
        val pBox = playerBounds
        val eBox = echoBounds

        for (sw in switches) {
            val playerOnSwitch = pBox.intersects(sw.bounds)
            val echoOnSwitch = eBox?.intersects(sw.bounds) == true

            when (sw.type) {
                SwitchType.MOMENTARY_PRESSURE_PLATE -> {
                    val shouldBeActive = playerOnSwitch || echoOnSwitch
                    if (shouldBeActive != sw.isActivated) {
                        sw.isActivated = shouldBeActive
                        AudioEngine.playSound(AudioEngine.SoundType.SWITCH_CLICK)
                        spawnParticles(sw.x + sw.width / 2, sw.y, 4, 0xFF00F0FF)
                    }
                }
                SwitchType.TOGGLE -> {
                    if ((playerOnSwitch || echoOnSwitch) && !sw.isActivated) {
                        sw.isActivated = true
                        AudioEngine.playSound(AudioEngine.SoundType.SWITCH_CLICK)
                        spawnParticles(sw.x + sw.width / 2, sw.y, 6, 0xFF00F0FF)
                    }
                }
                SwitchType.TIMED -> {
                    if (playerOnSwitch || echoOnSwitch) {
                        if (!sw.isActivated) {
                            AudioEngine.playSound(AudioEngine.SoundType.SWITCH_CLICK)
                        }
                        sw.isActivated = true
                        sw.timerRemainingMs = sw.timerDurationMs
                    } else if (sw.isActivated) {
                        sw.timerRemainingMs -= deltaMs
                        if (sw.timerRemainingMs <= 0L) {
                            sw.isActivated = false
                            sw.timerRemainingMs = 0L
                        }
                    }
                }
            }
        }

        // Open doors if all required switches are activated
        for (door in doors) {
            val allActive = door.requiredSwitchIds.all { sId ->
                switches.find { it.id == sId }?.isActivated == true
            }
            if (door.isOpen != allActive) {
                door.isOpen = allActive
                AudioEngine.playSound(AudioEngine.SoundType.SWITCH_CLICK)
                spawnParticles(door.x + door.width / 2, door.y + door.height / 2, 10, 0xFF00F0FF)
            }
        }
    }

    private fun updateLasers(deltaMs: Long) {
        for (laser in lasers) {
            if (laser.isPulsing) {
                laser.pulseTimerMs = (laser.pulseTimerMs + deltaMs) % 3000L
                laser.isActive = laser.pulseTimerMs < 1800L
            } else if (laser.linkedSwitchId != null) {
                val sw = switches.find { it.id == laser.linkedSwitchId }
                laser.isActive = !(sw?.isActivated ?: false)
            }
        }
    }

    private fun checkHazardCollisions() {
        if (invulnerableTimerMs > 0L) return

        val pBox = playerBounds
        // Lasers
        for (laser in lasers) {
            if (laser.isActive && pBox.intersects(laser.bounds)) {
                takeDamage(35f)
                break
            }
        }
        // Hazards (Spikes, Plasma)
        for (hazard in hazards) {
            if (pBox.intersects(hazard.bounds)) {
                takeDamage(40f)
                break
            }
        }
    }

    private fun checkCollectibleCollisions() {
        val pBox = playerBounds
        for (c in collectibles) {
            if (!c.isCollected && pBox.intersects(c.bounds)) {
                c.isCollected = true
                if (c.type == CollectibleType.QUANTUM_CORE) {
                    coresCollectedCount++
                    AudioEngine.playSound(AudioEngine.SoundType.COLLECTIBLE)
                    context?.let { HapticsManager.vibrate(it, 35L) }
                    spawnParticles(c.x, c.y, 14, 0xFFFFD700)
                }
            }
        }
    }

    private fun updateTeleporters(deltaMs: Long) {
        val pBox = playerBounds
        for (tp in teleporters) {
            if (tp.cooldownMs > 0L) {
                tp.cooldownMs -= deltaMs
            } else if (pBox.intersects(tp.bounds)) {
                playerX = tp.targetX
                playerY = tp.targetY
                tp.cooldownMs = 1500L
                AudioEngine.playSound(AudioEngine.SoundType.ECHO_SWAP)
                spawnParticles(playerX, playerY, 12, 0xFF00F0FF)
            }
        }
    }

    private fun updateEnemies(deltaSeconds: Float, deltaMs: Long) {
        val pBox = playerBounds
        val eBox = echoBounds

        for (enemy in enemies) {
            if (enemy.isDefeated) continue

            // Stun logic
            if (enemy.isStunned) {
                enemy.stunTimerMs -= deltaMs
                if (enemy.stunTimerMs <= 0L) enemy.isStunned = false
                continue
            }

            // Patrol movement
            if (enemy.type == EnemyType.SENTRY_DRONE || enemy.type == EnemyType.SHIELDED_ENFORCER) {
                val speed = if (enemy.type == EnemyType.SENTRY_DRONE) 90f else 60f
                if (enemy.facingRight) {
                    enemy.x += speed * deltaSeconds
                    if (enemy.x >= enemy.patrolRight) {
                        enemy.x = enemy.patrolRight
                        enemy.facingRight = false
                    }
                } else {
                    enemy.x -= speed * deltaSeconds
                    if (enemy.x <= enemy.patrolLeft) {
                        enemy.x = enemy.patrolLeft
                        enemy.facingRight = true
                    }
                }
            } else if (enemy.type == EnemyType.STALKER_HOUND) {
                // Chases nearest target (Player or Echo!)
                val targetX = if (echoState.isActive && abs(echoState.x - enemy.x) < abs(playerX - enemy.x)) {
                    echoState.x
                } else {
                    playerX
                }
                val dir = if (targetX > enemy.x) 1f else -1f
                enemy.facingRight = dir > 0
                enemy.x += dir * 130f * deltaSeconds
            } else if (enemy.type == EnemyType.BOSS) {
                // Boss AI
                enemy.specialTimerMs += deltaMs
                val targetX = playerX
                enemy.facingRight = targetX > enemy.x
                enemy.x += (if (enemy.facingRight) 1f else -1f) * 60f * deltaSeconds

                // Boss shoots salvo every 2.5s
                if (enemy.specialTimerMs >= 2500L) {
                    enemy.specialTimerMs = 0L
                    val dirX = if (playerX > enemy.x) 1f else -1f
                    projectiles.add(
                        Projectile(
                            id = (1..100000).random(),
                            x = enemy.x + enemy.width / 2,
                            y = enemy.y + enemy.height / 2,
                            vx = dirX * 240f,
                            vy = -50f,
                            isPlayerOwned = false,
                            damage = 25f
                        )
                    )
                    AudioEngine.playSound(AudioEngine.SoundType.DAMAGE)
                }
            }

            // Damage player on contact
            if (invulnerableTimerMs <= 0L && pBox.intersects(enemy.bounds)) {
                takeDamage(20f)
            }
        }
    }

    private fun updateProjectiles(deltaSeconds: Float, deltaMs: Long) {
        val pBox = playerBounds
        val iter = projectiles.iterator()
        while (iter.hasNext()) {
            val proj = iter.next()
            proj.x += proj.vx * deltaSeconds
            proj.y += proj.vy * deltaSeconds
            proj.lifeRemainingMs -= deltaMs

            if (proj.lifeRemainingMs <= 0L) {
                iter.remove()
                continue
            }

            if (!proj.isPlayerOwned && invulnerableTimerMs <= 0L && pBox.intersects(proj.bounds)) {
                takeDamage(proj.damage)
                iter.remove()
                spawnParticles(proj.x, proj.y, 8, 0xFFFF0055)
            }
        }
    }

    private fun updateParticles(deltaMs: Long) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * (deltaMs / 1000f)
            p.y += p.vy * (deltaMs / 1000f)
            p.currentLifeMs += deltaMs
            p.alpha = 1f - (p.currentLifeMs.toFloat() / p.maxLifeMs)
            if (p.currentLifeMs >= p.maxLifeMs) {
                iter.remove()
            }
        }
    }

    private fun spawnParticles(x: Float, y: Float, count: Int, colorHex: Long) {
        for (i in 0 until count) {
            val angle = (Math.random() * Math.PI * 2).toFloat()
            val speed = 60f + (Math.random() * 140f).toFloat()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    colorHex = colorHex,
                    size = 3f + (Math.random() * 3f).toFloat(),
                    maxLifeMs = 300L + (Math.random() * 300L).toLong()
                )
            )
        }
    }

    fun takeDamage(amount: Float) {
        if (invulnerableTimerMs > 0L || isGameOver || isVictory) return
        playerHealth -= amount
        invulnerableTimerMs = 800L
        screenShakeAmount = 8f
        tookDamageInLevel = true
        AudioEngine.playSound(AudioEngine.SoundType.DAMAGE)
        context?.let { HapticsManager.vibrate(it, 70L) }
        spawnParticles(playerX + playerWidth / 2, playerY + playerHeight / 2, 10, 0xFFFF0055)

        if (playerHealth <= 0f) {
            playerHealth = 0f
            isGameOver = true
            AudioEngine.playSound(AudioEngine.SoundType.GAME_OVER)
        }
    }

    private fun triggerVictory() {
        isVictory = true
        AudioEngine.playSound(AudioEngine.SoundType.VICTORY)
        context?.let { HapticsManager.vibrate(it, 80L) }
        spawnParticles(level.goalBounds.left + level.goalBounds.width / 2, level.goalBounds.top + level.goalBounds.height / 2, 30, 0xFF00F0FF)
    }

    fun restart() {
        playerX = level.playerStartX
        playerY = level.playerStartY
        playerVx = 0f
        playerVy = 0f
        playerHealth = maxHealth
        playerEnergy = maxEnergy
        isGrounded = false
        isDashing = false
        isRecording = false
        echoState = EchoState()
        recordedFrames.clear()
        elapsedTimeSeconds = 0f
        isGameOver = false
        isVictory = false
        tookDamageInLevel = false
        coresCollectedCount = 0

        // Reset elements
        for (c in collectibles) c.isCollected = false
        for (s in switches) {
            s.isActivated = false
            s.timerRemainingMs = 0L
        }
        for (d in doors) d.isOpen = false
        for (p in platforms) {
            p.crumbleTimerMs = 0L
            p.isCrumbled = false
        }
        for (e in enemies) {
            e.health = e.maxHealth
            e.isDefeated = false
            e.isStunned = false
        }
        projectiles.clear()
        particles.clear()
    }

    private fun updateCamera() {
        // Center camera near player with slight forward offset
        val lookAhead = if (facingRight) 60f else -60f
        val targetCamX = playerX + lookAhead - 400f
        val targetCamY = playerY - 260f

        cameraX = cameraX * 0.90f + targetCamX * 0.10f
        cameraY = cameraY * 0.90f + targetCamY * 0.10f

        // Clamp camera
        cameraX = cameraX.coerceIn(0f, 800f)
        cameraY = cameraY.coerceIn(-100f, 200f)
    }
}
