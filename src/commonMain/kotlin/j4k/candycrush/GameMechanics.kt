package j4k.candycrush

import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell

/**
 * Actions and checks on a grid: swapping, alignment detection, removal, drop and refill.
 * Every method works directly on [field] (no copy).
 */
class GameMechanics(val field: GameField) {

    /** Swaps the content of the cells [a] and [b]. */
    fun swapTiles(a: Position, b: Position) {
        val tileA = field[a]
        field[a] = field[b]
        field[b] = tileA
    }

    /**
     * Returns true if both cells hold candies and, once swapped, at least one of them is part
     * of an alignment. The grid is left unchanged.
     */
    fun isSwapAllowed(a: Position, b: Position): Boolean {
        if (field[a].isNotTile() || field[b].isNotTile()) return false
        swapTiles(a, b)
        val allowed = isInRowWithThree(a) || isInRowWithThree(b)
        swapTiles(a, b)
        return allowed
    }

    /** Puts a hole at [position]. */
    fun removeTile(position: Position) {
        field[position] = Tile.Hole
    }

    /**
     * Returns the continuous run of candies identical to the one at [pos] on its row, sorted from
     * left to right, or only the cell of [pos] when it does not hold a candy.
     */
    fun getHorizontalSurroundings(pos: Position): List<TileCell> =
        getSurroundings(pos) { position, step -> Position(position.column + step, position.row) }

    /**
     * Returns the continuous run of candies identical to the one at [pos] on its column, sorted from
     * top to bottom, or only the cell of [pos] when it does not hold a candy.
     */
    fun getVerticalSurroundings(pos: Position): List<TileCell> =
        getSurroundings(pos) { position, step -> Position(position.column, position.row + step) }

    private fun getSurroundings(pos: Position, next: (Position, Int) -> Position): List<TileCell> {
        val tile = field[pos]
        if (tile.isNotTile()) return listOf(field.getTileCell(pos))
        var first = pos
        while (field[next(first, -1)] == tile) first = next(first, -1)
        var last = pos
        while (field[next(last, 1)] == tile) last = next(last, 1)
        return generateSequence(first) { if (it == last) null else next(it, 1) }
            .map { field.getTileCell(it) }
            .toList()
    }

    /** Returns true if the cell at [pos] belongs to a horizontal or vertical alignment. */
    fun isInRowWithThree(pos: Position): Boolean = getConnectedTiles(pos).isNotEmpty()

    /**
     * Returns the vertical alignment through [pos] (if it has at least [MIN_ALIGNMENT] cells)
     * followed by the horizontal one (same condition). Empty when there is no alignment.
     */
    fun getConnectedTiles(pos: Position): List<TileCell> =
        getAlignments(pos).flatten()

    /** Returns the alignments (vertical first, then horizontal) passing through [pos]. */
    fun getAlignments(pos: Position): List<List<TileCell>> {
        if (field[pos].isNotTile()) return emptyList()
        return listOf(getVerticalSurroundings(pos), getHorizontalSurroundings(pos))
            .filter { it.size >= MIN_ALIGNMENT }
    }

    /** Returns every cell of the grid that belongs to an alignment, each cell once. */
    fun findAllAlignedCells(): List<TileCell> =
        field.positions().filter { isInRowWithThree(it) }.map { field.getTileCell(it) }

    /**
     * Scanning [column] from bottom to top, finds the first hole, then the first candy above it,
     * and returns the move of that candy into that hole; null when nothing has to fall.
     */
    fun getNextDropMove(column: Int): Move? {
        val holeRow = (field.rowSize - 1 downTo 0).firstOrNull { field[column, it].isHole() } ?: return null
        val tileRow = (holeRow - 1 downTo 0).firstOrNull { field[column, it].isTile() } ?: return null
        return Move(target = Position(column, holeRow), tile = Position(column, tileRow))
    }

    /** Moves the candy at [Move.tile] into [Move.target] and leaves a hole behind. */
    fun move(move: Move) {
        field[move.target] = field[move.tile]
        field[move.tile] = Tile.Hole
    }

    /** Lets every candy of [column] fall onto the candy or the ground below; returns the moves, in order. */
    fun dropToGround(column: Int): List<Move> {
        val moves = mutableListOf<Move>()
        while (true) {
            val next = getNextDropMove(column) ?: return moves
            move(next)
            moves += next
        }
    }

    /** Applies [dropToGround] to every column, from left to right. */
    fun dropAllToGround(): List<Move> = (0 until field.columnsSize).flatMap { dropToGround(it) }

    /** Returns the positions of every hole of the grid. */
    fun listEmptyCells(): List<Position> = field.positions().filter { field[it].isHole() }

    /**
     * Returns one [InsertMove] per hole, from the lowest row to the highest; the candy of each
     * insertion is obtained by calling [tileStore] with the hole's column, in that order.
     * The grid is not modified.
     */
    fun getNewTileMoves(tileStore: (Int) -> Tile): List<InsertMove> =
        listEmptyCells()
            .sortedWith(compareByDescending<Position> { it.row }.thenBy { it.column })
            .map { InsertMove(it, tileStore(it.column)) }

    /** Places each candy of [moves] at its target position. */
    fun insert(moves: List<InsertMove>) {
        moves.forEach { field[it.target] = it.tile }
    }

    override fun toString(): String = field.toString()

    /** Move of the candy located at [tile] into the hole located at [target]. */
    data class Move(val target: Position, val tile: Position)

    /** Insertion of a new candy [tile] at [target]; the natural order goes from the lowest row to the highest. */
    data class InsertMove(val target: Position, val tile: Tile) : Comparable<InsertMove> {
        override fun compareTo(other: InsertMove): Int =
            compareValuesBy(other, this, { it.target.row }, { -it.target.column })
    }

    companion object {
        /** Minimum number of identical consecutive candies forming an alignment. */
        const val MIN_ALIGNMENT = 3
    }
}
