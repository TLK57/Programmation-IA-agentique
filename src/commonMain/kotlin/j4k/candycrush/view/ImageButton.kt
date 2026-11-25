package j4k.candycrush.view

import com.soywiz.korge.input.mouse
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.image
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.slice

/**
 * A button drawn with an image, showing optional "hover" and "pressed" images (EF-35).
 * [normal] can be changed at any time, for example to show an on/off state.
 */
class ImageButton(
    normal: Bitmap,
    private val hover: Bitmap? = null,
    private val pressed: Bitmap? = null,
    action: () -> Unit,
) : Container() {

    /** Image shown when the pointer is neither over nor pressing the button. */
    var normal: Bitmap = normal
        set(value) {
            field = value
            refresh()
        }

    private val image = image(normal)
    private var hovered = false
    private var pressedDown = false

    init {
        mouse {
            onOver { hovered = true; refresh() }
            onOut { hovered = false; pressedDown = false; refresh() }
            onDown { pressedDown = true; refresh() }
            onUpAnywhere { pressedDown = false; refresh() }
            onClick { action() }
        }
    }

    private fun refresh() {
        val current = when {
            pressedDown && pressed != null -> pressed
            (hovered || pressedDown) && hover != null -> hover
            else -> normal
        }
        image.bitmap = current.slice()
    }
}
