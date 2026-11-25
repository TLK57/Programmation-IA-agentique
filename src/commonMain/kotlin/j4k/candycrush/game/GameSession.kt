package j4k.candycrush.game

import j4k.candycrush.GameMechanics
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell
import kotlin.math.abs
import kotlin.math.max

/** State of a game: being played, won or lost. */
enum class GameStatus { PLAYING, WON, LOST }

/** Progress of one objective: [remaining] candies of type [tile] still to collect (never below 0). */
data class ObjectiveProgress(val tile: Tile, val goal: Int, val remaining: Int)

/**
 * One "removal → drop → refill" step of a move.
 *
 * @property removed candies removed in this wave, each cell once.
 * @property drops candy moves of the drop, in the order they were applied.
 * @property inserts new candies added by the refill, from the lowest row to the highest.
 * @property scoreAfter total score once this wave is counted.
 * @property objectivesAfter objective progress once this wave is counted.
 */
data class Wave(
    val multiplier: Int,
    val points: Int,
    val removed: List<TileCell>,
    val drops: List<GameMechanics.Move>,
    val inserts: List<GameMechanics.InsertMove>,
    val scoreAfter: Int,
    val objectivesAfter: List<ObjectiveProgress>,
)

/** Outcome of a swap request from the player. */
sealed class MoveResult {
    /** The gesture is not a swap of two neighbouring cells of a running game: nothing happens. */
    object Ignored : MoveResult()

    /** The swap does not form any alignment: the grid is unchanged and no move is used. */
    object Rejected : MoveResult()

    /** The swap was played; [waves] describes the whole resolution, [status] the state once the grid is stable. */
    data class Accepted(val waves: List<Wave>, val movesLeft: Int, val status: GameStatus) : MoveResult()
}

/**
 * A game in progress: the level, score, remaining moves and objectives. It resolves a whole move
 * (swap, waves of chain reactions, end of level) without any display, so that it can be tested.
 *
 * @param levelFactory creates a fresh copy of the level (used at start and on restart).
 * @param randomTiles candy source used to build a shuffled grid.
 */
class GameSession(
    private val levelFactory: () -> Level = { DefaultLevel.create() },
    private val randomTiles: () -> Tile = { Tile.randomTile() },
) {
    /** The level currently played (grid and reserve). */
    var level: Level = levelFactory()
        private set

    /** The game grid. */
    val field: GameField get() = level.field

    /** Total score of the game. */
    var score: Int = 0
        private set

    /** Number of moves left (never below 0). */
    var movesLeft: Int = level.maxMoves
        private set

    /** Current state of the game. */
    var status: GameStatus = GameStatus.PLAYING
        private set

    private val collected = mutableMapOf<Tile, Int>()

    private val mechanics: GameMechanics get() = GameMechanics(level.field)

    /** Progress of every objective of the level. */
    fun objectives(): List<ObjectiveProgress> = level.objectives.map {
        ObjectiveProgress(it.tile, it.goal, max(0, it.goal - (collected[it.tile] ?: 0)))
    }

    /**
     * Plays the swap of [a] and [b]: ignored if the game is over or the cells are not neighbours
     * inside the grid, rejected if no alignment is formed, otherwise fully resolved.
     */
    fun play(a: Position, b: Position): MoveResult {
        if (status != GameStatus.PLAYING || !areNeighbours(a, b)) return MoveResult.Ignored
        if (!mechanics.isSwapAllowed(a, b)) return MoveResult.Rejected
        mechanics.swapTiles(a, b)
        movesLeft = max(0, movesLeft - 1)
        val waves = resolve(a, b)
        status = computeStatus()
        return MoveResult.Accepted(waves, movesLeft, status)
    }

    /** Reloads the original level and resets score, moves and objectives. */
    fun restart() {
        level = levelFactory()
        resetCounters()
    }

    /** Replaces the grid by a random grid without any alignment and resets score, moves and objectives. */
    fun shuffle() {
        level = levelFactory()
        field.copyFrom(FieldGenerator.randomFieldWithoutAlignment(field.columnsSize, field.rowSize, randomTiles))
        resetCounters()
    }

    private fun resetCounters() {
        score = 0
        movesLeft = level.maxMoves
        status = GameStatus.PLAYING
        collected.clear()
    }

    private fun areNeighbours(a: Position, b: Position): Boolean {
        val inside = field[a] != Tile.OutOfSpace && field[b] != Tile.OutOfSpace
        return inside && abs(a.column - b.column) + abs(a.row - b.row) == 1
    }

    private fun resolve(a: Position, b: Position): List<Wave> {
        val swapAlignments = (mechanics.getAlignments(a) + mechanics.getAlignments(b))
            .map { alignment -> alignment.map { it.position }.toSet() }
            .distinct()
        val waves = mutableListOf<Wave>()
        var multiplier = ScoreRules.firstWaveMultiplier(swapAlignments.size)
        var toRemove = swapAlignments.flatten().distinct().map { field.getTileCell(it) }
        while (toRemove.isNotEmpty()) {
            waves += playWave(toRemove, multiplier)
            multiplier = ScoreRules.nextWaveMultiplier(multiplier)
            toRemove = mechanics.findAllAlignedCells()
        }
        return waves
    }

    private fun playWave(removed: List<TileCell>, multiplier: Int): Wave {
        removed.forEach {
            mechanics.removeTile(it.position)
            collected[it.tile] = (collected[it.tile] ?: 0) + 1
        }
        val points = ScoreRules.wavePoints(removed.size, multiplier)
        score += points
        val drops = mechanics.dropAllToGround()
        val inserts = mechanics.getNewTileMoves(level::getNextTile)
        mechanics.insert(inserts)
        return Wave(multiplier, points, removed, drops, inserts, score, objectives())
    }

    private fun computeStatus(): GameStatus = when {
        objectives().all { it.remaining == 0 } -> GameStatus.WON
        movesLeft == 0 -> GameStatus.LOST
        else -> GameStatus.PLAYING
    }
}
