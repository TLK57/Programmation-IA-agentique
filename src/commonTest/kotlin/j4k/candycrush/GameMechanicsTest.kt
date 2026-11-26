package j4k.candycrush

import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameMechanicsTest {
    @Test
    fun swapAndSwapValidationDoNotMutateDuringCheck() {
        val field = GameField.fromString("[A, B, A]\n[B, A, B]")
        val mechanics = GameMechanics(field)
        val before = field.toString()
        assertFalse(mechanics.isSwapAllowed(Position(0, 0), Position(1, 1)))
        assertFalse(mechanics.isSwapAllowed(Position(0, 0), Position(2, 0)))
        assertFalse(mechanics.isSwapAllowed(Position(-1, 0), Position(0, 0)))
        assertEquals(before, field.toString())

        val matching = GameField.fromString("[A, B, A]\n[H, A, H]")
        val matchingMechanics = GameMechanics(matching)
        assertTrue(matchingMechanics.isSwapAllowed(Position(1, 0), Position(1, 1)))
        assertEquals("[A, B, A]\n[H, A, H]", matching.toString())
        matchingMechanics.swapTiles(Position(1, 0), Position(1, 1))
        assertEquals(Tile.A, matching[1, 0])
    }

    @Test
    fun matchesIncludeLongRunsCrossesAndIgnoreHoles() {
        val field = GameField.fromString(
            """
                [H, A, H, H, H]
                [A, A, A, A, A]
                [H, A, H, H, H]
            """,
        )
        val mechanics = GameMechanics(field)
        val center = Position(1, 1)
        assertEquals(5, mechanics.getHorizontalSurroundings(center).size)
        assertEquals(3, mechanics.getVerticalSurroundings(center).size)
        assertTrue(mechanics.isInRowWithThree(center))
        assertEquals(8, mechanics.getConnectedTiles(center).size)
        assertEquals(3, mechanics.getConnectedTiles(center).take(3).size)
        assertEquals(TileCellForTest(Tile.Hole, Position(0, 0)), mechanics.getHorizontalSurroundings(Position(0, 0)).single().let {
            TileCellForTest(it.tile, it.position)
        })
        assertFalse(mechanics.isInRowWithThree(Position(0, 0)))
        assertTrue(mechanics.getConnectedTiles(Position(0, 0)).isEmpty())
    }

    @Test
    fun gravityAndRefillAreOrderedByLowestRowThenColumn() {
        val field = GameField.fromString(
            """
                [A, H]
                [H, B]
                [C, H]
                [H, D]
            """,
        )
        val mechanics = GameMechanics(field)
        val moves = mechanics.dropAllToGround()
        assertEquals(
            listOf(
                GameMechanics.Move(Position(0, 3), Position(0, 2)),
                GameMechanics.Move(Position(0, 2), Position(0, 0)),
                GameMechanics.Move(Position(1, 2), Position(1, 1)),
            ),
            moves,
        )
        assertEquals("[H, H]\n[H, H]\n[A, B]\n[C, D]", field.toString())
        assertEquals(listOf(Position(0, 0), Position(1, 0), Position(0, 1), Position(1, 1)), mechanics.listEmptyCells())

        val requestedColumns = mutableListOf<Int>()
        val insertions = mechanics.getNewTileMoves { column ->
            requestedColumns += column
            if (column == 0) Tile.E else Tile.A
        }
        assertEquals(listOf(0, 1, 0, 1), requestedColumns)
        assertEquals(listOf(1, 1, 0, 0), insertions.map { it.target.row })
        mechanics.insert(insertions)
        assertEquals(Tile.E, field[0, 0])
        assertEquals(Tile.A, field[1, 0])
        assertTrue(mechanics.listEmptyCells().isEmpty())
        assertNull(mechanics.getNextDropMove(0))

        val orderedField = GameField.fromString("[H, H]\n[H, H]")
        val orderedTiles = mutableListOf(Tile.A, Tile.B, Tile.C, Tile.D)
        val orderedMoves = GameMechanics(orderedField).getNewTileMoves {
            orderedTiles.removeAt(0)
        }
        assertEquals(listOf(Tile.A, Tile.B, Tile.C, Tile.D), orderedMoves.map { it.tile })
        assertEquals(listOf(1, 1, 0, 0), orderedMoves.map { it.target.row })
    }

    @Test
    fun matchFreeBoardFactoryAcceptsDeterministicTileSource() {
        val source = buildList {
            for (row in 0 until 9) {
                for (column in 0 until 5) add(listOf(Tile.A, Tile.B, Tile.C, Tile.D, Tile.E)[(row + column) % 5])
            }
        }.iterator()
        val field = BoardFactory.createMatchFreeField(5, 9) { source.next() }
        val mechanics = GameMechanics(field)
        for (row in 0 until field.rowSize) {
            for (column in 0 until field.columnsSize) {
                val position = Position(column, row)
                assertTrue(field[position].isTile())
                assertFalse(mechanics.isInRowWithThree(position))
            }
        }
    }

    private data class TileCellForTest(val tile: Tile, val position: Position)
}
