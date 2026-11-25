package j4k.candycrush.game

import j4k.candycrush.GameMechanics
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile

/** Builds random grids (EF-32). */
object FieldGenerator {

    /**
     * Fills a new grid cell by cell (row by row, from the top-left corner) with candies drawn from
     * [tileSource], drawing again while the candy forms an alignment with its already placed neighbours.
     * The candy source must be able to return at least three different candies.
     */
    fun randomFieldWithoutAlignment(columns: Int, rows: Int, tileSource: () -> Tile): GameField {
        val field = GameField(columns, rows)
        field.positions().forEach { position ->
            do {
                field[position] = tileSource()
            } while (formsAlignment(field, position.column, position.row))
        }
        return field
    }

    private fun formsAlignment(field: GameField, column: Int, row: Int): Boolean {
        val tile = field[column, row]
        val before = 1 until GameMechanics.MIN_ALIGNMENT
        return before.all { field[column - it, row] == tile } || before.all { field[column, row - it] == tile }
    }
}
