package j4k.candycrush

import j4k.candycrush.GameMechanics.InsertMove
import j4k.candycrush.GameMechanics.Move
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameMechanicsTest {

    private fun mechanics(text: String) = GameMechanics(GameField.fromString(text.trimIndent()))

    private fun cells(tile: Tile, vararg positions: Pair<Int, Int>) =
        positions.map { (column, row) -> TileCell(tile, Position(column, row)) }

    @Test
    fun swapExchangesTwoCells() {
        val m = mechanics("[A, B]\n[C, D]")
        m.swapTiles(Position(0, 0), Position(1, 1))
        assertEquals("[D, B]\n[C, A]", m.toString())
    }

    @Test
    fun removeTilePutsAHole() {
        val m = mechanics("[A, B]")
        m.removeTile(Position(1, 0))
        assertEquals("[A, H]", m.toString())
    }

    @Test
    fun swapFormingAnAlignmentIsAllowedAndLeavesGridUnchanged() {
        val m = mechanics(
            """
            [A, A, B, A]
            [C, D, E, C]
            """
        )
        val before = m.toString()
        assertTrue(m.isSwapAllowed(Position(2, 0), Position(3, 0)))
        assertEquals(before, m.toString())
    }

    @Test
    fun swapFormingAVerticalAlignmentForTheOtherTileIsAllowed() {
        val m = mechanics(
            """
            [B, C]
            [B, D]
            [C, B]
            """
        )
        assertTrue(m.isSwapAllowed(Position(0, 2), Position(1, 2)))
    }

    @Test
    fun swapWithoutAlignmentIsRefused() {
        val m = mechanics("[A, B, C, D]\n[B, C, D, E]")
        assertFalse(m.isSwapAllowed(Position(0, 0), Position(1, 0)))
        assertEquals("[A, B, C, D]\n[B, C, D, E]", m.toString())
    }

    @Test
    fun swapWithHoleOrOutsideIsRefused() {
        val m = mechanics("[A, A, H, A]")
        assertFalse(m.isSwapAllowed(Position(2, 0), Position(3, 0)))
        assertFalse(m.isSwapAllowed(Position(3, 0), Position(4, 0)))
    }

    @Test
    fun horizontalSurroundingsOfThree() {
        val m = mechanics("[B, A, A, A, C]")
        assertEquals(cells(Tile.A, 1 to 0, 2 to 0, 3 to 0), m.getHorizontalSurroundings(Position(2, 0)))
    }

    @Test
    fun surroundingsAreNotLimitedInLength() {
        val m = mechanics("[C, C, C, C, C, C]")
        assertEquals(6, m.getHorizontalSurroundings(Position(0, 0)).size)
        assertEquals(6, m.getConnectedTiles(Position(5, 0)).size)
    }

    @Test
    fun verticalSurroundingsSortedFromTopToBottom() {
        val m = mechanics("[A]\n[D]\n[D]\n[D]\n[D]")
        assertEquals(cells(Tile.D, 0 to 1, 0 to 2, 0 to 3, 0 to 4), m.getVerticalSurroundings(Position(0, 4)))
    }

    @Test
    fun surroundingsOfAHoleIsTheHoleOnly() {
        val m = mechanics("[H, H, H]")
        assertEquals(listOf(TileCell(Tile.Hole, Position(1, 0))), m.getHorizontalSurroundings(Position(1, 0)))
        assertEquals(listOf(TileCell(Tile.Hole, Position(1, 0))), m.getVerticalSurroundings(Position(1, 0)))
    }

    @Test
    fun holesAreNeverAligned() {
        val m = mechanics("[H, H, H]\n[H, H, H]\n[H, H, H]")
        assertFalse(m.isInRowWithThree(Position(1, 1)))
        assertTrue(m.getConnectedTiles(Position(1, 1)).isEmpty())
    }

    @Test
    fun twoIdenticalTilesAreNotAnAlignment() {
        val m = mechanics("[A, A, B]\n[C, D, B]")
        assertFalse(m.isInRowWithThree(Position(0, 0)))
        assertTrue(m.getConnectedTiles(Position(0, 0)).isEmpty())
    }

    @Test
    fun crossingAlignmentListsVerticalThenHorizontalWithCenterTwice() {
        val m = mechanics(
            """
            [B, A, C]
            [A, A, A]
            [D, A, E]
            """
        )
        val expected = cells(Tile.A, 1 to 0, 1 to 1, 1 to 2) + cells(Tile.A, 0 to 1, 1 to 1, 2 to 1)
        assertEquals(expected, m.getConnectedTiles(Position(1, 1)))
        assertTrue(m.isInRowWithThree(Position(1, 0)))
        assertTrue(m.isInRowWithThree(Position(2, 1)))
        assertFalse(m.isInRowWithThree(Position(0, 0)))
    }

    @Test
    fun findAllAlignedCellsReturnsEachCellOnce() {
        val m = mechanics(
            """
            [B, A, C]
            [A, A, A]
            [D, A, E]
            """
        )
        assertEquals(5, m.findAllAlignedCells().size)
    }

    @Test
    fun nextDropMoveFindsLowestHoleAndFirstTileAbove() {
        val m = mechanics("[A]\n[H]\n[B]\n[H]")
        assertEquals(Move(target = Position(0, 3), tile = Position(0, 2)), m.getNextDropMove(0))
    }

    @Test
    fun nextDropMoveIsNullWhenNothingFalls() {
        assertNull(mechanics("[H]\n[H]\n[A]").getNextDropMove(0))
        assertNull(mechanics("[A]\n[B]").getNextDropMove(0))
    }

    @Test
    fun moveMovesTheTileAndLeavesAHole() {
        val m = mechanics("[A]\n[H]")
        m.move(Move(target = Position(0, 1), tile = Position(0, 0)))
        assertEquals("[H]\n[A]", m.toString())
    }

    @Test
    fun dropToGroundKeepsOrderAndPutsHolesOnTop() {
        val m = mechanics("[A]\n[H]\n[B]\n[H]\n[C]\n[H]")
        val moves = m.dropToGround(0)
        assertEquals("[H]\n[H]\n[H]\n[A]\n[B]\n[C]", m.toString())
        assertEquals(
            listOf(
                Move(Position(0, 5), Position(0, 4)),
                Move(Position(0, 4), Position(0, 2)),
                Move(Position(0, 3), Position(0, 0)),
            ),
            moves,
        )
    }

    @Test
    fun dropAllToGroundWorksOnEveryColumnFromLeftToRight() {
        val m = mechanics("[A, B]\n[H, H]")
        val moves = m.dropAllToGround()
        assertEquals("[H, H]\n[A, B]", m.toString())
        assertEquals(listOf(Move(Position(0, 1), Position(0, 0)), Move(Position(1, 1), Position(1, 0))), moves)
    }

    @Test
    fun listEmptyCells() {
        val m = mechanics("[H, A]\n[B, H]")
        assertEquals(listOf(Position(0, 0), Position(1, 1)), m.listEmptyCells())
    }

    @Test
    fun newTileMovesGoFromBottomToTopWithoutChangingTheGrid() {
        val m = mechanics("[H, H]\n[H, A]\n[H, A]")
        val calls = mutableListOf<Int>()
        val supply = ArrayDeque(listOf(Tile.B, Tile.C, Tile.D, Tile.E))
        val moves = m.getNewTileMoves { column -> calls += column; supply.removeFirst() }
        assertEquals(
            listOf(
                InsertMove(Position(0, 2), Tile.B),
                InsertMove(Position(0, 1), Tile.C),
                InsertMove(Position(0, 0), Tile.D),
                InsertMove(Position(1, 0), Tile.E),
            ),
            moves,
        )
        assertEquals(listOf(0, 0, 0, 1), calls)
        assertEquals("[H, H]\n[H, A]\n[H, A]", m.toString())
        m.insert(moves)
        assertEquals("[D, E]\n[C, A]\n[B, A]", m.toString())
    }

    @Test
    fun insertMovesNaturalOrderIsFromLowestRow() {
        val top = InsertMove(Position(0, 0), Tile.A)
        val bottom = InsertMove(Position(0, 3), Tile.B)
        val middle = InsertMove(Position(1, 1), Tile.C)
        assertEquals(listOf(bottom, middle, top), listOf(top, bottom, middle).sorted())
    }
}
