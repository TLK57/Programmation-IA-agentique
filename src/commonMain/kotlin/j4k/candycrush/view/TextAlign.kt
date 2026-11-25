package j4k.candycrush.view

import com.soywiz.korge.view.Text
import com.soywiz.korma.geom.Rectangle

/** Horizontal anchor ratio for centered text. */
const val ALIGN_CENTER = 0.5

/** Horizontal anchor ratio for right-aligned text. */
const val ALIGN_RIGHT = 1.0

/**
 * Sets [value] and moves the text so that the point at [ratio] of its width (0 = left edge,
 * 1 = right edge) lies on [anchorX]. Bitmap-font texts ignore the horizontal alignment in this
 * KorGE version, so it is computed from the text bounds.
 */
fun Text.setTextAligned(value: String, anchorX: Double, ratio: Double) {
    text = value
    val bounds = getLocalBounds(Rectangle())
    x = anchorX - (bounds.x + bounds.width * ratio)
}
