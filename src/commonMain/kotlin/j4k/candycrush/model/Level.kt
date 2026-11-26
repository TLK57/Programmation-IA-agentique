package j4k.candycrush.model

/** A target count for collecting one candy type. */
data class TileObjective(val tile: Tile, val goal: Int)

/** Initial board, refill reserve, move limit, and collection goals for one level. */
class Level(
    levelData: String,
    reserveData: String? = null,
    val initialMoves: Int = 42,
    val objectives: List<TileObjective> = listOf(TileObjective(Tile.A, 18), TileObjective(Tile.C, 22)),
) {
    val field: GameField = GameField.fromString(levelData)
    private val initialReserve = reserveData?.let(GameField::fromString)
    val reserve: GameField? = initialReserve?.clone()
    /** Consumes the lowest remaining reserve candy in a column, or chooses a random candy. */
    fun getNextTile(column: Int): Tile {
        val r = reserve
        if (r != null && column in 0 until r.columnsSize) {
            for (row in r.rowSize - 1 downTo 0) {
                val tile = r[column, row]
                if (tile.isCandy()) {
                    for (y in row downTo 1) r[column, y] = r[column, y - 1]
                    r[column, 0] = Tile.Hole
                    return tile
                }
            }
        }
        return Tile.values().take(5).random()
    }
    /** Restores the reserve after a replay. */
    fun resetReserve() { if (initialReserve != null) for (y in 0 until initialReserve.rowSize) for (x in 0 until initialReserve.columnsSize) reserve!![x, y] = initialReserve[x, y] }
}
