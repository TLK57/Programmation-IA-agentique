package j4k.candycrush.view

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.seconds
import com.soywiz.korge.tween.get
import com.soywiz.korge.tween.tween
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.anchor
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korma.geom.Point
import j4k.candycrush.game.ObjectiveProgress

/**
 * Top of the screen (EF-25, EF-26, EF-19, EF-21): remaining moves, objectives, score and
 * multiplier, plus the floating points shown where candies were removed (EF-20).
 */
class Hud(private val assets: Assets, objectives: List<ObjectiveProgress>) : Container() {

    /** A text kept aligned on a fixed horizontal anchor. */
    private class Label(val text: Text, val anchorX: Double, val ratio: Double) {
        fun show(value: String) = text.setTextAligned(value, anchorX, ratio)
    }

    private val movesLabel: Label
    private val objectiveLabels: List<Label>
    private val scoreLabel: Label
    private val effects = Container()

    init {
        image(assets.movesIcon).anchor(0.5, 0.5).position(ITEMS_X, ICON_Y)
        movesLabel = counterLabel(ITEMS_X)
        objectiveLabels = objectives.mapIndexed { index, objective ->
            val x = ITEMS_X + (index + 1) * ITEMS_SPACING
            image(assets.tileImage(objective.tile)) {
                anchor(0.5, 0.5)
                scale = ICON_SIZE / Assets.SHEET_CELL_SIZE
                position(x, ICON_Y)
            }
            counterLabel(x)
        }
        scoreLabel = Label(newText(this, assets.bigFont, SCORE_SIZE, SCORE_Y), SCORE_RIGHT_X, ALIGN_RIGHT)
        effects.addTo(this)
    }

    /** Shows the number of remaining moves. */
    fun showMoves(moves: Int) = movesLabel.show(moves.toString())

    /** Shows the remaining count of each objective. */
    fun showObjectives(objectives: List<ObjectiveProgress>) {
        objectives.zip(objectiveLabels).forEach { (objective, label) -> label.show(objective.remaining.toString()) }
    }

    /** Shows the total score. */
    fun showScore(score: Int) = scoreLabel.show(score.toString())

    /** Shows the points of a wave centered on [point]; they rise and fade out. */
    suspend fun popPoints(points: Int, point: Point) {
        val label = newText(effects, assets.bigFont, POPUP_SIZE, point.y - POPUP_SIZE / 2)
        label.setTextAligned("+$points", point.x, ALIGN_CENTER)
        label.tween(label::y[label.y - POPUP_RISE], label::alpha[0.0], time = POPUP_TIME)
        label.removeFromParent()
    }

    /** Shows "x2", "x3"… below the score, fading out. */
    suspend fun popMultiplier(multiplier: Int) {
        val label = newText(effects, assets.bigFont, MULTIPLIER_SIZE, MULTIPLIER_Y)
        label.setTextAligned("x$multiplier", SCORE_RIGHT_X, ALIGN_RIGHT)
        label.tween(label::alpha[0.0], time = MULTIPLIER_TIME)
        label.removeFromParent()
    }

    /** Removes the floating texts still on screen. */
    fun clearEffects() {
        effects.removeChildren()
    }

    private fun counterLabel(x: Double) = Label(newText(this, assets.smallFont, COUNTER_SIZE, COUNTER_Y), x, ALIGN_CENTER)

    private fun newText(parent: Container, font: BitmapFont, size: Double, y: Double): Text =
        Text("", size, font = font, alignment = TextAlignment.TOP_LEFT).addTo(parent).position(0.0, y)

    private companion object {
        const val ITEMS_X = 135.0
        const val ITEMS_SPACING = 100.0
        const val ICON_Y = 105.0
        const val ICON_SIZE = 64.0
        const val COUNTER_Y = 135.0
        const val COUNTER_SIZE = 64.0
        const val SCORE_RIGHT_X = 680.0
        const val SCORE_Y = 70.0
        const val SCORE_SIZE = 80.0
        const val MULTIPLIER_Y = 150.0
        const val MULTIPLIER_SIZE = 100.0
        const val POPUP_SIZE = 70.0
        const val POPUP_RISE = 60.0
        val POPUP_TIME: TimeSpan = 1.seconds
        val MULTIPLIER_TIME: TimeSpan = 1.5.seconds
    }
}
