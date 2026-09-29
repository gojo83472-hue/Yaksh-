package com.example.echoshift.model

data class LevelDefinition(
    val levelNumber: Int,
    val title: String,
    val biome: Biome,
    val hint: String,
    val parTimeSeconds: Int,
    val playerStartX: Float,
    val playerStartY: Float,
    val goalX: Float,
    val goalY: Float,
    val goalWidth: Float = 44f,
    val goalHeight: Float = 70f,
    val platforms: List<Platform>,
    val switches: List<PuzzleSwitch> = emptyList(),
    val doors: List<Door> = emptyList(),
    val lasers: List<LaserBarrier> = emptyList(),
    val hazards: List<Hazard> = emptyList(),
    val collectibles: List<Collectible> = emptyList(),
    val enemies: List<Enemy> = emptyList(),
    val teleporters: List<Teleporter> = emptyList(),
    val isBossLevel: Boolean = false,
    val abilityUnlocked: String? = null
) {
    val goalBounds: Rect2D get() = Rect2D(goalX, goalY, goalWidth, goalHeight)
}
