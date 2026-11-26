package j4k.candycrush

import j4k.candycrush.math.PositionGrid
import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile
import kotlin.test.Test
import kotlin.test.assertEquals

class LevelAndProgressTest {
    @Test
    fun reserveIsConsumedBottomUpThenUsesInjectedRandomSource() {
        val level = Level(
            "[A, B]\n[C, D]",
            "[B, D]\n[A, C]",
            randomTile = { Tile.E },
        )
        assertEquals(Tile.A, level.getNextTile(0))
        assertEquals(Tile.C, level.getNextTile(1))
        assertEquals("[H, H]\n[B, D]", level.reserve.toString())
        assertEquals(Tile.B, level.getNextTile(0))
        assertEquals(Tile.E, level.getNextTile(0))
    }

    @Test
    fun scoreObjectivesVictoryPriorityAndDefeatAreTestableWithoutRendering() {
        val level = Level("[A]", moves = 2, objectives = listOf(Level.TileObjective(Tile.A, 3)))
        val progress = GameProgress(level)
        assertEquals(80, progress.recordWave(mapOf(Tile.A to 2), multiplier = 2))
        assertEquals(1, progress.remainingFor(Tile.A))
        progress.beginTurn()
        assertEquals(null, progress.finishTurn())

        progress.recordWave(mapOf(Tile.A to 1), multiplier = 1)
        progress.beginTurn()
        assertEquals(GameProgress.Outcome.Won, progress.finishTurn())
        assertEquals(100, progress.score)
        progress.reset()
        progress.beginTurn()
        assertEquals(null, progress.finishTurn())
        progress.beginTurn()
        assertEquals(GameProgress.Outcome.Lost, progress.finishTurn())
        assertEquals(0, progress.score)
    }

    @Test
    fun positionGridMapsCellsAndRejectsOutsideCoordinates() {
        val grid = PositionGrid(x = 10, y = 20, columns = 5, rows = 9, tileSize = 32)
        assertEquals(42.0, grid.getPosition(1, 2).x)
        assertEquals(PositionGrid.Position(1, 2), grid.getField(63, 95))
        assertEquals(PositionGrid.Position(-1, 0), grid.getField(9, 20))
        assertEquals(true, grid.isOnGrid(PositionGrid.Position(4, 8)))
        assertEquals(false, grid.isOnGrid(PositionGrid.Position(5, 8)))
    }
}
