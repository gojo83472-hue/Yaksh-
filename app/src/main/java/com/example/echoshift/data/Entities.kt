package com.example.echoshift.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestTimeSeconds: Float = 0f,
    val quantumCoresCollected: Int = 0,
    val timesPlayed: Int = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAtTimestamp: Long = 0L
)

@Entity(tableName = "game_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val sfxVolume: Float = 0.8f,
    val musicVolume: Float = 0.6f,
    val hapticsEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val touchButtonScale: Float = 1.0f,
    val showTimer: Boolean = true
)
