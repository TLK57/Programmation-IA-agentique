package j4k.candycrush.model

/** A mutable row of tiles, returning [Tile.OutOfSpace] for reads outside its bounds. */
class Row {
    private val cells: MutableList<Tile>

    /** Creates a row of [rowSize] empty cells. */
    constructor(rowSize: Int) {
        require(rowSize >= 0) { "Row size cannot be negative" }
        cells = MutableList(rowSize) { Tile.Hole }
    }

    /** Creates a row containing the supplied tiles. */
    constructor(vararg tiles: Tile) {
        cells = tiles.toMutableList()
    }

    /** Reads a cell, returning [Tile.OutOfSpace] when the column is invalid. */
    operator fun get(column: Int): Tile = cells.getOrElse(column) { Tile.OutOfSpace }

    /** Writes a tile when the column is in range; out-of-range writes are ignored. */
    operator fun set(column: Int, tile: Tile) {
        if (column in cells.indices) cells[column] = tile
    }

    /** Writes a tile using its one-letter notation. */
    operator fun set(column: Int, tileShortName: String) {
        set(column, Tile.getTile(tileShortName))
    }

    /** Returns the number of cells in this row. */
    fun size(): Int = cells.size

    /** Compares rows by their tile contents. */
    override fun equals(other: Any?): Boolean = other is Row && cells == other.cells

    /** Returns a hash consistent with content-based row equality. */
    override fun hashCode(): Int = cells.hashCode()

    /** Formats this row using the level notation. */
    override fun toString(): String = cells.joinToString(prefix = "[", postfix = "]") { it.shortName() }

    companion object {
        /** Returns the shared empty row used for out-of-range row reads. */
        fun outOfSpace(): Row = OutOfSpaceRow

        private val OutOfSpaceRow = Row()
    }
}
