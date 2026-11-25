package j4k.candycrush.model

/**
 * A level: the starting grid ([levelData]), an optional reserve feeding the refill ([reserveData]),
 * the number of moves and the collection objectives. Both grids use the text notation.
 *
 * @param randomTiles source of candies used once a reserve column is empty (injectable for tests).
 */
class Level(
    levelData: String,
    reserveData: String? = null,
    val maxMoves: Int = DEFAULT_MOVES,
    val objectives: List<TileObjective> = emptyList(),
    private val randomTiles: () -> Tile = { Tile.randomTile() },
) {
    /** The game grid, read from the level data. */
    val field: GameField = GameField.fromString(levelData)

    /** The current reserve, or null when the level has none; its `toString()` gives the text notation. */
    val reserve: GameField? = reserveData?.let { GameField.fromString(it) }

    /** A collection objective: remove at least [goal] candies of type [tile]. */
    data class TileObjective(val tile: Tile, val goal: Int)

    /**
     * Returns the next candy for [column] and updates the reserve: the lowest candy of the
     * reserve column is taken out and the candies above it move down one cell. When the
     * reserve column is empty, a random candy is returned.
     */
    fun getNextTile(column: Int): Tile {
        val reserveField = reserve ?: return randomTiles()
        val lowestRow = (reserveField.rowSize - 1 downTo 0).firstOrNull { reserveField[column, it].isTile() }
            ?: return randomTiles()
        val tile = reserveField[column, lowestRow]
        for (row in lowestRow downTo 1) {
            reserveField[column, row] = reserveField[column, row - 1]
        }
        reserveField[column, 0] = Tile.Hole
        return tile
    }

    companion object {
        /** Default number of moves of a level. */
        const val DEFAULT_MOVES = 42
    }
}
