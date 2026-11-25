package j4k.candycrush

import j4k.candycrush.GameMechanics.InsertMove
import j4k.candycrush.GameMechanics.Move
import j4k.candycrush.math.PositionGrid
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Level
import j4k.candycrush.model.Row
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell
import kotlin.test.Test
import kotlin.test.assertEquals

/** Calls the imposed interface (section 7.2) with the named arguments used by the independent tests. */
class ContractTest {
    @Test
    fun namedArgumentsCompileAndBehave() {
        val field = GameField(columnsSize = 2, rowSize = 2)
        field.set(column = 0, row = 1, value = Tile.A)
        field.set(position = PositionGrid.Position(column = 1, row = 1), value = Tile.B)
        field.getRow(row = 0)[1] = "C"
        assertEquals("[H, C]\n[A, B]", field.toString())
        assertEquals(TileCell(Tile.A, PositionGrid.Position(0, 1)), field.getTileCell(column = 0, row = 1))
        assertEquals(listOf(Tile.C, Tile.B), field.getColumn(column = 1))
        assertEquals(field, GameField.fromString(string = field.toString()))

        val row = Row(rowSize = 2)
        row.set(column = 0, tileShortName = "D")
        row.set(column = 1, tile = Tile.E)
        assertEquals(Row(Tile.D, Tile.E), row)
        assertEquals(Tile.A, Tile.getTile(shortName = "A"))

        val mechanics = GameMechanics(field = field)
        mechanics.swapTiles(PositionGrid.Position(0, 0), PositionGrid.Position(0, 1))
        mechanics.move(move = Move(target = PositionGrid.Position(0, 1), tile = PositionGrid.Position(0, 0)))
        assertEquals("[H, C]\n[A, B]", field.toString())
        assertEquals(null, mechanics.getNextDropMove(column = 1))
        assertEquals(
            listOf(InsertMove(target = PositionGrid.Position(0, 0), tile = Tile.E)),
            mechanics.getNewTileMoves(tileStore = { Tile.E }),
        )

        val level = Level("[A]", "[B]")
        assertEquals(Tile.B, level.getNextTile(column = 0))
        assertEquals(Level.TileObjective(tile = Tile.A, goal = 3).goal, 3)

        val grid = PositionGrid(columns = 5, rows = 9, tileSize = 10)
        assertEquals(PositionGrid.Position(column = 1, row = 2), grid.getField(posX = 15, posY = 25))
        assertEquals(grid.getPosition(column = 1, row = 2).x, 10.0)
    }
}
