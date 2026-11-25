package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid.Position
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TileTest {
    @Test
    fun candiesAreTilesAndOthersAreNot() {
        listOf(Tile.A, Tile.B, Tile.C, Tile.D, Tile.E).forEach {
            assertTrue(it.isTile())
            assertFalse(it.isNotTile())
        }
        listOf(Tile.Wall, Tile.Hole, Tile.OutOfSpace).forEach {
            assertFalse(it.isTile())
            assertTrue(it.isNotTile())
        }
    }

    @Test
    fun onlyHoleIsHole() {
        assertEquals(listOf(Tile.Hole), Tile.values().filter { it.isHole() })
    }

    @Test
    fun shortNameRoundTrip() {
        assertEquals(listOf("A", "B", "C", "D", "E", "W", "H", "O"), Tile.values().map { it.shortName() })
        Tile.values().forEach { assertEquals(it, Tile.getTile(it.shortName())) }
    }

    @Test
    fun unknownShortNameThrows() {
        assertFailsWith<IllegalArgumentException> { Tile.getTile("X") }
    }

    @Test
    fun randomTileIsAlwaysACandy() {
        repeat(100) { assertTrue(Tile.randomTile().isTile()) }
    }
}

class RowTest {
    @Test
    fun newRowIsFullOfHoles() {
        val row = Row(3)
        assertEquals(3, row.size())
        assertEquals("[H, H, H]", row.toString())
    }

    @Test
    fun readOutsideReturnsOutOfSpace() {
        val row = Row(Tile.A, Tile.B)
        assertEquals(Tile.OutOfSpace, row[-1])
        assertEquals(Tile.OutOfSpace, row[2])
    }

    @Test
    fun setByTileAndByShortName() {
        val row = Row(2)
        row[0] = Tile.C
        row[1] = "E"
        assertEquals(Row(Tile.C, Tile.E), row)
        assertEquals(Row(Tile.C, Tile.E).hashCode(), row.hashCode())
    }

    @Test
    fun outOfSpaceRowsAreEqualAndAlwaysOutOfSpace() {
        val row = Row.outOfSpace()
        assertEquals(Row.outOfSpace(), row)
        row[0] = Tile.A
        assertEquals(Tile.OutOfSpace, row[0])
        assertNotEquals(Row(Tile.A), row)
    }
}

class GameFieldTest {
    private val text = "[A, B, C]\n[D, E, H]"

    @Test
    fun readAndWriteTextNotation() {
        val field = GameField.fromString(text)
        assertEquals(3, field.columnsSize)
        assertEquals(2, field.rowSize)
        assertEquals(text, field.toString())
    }

    @Test
    fun readAcceptsMissingSpacesIndentationAndBlankLines() {
        val field = GameField.fromString("\n    [A,B,C]\n\n    [D,E,H]\n")
        assertEquals(text, field.toString())
    }

    @Test
    fun newFieldIsFullOfHoles() {
        assertEquals("[H, H]\n[H, H]", GameField(2, 2).toString())
    }

    @Test
    fun accessByColumnRowAndPosition() {
        val field = GameField.fromString(text)
        assertEquals(Tile.B, field[1, 0])
        assertEquals(Tile.E, field[Position(column = 1, row = 1)])
        assertEquals(Tile.D, field[1][0])
        assertEquals(Row(Tile.D, Tile.E, Tile.Hole), field.getRow(1))
    }

    @Test
    fun readOutsideReturnsOutOfSpace() {
        val field = GameField.fromString(text)
        assertEquals(Tile.OutOfSpace, field[-1, 0])
        assertEquals(Tile.OutOfSpace, field[3, 0])
        assertEquals(Tile.OutOfSpace, field[0, 2])
        assertEquals(Tile.OutOfSpace, field[Position(0, -1)])
        assertEquals(Row.outOfSpace(), field[5])
    }

    @Test
    fun writeThroughRowAndPosition() {
        val field = GameField(3, 2)
        field[1][2] = "B"
        field[Position(0, 0)] = Tile.A
        field[1, 0] = Tile.C
        assertEquals("[A, C, H]\n[H, H, B]", field.toString())
    }

    @Test
    fun writeOutsideIsIgnored() {
        val field = GameField(1, 1)
        field[3, 3] = Tile.A
        assertEquals("[H]", field.toString())
    }

    @Test
    fun getColumnFromTopToBottom() {
        val field = GameField.fromString(text)
        assertEquals(listOf(Tile.C, Tile.Hole), field.getColumn(2))
        assertFailsWith<IllegalArgumentException> { field.getColumn(3) }
        assertFailsWith<IllegalArgumentException> { field.getColumn(-1) }
    }

    @Test
    fun getTileCell() {
        val field = GameField.fromString(text)
        assertEquals(TileCell(Tile.E, Position(1, 1)), field.getTileCell(column = 1, row = 1))
        assertEquals(TileCell(Tile.A, Position(0, 0)), field.getTileCell())
        assertEquals(TileCell(Tile.OutOfSpace, Position(9, 9)), field.getTileCell(Position(9, 9)))
    }

    @Test
    fun cloneIsIndependent() {
        val field = GameField.fromString(text)
        val copy = field.clone()
        copy[0, 0] = Tile.E
        assertEquals(Tile.A, field[0, 0])
        assertEquals(Tile.E, copy[0, 0])
    }
}

class LevelTest {
    @Test
    fun levelReadsGridAndReserve() {
        val level = Level("[A, B]\n[C, D]", "[E, E]")
        assertEquals("[A, B]\n[C, D]", level.field.toString())
        assertEquals("[E, E]", level.reserve.toString())
        assertEquals(Level.DEFAULT_MOVES, level.maxMoves)
    }

    @Test
    fun nextTileComesFromTheBottomOfTheReserveColumn() {
        val level = Level("[H, H, H, H]", "[B, D, E, A]\n[A, B, D, E]")
        assertEquals(Tile.A, level.getNextTile(0))
        assertEquals("[H, D, E, A]\n[B, B, D, E]", level.reserve.toString())
        assertEquals(Tile.B, level.getNextTile(0))
        assertEquals("[H, D, E, A]\n[H, B, D, E]", level.reserve.toString())
    }

    @Test
    fun emptyReserveColumnFallsBackToRandomSource() {
        val level = Level("[H, H]", "[A, H]", randomTiles = { Tile.D })
        assertEquals(Tile.A, level.getNextTile(0))
        assertEquals(Tile.D, level.getNextTile(0))
        assertEquals(Tile.D, level.getNextTile(1))
    }

    @Test
    fun levelWithoutReserveUsesRandomSource() {
        val level = Level("[H]", randomTiles = { Tile.C })
        assertEquals(null, level.reserve)
        assertEquals(Tile.C, level.getNextTile(0))
    }
}
