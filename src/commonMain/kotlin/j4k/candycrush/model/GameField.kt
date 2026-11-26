package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid

/** A mutable rectangular grid whose public coordinates are column then row. */
class GameField(val columnsSize: Int, val rowSize: Int) {
    private val rows: MutableList<Row>

    init {
        require(columnsSize >= 0 && rowSize >= 0) { "Field dimensions cannot be negative" }
        rows = MutableList(rowSize) { Row(columnsSize) }
    }

    /** Reads a cell, returning [Tile.OutOfSpace] outside the grid. */
    operator fun get(column: Int, row: Int): Tile =
        if (row in rows.indices) rows[row][column] else Tile.OutOfSpace

    /** Reads a cell by grid position. */
    operator fun get(position: PositionGrid.Position): Tile = get(position.column, position.row)

    /** Returns a mutable row or an out-of-space row for an invalid index. */
    operator fun get(row: Int): Row = getRow(row)

    /** Returns a mutable row or an out-of-space row for an invalid index. */
    fun getRow(row: Int): Row = rows.getOrElse(row) { Row.outOfSpace() }

    /** Writes a cell by grid position; out-of-range writes are ignored. */
    operator fun set(position: PositionGrid.Position, value: Tile) {
        set(position.column, position.row, value)
    }

    /** Writes a cell; out-of-range writes are ignored. */
    operator fun set(column: Int, row: Int, value: Tile) {
        if (row in rows.indices) rows[row][column] = value
    }

    /** Returns a column from top to bottom, rejecting invalid column indices. */
    fun getColumn(column: Int): List<Tile> {
        require(column in 0 until columnsSize) { "Column $column is outside the field" }
        return rows.map { it[column] }
    }

    /** Returns a tile and its position for the supplied coordinate. */
    fun getTileCell(column: Int = 0, row: Int = 0): TileCell =
        getTileCell(PositionGrid.Position(column, row))

    /** Returns a tile and its position for the supplied coordinate. */
    fun getTileCell(position: PositionGrid.Position): TileCell = TileCell(get(position), position)

    /** Creates an independent copy of this grid. */
    public fun clone(): GameField {
        val copy = GameField(columnsSize, rowSize)
        for (row in rows.indices) {
            for (column in 0 until columnsSize) copy[column, row] = this[column, row]
        }
        return copy
    }

    /** Formats the grid as one notation row per line. */
    override fun toString(): String = rows.joinToString("\n")

    companion object {
        /** Parses a rectangular grid from bracketed notation, ignoring blank and indented lines. */
        fun fromString(string: String): GameField {
            val parsedRows = string.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { line ->
                    require(line.startsWith("[") && line.endsWith("]")) { "Invalid field row: $line" }
                    val content = line.substring(1, line.length - 1).trim()
                    if (content.isEmpty()) emptyList() else content.split(',').map { Tile.getTile(it) }
                }
                .toList()
            require(parsedRows.isNotEmpty()) { "A field must contain at least one row" }
            val width = parsedRows.first().size
            require(parsedRows.all { it.size == width }) { "Field rows must have the same number of cells" }
            val field = GameField(width, parsedRows.size)
            parsedRows.forEachIndexed { row, tiles ->
                tiles.forEachIndexed { column, tile -> field[column, row] = tile }
            }
            return field
        }
    }
}
