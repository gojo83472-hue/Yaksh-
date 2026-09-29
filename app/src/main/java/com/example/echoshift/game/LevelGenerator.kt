package com.example.echoshift.game

import com.example.echoshift.model.*

object LevelGenerator {

    fun getLevel(levelNumber: Int): LevelDefinition {
        val clampedNumber = levelNumber.coerceIn(1, 50)
        val biome = Biome.forLevel(clampedNumber)
        return generateLevel(clampedNumber, biome)
    }

    private fun generateLevel(lvl: Int, biome: Biome): LevelDefinition {
        val platforms = mutableListOf<Platform>()
        val switches = mutableListOf<PuzzleSwitch>()
        val doors = mutableListOf<Door>()
        val lasers = mutableListOf<LaserBarrier>()
        val hazards = mutableListOf<Hazard>()
        val collectibles = mutableListOf<Collectible>()
        val enemies = mutableListOf<Enemy>()
        val teleporters = mutableListOf<Teleporter>()

        // Boundary base floors and walls
        platforms.add(Platform(1, 0f, 520f, 1600f, 80f, PlatformType.STATIC)) // Base ground
        platforms.add(Platform(2, -20f, 0f, 20f, 600f, PlatformType.STATIC)) // Left wall
        platforms.add(Platform(3, 1600f, 0f, 20f, 600f, PlatformType.STATIC)) // Right wall
        platforms.add(Platform(4, 0f, -20f, 1600f, 20f, PlatformType.STATIC)) // Ceiling

        var title: String
        var hint: String
        var parTime = 30 + (lvl * 2)
        var isBoss = false
        var unlockedAbility: String? = null
        var playerStartX = 80f
        var playerStartY = 450f
        var goalX = 1450f
        var goalY = 450f

        when (lvl) {
            1 -> {
                title = "Awakening"
                hint = "Use movement controls to jump across platforms and reach the Quantum Rift."
                parTime = 25
                platforms.add(Platform(10, 300f, 440f, 140f, 24f))
                platforms.add(Platform(11, 550f, 370f, 140f, 24f))
                platforms.add(Platform(12, 800f, 310f, 160f, 24f))
                platforms.add(Platform(13, 1100f, 380f, 160f, 24f))

                collectibles.add(Collectible(1, 370f, 400f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 880f, 270f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1180f, 340f, CollectibleType.QUANTUM_CORE))
            }
            2 -> {
                title = "First Echo"
                hint = "Hold ECHO RECORD, step on the pressure plate to open the door, then deploy Echo!"
                parTime = 30
                unlockedAbility = "Echo Clone"
                // Wall dividing rooms
                platforms.add(Platform(10, 600f, 150f, 40f, 370f))
                doors.add(Door(1, 600f, 380f, 40f, 140f, requiredSwitchIds = listOf(1)))
                switches.add(PuzzleSwitch(1, 350f, 504f, 60f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))

                collectibles.add(Collectible(1, 380f, 470f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 620f, 280f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1200f, 480f, CollectibleType.QUANTUM_CORE))
            }
            3 -> {
                title = "Dual Coordination"
                hint = "Two switches must be held down together. Let your Echo hold one while you stand on the other!"
                parTime = 35
                platforms.add(Platform(10, 800f, 200f, 40f, 320f))
                doors.add(Door(1, 800f, 420f, 40f, 100f, requiredSwitchIds = listOf(1, 2)))
                switches.add(PuzzleSwitch(1, 280f, 504f, 50f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))
                switches.add(PuzzleSwitch(2, 540f, 504f, 50f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))

                collectibles.add(Collectible(1, 305f, 460f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 565f, 460f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1300f, 480f, CollectibleType.QUANTUM_CORE))
            }
            4 -> {
                title = "Sentry Patrol"
                hint = "Sentry drones detect intruders! Use your Echo as a diversion or avoid their sight."
                parTime = 40
                platforms.add(Platform(10, 400f, 400f, 300f, 24f))
                platforms.add(Platform(11, 850f, 350f, 300f, 24f))
                enemies.add(Enemy(1, 500f, 360f, 36f, 36f, EnemyType.SENTRY_DRONE, 30f, 30f, patrolLeft = 400f, patrolRight = 680f))
                enemies.add(Enemy(2, 950f, 310f, 36f, 36f, EnemyType.SENTRY_DRONE, 30f, 30f, patrolLeft = 860f, patrolRight = 1120f))

                collectibles.add(Collectible(1, 550f, 340f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 1000f, 290f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1400f, 470f, CollectibleType.QUANTUM_CORE))
            }
            5 -> {
                title = "Timed Velocity"
                hint = "Hit the timed switch and hurry before the temporal shutter slams shut!"
                parTime = 45
                platforms.add(Platform(10, 500f, 420f, 160f, 24f))
                hazards.add(Hazard(1, 700f, 505f, 250f, 15f, HazardType.PLASMA_POOL))
                platforms.add(Platform(11, 740f, 360f, 160f, 24f))
                platforms.add(Platform(12, 1000f, 200f, 40f, 320f))
                doors.add(Door(1, 1000f, 420f, 40f, 100f, requiredSwitchIds = listOf(1)))
                switches.add(PuzzleSwitch(1, 260f, 504f, 50f, 16f, SwitchType.TIMED, timerDurationMs = 5000L, targetDoorId = 1))

                collectibles.add(Collectible(1, 580f, 380f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 820f, 320f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1350f, 480f, CollectibleType.QUANTUM_CORE))
            }
            6 -> {
                title = "Echo Swap Protocol"
                hint = "NEW ABILITY: ECHO SWAP! Press SWAP to instantly trade places with your active Echo!"
                parTime = 45
                unlockedAbility = "Echo Swap"
                platforms.add(Platform(10, 450f, 180f, 40f, 340f)) // High barrier
                platforms.add(Platform(11, 550f, 320f, 180f, 24f))
                platforms.add(Platform(12, 900f, 220f, 200f, 24f))
                goalX = 1000f
                goalY = 150f

                collectibles.add(Collectible(1, 300f, 380f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 640f, 270f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 920f, 170f, CollectibleType.QUANTUM_CORE))
            }
            7 -> {
                title = "Laser Grid Alpha"
                hint = "Deadly laser grids disintegrate matter. Use switches or swap past them."
                parTime = 50
                platforms.add(Platform(10, 400f, 380f, 180f, 24f))
                lasers.add(LaserBarrier(1, 650f, 300f, 20f, 220f, isHorizontal = false, linkedSwitchId = 1))
                switches.add(PuzzleSwitch(1, 460f, 364f, 40f, 16f, SwitchType.TOGGLE, targetDoorId = 0))
                platforms.add(Platform(11, 800f, 420f, 220f, 24f))
                hazards.add(Hazard(1, 750f, 505f, 300f, 15f, HazardType.SPIKES))

                collectibles.add(Collectible(1, 480f, 320f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 900f, 370f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1400f, 480f, CollectibleType.QUANTUM_CORE))
            }
            8 -> {
                title = "Moving Sync"
                hint = "Ride moving platforms and coordinate switch triggers with your Echo."
                parTime = 50
                platforms.add(Platform(10, 350f, 400f, 140f, 20f, PlatformType.MOVING_H, minX = 300f, maxX = 650f, speed = 90f))
                platforms.add(Platform(11, 800f, 300f, 140f, 20f, PlatformType.MOVING_V, minY = 200f, maxY = 450f, speed = 80f))
                doors.add(Door(1, 1100f, 420f, 40f, 100f, requiredSwitchIds = listOf(1)))
                switches.add(PuzzleSwitch(1, 200f, 504f, 50f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))

                collectibles.add(Collectible(1, 480f, 280f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 850f, 160f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1350f, 480f, CollectibleType.QUANTUM_CORE))
            }
            9 -> {
                title = "Stalker Infiltration"
                hint = "Stalker hounds react to sound and vision. Lure them away with your Echo duplicate!"
                parTime = 55
                platforms.add(Platform(10, 350f, 380f, 220f, 24f))
                platforms.add(Platform(11, 700f, 320f, 220f, 24f))
                platforms.add(Platform(12, 1050f, 260f, 220f, 24f))
                enemies.add(Enemy(1, 400f, 480f, 48f, 36f, EnemyType.STALKER_HOUND, 50f, 50f, patrolLeft = 200f, patrolRight = 600f))
                enemies.add(Enemy(2, 800f, 480f, 48f, 36f, EnemyType.STALKER_HOUND, 50f, 50f, patrolLeft = 700f, patrolRight = 1100f))

                collectibles.add(Collectible(1, 450f, 330f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 270f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1150f, 210f, CollectibleType.QUANTUM_CORE))
            }
            10 -> {
                title = "BOSS: Overseer Matrix"
                hint = "BOSS BATTLE! Trigger both power pylons to lower the Overseer's invulnerability shield, then strike!"
                parTime = 80
                isBoss = true
                platforms.add(Platform(10, 200f, 380f, 180f, 24f))
                platforms.add(Platform(11, 1200f, 380f, 180f, 24f))
                platforms.add(Platform(12, 600f, 260f, 400f, 24f))

                switches.add(PuzzleSwitch(1, 250f, 364f, 50f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))
                switches.add(PuzzleSwitch(2, 1280f, 364f, 50f, 16f, SwitchType.MOMENTARY_PRESSURE_PLATE, targetDoorId = 1))
                doors.add(Door(1, 750f, 160f, 100f, 20f, requiredSwitchIds = listOf(1, 2))) // Shield over boss

                enemies.add(Enemy(1, 760f, 180f, 80f, 80f, EnemyType.BOSS, 250f, 250f, patrolLeft = 650f, patrolRight = 950f))

                collectibles.add(Collectible(1, 280f, 200f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 120f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1320f, 200f, CollectibleType.QUANTUM_CORE))
            }
            16 -> {
                title = "Chrono Dash Unlocked"
                hint = "NEW ABILITY: CHRONO DASH! Dash across air gaps and through high-frequency energy beams!"
                parTime = 45
                unlockedAbility = "Chrono Dash"
                hazards.add(Hazard(1, 350f, 505f, 350f, 15f, HazardType.SPIKES))
                lasers.add(LaserBarrier(1, 500f, 380f, 20f, 140f, isHorizontal = false))
                platforms.add(Platform(10, 750f, 420f, 200f, 24f))
                hazards.add(Hazard(2, 1000f, 505f, 350f, 15f, HazardType.SPIKES))

                collectibles.add(Collectible(1, 520f, 440f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 850f, 370f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1150f, 440f, CollectibleType.QUANTUM_CORE))
            }
            20 -> {
                title = "BOSS: Stalker Alpha"
                hint = "The Apex predator prowls! Echo Swap behind the beast when it prepares to pounce!"
                parTime = 90
                isBoss = true
                platforms.add(Platform(10, 300f, 380f, 200f, 24f))
                platforms.add(Platform(11, 1100f, 380f, 200f, 24f))
                enemies.add(Enemy(1, 800f, 450f, 70f, 60f, EnemyType.BOSS, 350f, 350f, patrolLeft = 300f, patrolRight = 1300f))

                collectibles.add(Collectible(1, 400f, 320f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 250f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1200f, 320f, CollectibleType.QUANTUM_CORE))
            }
            25 -> {
                title = "BOSS: Cryo Goliath"
                hint = "Ice Goliath slams shockwaves across the floor! Jump to platforms and deploy Echo to stagger it!"
                parTime = 100
                isBoss = true
                platforms.add(Platform(10, 250f, 380f, 220f, 24f, PlatformType.ICE))
                platforms.add(Platform(11, 600f, 280f, 400f, 24f, PlatformType.ICE))
                platforms.add(Platform(12, 1150f, 380f, 220f, 24f, PlatformType.ICE))
                enemies.add(Enemy(1, 750f, 440f, 90f, 80f, EnemyType.BOSS, 450f, 450f, patrolLeft = 400f, patrolRight = 1200f))

                collectibles.add(Collectible(1, 350f, 300f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 200f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1250f, 300f, CollectibleType.QUANTUM_CORE))
            }
            26 -> {
                title = "Phase Pulse Unlocked"
                hint = "NEW ABILITY: PHASE PULSE! Release stored chrono-energy to destroy drones and disable traps!"
                parTime = 50
                unlockedAbility = "Phase Pulse"
                enemies.add(Enemy(1, 450f, 470f, 36f, 36f, EnemyType.SENTRY_DRONE, 40f, 40f, patrolLeft = 350f, patrolRight = 600f))
                enemies.add(Enemy(2, 800f, 470f, 36f, 36f, EnemyType.SHIELDED_ENFORCER, 60f, 60f, patrolLeft = 700f, patrolRight = 950f))
                enemies.add(Enemy(3, 1150f, 470f, 36f, 36f, EnemyType.SENTRY_DRONE, 40f, 40f, patrolLeft = 1050f, patrolRight = 1300f))

                collectibles.add(Collectible(1, 500f, 420f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 850f, 420f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1200f, 420f, CollectibleType.QUANTUM_CORE))
            }
            30 -> {
                title = "BOSS: Solar Colossus"
                hint = "Shielded Colossus absorbs frontal fire! Use Echo to draw its attention, then blast the exposed rear cooling vents!"
                parTime = 100
                isBoss = true
                platforms.add(Platform(10, 300f, 360f, 240f, 24f))
                platforms.add(Platform(11, 1050f, 360f, 240f, 24f))
                enemies.add(Enemy(1, 750f, 440f, 80f, 80f, EnemyType.BOSS, 550f, 550f, patrolLeft = 400f, patrolRight = 1100f))

                collectibles.add(Collectible(1, 420f, 300f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 250f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1170f, 300f, CollectibleType.QUANTUM_CORE))
            }
            35 -> {
                title = "BOSS: Glitch Void Entity"
                hint = "The Glitch Phantom splits into phase illusions! Watch for the real entity and swap past its void blast!"
                parTime = 110
                isBoss = true
                platforms.add(Platform(10, 250f, 400f, 200f, 24f))
                platforms.add(Platform(11, 600f, 300f, 400f, 24f))
                platforms.add(Platform(12, 1150f, 400f, 200f, 24f))
                enemies.add(Enemy(1, 750f, 220f, 75f, 75f, EnemyType.BOSS, 650f, 650f, patrolLeft = 300f, patrolRight = 1200f))

                collectibles.add(Collectible(1, 350f, 340f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 230f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1250f, 340f, CollectibleType.QUANTUM_CORE))
            }
            40 -> {
                title = "BOSS: Omega Automaton"
                hint = "Industrial Foundry Titan! Use moving conveyors and high-speed Echo Swaps to bypass its crushers!"
                parTime = 120
                isBoss = true
                platforms.add(Platform(10, 200f, 420f, 300f, 24f, PlatformType.CONVEYOR_RIGHT, speed = 80f))
                platforms.add(Platform(11, 1100f, 420f, 300f, 24f, PlatformType.CONVEYOR_LEFT, speed = 80f))
                platforms.add(Platform(12, 600f, 280f, 400f, 24f))
                enemies.add(Enemy(1, 750f, 430f, 90f, 90f, EnemyType.BOSS, 750f, 750f, patrolLeft = 350f, patrolRight = 1150f))

                collectibles.add(Collectible(1, 350f, 350f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 220f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1250f, 350f, CollectibleType.QUANTUM_CORE))
            }
            50 -> {
                title = "FINAL BOSS: Chronos Prime"
                hint = "THE TEMPORAL ARCHON! The master of time summons anti-echoes and warps space. Combine Dash, Echo Swap, and Phase Pulse to restore reality!"
                parTime = 150
                isBoss = true
                platforms.add(Platform(10, 200f, 380f, 260f, 24f))
                platforms.add(Platform(11, 550f, 260f, 500f, 24f))
                platforms.add(Platform(12, 1140f, 380f, 260f, 24f))
                platforms.add(Platform(13, 700f, 140f, 200f, 24f))
                enemies.add(Enemy(1, 750f, 170f, 100f, 100f, EnemyType.BOSS, 1200f, 1200f, patrolLeft = 250f, patrolRight = 1350f))

                collectibles.add(Collectible(1, 300f, 300f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 800f, 80f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1280f, 300f, CollectibleType.QUANTUM_CORE))
            }
            else -> {
                // Procedural generation tailored per level index & biome
                val worldName = biome.displayName
                val biomeIndexInWorld = ((lvl - 1) % 5) + 1
                title = "$worldName - Chamber $biomeIndexInWorld"
                hint = when (biomeIndexInWorld) {
                    1 -> "Master the terrain of $worldName. Coordinate Echo switches to navigate the sectors."
                    2 -> "Watch out for synchronized hazards and timed doors. Use Echo Swap to traverse gaps."
                    3 -> "Dangerous sentry units patrol this zone. Distract them with an Echo clone."
                    4 -> "Heavy laser grids ahead. Find the primary junction switches to clear the path."
                    else -> "Prepare for high-intensity temporal coordination to unlock the gate."
                }

                // Create varied platform geometry based on level index
                val stepCount = 4 + (lvl % 4)
                val stepWidth = 140f + ((lvl * 17) % 60)
                for (i in 0 until stepCount) {
                    val px = 220f + i * (1100f / stepCount)
                    val py = 460f - ((i * 45 + (lvl * 31)) % 240)
                    val pType = when {
                        lvl in 21..25 -> PlatformType.ICE
                        lvl in 36..40 && i % 2 == 0 -> PlatformType.CONVEYOR_RIGHT
                        lvl in 36..40 && i % 2 == 1 -> PlatformType.CONVEYOR_LEFT
                        i % 3 == 1 && lvl > 10 -> PlatformType.MOVING_H
                        i % 4 == 2 && lvl > 20 -> PlatformType.MOVING_V
                        i % 5 == 3 && lvl > 15 -> PlatformType.CRUMBLING
                        else -> PlatformType.STATIC
                    }
                    platforms.add(
                        Platform(
                            id = 20 + i,
                            x = px,
                            y = py,
                            width = stepWidth,
                            height = 24f,
                            type = pType,
                            minX = px - 80f,
                            maxX = px + 80f,
                            minY = py - 70f,
                            maxY = py + 70f,
                            speed = 70f + (lvl * 1.5f)
                        )
                    )
                }

                // Add puzzle doors and switches
                val doorCount = if (lvl > 15) 2 else 1
                for (d in 0 until doorCount) {
                    val doorX = 600f + d * 450f
                    val switchX = 250f + d * 400f
                    val switchType = if (lvl % 2 == 0) SwitchType.MOMENTARY_PRESSURE_PLATE else SwitchType.TIMED
                    val doorId = d + 1
                    val switchId = d + 1
                    doors.add(Door(doorId, doorX, 390f, 30f, 130f, requiredSwitchIds = listOf(switchId)))
                    switches.add(
                        PuzzleSwitch(
                            id = switchId,
                            x = switchX,
                            y = 504f,
                            width = 50f,
                            height = 16f,
                            type = switchType,
                            timerDurationMs = 5500L,
                            targetDoorId = doorId
                        )
                    )
                }

                // Add hazards
                if (lvl > 5) {
                    hazards.add(Hazard(1, 450f, 505f, 180f, 15f, if (lvl in 6..10) HazardType.PLASMA_POOL else HazardType.SPIKES))
                }
                if (lvl > 20) {
                    hazards.add(Hazard(2, 950f, 505f, 200f, 15f, HazardType.SPIKES))
                }

                // Add Lasers
                if (lvl > 7) {
                    val laserX = 750f + ((lvl * 43) % 300)
                    lasers.add(LaserBarrier(1, laserX, 320f, 16f, 200f, isHorizontal = false, isPulsing = lvl > 25))
                }

                // Add Enemies
                val enemyCount = ((lvl - 1) / 8).coerceIn(1, 4)
                for (e in 0 until enemyCount) {
                    val ex = 400f + e * 280f
                    val eType = when {
                        lvl > 35 && e % 2 == 1 -> EnemyType.GLITCH_PHANTOM
                        lvl > 25 && e % 2 == 0 -> EnemyType.SHIELDED_ENFORCER
                        lvl > 12 && e % 2 == 1 -> EnemyType.STALKER_HOUND
                        else -> EnemyType.SENTRY_DRONE
                    }
                    enemies.add(
                        Enemy(
                            id = e + 1,
                            x = ex,
                            y = 480f,
                            width = 40f,
                            height = 40f,
                            type = eType,
                            health = 30f + lvl * 4f,
                            maxHealth = 30f + lvl * 4f,
                            patrolLeft = ex - 120f,
                            patrolRight = ex + 120f
                        )
                    )
                }

                // Add Teleporters in higher worlds
                if (lvl in 41..49 && lvl % 2 == 1) {
                    teleporters.add(Teleporter(1, 350f, 460f, 1200f, 300f))
                }

                // 3 Quantum Cores
                collectibles.add(Collectible(1, 380f, 420f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(2, 850f, 280f, CollectibleType.QUANTUM_CORE))
                collectibles.add(Collectible(3, 1300f, 440f, CollectibleType.QUANTUM_CORE))
            }
        }

        return LevelDefinition(
            levelNumber = lvl,
            title = title,
            biome = biome,
            hint = hint,
            parTimeSeconds = parTime,
            playerStartX = playerStartX,
            playerStartY = playerStartY,
            goalX = goalX,
            goalY = goalY,
            platforms = platforms,
            switches = switches,
            doors = doors,
            lasers = lasers,
            hazards = hazards,
            collectibles = collectibles,
            enemies = enemies,
            teleporters = teleporters,
            isBossLevel = isBoss,
            abilityUnlocked = unlockedAbility
        )
    }
}
