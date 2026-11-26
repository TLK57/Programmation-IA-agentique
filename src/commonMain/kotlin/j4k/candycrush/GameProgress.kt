package j4k.candycrush

import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile

/** Keeps the score, remaining moves, collected goals, and terminal state independent of rendering. */
class GameProgress(private val level: Level) {
    /** The total score accumulated in this game. */
    var score: Int = 0
        private set
    /** The remaining moves in this game. */
    var movesRemaining: Int = level.moves
        private set
    /** Counts of candies collected by type. */
    val collected: MutableMap<Tile, Int> = mutableMapOf()
    /** The current terminal state, or null while play continues. */
    var outcome: Outcome? = null
        private set

    /** Adds one wave's points and collection progress. */
    fun recordWave(counts: Map<Tile, Int>, multiplier: Int): Int {
        require(multiplier >= 1 && counts.values.all { it >= 0 }) { "Wave counts and multiplier must be non-negative" }
        val count = counts.values.sum()
        counts.forEach { (tile, amount) ->
            if (tile.isTile()) collected[tile] = (collected[tile] ?: 0) + amount
        }
        val points = count * POINTS_PER_CANDY * multiplier
        score += points
        return points
    }

    /** Decrements the move counter as soon as a valid swap begins. */
    fun beginTurn() {
        if (movesRemaining > 0) movesRemaining--
    }

    /** Determines victory or defeat after the board stabilizes, prioritizing victory on the last move. */
    fun finishTurn(): Outcome? {
        outcome = when {
            level.objectives.all { (collected[it.tile] ?: 0) >= it.goal } -> Outcome.Won
            movesRemaining == 0 -> Outcome.Lost
            else -> null
        }
        return outcome
    }

    /** Restores score, objectives, moves, and outcome to their initial values. */
    fun reset() {
        score = 0
        movesRemaining = level.moves
        collected.clear()
        outcome = null
    }

    /** Returns the number still needed for a tile objective. */
    fun remainingFor(tile: Tile): Int {
        val goal = level.objectives.firstOrNull { it.tile == tile }?.goal ?: return 0
        return (goal - (collected[tile] ?: 0)).coerceAtLeast(0)
    }

    /** Possible end-of-level results. */
    enum class Outcome { Won, Lost }

    companion object {
        /** Points awarded per removed candy before applying a combo multiplier. */
        const val POINTS_PER_CANDY = 20
    }
}
