package j4k.candycrush.view

import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.SContainer
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.geom.Point
import j4k.candycrush.game.GameSession
import j4k.candycrush.game.GameStatus
import j4k.candycrush.game.MoveResult
import j4k.candycrush.game.Wave
import j4k.candycrush.math.PositionGrid
import j4k.candycrush.math.PositionGrid.Position
import kotlinx.coroutines.Job
import kotlin.math.min

/**
 * The single scene of the game. It forwards the player's gestures to the [GameSession] (logic)
 * and replays the result with animations, sounds and HUD updates. Gestures on the grid are ignored
 * while an animation runs or while the end screen is shown (EF-06, EF-16, EF-30).
 */
class GameScene : Scene() {

    private val session = GameSession()
    private lateinit var assets: Assets
    private lateinit var audio: AudioPlayer
    private lateinit var board: BoardView
    private lateinit var hud: Hud
    private lateinit var settings: SettingsMenu
    private lateinit var endScreen: EndScreen
    private var animation: Job? = null

    override suspend fun SContainer.sceneInit() {
        assets = Assets.load()
        audio = AudioPlayer(this@GameScene, assets.sounds)
        image(assets.background)
        val grid = createGrid()
        board = BoardView(assets, grid).addTo(this).position(gridX(grid), GRID_TOP)
        hud = Hud(assets, session.objectives()).addTo(this)
        settings = SettingsMenu(assets, audio, onShuffle = ::shuffle, onRestart = ::restart)
            .addTo(this).position(SETTINGS_X, SETTINGS_Y)
        endScreen = EndScreen(
            assets, VIRTUAL_WIDTH.toDouble(), VIRTUAL_HEIGHT.toDouble(),
            onReplay = ::restart, onNewGrid = ::shuffle,
        ).addTo(this)
        SwipeInput(board, ::onSwipe).attach(this)
        registerDebugKeys()
        refreshAll()
    }

    private fun createGrid(): PositionGrid {
        val field = session.field
        val byWidth = (VIRTUAL_WIDTH - 2 * SIDE_MARGIN) / field.columnsSize
        val byHeight = (VIRTUAL_HEIGHT - GRID_TOP.toInt() - BOTTOM_MARGIN) / field.rowSize
        return PositionGrid(columns = field.columnsSize, rows = field.rowSize, tileSize = min(byWidth, byHeight))
    }

    private fun gridX(grid: PositionGrid): Double = (VIRTUAL_WIDTH - grid.columns * grid.tileSize) / 2.0

    private val isBusy: Boolean get() = animation?.isActive == true

    private fun onSwipe(from: Position, to: Position) {
        if (isBusy || session.status != GameStatus.PLAYING) return
        when (val result = session.play(from, to)) {
            MoveResult.Ignored -> Unit
            MoveResult.Rejected -> animate {
                audio.playWrongMove()
                board.animateRejectedSwap(from, to)
            }
            is MoveResult.Accepted -> animate { playAccepted(from, to, result) }
        }
    }

    private fun animate(block: suspend () -> Unit) {
        animation = launchImmediately { block() }
    }

    private suspend fun playAccepted(from: Position, to: Position, result: MoveResult.Accepted) {
        hud.showMoves(result.movesLeft)
        board.animateSwap(from, to)
        result.waves.forEachIndexed { index, wave -> playWave(wave, isChainReaction = index > 0) }
        when (result.status) {
            GameStatus.WON -> endScreen.show(victory = true)
            GameStatus.LOST -> endScreen.show(victory = false)
            GameStatus.PLAYING -> Unit
        }
    }

    private suspend fun playWave(wave: Wave, isChainReaction: Boolean) {
        if (isChainReaction) audio.playCombo(wave.multiplier) else audio.playClear()
        showWaveScore(wave)
        board.animateRemoval(wave.removed)
        board.animateFall(wave.drops, wave.inserts)
    }

    private fun showWaveScore(wave: Wave) {
        hud.showScore(wave.scoreAfter)
        hud.showObjectives(wave.objectivesAfter)
        val middle = wave.removed.map { it.position }
            .sortedWith(compareBy({ it.row }, { it.column }))[wave.removed.size / 2]
        val center = board.centerOf(middle)
        val popupPoint = Point(board.x + center.x, board.y + center.y)
        launchImmediately { hud.popPoints(wave.points, popupPoint) }
        if (wave.multiplier > 1) launchImmediately { hud.popMultiplier(wave.multiplier) }
    }

    private fun restart() = reset { session.restart() }

    private fun shuffle() = reset { session.shuffle() }

    private fun reset(action: () -> Unit) {
        animation?.cancel()
        animation = null
        action()
        refreshAll()
    }

    private fun refreshAll() {
        board.rebuild(session.field)
        hud.clearEffects()
        hud.showMoves(session.movesLeft)
        hud.showObjectives(session.objectives())
        hud.showScore(session.score)
        endScreen.hide()
    }

    private fun SContainer.registerDebugKeys() {
        keys {
            down(Key.P) { println(session.field) }
            down(Key.D) { if (!isBusy) board.toggleDebugImages(session.field) }
            down(Key.S) { shuffle() }
            down(Key.R) { restart() }
        }
    }

    companion object {
        /** Virtual resolution of the game (portrait). */
        const val VIRTUAL_WIDTH = 810
        const val VIRTUAL_HEIGHT = 1440

        private const val GRID_TOP = 250.0
        private const val SIDE_MARGIN = 90
        private const val BOTTOM_MARGIN = 80
        private const val SETTINGS_X = 715.0
        private const val SETTINGS_Y = 15.0
    }
}
