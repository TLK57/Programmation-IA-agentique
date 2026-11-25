package j4k.candycrush.game

import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile

/** Scoring and multiplier rules (EF-17, EF-18). */
object ScoreRules {
    /** Points earned by each removed candy, before the multiplier. */
    const val POINTS_PER_TILE = 20

    /** Multiplier of the first wave when the swap formed a single alignment. */
    const val BASE_MULTIPLIER = 1

    /** Multiplier of the first wave when the swap formed several alignments at once. */
    const val MULTI_ALIGNMENT_MULTIPLIER = 2

    /** Points of a wave: removed candies × [POINTS_PER_TILE] × multiplier. */
    fun wavePoints(removedTiles: Int, multiplier: Int): Int = removedTiles * POINTS_PER_TILE * multiplier

    /** Multiplier of the first wave of a move, given the number of alignments formed by the swap. */
    fun firstWaveMultiplier(alignmentCount: Int): Int =
        if (alignmentCount > 1) MULTI_ALIGNMENT_MULTIPLIER else BASE_MULTIPLIER

    /** Multiplier of a chain-reaction wave, one more than the previous wave. */
    fun nextWaveMultiplier(previous: Int): Int = previous + 1
}

/** The level played by the game (EF-22): starting grid, reserve, 42 moves, collect 18 A and 22 C. */
object DefaultLevel {
    /** Starting grid, row 0 at the top. */
    val GRID = """
        [D,B,A,B,D]
        [B,E,C,A,B]
        [B,E,C,C,B]
        [C,C,E,C,D]
        [B,B,C,A,D]
        [A,C,B,E,E]
        [D,E,E,B,A]
        [E,D,A,E,C]
        [E,A,D,C,B]
    """.trimIndent()

    /** Reserve feeding the refill, the lowest candy of each column comes out first. */
    val RESERVE = """
        [E,C,B,A,D]
        [D,A,E,C,B]
    """.trimIndent()

    /** Number of moves available. */
    const val MOVES = 42

    private const val GOAL_A = 18
    private const val GOAL_C = 22

    /** Collection objectives of the level. */
    val OBJECTIVES = listOf(Level.TileObjective(Tile.A, GOAL_A), Level.TileObjective(Tile.C, GOAL_C))

    /** Creates a fresh copy of the level, with an optional injectable random candy source. */
    fun create(randomTiles: () -> Tile = { Tile.randomTile() }): Level =
        Level(GRID, RESERVE, MOVES, OBJECTIVES, randomTiles)
}
