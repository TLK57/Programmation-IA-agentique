package j4k.candycrush.model

/** Mutable row of tiles, backed by a field when constructed by GameField. */
class Row internal constructor(private val values: MutableList<Tile>, private val writable: Boolean = true) {
    constructor(rowSize: Int) : this(MutableList(rowSize) { Tile.Hole })
    constructor(vararg tiles: Tile) : this(tiles.toMutableList())
    /** Reads a tile, returning OutOfSpace beyond this row. */
    operator fun get(column: Int): Tile = values.getOrElse(column) { Tile.OutOfSpace }
    /** Replaces a tile in this row. */
    operator fun set(column: Int, tile: Tile) { if (writable && column in values.indices) values[column] = tile }
    /** Replaces a tile using its notation name. */
    operator fun set(column: Int, tileShortName: String) { set(column, Tile.fromShortName(tileShortName)) }
    /** Number of cells in this row. */
    fun size(): Int = values.size
    override fun equals(other: Any?): Boolean = other is Row && values == other.values
    override fun hashCode(): Int = values.hashCode()
    override fun toString(): String = values.joinToString(prefix = "[", postfix = "]", separator = ", ")
    companion object {
        /** Sentinel row returned for an out-of-grid row lookup. */
        fun outOfSpace(): Row = Row(mutableListOf(Tile.OutOfSpace), false)
    }
}
