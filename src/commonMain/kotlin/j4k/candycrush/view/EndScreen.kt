package j4k.candycrush.view

import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.SolidRect
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.container
import com.soywiz.korge.view.ninePatch
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.text.TextAlignment

/**
 * End-of-level window (EF-30), centered over the grid, with a title ("Bravo !" or "Perdu !")
 * and the "Rejouer" and "Nouvelle grille" buttons. A transparent layer covers the screen to
 * catch the gestures while it is shown.
 */
class EndScreen(
    assets: Assets,
    screenWidth: Double,
    screenHeight: Double,
    onReplay: () -> Unit,
    onNewGrid: () -> Unit,
) : Container() {

    private val box = Container()
    private val title: Text

    init {
        SolidRect(screenWidth, screenHeight, SHADE).addTo(this)
        box.addTo(this).position((screenWidth - BOX_WIDTH) / 2, (screenHeight - BOX_HEIGHT) / 2)
        box.ninePatch(assets.messageBox, BOX_WIDTH, BOX_HEIGHT)
        title = box.text("", TITLE_SIZE, font = assets.bigFont, alignment = TextAlignment.TOP_LEFT)
            .position(0.0, TITLE_Y)
        box.button(assets, "Rejouer", REPLAY_Y, onReplay)
        box.button(assets, "Nouvelle grille", NEW_GRID_Y, onNewGrid)
        visible = false
    }

    /** Shows the window with the victory or defeat title. */
    fun show(victory: Boolean) {
        title.setTextAligned(if (victory) "Bravo !" else "Perdu !", BOX_WIDTH / 2, ALIGN_CENTER)
        visible = true
    }

    /** Hides the window. */
    fun hide() {
        visible = false
    }

    private fun Container.button(assets: Assets, label: String, y: Double, action: () -> Unit) {
        val button = container {
            ninePatch(assets.button, BUTTON_WIDTH, BUTTON_HEIGHT)
            text("", BUTTON_TEXT_SIZE, font = assets.smallFont, alignment = TextAlignment.MIDDLE_LEFT)
                .position(0.0, BUTTON_HEIGHT / 2)
                .setTextAligned(label, BUTTON_WIDTH / 2, ALIGN_CENTER)
        }
        button.position((BOX_WIDTH - BUTTON_WIDTH) / 2, y)
        button.onClick { action() }
    }

    private companion object {
        val SHADE = RGBA(0, 0, 0, 0x40)
        const val BOX_WIDTH = 440.0
        const val BOX_HEIGHT = 380.0
        const val TITLE_Y = 30.0
        const val TITLE_SIZE = 80.0
        const val BUTTON_WIDTH = 300.0
        const val BUTTON_HEIGHT = 96.0
        const val BUTTON_TEXT_SIZE = 38.0
        const val REPLAY_Y = 140.0
        const val NEW_GRID_Y = 250.0
    }
}
