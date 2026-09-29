package com.example.echoshift.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: ProgressDao) {

    val allLevels: Flow<List<LevelProgressEntity>> = dao.getAllLevelProgress()
    val allAchievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()
    val settings: Flow<SettingsEntity?> = dao.getSettings()

    suspend fun initializeIfNeeded() {
        val existing = dao.getAllLevelProgress().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val initialLevels = (1..50).map { lvl ->
                LevelProgressEntity(
                    levelNumber = lvl,
                    isUnlocked = lvl == 1,
                    isCompleted = false,
                    stars = 0,
                    bestTimeSeconds = 0f,
                    quantumCoresCollected = 0,
                    timesPlayed = 0
                )
            }
            dao.insertInitialProgressList(initialLevels)
        }

        val initialAchievements = listOf(
            AchievementEntity("first_echo", "Temporal Inception", "Created your very first Echo Copy", "ic_echo"),
            AchievementEntity("dual_switch", "Synchronized Minds", "Held two distant pressure plates simultaneously", "ic_switch"),
            AchievementEntity("echo_swap", "Spatial Translocation", "Executed an Echo Swap across an obstacle", "ic_swap"),
            AchievementEntity("chrono_dash", "Hypershift", "Dashed straight through an active laser barrier", "ic_dash"),
            AchievementEntity("phase_pulse", "EMP Surge", "Disintegrated a patrol drone with Phase Pulse", "ic_pulse"),
            AchievementEntity("boss_1", "Matrix Breaker", "Neutralized the Overseer Matrix in Quantum Labs", "ic_boss"),
            AchievementEntity("boss_2", "Cryo Meltdown", "Defeated the Cryo Goliath in the Frozen Wastes", "ic_boss"),
            AchievementEntity("boss_3", "Void Purge", "Overcame the Glitch Void Entity", "ic_boss"),
            AchievementEntity("boss_4", "Foundry Master", "Crushed the Omega Automaton", "ic_boss"),
            AchievementEntity("boss_5", "Temporal Archon", "Defeated Chronos Prime and restored timeline", "ic_boss"),
            AchievementEntity("collector_10", "Core Harvester", "Collected 15 Quantum Data Cores", "ic_core"),
            AchievementEntity("collector_all", "Omniscient Scholar", "Collected all Quantum Data Cores in 10 levels", "ic_core"),
            AchievementEntity("speedrun", "Quantum Leaper", "Beat any level under par time", "ic_time"),
            AchievementEntity("master_runner", "Three Star Vector", "Earned 3 Stars in 5 different levels", "ic_star"),
            AchievementEntity("untouchable", "Flawless Anomaly", "Completed a level without taking any damage", "ic_shield"),
            AchievementEntity("halfway_there", "Midpoint Shift", "Unlocked World 6 (Level 26)", "ic_world")
        )
        dao.insertInitialAchievements(initialAchievements)

        val existingSettings = dao.getSettings().firstOrNull()
        if (existingSettings == null) {
            dao.saveSettings(SettingsEntity())
        }
    }

    suspend fun recordLevelCompletion(
        levelNumber: Int,
        timeSeconds: Float,
        parTimeSeconds: Int,
        coresCollected: Int,
        tookDamage: Boolean
    ): LevelProgressEntity {
        var stars = 1 // 1 star for completion
        if (timeSeconds <= parTimeSeconds) stars++
        if (coresCollected >= 3) stars++

        val currentProgress = dao.getLevelProgress(levelNumber).firstOrNull()
        val prevBestTime = currentProgress?.bestTimeSeconds ?: 0f
        val newBestTime = if (prevBestTime > 0f) minOf(prevBestTime, timeSeconds) else timeSeconds
        val maxStars = maxOf(currentProgress?.stars ?: 0, stars)
        val maxCores = maxOf(currentProgress?.quantumCoresCollected ?: 0, coresCollected)

        val updated = LevelProgressEntity(
            levelNumber = levelNumber,
            isUnlocked = true,
            isCompleted = true,
            stars = maxStars,
            bestTimeSeconds = newBestTime,
            quantumCoresCollected = maxCores,
            timesPlayed = (currentProgress?.timesPlayed ?: 0) + 1
        )
        dao.insertOrUpdateProgress(updated)

        // Unlock next level if exists
        if (levelNumber < 50) {
            val nextLevel = dao.getLevelProgress(levelNumber + 1).firstOrNull()
            if (nextLevel == null || !nextLevel.isUnlocked) {
                dao.insertOrUpdateProgress(
                    LevelProgressEntity(
                        levelNumber = levelNumber + 1,
                        isUnlocked = true,
                        isCompleted = nextLevel?.isCompleted ?: false,
                        stars = nextLevel?.stars ?: 0,
                        bestTimeSeconds = nextLevel?.bestTimeSeconds ?: 0f,
                        quantumCoresCollected = nextLevel?.quantumCoresCollected ?: 0,
                        timesPlayed = nextLevel?.timesPlayed ?: 0
                    )
                )
            }
        }

        // Check achievements
        if (timeSeconds <= parTimeSeconds) {
            unlockAchievement("speedrun")
        }
        if (!tookDamage) {
            unlockAchievement("untouchable")
        }
        if (levelNumber == 10) unlockAchievement("boss_1")
        if (levelNumber == 20) unlockAchievement("boss_2")
        if (levelNumber == 25) unlockAchievement("boss_2")
        if (levelNumber == 35) unlockAchievement("boss_3")
        if (levelNumber == 40) unlockAchievement("boss_4")
        if (levelNumber == 50) unlockAchievement("boss_5")
        if (levelNumber >= 26) unlockAchievement("halfway_there")

        return updated
    }

    suspend fun unlockAchievement(id: String) {
        val list = dao.getAllAchievements().firstOrNull() ?: return
        val item = list.find { it.id == id } ?: return
        if (!item.isUnlocked) {
            dao.updateAchievement(
                item.copy(isUnlocked = true, unlockedAtTimestamp = System.currentTimeMillis())
            )
        }
    }

    suspend fun saveSettings(settings: SettingsEntity) {
        dao.saveSettings(settings)
    }

    suspend fun resetAllProgress() {
        dao.clearProgress()
        val initialLevels = (1..50).map { lvl ->
            LevelProgressEntity(
                levelNumber = lvl,
                isUnlocked = lvl == 1,
                isCompleted = false,
                stars = 0,
                bestTimeSeconds = 0f,
                quantumCoresCollected = 0,
                timesPlayed = 0
            )
        }
        dao.insertInitialProgressList(initialLevels)
    }
}
