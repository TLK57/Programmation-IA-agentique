package j4k.candycrush.model

/**
 * One row of the grid. Reading outside of the row returns [Tile.OutOfSpace];
 * writing outside of the row is ignored.
 */
class Row private constructor(private val tiles: Array<Tile>, private val readOnly: Boolean) {

    /** Creates a row of [rowSize] cells, all set to [Tile.Hole]. */
    constructor(rowSize: Int) : this(Array(rowSize) { Tile.Hole }, false)

    /** Creates a row containing the given [tiles], in order. */
    constructor(vararg tiles: Tile) : this(arrayOf(*tiles), false)

    /** Returns the tile at [column], or [Tile.OutOfSpace] outside of the row. */
    operator fun get(column: Int): Tile = tiles.getOrElse(column) { Tile.OutOfSpace }

    /** Sets the tile at [column]; ignored outside of the row. */
    operator fun set(column: Int, tile: Tile) {
        if (!readOnly && column in tiles.indices) tiles[column] = tile
    }

    /** Sets the tile at [column] from its one-letter name (for example `"E"`). */
    operator fun set(column: Int, tileShortName: String) {
        set(column, Tile.getTile(tileShortName))
    }

    /** Number of cells in the row. */
    fun size(): Int = tiles.size

    /** Returns an independent copy of this row. */
    fun copy(): Row = Row(tiles.copyOf(), readOnly)

    override fun equals(other: Any?): Boolean = other is Row && tiles.contentEquals(other.tiles)

    override fun hashCode(): Int = tiles.contentHashCode()

    override fun toString(): String = tiles.joinToString(", ", "[", "]") { it.shortName() }

    companion object {
        /** Row returned when reading outside of the grid: every read gives [Tile.OutOfSpace]. */
        fun outOfSpace(): Row = Row(emptyArray(), true)
    }
}
