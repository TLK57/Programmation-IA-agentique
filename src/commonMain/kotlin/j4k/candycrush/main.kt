package j4k.candycrush

import com.soywiz.korge.Korge
import com.soywiz.korge.scene.sceneContainer
import com.soywiz.korim.color.Colors
import j4k.candycrush.view.GameScene

/** Window size on PC at launch (portrait). */
private const val WINDOW_WIDTH = 540
private const val WINDOW_HEIGHT = 960

/** Entry point: opens the "Candy Crush" window and shows the game scene. */
suspend fun main() = Korge(
    title = "Candy Crush",
    width = WINDOW_WIDTH,
    height = WINDOW_HEIGHT,
    virtualWidth = GameScene.VIRTUAL_WIDTH,
    virtualHeight = GameScene.VIRTUAL_HEIGHT,
    bgcolor = Colors.BLACK,
) {
    sceneContainer().changeTo({ GameScene() })
}
