package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.echoshift.game.GameEngine
import com.example.echoshift.game.LevelGenerator
import com.example.echoshift.model.Biome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Echo Shift", appName)
    }

    @Test
    fun `verify all 50 levels are generated with valid biomes`() {
        for (lvl in 1..50) {
            val levelDef = LevelGenerator.getLevel(lvl)
            assertEquals(lvl, levelDef.levelNumber)
            assertNotNull(levelDef.title)
            assertTrue(levelDef.platforms.isNotEmpty())
            assertTrue(levelDef.parTimeSeconds > 0)
        }
    }

    @Test
    fun `verify echo recording and playback logic in game engine`() {
        val levelDef = LevelGenerator.getLevel(2)
        val engine = GameEngine(levelDef)

        // Initial state
        assertEquals(false, engine.isRecording)
        assertEquals(false, engine.echoState.isActive)

        // Toggle record on
        engine.toggleEchoRecord()
        assertTrue(engine.isRecording)

        // Simulate 20 frames of movement
        for (i in 0 until 20) {
            engine.update(0.016f, moveX = 1f, jumpPressed = false, dashPressed = false)
        }

        // Deploy Echo
        engine.toggleEchoRecord()
        assertEquals(false, engine.isRecording)
        assertTrue(engine.echoState.isActive)
        assertTrue(engine.echoState.frames.isNotEmpty())
    }

    @Test
    fun `verify world biomes map correctly across all 50 sectors`() {
        assertEquals(Biome.NEO_GRID, Biome.forLevel(1))
        assertEquals(Biome.QUANTUM_LABS, Biome.forLevel(6))
        assertEquals(Biome.SUBTERRANEAN_CRYPTS, Biome.forLevel(12))
        assertEquals(Biome.NEON_GROVE, Biome.forLevel(18))
        assertEquals(Biome.CRYO_WASTES, Biome.forLevel(22))
        assertEquals(Biome.SOLARIS_BARRENS, Biome.forLevel(28))
        assertEquals(Biome.GLITCH_VOID, Biome.forLevel(33))
        assertEquals(Biome.OMEGA_WORKS, Biome.forLevel(38))
        assertEquals(Biome.AETHER_ISLES, Biome.forLevel(42))
        assertEquals(Biome.TEMPORAL_SANCTUM, Biome.forLevel(46))
        assertEquals(Biome.APEX_STATION, Biome.forLevel(49))
        assertEquals(Biome.XENON_CORE, Biome.forLevel(50))
    }
}
