package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid.Position

/** Rectangular mutable board; columns are addressed before rows in coordinate accessors. */
class GameField(val columnsSize: Int, val rowSize: Int) {
    private val rows = MutableList(rowSize) { MutableList(columnsSize) { Tile.Hole } }
    /** Reads a tile at (column,row), returning OutOfSpace beyond the board. */
    operator fun get(column: Int, row: Int): Tile = if (column in 0 until columnsSize && row in 0 until rowSize) rows[row][column] else Tile.OutOfSpace
    /** Reads a tile at a position. */
    operator fun get(position: Position): Tile = get(position.column, position.row)
    /** Returns a mutable view of a row or an out-of-space sentinel. */
    operator fun get(row: Int): Row = getRow(row)
    /** Returns a mutable row view. */
    fun getRow(row: Int): Row = if (row in 0 until rowSize) Row(rows[row]) else Row.outOfSpace()
    /** Sets a tile at a position; out-of-board writes are ignored. */
    operator fun set(position: Position, value: Tile) { set(position.column, position.row, value) }
    /** Sets a tile at (column,row); out-of-board writes are ignored. */
    operator fun set(column: Int, row: Int, value: Tile) { if (column in 0 until columnsSize && row in 0 until rowSize) rows[row][column] = value }
    /** Returns a column from top to bottom. */
    fun getColumn(column: Int): List<Tile> { require(column in 0 until columnsSize); return (0 until rowSize).map { get(column, it) } }
    /** Returns a tile cell at the given coordinates. */
    fun getTileCell(column: Int = 0, row: Int = 0): TileCell = TileCell(get(column, row), Position(column, row))
    /** Returns a tile cell at a position. */
    fun getTileCell(position: Position): TileCell = getTileCell(position.column, position.row)
    /** Makes an independent copy of the board. */
    fun clone(): GameField = fromString(toString())
    override fun toString(): String = (0 until rowSize).joinToString("\n") { r -> (0 until columnsSize).map { get(it, r) }.joinToString(prefix = "[", postfix = "]", separator = ", ") }
    companion object {
        /** Parses rows such as `[A, H]`, ignoring blank lines and indentation. */
        fun fromString(string: String): GameField {
            val parsed = string.lines().map { it.trim() }.filter { it.isNotEmpty() }.map { line ->
                require(line.startsWith("[") && line.endsWith("]")) { "Invalid row: $line" }
                line.substring(1, line.length - 1).split(',').map { Tile.fromShortName(it) }
            }
            require(parsed.isNotEmpty() && parsed.all { it.size == parsed.first().size }) { "Field must be nonempty and rectangular" }
            return GameField(parsed.first().size, parsed.size).also { f -> parsed.forEachIndexed { y, row -> row.forEachIndexed { x, tile -> f[x, y] = tile } } }
        }
    }
}

/** A tile together with its location on a field. */
data class TileCell(val tile: Tile, val position: Position)
