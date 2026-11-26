package j4k.candycrush

import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell

/** Board operations and match detection for the game. */
class GameMechanics(val field: GameField) {
    /** Swaps two field cells. */
    fun swapTiles(a: Position, b: Position) { val t = field[a]; field[a] = field[b]; field[b] = t }
    /** Checks whether a neighboring candy swap creates a match. */
    fun isSwapAllowed(a: Position, b: Position): Boolean {
        if (field[a].isCandy().not() || field[b].isCandy().not() || kotlin.math.abs(a.column - b.column) + kotlin.math.abs(a.row - b.row) != 1) return false
        swapTiles(a, b); val allowed = isInRowWithThree(a) || isInRowWithThree(b); swapTiles(a, b); return allowed
    }
    /** Removes a candy by making its cell empty. */
    fun removeTile(position: Position) { field[position] = Tile.Hole }
    /** Gets the contiguous same-type horizontal run through a cell. */
    fun getHorizontalSurroundings(pos: Position): List<TileCell> = surroundings(pos, true)
    /** Gets the contiguous same-type vertical run through a cell. */
    fun getVerticalSurroundings(pos: Position): List<TileCell> = surroundings(pos, false)
    private fun surroundings(pos: Position, horizontal: Boolean): List<TileCell> {
        val tile = field[pos]
        if (!tile.isCandy()) return listOf(field.getTileCell(pos))
        val result = mutableListOf<TileCell>(); var start = if (horizontal) pos.column else pos.row
        val limit = if (horizontal) field.columnsSize else field.rowSize
        fun at(i: Int) = if (horizontal) field[i, pos.row] else field[pos.column, i]
        while (start > 0 && at(start - 1) == tile) start--
        var i = start
        while (i < limit && at(i) == tile) { result += field.getTileCell(if (horizontal) i else pos.column, if (horizontal) pos.row else i); i++ }
        return result
    }
    /** Tests whether a cell belongs to a horizontal or vertical match of at least three. */
    fun isInRowWithThree(pos: Position): Boolean = field[pos].isCandy() && (getHorizontalSurroundings(pos).size >= 3 || getVerticalSurroundings(pos).size >= 3)
    /** Returns vertical then horizontal matching runs through this cell, including duplicates at crossings. */
    fun getConnectedTiles(pos: Position): List<TileCell> {
        if (!field[pos].isCandy()) return emptyList()
        return getVerticalSurroundings(pos).takeIf { it.size >= 3 }.orEmpty() + getHorizontalSurroundings(pos).takeIf { it.size >= 3 }.orEmpty()
    }
    /** Finds the next candy above a hole in the selected column. */
    fun getNextDropMove(column: Int): Move? {
        if (column !in 0 until field.columnsSize) return null
        for (hole in field.rowSize - 1 downTo 0) if (field[column, hole] == Tile.Hole) {
            for (source in hole - 1 downTo 0) if (field[column, source].isCandy()) return Move(Position(column, hole), Position(column, source))
        }
        return null
    }
    /** Applies a single downward move. */
    fun move(move: Move) { field[move.target] = field[move.tile]; field[move.tile] = Tile.Hole }
    /** Drops one column and returns each move in execution order. */
    fun dropToGround(column: Int): List<Move> = buildList { while (true) { val m = getNextDropMove(column) ?: break; move(m); add(m) } }
    /** Drops every column from left to right. */
    fun dropAllToGround(): List<Move> = (0 until field.columnsSize).flatMap(::dropToGround)
    /** Lists empty cells from the bottom row to the top. */
    fun listEmptyCells(): List<Position> = (field.rowSize - 1 downTo 0).flatMap { y -> (0 until field.columnsSize).filter { field[it, y] == Tile.Hole }.map { Position(it, y) } }
    /** Creates deterministic refill instructions without changing the field. */
    fun getNewTileMoves(tileStore: (Int) -> Tile): List<InsertMove> = listEmptyCells().map { InsertMove(it, tileStore(it.column)) }
    /** Places the candies described by refill instructions. */
    fun insert(moves: List<InsertMove>) { moves.forEach { field[it.target] = it.tile } }
    /** Finds every candy currently in a match, with intersections counted once. */
    fun findAllMatches(): Set<Position> = buildSet {
        for (y in 0 until field.rowSize) for (x in 0 until field.columnsSize) {
            val p = Position(x, y)
            if (getHorizontalSurroundings(p).size >= 3) getHorizontalSurroundings(p).forEach { add(it.position) }
            if (getVerticalSurroundings(p).size >= 3) getVerticalSurroundings(p).forEach { add(it.position) }
        }
    }
    override fun toString(): String = field.toString()
    /** A candy relocation from tile to target. */
    data class Move(val target: Position, val tile: Position)
    /** A newly supplied candy and its destination. */
    data class InsertMove(val target: Position, val tile: Tile) : Comparable<InsertMove> {
        override fun compareTo(other: InsertMove): Int = other.target.row.compareTo(target.row)
    }
}
