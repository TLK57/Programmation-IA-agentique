package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class GameFieldTest {
    @Test
    fun defaultLevelMatchesSpecifiedBoardAndGoals() {
        val level = Level.defaultLevel { Tile.E }
        assertEquals(
            "[D, B, A, B, D]\n[B, E, C, A, B]\n[B, E, C, C, B]\n[C, C, E, C, D]\n[B, B, C, A, D]\n[A, C, B, E, E]\n[D, E, E, B, A]\n[E, D, A, E, C]\n[E, A, D, C, B]",
            level.field.toString(),
        )
        assertEquals(42, level.moves)
        assertEquals(listOf(18, 22), level.objectives.map { it.goal })
    }

    @Test
    fun notationAndOutOfBoundsAccess() {
        val field = GameField.fromString(
            """
                [A,B, H]

                    [C, D,E]
            """,
        )
        assertEquals(3, field.columnsSize)
        assertEquals(2, field.rowSize)
        assertEquals("[A, B, H]\n[C, D, E]", field.toString())
        assertEquals(Tile.OutOfSpace, field[-1, 0])
        assertEquals(Tile.OutOfSpace, field[PositionGrid.Position(0, 2)])
        assertEquals(Tile.OutOfSpace, field[0][-1])
        assertEquals(Tile.OutOfSpace, field[0][3])
        assertEquals(Row.outOfSpace(), field[-1])
        assertFailsWith<IllegalArgumentException> { field.getColumn(3) }
    }

    @Test
    fun rowsAreMutableComparableAndFieldCloneIsIndependent() {
        val field = GameField.fromString("[A, H]\n[H, B]")
        field[1][1] = "E"
        assertEquals("[A, H]\n[H, E]", field.toString())
        assertEquals(Row(Tile.A, Tile.Hole), field[0])
        assertEquals(listOf(Tile.Hole, Tile.E), field.getColumn(1))

        val copy = field.clone()
        copy[0, 0] = Tile.C
        assertEquals(Tile.A, field[0, 0])
        assertNotEquals(field[0], copy[0])
    }

    @Test
    fun tileNotationAndProperties() {
        assertEquals(Tile.OutOfSpace, Tile.getTile(Tile.OutOfSpace.shortName()))
        assertEquals("W", Tile.Wall.shortName())
        assertTrue(Tile.E.isTile())
        assertFalse(Tile.Wall.isTile())
        assertTrue(Tile.Hole.isHole())
        assertTrue(Tile.Hole.isNotTile())
        assertFailsWith<IllegalArgumentException> { Tile.getTile("?") }
    }
}
