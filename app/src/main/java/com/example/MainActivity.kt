package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.echoshift.data.SettingsEntity
import com.example.echoshift.ui.*
import com.example.ui.theme.EchoShiftTheme
import com.example.echoshift.viewmodel.GameViewModel
import com.example.echoshift.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EchoShiftTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF060913)),
                    color = Color(0xFF060913)
                ) {
                    EchoShiftApp(viewModel = viewModel)
                }
            }
        }
    }

    // Support PC keyboard controls (WASD, Arrows, Space, Shift, E, Q, F, Esc, P)
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val engine = viewModel.gameEngine.value
        val isPlaying = viewModel.screenState.value == ScreenState.PLAYING

        if (isPlaying && engine != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
                    viewModel.inputMoveX = -1f
                    return true
                }
                KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    viewModel.inputMoveX = 1f
                    return true
                }
                KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP -> {
                    viewModel.inputJump = true
                    return true
                }
                KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                    viewModel.inputDash = true
                    return true
                }
                KeyEvent.KEYCODE_E -> {
                    engine.toggleEchoRecord()
                    return true
                }
                KeyEvent.KEYCODE_Q -> {
                    engine.performEchoSwap()
                    return true
                }
                KeyEvent.KEYCODE_F -> {
                    engine.triggerPhasePulse()
                    return true
                }
                KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_ESCAPE -> {
                    if (viewModel.isPaused.value) {
                        viewModel.resumeGame()
                    } else {
                        viewModel.pauseGame()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val isPlaying = viewModel.screenState.value == ScreenState.PLAYING
        if (isPlaying) {
            when (keyCode) {
                KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (viewModel.inputMoveX < 0) viewModel.inputMoveX = 0f
                    return true
                }
                KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (viewModel.inputMoveX > 0) viewModel.inputMoveX = 0f
                    return true
                }
                KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_DPAD_UP -> {
                    viewModel.inputJump = false
                    return true
                }
                KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                    viewModel.inputDash = false
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }
}

@Composable
fun EchoShiftApp(viewModel: GameViewModel) {
    val screenState by viewModel.screenState.collectAsState()
    val allLevels by viewModel.allLevels.collectAsState()
    val allAchievements by viewModel.allAchievements.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val gameEngine by viewModel.gameEngine.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (screenState) {
            ScreenState.MAIN_MENU -> {
                MainMenuScreen(
                    levelProgressList = allLevels,
                    onPlay = {
                        // Find first uncompleted level or start level 1
                        val nextLvl = allLevels.find { !it.isCompleted && it.isUnlocked }?.levelNumber ?: 1
                        viewModel.startLevel(nextLvl)
                    },
                    onLevelSelect = { viewModel.screenState.value = ScreenState.LEVEL_SELECT },
                    onTutorial = { viewModel.showTutorial.value = true },
                    onAchievements = { viewModel.screenState.value = ScreenState.ACHIEVEMENTS },
                    onSettings = { viewModel.screenState.value = ScreenState.SETTINGS }
                )
            }
            ScreenState.LEVEL_SELECT -> {
                BackHandler {
                    viewModel.screenState.value = ScreenState.MAIN_MENU
                }
                LevelSelectScreen(
                    levels = allLevels,
                    onSelectLevel = { lvlNum ->
                        viewModel.startLevel(lvlNum)
                    },
                    onBack = {
                        viewModel.screenState.value = ScreenState.MAIN_MENU
                    }
                )
            }
            ScreenState.PLAYING -> {
                gameEngine?.let { engine ->
                    GameScreen(
                        engine = engine,
                        viewModel = viewModel,
                        isPaused = isPaused
                    )
                }
            }
            ScreenState.SETTINGS -> {
                BackHandler {
                    viewModel.screenState.value = if (gameEngine != null && isPaused) ScreenState.PLAYING else ScreenState.MAIN_MENU
                }
                SettingsScreen(
                    currentSettings = settings ?: SettingsEntity(),
                    onSaveSettings = { viewModel.saveSettings(it) },
                    onResetAllProgress = { viewModel.resetAllProgress() },
                    onBack = {
                        viewModel.screenState.value = if (gameEngine != null && isPaused) ScreenState.PLAYING else ScreenState.MAIN_MENU
                    }
                )
            }
            ScreenState.ACHIEVEMENTS -> {
                BackHandler {
                    viewModel.screenState.value = ScreenState.MAIN_MENU
                }
                AchievementsScreen(
                    achievements = allAchievements,
                    onBack = {
                        viewModel.screenState.value = ScreenState.MAIN_MENU
                    }
                )
            }
        }

        if (showTutorial) {
            TutorialOverlay(
                onDismiss = { viewModel.showTutorial.value = false }
            )
        }
    }
}
