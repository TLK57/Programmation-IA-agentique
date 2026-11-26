package j4k.candycrush

import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile

/** Builds randomized starting boards without pre-existing matches. */
object BoardFactory {
    /** Fills a field in row-major order, drawing again whenever a candy creates a match. */
    fun createMatchFreeField(columns: Int, rows: Int, tileStore: () -> Tile): GameField {
        require(columns > 0 && rows > 0) { "Board dimensions must be positive" }
        val field = GameField(columns, rows)
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val position = Position(column, row)
                var candidate = tileStore()
                while (wouldMatch(field, position, candidate)) candidate = tileStore()
                field[position] = candidate
            }
        }
        return field
    }

    private fun wouldMatch(field: GameField, position: Position, tile: Tile): Boolean {
        val horizontal = position.column >= MIN_MATCH_SIZE - 1 &&
            field[position.column - 1, position.row] == tile &&
            field[position.column - 2, position.row] == tile
        val vertical = position.row >= MIN_MATCH_SIZE - 1 &&
            field[position.column, position.row - 1] == tile &&
            field[position.column, position.row - 2] == tile
        return horizontal || vertical
    }

    private const val MIN_MATCH_SIZE = 3
}
