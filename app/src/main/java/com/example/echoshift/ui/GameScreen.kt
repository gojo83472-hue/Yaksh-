package com.example.echoshift.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.echoshift.game.GameEngine
import com.example.echoshift.viewmodel.GameViewModel

@Composable
fun GameScreen(
    engine: GameEngine,
    viewModel: GameViewModel,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (!isPaused && !engine.isGameOver && !engine.isVictory) {
            viewModel.pauseGame()
        } else if (isPaused) {
            viewModel.resumeGame()
        } else {
            viewModel.returnToLevelSelect()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Rendering Canvas
        GameCanvas(engine = engine)

        // 2. Heads-Up Display
        GameHud(
            engine = engine,
            onPauseClick = { viewModel.pauseGame() }
        )

        // 3. On-Screen Touch Controls
        ControlsOverlay(
            engine = engine,
            onMove = { viewModel.inputMoveX = it },
            onJump = { viewModel.inputJump = it },
            onDash = { viewModel.inputDash = it }
        )

        // 4. Overlays & Dialogs
        if (isPaused) {
            PauseDialog(
                onResume = { viewModel.resumeGame() },
                onRestart = { viewModel.restartCurrentLevel() },
                onLevelSelect = { viewModel.returnToLevelSelect() },
                onMainMenu = { viewModel.returnToMainMenu() },
                onOpenSettings = {
                    viewModel.screenState.value = com.example.echoshift.viewmodel.ScreenState.SETTINGS
                }
            )
        }

        if (engine.isVictory) {
            val starsEarned = 1 + (if (engine.elapsedTimeSeconds <= engine.level.parTimeSeconds) 1 else 0) + (if (engine.coresCollectedCount >= 3) 1 else 0)
            VictoryDialog(
                levelNumber = engine.level.levelNumber,
                timeSeconds = engine.elapsedTimeSeconds,
                parTimeSeconds = engine.level.parTimeSeconds,
                coresCollected = engine.coresCollectedCount,
                starsEarned = starsEarned,
                unlockedAbility = engine.level.abilityUnlocked,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartCurrentLevel() },
                onLevelSelect = { viewModel.returnToLevelSelect() }
            )
        }

        if (engine.isGameOver) {
            GameOverDialog(
                hint = engine.level.hint,
                onRestart = { viewModel.restartCurrentLevel() },
                onLevelSelect = { viewModel.returnToLevelSelect() },
                onMainMenu = { viewModel.returnToMainMenu() }
            )
        }
    }
}
