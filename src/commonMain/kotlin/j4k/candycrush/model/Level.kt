package j4k.candycrush.model

/** Defines a playable board, its refill reserve, move budget, and collection objectives. */
class Level(
    levelData: String,
    reserveData: String? = null,
    val moves: Int = DEFAULT_MOVES,
    val objectives: List<TileObjective> = DEFAULT_OBJECTIVES,
    private val randomTile: () -> Tile = { Tile.randomTile() },
) {
    /** The mutable starting game field. */
    val field: GameField = GameField.fromString(levelData)

    /** The mutable reserve consumed from the bottom of each column. */
    val reserve: GameField? = reserveData?.let { GameField.fromString(it) }

    /** A target count for collecting one candy type. */
    data class TileObjective(val tile: Tile, val goal: Int)

    init {
        require(moves >= 0) { "Move count cannot be negative" }
        require(objectives.all { it.tile.isTile() && it.goal >= 0 }) { "Objectives require candies and non-negative goals" }
        require(reserve == null || reserve.columnsSize == field.columnsSize) {
            "The reserve and game field must have the same number of columns"
        }
    }

    /** Takes the next candy from the bottom of a reserve column, then uses randomness. */
    fun getNextTile(column: Int): Tile {
        require(column in 0 until field.columnsSize) { "Column $column is outside the field" }
        val currentReserve = reserve ?: return randomTile()
        val nextRow = (currentReserve.rowSize - 1 downTo 0).firstOrNull {
            currentReserve[column, it].isTile()
        } ?: return randomTile()
        val next = currentReserve[column, nextRow]
        for (row in nextRow downTo 1) currentReserve[column, row] = currentReserve[column, row - 1]
        currentReserve[column, 0] = Tile.Hole
        return next
    }

    companion object {
        /** Standard move budget for the supplied level. */
        const val DEFAULT_MOVES = 42
        /** Standard collection goals for the supplied level. */
        val DEFAULT_OBJECTIVES = listOf(TileObjective(Tile.A, 18), TileObjective(Tile.C, 22))

        /** The fixed five-column, nine-row board described by the project specification. */
        const val DEFAULT_LEVEL_DATA = """
            [D, B, A, B, D]
            [B, E, C, A, B]
            [B, E, C, C, B]
            [C, C, E, C, D]
            [B, B, C, A, D]
            [A, C, B, E, E]
            [D, E, E, B, A]
            [E, D, A, E, C]
            [E, A, D, C, B]
        """

        /** The supplied two-row candy reserve. */
        const val DEFAULT_RESERVE_DATA = """
            [E, C, B, A, D]
            [D, A, E, C, B]
        """

        /** Creates a fresh copy of the default level and reserve. */
        fun defaultLevel(randomTile: () -> Tile = { Tile.randomTile() }): Level =
            Level(DEFAULT_LEVEL_DATA, DEFAULT_RESERVE_DATA, randomTile = randomTile)
    }
}

/** Allows callers to refer to level goals without qualifying them through [Level]. */
typealias TileObjective = Level.TileObjective
