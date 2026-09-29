package com.example.echoshift.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllLevelProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
    fun getLevelProgress(levelNumber: Int): Flow<LevelProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialProgressList(list: List<LevelProgressEntity>)

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialAchievements(list: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)

    @Query("DELETE FROM level_progress")
    suspend fun clearProgress()
}
