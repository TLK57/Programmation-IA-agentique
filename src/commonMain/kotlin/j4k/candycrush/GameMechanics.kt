package j4k.candycrush

import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell

/** Implements swaps, matches, gravity, and refill operations on a game field. */
class GameMechanics(val field: GameField) {
    /** Exchanges the contents at two positions. */
    fun swapTiles(a: Position, b: Position) {
        val first = field[a]
        field[a] = field[b]
        field[b] = first
    }

    /** Checks whether adjacent candies can be swapped to create a match without changing the field. */
    fun isSwapAllowed(a: Position, b: Position): Boolean {
        if (!isOnField(a) || !isOnField(b) || !areNeighbours(a, b)) return false
        if (!field[a].isTile() || !field[b].isTile()) return false
        swapTiles(a, b)
        val valid = isInRowWithThree(a) || isInRowWithThree(b)
        swapTiles(a, b)
        return valid
    }

    /** Replaces a cell with a hole. */
    fun removeTile(position: Position) {
        field[position] = Tile.Hole
    }

    /** Returns the contiguous horizontal run containing the position. */
    fun getHorizontalSurroundings(pos: Position): List<TileCell> =
        getSurroundings(pos, horizontal = true)

    /** Returns the contiguous vertical run containing the position. */
    fun getVerticalSurroundings(pos: Position): List<TileCell> =
        getSurroundings(pos, horizontal = false)

    /** Reports whether a candy belongs to a horizontal or vertical run of at least three. */
    fun isInRowWithThree(pos: Position): Boolean =
        field[pos].isTile() &&
            (getHorizontalSurroundings(pos).size >= MIN_MATCH_SIZE ||
                getVerticalSurroundings(pos).size >= MIN_MATCH_SIZE)

    /** Returns qualifying vertical cells followed by qualifying horizontal cells. */
    fun getConnectedTiles(pos: Position): List<TileCell> {
        if (!field[pos].isTile()) return emptyList()
        val vertical = getVerticalSurroundings(pos).takeIf { it.size >= MIN_MATCH_SIZE }.orEmpty()
        val horizontal = getHorizontalSurroundings(pos).takeIf { it.size >= MIN_MATCH_SIZE }.orEmpty()
        return vertical + horizontal
    }

    /** Finds the next candy above the lowest hole in a column. */
    fun getNextDropMove(column: Int): Move? {
        if (column !in 0 until field.columnsSize) return null
        var target: Position? = null
        for (row in field.rowSize - 1 downTo 0) {
            val tile = field[column, row]
            if (tile.isHole()) {
                if (target == null) target = Position(column, row)
            } else if (target != null && tile.isTile()) {
                return Move(target, Position(column, row))
            }
        }
        return null
    }

    /** Moves a candy into the target cell and leaves a hole at its source. */
    fun move(move: Move) {
        field[move.target] = field[move.tile]
        field[move.tile] = Tile.Hole
    }

    /** Applies gravity in one column and returns the moves in execution order. */
    fun dropToGround(column: Int): List<Move> {
        val moves = mutableListOf<Move>()
        while (true) {
            val nextMove = getNextDropMove(column) ?: break
            move(nextMove)
            moves += nextMove
        }
        return moves
    }

    /** Applies gravity in every column, from left to right. */
    fun dropAllToGround(): List<Move> =
        (0 until field.columnsSize).flatMap { dropToGround(it) }

    /** Lists all empty cells from top to bottom, left to right. */
    fun listEmptyCells(): List<Position> = buildList {
        for (row in 0 until field.rowSize) {
            for (column in 0 until field.columnsSize) {
                if (field[column, row].isHole()) add(Position(column, row))
            }
        }
    }

    /** Creates refill moves from the lowest row upward without modifying the field. */
    fun getNewTileMoves(tileStore: (Int) -> Tile): List<InsertMove> =
        listEmptyCells()
            .sortedWith(compareByDescending<Position> { it.row }.thenBy { it.column })
            .map { position -> InsertMove(position, tileStore(position.column)) }

    /** Applies each refill move to its target position. */
    fun insert(moves: List<InsertMove>) {
        moves.forEach { field[it.target] = it.tile }
    }

    /** Returns the field's notation. */
    override fun toString(): String = field.toString()

    private fun getSurroundings(pos: Position, horizontal: Boolean): List<TileCell> {
        val tile = field[pos]
        if (!tile.isTile()) return listOf(TileCell(tile, pos))
        val result = mutableListOf<Position>()
        var start = if (horizontal) pos.column else pos.row
        val limit = if (horizontal) field.columnsSize else field.rowSize
        while (start > 0 && tileAt(start - 1, pos, horizontal) == tile) start--
        var index = start
        while (index < limit && tileAt(index, pos, horizontal) == tile) {
            result += if (horizontal) Position(index, pos.row) else Position(pos.column, index)
            index++
        }
        return result.map { TileCell(tile, it) }
    }

    private fun tileAt(index: Int, pos: Position, horizontal: Boolean): Tile =
        if (horizontal) field[index, pos.row] else field[pos.column, index]

    private fun isOnField(pos: Position): Boolean =
        pos.column in 0 until field.columnsSize && pos.row in 0 until field.rowSize

    private fun areNeighbours(a: Position, b: Position): Boolean =
        (a.column == b.column && kotlin.math.abs(a.row - b.row) == 1) ||
            (a.row == b.row && kotlin.math.abs(a.column - b.column) == 1)

    /** A source/target pair for gravity animation and board updates. */
    data class Move(val target: Position, val tile: Position)

    /** A candy insertion ordered from the lowest row to the highest. */
    data class InsertMove(val target: Position, val tile: Tile) : Comparable<InsertMove> {
        override fun compareTo(other: InsertMove): Int =
            if (target.row != other.target.row) other.target.row.compareTo(target.row)
            else target.column.compareTo(other.target.column)
    }

    companion object {
        private const val MIN_MATCH_SIZE = 3
    }
}
