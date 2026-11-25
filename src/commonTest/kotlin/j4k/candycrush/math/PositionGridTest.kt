package j4k.candycrush.math

import j4k.candycrush.math.PositionGrid.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PositionGridTest {
    private val grid = PositionGrid(x = 100, y = 250, columns = 5, rows = 9, tileSize = 120)

    @Test
    fun getPositionReturnsTopLeftCornerOfCell() {
        val point = grid.getPosition(2, 3)
        assertEquals(340.0, point.x)
        assertEquals(610.0, point.y)
    }

    @Test
    fun getFieldReturnsCellContainingPixel() {
        assertEquals(Position(0, 0), grid.getField(100, 250))
        assertEquals(Position(2, 3), grid.getField(459, 729))
        assertEquals(Position(3, 4), grid.getField(460, 730))
    }

    @Test
    fun isOnGrid() {
        assertTrue(grid.isOnGrid(Position(0, 0)))
        assertTrue(grid.isOnGrid(Position(4, 8)))
        assertFalse(grid.isOnGrid(Position(5, 0)))
        assertFalse(grid.isOnGrid(Position(0, 9)))
        assertFalse(grid.isOnGrid(Position(-1, 0)))
    }

    @Test
    fun isPixelOnGridRejectsPixelsAroundTheGrid() {
        assertTrue(grid.isPixelOnGrid(100, 250))
        assertFalse(grid.isPixelOnGrid(99, 300))
        assertFalse(grid.isPixelOnGrid(150, 249))
        assertFalse(grid.isPixelOnGrid(700, 300))
    }
}
