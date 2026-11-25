package j4k.candycrush.math

import com.soywiz.korma.geom.Point

/**
 * Mapping between grid cells and screen pixels, for a grid whose top-left corner
 * is at ([x], [y]) and whose square cells are [tileSize] pixels wide.
 */
class PositionGrid(val x: Int = 0, val y: Int = 0, val columns: Int, val rows: Int, val tileSize: Int) {

    /** A cell of the grid: column 0 is on the left, row 0 at the top. */
    data class Position(val column: Int = 0, val row: Int = 0)

    /** Top-left corner of the cell in pixels, without checking that the cell exists. */
    fun getPosition(column: Int, row: Int): Point = Point(x + column * tileSize, y + row * tileSize)

    /**
     * Cell that contains the pixel ([posX], [posY]), by integer division of the coordinates
     * relative to the grid corner. Pixels left of or above the grid should be filtered by the caller.
     */
    fun getField(posX: Int, posY: Int): Position = Position((posX - x) / tileSize, (posY - y) / tileSize)

    /** Returns true if the pixel ([posX], [posY]) lies inside the grid area. */
    fun isPixelOnGrid(posX: Int, posY: Int): Boolean =
        posX >= x && posY >= y && isOnGrid(getField(posX, posY))

    /** Returns true if [pos] is inside the grid. */
    fun isOnGrid(pos: Position): Boolean = pos.column in 0 until columns && pos.row in 0 until rows
}
