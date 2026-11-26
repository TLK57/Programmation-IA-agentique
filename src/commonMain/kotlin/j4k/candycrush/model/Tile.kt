package j4k.candycrush.model

import kotlin.random.Random

/** Identifies a candy, an empty cell, a reserved wall, or a read outside the field. */
enum class Tile {
    A, B, C, D, E, Wall, Hole, OutOfSpace;

    /** Returns whether this value represents one of the five playable candies. */
    fun isTile(): Boolean = this in A..E

    /** Returns whether this value is not a playable candy. */
    fun isNotTile(): Boolean = !isTile()

    /** Returns whether this cell is empty. */
    fun isHole(): Boolean = this == Hole

    /** Returns the one-letter notation used by levels and diagnostics. */
    fun shortName(): String = when (this) {
        Wall -> "W"
        Hole -> "H"
        OutOfSpace -> "O"
        else -> name
    }

    companion object {
        /** Parses a notation letter, rejecting values outside A-E, W, H, and O. */
        fun getTile(shortName: String): Tile = when (shortName.trim().uppercase()) {
            "A" -> A
            "B" -> B
            "C" -> C
            "D" -> D
            "E" -> E
            "W" -> Wall
            "H" -> Hole
            "O" -> OutOfSpace
            else -> throw IllegalArgumentException("Unknown tile: $shortName")
        }

        /** Selects a candy uniformly from A through E. */
        fun randomTile(): Tile = playableTiles[Random.nextInt(playableTiles.size)]

        private val playableTiles = listOf(A, B, C, D, E)
    }
}
