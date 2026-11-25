package j4k.candycrush.model

import kotlin.random.Random

/**
 * Content of a grid cell: one of the five candies (`A` to `E`), a wall, a hole,
 * or the special value returned when reading outside of the grid.
 */
enum class Tile {
    A, B, C, D, E, Wall, Hole, OutOfSpace;

    /** Returns true for the playable candies `A` to `E`. */
    fun isTile(): Boolean = this in CANDIES

    /** Returns true for anything that is not a candy (wall, hole, out of grid). */
    fun isNotTile(): Boolean = !isTile()

    /** Returns true only for an empty cell. */
    fun isHole(): Boolean = this == Hole

    /** One-letter name used in the text notation (`A`…`E`, `W`, `H`, `O`). */
    fun shortName(): String = name.substring(0, 1)

    companion object {
        private val CANDIES = listOf(A, B, C, D, E)

        /** Inverse of [shortName]; throws [IllegalArgumentException] for an unknown letter. */
        fun getTile(shortName: String): Tile =
            values().firstOrNull { it.shortName() == shortName.trim() }
                ?: throw IllegalArgumentException("Unknown tile: '$shortName'")

        /** Draws a candy uniformly among `A` to `E`. */
        fun randomTile(): Tile = randomTile(Random.Default)

        /** Draws a candy uniformly among `A` to `E` using the given random source. */
        fun randomTile(random: Random): Tile = CANDIES[random.nextInt(CANDIES.size)]
    }
}
