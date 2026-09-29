package com.example.echoshift.model

import kotlin.math.max
import kotlin.math.min

data class Vector2D(val x: Float = 0f, val y: Float = 0f) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(factor: Float) = Vector2D(x * factor, y * factor)
}

data class Rect2D(val x: Float, val y: Float, val width: Float, val height: Float) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height

    fun intersects(other: Rect2D): Boolean {
        return left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
    }

    fun contains(px: Float, py: Float): Boolean {
        return px in left..right && py in top..bottom
    }
}

data class EchoFrame(
    val timeOffsetMs: Long,
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    val isJumping: Boolean,
    val isDashing: Boolean,
    val isAttacking: Boolean,
    val isInteracting: Boolean
)

data class EchoState(
    val isActive: Boolean = false,
    val frames: List<EchoFrame> = emptyList(),
    val currentFrameIndex: Int = 0,
    val x: Float = 0f,
    val y: Float = 0f,
    val facingRight: Boolean = true,
    val isDashing: Boolean = false,
    val isAttacking: Boolean = false,
    val alpha: Float = 0.85f
)

enum class PlatformType {
    STATIC,
    MOVING_H,
    MOVING_V,
    CRUMBLING,
    ICE,
    CONVEYOR_LEFT,
    CONVEYOR_RIGHT
}

data class Platform(
    val id: Int,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType = PlatformType.STATIC,
    val minX: Float = x,
    val maxX: Float = x,
    val minY: Float = y,
    val maxY: Float = y,
    var moveDir: Float = 1f,
    val speed: Float = 60f,
    var crumbleTimerMs: Long = 0L,
    var isCrumbled: Boolean = false
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

enum class SwitchType {
    TOGGLE,
    MOMENTARY_PRESSURE_PLATE,
    TIMED
}

data class PuzzleSwitch(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 40f,
    val height: Float = 16f,
    val type: SwitchType,
    var isActivated: Boolean = false,
    var timerRemainingMs: Long = 0L,
    val timerDurationMs: Long = 6000L,
    val targetDoorId: Int
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

data class Door(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float = 20f,
    val height: Float = 80f,
    var isOpen: Boolean = false,
    val requiredSwitchIds: List<Int> = emptyList()
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

data class LaserBarrier(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val isHorizontal: Boolean,
    var isActive: Boolean = true,
    val linkedSwitchId: Int? = null,
    val isPulsing: Boolean = false,
    var pulseTimerMs: Long = 0L
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

enum class HazardType {
    SPIKES,
    PLASMA_POOL,
    CRUSHER
}

data class Hazard(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: HazardType = HazardType.SPIKES
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

enum class CollectibleType {
    QUANTUM_CORE,
    ENERGY_ORB,
    HEALTH_PACK
}

data class Collectible(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: CollectibleType,
    var isCollected: Boolean = false
) {
    val bounds: Rect2D get() = Rect2D(x - 12f, y - 12f, 24f, 24f)
}

enum class EnemyType {
    SENTRY_DRONE,
    STALKER_HOUND,
    SHIELDED_ENFORCER,
    GLITCH_PHANTOM,
    BOSS
}

data class Enemy(
    val id: Int,
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    val type: EnemyType,
    var health: Float,
    val maxHealth: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val patrolLeft: Float,
    val patrolRight: Float,
    var facingRight: Boolean = true,
    var attackCooldownMs: Long = 0L,
    var isAlerted: Boolean = false,
    var isStunned: Boolean = false,
    var stunTimerMs: Long = 0L,
    var isDefeated: Boolean = false,
    var bossPhase: Int = 1,
    var specialTimerMs: Long = 0L
) {
    val bounds: Rect2D get() = Rect2D(x, y, width, height)
}

data class Projectile(
    val id: Int,
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val isPlayerOwned: Boolean,
    val damage: Float,
    val radius: Float = 6f,
    var lifeRemainingMs: Long = 3000L
) {
    val bounds: Rect2D get() = Rect2D(x - radius, y - radius, radius * 2, radius * 2)
}

data class Teleporter(
    val id: Int,
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    var cooldownMs: Long = 0L
) {
    val bounds: Rect2D get() = Rect2D(x, y, 36f, 60f)
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val colorHex: Long,
    var alpha: Float = 1f,
    val size: Float = 4f,
    val maxLifeMs: Long = 500L,
    var currentLifeMs: Long = 0L
)
