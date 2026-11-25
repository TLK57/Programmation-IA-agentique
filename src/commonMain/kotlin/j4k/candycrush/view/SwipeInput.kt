package j4k.candycrush.view

import com.soywiz.korge.input.mouse
import com.soywiz.korge.view.View
import com.soywiz.korma.geom.Point
import j4k.candycrush.math.PositionGrid.Position
import kotlin.math.hypot

/**
 * Turns a press-and-drag gesture (mouse or touch) on the board into a swap request (EF-01).
 * The gesture ends as soon as the pointer is one cell away from its starting point, or when it is
 * released; the arrival cell is the cell under the pointer at that moment. Gestures starting or
 * ending outside of the grid are dropped.
 */
class SwipeInput(
    private val board: BoardView,
    private val onSwipe: (from: Position, to: Position) -> Unit,
) {
    private var start: Point? = null

    /** Listens to the pointer events of [root] (usually the stage, so that drags outside of the board are seen). */
    fun attach(root: View) {
        root.mouse {
            down { begin(board.globalToLocal(it.currentPosStage)) }
            moveAnywhere { update(board.globalToLocal(it.currentPosStage), released = false) }
            upAnywhere { update(board.globalToLocal(it.currentPosStage), released = true) }
        }
    }

    private fun begin(local: Point) {
        start = if (cellAt(local) != null) local.copy() else null
    }

    private fun update(local: Point, released: Boolean) {
        val origin = start ?: return
        val farEnough = hypot(local.x - origin.x, local.y - origin.y) >= board.grid.tileSize
        if (!farEnough && !released) return
        start = null
        val from = cellAt(origin) ?: return
        val to = cellAt(local) ?: return
        if (to != from) onSwipe(from, to)
    }

    private fun cellAt(local: Point): Position? {
        val x = local.x.toInt()
        val y = local.y.toInt()
        if (local.x < 0 || local.y < 0 || !board.grid.isPixelOnGrid(x, y)) return null
        return board.grid.getField(x, y)
    }
}
