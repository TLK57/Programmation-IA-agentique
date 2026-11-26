package j4k.candycrush.model

import j4k.candycrush.math.PositionGrid

/** A tile together with its grid coordinate. */
data class TileCell(val tile: Tile, val position: PositionGrid.Position)
