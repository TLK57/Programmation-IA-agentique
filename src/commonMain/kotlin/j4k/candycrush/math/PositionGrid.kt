package j4k.candycrush.math

import com.soywiz.korma.geom.Point

/** Maps grid cells to screen pixels. */
class PositionGrid(val x: Int = 0, val y: Int = 0, val columns: Int, val rows: Int, val tileSize: Int) {
    init { require(tileSize > 0) { "tileSize must be positive" } }
    /** A zero-based cell position. */
    data class Position(val column: Int = 0, val row: Int = 0)

    /** Returns the top-left pixel of a cell. */
    fun getPosition(column: Int, row: Int): Point = Point((x + column * tileSize).toDouble(), (y + row * tileSize).toDouble())

    /** Converts a pixel to its containing cell. */
    fun getField(posX: Int, posY: Int): Position = Position(cellAt(posX, x), cellAt(posY, y))

    private fun cellAt(pixel: Int, origin: Int): Int {
        val offset = pixel.toLong() - origin
        return if (offset >= 0) (offset / tileSize).toInt()
        else -(((-offset + tileSize - 1) / tileSize).toInt())
    }

    /** Returns whether a cell lies within this grid. */
    fun isOnGrid(pos: Position): Boolean = pos.column in 0 until columns && pos.row in 0 until rows
}
