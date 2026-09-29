package com.example.echoshift.model

/**
 * Biome types representing distinct worlds in Echo Shift.
 */
enum class Biome(
    val displayName: String,
    val description: String,
    val primaryColorHex: Long,
    val accentColorHex: Long,
    val bgGradientTopHex: Long,
    val bgGradientBottomHex: Long,
    val hazardColorHex: Long
) {
    NEO_GRID(
        "Neo Grid",
        "Cybernetic megacity with electric conduits and digital highways.",
        0xFF00F0FF, 0xFF38BDF8, 0xFF070B19, 0xFF0F172A, 0xFFFF0055
    ),
    QUANTUM_LABS(
        "Quantum Labs",
        "Decommissioned high-energy particle physics research facility.",
        0xFF10B981, 0xFF34D399, 0xFF061A14, 0xFF092920, 0xFFF59E0B
    ),
    SUBTERRANEAN_CRYPTS(
        "Subterranean Crypts",
        "Ancient subterranean ruins humming with dormant chrono-technology.",
        0xFFEAB308, 0xFFFDE047, 0xFF140E05, 0xFF1C1408, 0xFFEF4444
    ),
    NEON_GROVE(
        "Neon Grove",
        "Bioluminescent twilight forest filled with synthetic flora.",
        0xFFA855F7, 0xFFC084FC, 0xFF130826, 0xFF1E0E3D, 0xFFEC4899
    ),
    CRYO_WASTES(
        "Cryo Wastes",
        "Sub-zero glacier outpost where temporal fields freeze solid.",
        0xFF38BDF8, 0xFF93C5FD, 0xFF051329, 0xFF0C244D, 0xFF67E8F9
    ),
    SOLARIS_BARRENS(
        "Solaris Barrens",
        "Scorched desert ruins bombarded by concentrated solar radiation.",
        0xFFF97316, 0xFFFDBA74, 0xFF240E04, 0xFF3B1706, 0xFFEF4444
    ),
    GLITCH_VOID(
        "Glitch Void",
        "Reality-corrupted digital sector where spatial geometry warps.",
        0xFFEC4899, 0xFFF472B6, 0xFF1F051C, 0xFF2E0929, 0xFF06B6D4
    ),
    OMEGA_WORKS(
        "Omega Works",
        "Massive industrial foundry operating automated hazard machinery.",
        0xFFEF4444, 0xFFF87171, 0xFF1F0B0B, 0xFF331414, 0xFFF59E0B
    ),
    AETHER_ISLES(
        "Aether Isles",
        "Floating anti-gravity archipelagos suspended high in the ionosphere.",
        0xFF06B6D4, 0xFF67E8F9, 0xFF061E2E, 0xFF0B2E47, 0xFFA855F7
    ),
    TEMPORAL_SANCTUM(
        "Temporal Sanctum",
        "Timeless monument holding the prime temporal chronometer.",
        0xFF8B5CF6, 0xFFA78BFA, 0xFF120B2E, 0xFF1C1247, 0xFF00F0FF
    ),
    APEX_STATION(
        "Apex Station",
        "Orbital military research hub shrouded in automated defense grids.",
        0xFF6366F1, 0xFF818CF8, 0xFF0C0F2B, 0xFF131843, 0xFFFF007F
    ),
    XENON_CORE(
        "Xenon Core",
        "The primordial heart of the temporal anomaly. The final trial.",
        0xFF00F0FF, 0xFFFF007F, 0xFF08061A, 0xFF120B2E, 0xFFFFD700
    );

    companion object {
        fun forLevel(levelIndex: Int): Biome {
            return when (levelIndex) {
                in 1..5 -> NEO_GRID
                in 6..10 -> QUANTUM_LABS
                in 11..15 -> SUBTERRANEAN_CRYPTS
                in 16..20 -> NEON_GROVE
                in 21..25 -> CRYO_WASTES
                in 26..30 -> SOLARIS_BARRENS
                in 31..35 -> GLITCH_VOID
                in 36..40 -> OMEGA_WORKS
                in 41..44 -> AETHER_ISLES
                in 45..47 -> TEMPORAL_SANCTUM
                in 48..49 -> APEX_STATION
                else -> XENON_CORE
            }
        }
    }
}
