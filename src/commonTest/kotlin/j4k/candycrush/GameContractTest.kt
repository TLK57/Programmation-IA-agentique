package j4k.candycrush

import j4k.candycrush.math.PositionGrid
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GameContractTest {
    @Test fun fieldNotationAccessAndCopy() {
        val field = GameField.fromString("  [A,B]\n\n [H, C] ")
        assertEquals("[A, B]\n[H, C]", field.toString())
        assertEquals(Tile.OutOfSpace, field[-1, 0]); assertEquals(Tile.OutOfSpace, field[Position(2, 0)])
        assertEquals(Tile.OutOfSpace, field[0, 4]); assertEquals(Tile.OutOfSpace, field[0][7])
        assertEquals(listOf(Tile.A, Tile.H), field.getColumn(0).take(1) + field.getColumn(0).drop(1))
        val copy = field.clone(); copy[0, 0] = Tile.E
        assertEquals(Tile.A, field[0, 0]); assertNotEquals(field, copy)
        field[1][0] = "E"; assertEquals(Tile.E, field[1, 0])
    }

    @Test fun fieldConstructionAndCoordinates() {
        assertEquals(Tile.Hole, GameField(2, 2)[0, 0])
        val outsideRow = Row.outOfSpace()
        outsideRow[0] = Tile.A
        assertEquals(Tile.OutOfSpace, outsideRow[100])
        assertEquals(Tile.OutOfSpace, outsideRow[0])
        val grid = PositionGrid(10, 20, 5, 9, 30)
        assertEquals(40.0, grid.getPosition(1, 2).x); assertEquals(Position(1, 2), grid.getField(40, 80))
        assertTrue(grid.isOnGrid(Position(4, 8))); assertFalse(grid.isOnGrid(Position(5, 8)))
        assertEquals(Position(-1, -1), PositionGrid(columns = 2, rows = 2, tileSize = 4).getField(-1, -1))
    }

    @Test fun swapsAndMatchDetection() {
        val mechanics = GameMechanics(GameField.fromString("[A,A,A,B,B,B]\n[C,D,E,A,D,E]\n[B,C,D,E,A,B]"))
        val p = Position(1, 0)
        assertEquals(3, mechanics.getHorizontalSurroundings(p).size)
        assertEquals(3, mechanics.getHorizontalSurroundings(Position(4, 0)).size)
        assertTrue(mechanics.isInRowWithThree(p))
        assertEquals(3, mechanics.getConnectedTiles(p).size)
        val before = mechanics.toString()
        assertFalse(mechanics.isSwapAllowed(Position(2, 0), Position(3, 0)))
        assertEquals(before, mechanics.toString())
        assertFalse(mechanics.isSwapAllowed(Position(0, 1), Position(1, 1)))
        mechanics.swapTiles(Position(0, 0), Position(1, 0)); assertEquals(Tile.A, mechanics.field[0, 0])
        assertTrue(mechanics.getConnectedTiles(Position(0, 1)).isEmpty())
        val long = GameMechanics(GameField.fromString("[A,A,A,A,A,A]"))
        assertEquals(6, long.getHorizontalSurroundings(Position(3, 0)).size)
        val valid = GameMechanics(GameField.fromString("[A,A,B]\n[C,D,A]\n[E,D,A]"))
        assertTrue(valid.isSwapAllowed(Position(2, 0), Position(2, 1)))
    }

    @Test fun crossesAndHolesAreHandled() {
        val mechanics = GameMechanics(GameField.fromString("[H,A,H]\n[A,A,A]\n[H,A,H]"))
        assertEquals(6, mechanics.getConnectedTiles(Position(1, 1)).size)
        assertEquals(listOf(TileCell(Tile.Hole, Position(0, 0))), mechanics.getHorizontalSurroundings(Position(0, 0)))
        assertTrue(mechanics.getConnectedTiles(Position(0, 0)).isEmpty())
    }

    @Test fun fallingAndRefillOrder() {
        val mechanics = GameMechanics(GameField.fromString("[A,B]\n[H,C]\n[H,H]"))
        assertEquals(GameMechanics.Move(Position(0, 2), Position(0, 0)), mechanics.getNextDropMove(0))
        assertEquals(1, mechanics.dropToGround(0).size)
        assertEquals(listOf(Tile.Hole, Tile.Hole, Tile.A), mechanics.field.getColumn(0))
        val supplied = mutableListOf<Int>()
        val moves = mechanics.getNewTileMoves { column -> supplied += column; Tile.B }
        assertEquals(listOf(Position(1, 2), Position(0, 1), Position(0, 0)), moves.map { it.target })
        assertEquals(listOf(1, 0, 0), supplied)
        mechanics.insert(moves); assertTrue(mechanics.listEmptyCells().isEmpty())
        val allColumns = GameMechanics(GameField.fromString("[A,H]\n[H,B]\n[H,H]"))
        assertEquals(2, allColumns.dropAllToGround().size)
        assertEquals(listOf(Tile.Hole, Tile.Hole, Tile.A), allColumns.field.getColumn(0))
        assertEquals(listOf(Tile.Hole, Tile.Hole, Tile.B), allColumns.field.getColumn(1))
        val refillOrder = listOf(InsertMove(Position(0, 0), Tile.B), InsertMove(Position(1, 2), Tile.A)).sorted()
        assertEquals(listOf(InsertMove(Position(1, 2), Tile.A), InsertMove(Position(0, 0), Tile.B)), refillOrder)
    }

    @Test fun levelReserveAndGameOutcome() {
        val level = Level("[A,B]\n[C,D]", "[B,D]\n[A,C]", 1, listOf(TileObjective(Tile.A, 1), TileObjective(Tile.C, 1)))
        assertEquals(Tile.A, level.getNextTile(0)); assertEquals(Tile.B, level.getNextTile(0))
        assertEquals("[H, D]\n[H, C]", level.reserve.toString())
        val state = GameState(level)
        state.scoreWave(listOf(Tile.A, Tile.C), 2)
        assertEquals(80, state.score); assertEquals(2, state.multiplier); assertTrue(state.isWon())
        state.consumeMove(); assertTrue(state.isWon()); assertFalse(state.isLost())
        state.reset(); assertEquals(0, state.score); assertEquals(1, state.movesLeft)
        state.consumeMove(); assertTrue(state.isLost())
    }
}
