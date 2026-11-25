package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid.Position

/**
 * The game grid: [columnsSize] columns and [rowSize] rows, every cell set to [Tile.Hole] at creation.
 * Reading outside of the grid returns [Tile.OutOfSpace]; writing outside of the grid is ignored.
 */
class GameField(val columnsSize: Int, val rowSize: Int) {

    private val rows: MutableList<Row> = MutableList(rowSize) { Row(columnsSize) }

    /** Returns the tile at ([column], [row]), or [Tile.OutOfSpace] outside of the grid. */
    operator fun get(column: Int, row: Int): Tile = getRow(row)[column]

    /** Returns the tile at [position], or [Tile.OutOfSpace] outside of the grid. */
    operator fun get(position: Position): Tile = get(position.column, position.row)

    /** Returns the (modifiable) row number [row], or [Row.outOfSpace] outside of the grid. */
    operator fun get(row: Int): Row = getRow(row)

    /** Returns the (modifiable) row number [row], or [Row.outOfSpace] outside of the grid. */
    fun getRow(row: Int): Row = rows.getOrElse(row) { Row.outOfSpace() }

    /** Sets the tile at [position]; ignored outside of the grid. */
    operator fun set(position: Position, value: Tile) {
        set(position.column, position.row, value)
    }

    /** Sets the tile at ([column], [row]); ignored outside of the grid. */
    operator fun set(column: Int, row: Int, value: Tile) {
        getRow(row)[column] = value
    }

    /** Returns the column [column] from top to bottom; throws [IllegalArgumentException] outside of the grid. */
    fun getColumn(column: Int): List<Tile> {
        require(column in 0 until columnsSize) { "Column $column is outside of the grid" }
        return rows.map { it[column] }
    }

    /** Returns the tile at ([column], [row]) together with its position. */
    fun getTileCell(column: Int = 0, row: Int = 0): TileCell = TileCell(get(column, row), Position(column, row))

    /** Returns the tile at [position] together with its position. */
    fun getTileCell(position: Position): TileCell = TileCell(get(position), position)

    /** Returns every position of the grid, row by row from the top-left corner. */
    fun positions(): List<Position> =
        (0 until rowSize).flatMap { row -> (0 until columnsSize).map { column -> Position(column, row) } }

    /** Returns an independent copy of the grid. */
    fun clone(): GameField {
        val copy = GameField(columnsSize, rowSize)
        rows.forEachIndexed { index, row -> copy.rows[index] = row.copy() }
        return copy
    }

    /** Replaces the whole content of this grid by the content of [other], which must have the same size. */
    fun copyFrom(other: GameField) {
        require(other.columnsSize == columnsSize && other.rowSize == rowSize) { "Grid sizes differ" }
        positions().forEach { set(it, other[it]) }
    }

    override fun equals(other: Any?): Boolean =
        other is GameField && columnsSize == other.columnsSize && rows == other.rows

    override fun hashCode(): Int = rows.hashCode()

    override fun toString(): String = rows.joinToString("\n")

    companion object {
        /** Reads a grid written in text notation, for example `"[A, H]\n[H, B]"` (spaces and indentation are optional). */
        fun fromString(string: String): GameField {
            val lines = string.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val parsedRows = lines.map(::parseRow)
            val field = GameField(parsedRows.firstOrNull()?.size ?: 0, parsedRows.size)
            parsedRows.forEachIndexed { row, tiles ->
                tiles.forEachIndexed { column, tile -> field[column, row] = tile }
            }
            return field
        }

        private fun parseRow(line: String): List<Tile> =
            line.removePrefix("[").removeSuffix("]")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { Tile.getTile(it) }
    }
}
