package com.example.echoshift.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echoshift.audio.AudioEngine
import com.example.echoshift.data.*
import com.example.echoshift.game.GameEngine
import com.example.echoshift.game.LevelGenerator
import com.example.echoshift.model.LevelDefinition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenState {
    MAIN_MENU,
    LEVEL_SELECT,
    PLAYING,
    SETTINGS,
    ACHIEVEMENTS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository

    val screenState = MutableStateFlow(ScreenState.MAIN_MENU)
    val currentLevelNumber = MutableStateFlow(1)
    val gameEngine = MutableStateFlow<GameEngine?>(null)

    val isPaused = MutableStateFlow(false)
    val showTutorial = MutableStateFlow(false)

    val allLevels: StateFlow<List<LevelProgressEntity>>
    val allAchievements: StateFlow<List<AchievementEntity>>
    val settings: StateFlow<SettingsEntity?>

    private var gameLoopJob: Job? = null

    // Movement state for continuous updates
    var inputMoveX = 0f
    var inputJump = false
    var inputDash = false

    init {
        val database = AppDatabase.getInstance(application)
        repository = GameRepository(database.progressDao())

        allLevels = repository.allLevels.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allAchievements = repository.allAchievements.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        settings = repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsEntity()
        )

        viewModelScope.launch {
            repository.initializeIfNeeded()
            // Sync settings to AudioEngine
            settings.collect { set ->
                if (set != null) {
                    AudioEngine.sfxVolume = set.sfxVolume
                    AudioEngine.musicVolume = set.musicVolume
                }
            }
        }
    }

    fun startLevel(levelNum: Int) {
        val lvl = levelNum.coerceIn(1, 50)
        currentLevelNumber.value = lvl
        val def = LevelGenerator.getLevel(lvl)
        val engine = GameEngine(def, getApplication())
        gameEngine.value = engine
        isPaused.value = false
        screenState.value = ScreenState.PLAYING

        // Start BGM for biome
        AudioEngine.startMusic(def.biome, def.isBossLevel)

        startGameLoop()

        if (lvl == 1 || lvl == 2) {
            repository.let {
                viewModelScope.launch {
                    it.unlockAchievement("first_echo")
                }
            }
        }
    }

    fun resumeGame() {
        isPaused.value = false
    }

    fun pauseGame() {
        isPaused.value = true
    }

    fun restartCurrentLevel() {
        isPaused.value = false
        val lvl = currentLevelNumber.value
        val def = LevelGenerator.getLevel(lvl)
        gameEngine.value = GameEngine(def, getApplication())
        AudioEngine.startMusic(def.biome, def.isBossLevel)
        startGameLoop()
    }

    fun nextLevel() {
        val next = currentLevelNumber.value + 1
        if (next <= 50) {
            startLevel(next)
        } else {
            // Completed all 50 levels! Return to menu
            AudioEngine.stopMusic()
            screenState.value = ScreenState.MAIN_MENU
        }
    }

    fun returnToMainMenu() {
        stopGameLoop()
        AudioEngine.stopMusic()
        screenState.value = ScreenState.MAIN_MENU
    }

    fun returnToLevelSelect() {
        stopGameLoop()
        AudioEngine.stopMusic()
        screenState.value = ScreenState.LEVEL_SELECT
    }

    fun saveSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            AudioEngine.sfxVolume = newSettings.sfxVolume
            AudioEngine.musicVolume = newSettings.musicVolume
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
        }
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTime = now

                val engine = gameEngine.value
                if (engine != null && !isPaused.value) {
                    engine.update(
                        deltaSeconds = delta,
                        moveX = inputMoveX,
                        jumpPressed = inputJump,
                        dashPressed = inputDash
                    )

                    // On Victory, record completion to database
                    if (engine.isVictory) {
                        repository.recordLevelCompletion(
                            levelNumber = engine.level.levelNumber,
                            timeSeconds = engine.elapsedTimeSeconds,
                            parTimeSeconds = engine.level.parTimeSeconds,
                            coresCollected = engine.coresCollectedCount,
                            tookDamage = engine.tookDamageInLevel
                        )
                        break
                    }
                }
                delay(16L) // ~60 FPS
            }
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopGameLoop()
        AudioEngine.stopMusic()
    }
}
