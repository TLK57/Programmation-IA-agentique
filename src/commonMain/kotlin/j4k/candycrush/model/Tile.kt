package j4k.candycrush.model

/** Every possible cell value in a Candy Crush field. */
enum class Tile { A, B, C, D, E, Wall, Hole, OutOfSpace;
    companion object {
        /** Parses a short name used by the text field notation. */
        fun fromShortName(value: String): Tile = when (value.trim().uppercase()) {
            "A" -> A; "B" -> B; "C" -> C; "D" -> D; "E" -> E
            "W" -> Wall; "H" -> Hole; "O" -> OutOfSpace
            else -> throw IllegalArgumentException("Unknown tile: $value")
        }
    }
    /** Returns the notation token for this tile. */
    fun shortName(): String = when (this) { Wall -> "W"; Hole -> "H"; OutOfSpace -> "O"; else -> name }
    override fun toString(): String = shortName()
    /** Whether this value is a candy that can participate in matches. */
    fun isCandy(): Boolean = ordinal in A.ordinal..E.ordinal
}
