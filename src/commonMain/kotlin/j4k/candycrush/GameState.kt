package j4k.candycrush

import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile

/** Testable score, move, goal, and level completion state. */
class GameState(private val level: Level) {
    var score = 0; private set
    var movesLeft = level.initialMoves; private set
    var multiplier = 1; private set
    private val collected = level.objectives.associate { it.tile to 0 }.toMutableMap()
    /** Counts a successfully accepted player move. */
    fun consumeMove() { if (movesLeft > 0) movesLeft-- }
    /** Scores a resolved wave and credits every removed candy toward its objective. */
    fun scoreWave(tiles: List<Tile>, waveMultiplier: Int) {
        multiplier = waveMultiplier.coerceAtLeast(1); score += tiles.size * POINTS_PER_CANDY * multiplier
        tiles.forEach { tile -> if (tile in collected) collected[tile] = (collected[tile] ?: 0) + 1 }
    }
    /** Remaining count for a collection goal. */
    fun remaining(tile: Tile): Int = (level.objectives.firstOrNull { it.tile == tile }?.goal ?: 0) - (collected[tile] ?: 0).let { it.coerceAtMost(level.objectives.firstOrNull { o -> o.tile == tile }?.goal ?: 0) }
    /** Whether all collection goals have been met. */
    fun isWon(): Boolean = level.objectives.all { remaining(it.tile) == 0 }
    /** Whether the level ended in a loss. Victory takes precedence on the final move. */
    fun isLost(): Boolean = movesLeft == 0 && !isWon()
    /** Resets counters and the reserve to the level start. */
    fun reset() { score = 0; movesLeft = level.initialMoves; multiplier = 1; collected.keys.forEach { collected[it] = 0 }; level.resetReserve() }
    companion object { const val POINTS_PER_CANDY = 20 }
}
