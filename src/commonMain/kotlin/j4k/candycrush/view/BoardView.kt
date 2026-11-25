package j4k.candycrush.view

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.ClipContainer
import com.soywiz.korge.view.Image
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.anchor
import com.soywiz.korma.geom.Point
import com.soywiz.korma.interpolation.Easing
import j4k.candycrush.GameMechanics
import j4k.candycrush.math.PositionGrid
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileCell
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Displays the candies of the grid and plays their animations. The view only mirrors the model:
 * it is rebuilt from a [GameField] and then follows the moves computed by the logic.
 */
class BoardView(
    private val assets: Assets,
    val grid: PositionGrid,
) : ClipContainer(grid.columns * grid.tileSize.toDouble(), grid.rows * grid.tileSize.toDouble()) {

    private val sprites = mutableMapOf<Position, Image>()

    /** When true, candies are drawn with the letter sheet (debug key `D`). */
    var debugImages = false
        private set

    /** Removes every sprite and draws [field] as it is. */
    fun rebuild(field: GameField) {
        removeChildren()
        sprites.clear()
        field.positions()
            .filter { field[it].isTile() }
            .forEach { sprites[it] = createSprite(field[it], centerOf(it)) }
    }

    /** Switches between candy images and letter images, then redraws [field]. */
    fun toggleDebugImages(field: GameField) {
        debugImages = !debugImages
        rebuild(field)
    }

    /** Swaps the two sprites (valid swap). */
    suspend fun animateSwap(a: Position, b: Position) {
        moveSprites(a, b, targetOfA = b, targetOfB = a)
        val spriteA = sprites.remove(a)
        val spriteB = sprites.remove(b)
        spriteA?.let { sprites[b] = it }
        spriteB?.let { sprites[a] = it }
    }

    /** The two sprites cross each other then come back (invalid swap). */
    suspend fun animateRejectedSwap(a: Position, b: Position) {
        moveSprites(a, b, targetOfA = b, targetOfB = a)
        moveSprites(a, b, targetOfA = a, targetOfB = b)
    }

    private suspend fun moveSprites(a: Position, b: Position, targetOfA: Position, targetOfB: Position) {
        val spriteA = sprites[a] ?: return
        val spriteB = sprites[b] ?: return
        val targetA = centerOf(targetOfA)
        val targetB = centerOf(targetOfB)
        tween(
            spriteA::x[targetA.x], spriteA::y[targetA.y],
            spriteB::x[targetB.x], spriteB::y[targetB.y],
            time = SWAP_TIME, easing = Easing.EASE_IN_OUT,
        )
    }

    /** Shrinks and fades the removed candies, then deletes their sprites. */
    suspend fun animateRemoval(removed: List<TileCell>) {
        val removedSprites = removed.mapNotNull { sprites.remove(it.position) }
        if (removedSprites.isEmpty()) return
        val properties = removedSprites.flatMap { listOf(it::alpha[0.0], it::scale[0.0]) }
        tween(*properties.toTypedArray(), time = REMOVE_TIME)
        removedSprites.forEach { it.removeFromParent() }
    }

    /** Lets the candies fall ([drops]) while the new candies ([inserts]) come in from the top of their column. */
    suspend fun animateFall(drops: List<GameMechanics.Move>, inserts: List<GameMechanics.InsertMove>) = coroutineScope {
        drops.forEach { move ->
            val sprite = sprites.remove(move.tile) ?: return@forEach
            sprites[move.target] = sprite
            launch { fall(sprite, move.target, move.target.row - move.tile.row) }
        }
        inserts.groupBy { it.target.column }.values.forEach { columnInserts ->
            val count = columnInserts.size
            columnInserts.forEach { insert ->
                val start = centerOf(Position(insert.target.column, insert.target.row - count))
                val sprite = createSprite(insert.tile, start).also { it.alpha = 0.0 }
                sprites[insert.target] = sprite
                launch { fall(sprite, insert.target, count) }
            }
        }
    }

    private suspend fun fall(sprite: Image, target: Position, rows: Int) {
        val center = centerOf(target)
        tween(
            sprite::y[center.y], sprite::alpha[1.0],
            time = DROP_TIME_PER_ROW * rows.coerceAtLeast(1), easing = Easing.EASE_IN,
        )
    }

    /** Center of [position] in board coordinates. */
    fun centerOf(position: Position): Point {
        val corner = grid.getPosition(position.column, position.row)
        val half = grid.tileSize / 2.0
        return Point(corner.x + half, corner.y + half)
    }

    private fun createSprite(tile: Tile, center: Point): Image =
        Image(assets.tileImage(tile, debugImages)).apply {
            anchor(0.5, 0.5)
            scale = grid.tileSize * CANDY_RATIO / Assets.SHEET_CELL_SIZE
            x = center.x
            y = center.y
        }.addTo(this)

    private companion object {
        /** Share of the cell occupied by a candy (EF-34). */
        const val CANDY_RATIO = 0.85
        val SWAP_TIME: TimeSpan = 0.2.seconds
        val REMOVE_TIME: TimeSpan = 0.2.seconds
        val DROP_TIME_PER_ROW: TimeSpan = 0.5.seconds
    }
}
