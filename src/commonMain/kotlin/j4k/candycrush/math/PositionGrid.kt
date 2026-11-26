package j4k.candycrush.math

import com.soywiz.korma.geom.Point

/** Converts grid coordinates to pixels and pixels back to grid coordinates. */
class PositionGrid(
    val x: Int = 0,
    val y: Int = 0,
    val columns: Int,
    val rows: Int,
    val tileSize: Int,
) {
    init {
        require(tileSize > 0) { "Tile size must be positive" }
    }

    /** A zero-based grid coordinate. */
    data class Position(val column: Int = 0, val row: Int = 0)

    /** Returns the pixel coordinate of a cell's top-left corner. */
    fun getPosition(column: Int, row: Int): Point =
        Point((x + column * tileSize).toDouble(), (y + row * tileSize).toDouble())

    /** Returns the cell containing the pixel coordinate. */
    fun getField(posX: Int, posY: Int): Position =
        Position(floorCell(posX - x), floorCell(posY - y))

    /** Returns whether the supplied position is within this grid. */
    fun isOnGrid(pos: Position): Boolean =
        pos.column in 0 until columns && pos.row in 0 until rows

    private fun floorCell(distance: Int): Int {
        val quotient = distance / tileSize
        return if (distance < 0 && distance % tileSize != 0) quotient - 1 else quotient
    }
}
