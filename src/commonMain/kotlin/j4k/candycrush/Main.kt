package j4k.candycrush

import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onMouseDrag
import com.soywiz.korge.view.*
import com.soywiz.korau.sound.*
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.color.Colors
import kotlinx.coroutines.delay
import j4k.candycrush.math.PositionGrid
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.GameField
import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile
import j4k.candycrush.model.TileObjective

/** Starts the portrait game window. */
suspend fun main() = Korge(width = 540, height = 960, virtualWidth = 810, virtualHeight = 1440, title = "Candy Crush") {
    val level = Level(LevelData.BOARD, LevelData.RESERVE, 42, listOf(TileObjective(Tile.A, 18), TileObjective(Tile.C, 22)))
    val mechanics = GameMechanics(level.field)
    val state = GameState(level)
    val grid = PositionGrid(90, 290, 5, 9, 126)
    var selected: Position? = null
    var dragOrigin: Position? = null
    var busy = false
    var settingsOpen = false
    var soundEnabled = true
    var musicEnabled = false
    var gameEnded = false

    val boardLayer = container()
    val hud = container()
    val dialog = container()
    val background = resourcesVfs["images/background.png"].readBitmap()
    val fruits = resourcesVfs["images/candy_fruits.png"].readBitmap()
    val settingsIcon = resourcesVfs["images/gui_settings.png"].readBitmap()
    val settingsOpenIcon = resourcesVfs["images/gui_settings_on.png"].readBitmap()
    val soundOnIcon = resourcesVfs["images/gui_sound_on.png"].readBitmap()
    val soundOffIcon = resourcesVfs["images/gui_sound_off.png"].readBitmap()
    val musicOnIcon = resourcesVfs["images/gui_music_on.png"].readBitmap()
    val musicOffIcon = resourcesVfs["images/gui_music_off.png"].readBitmap()
    val shuffleIcon = resourcesVfs["images/gui_shuffle.png"].readBitmap()
    val restartIcon = resourcesVfs["images/gui_restart.png"].readBitmap()
    val clearSound = resourcesVfs["sounds/clear.mp3"].readSound()
    val errorSound = resourcesVfs["sounds/wrong_move.mp3"].readSound()
    val comboSounds = (2..6).map { resourcesVfs["sounds/multi_$it.mp3"].readSound() }
    val songs = listOf(
        resourcesVfs["music/monkey_drama.mp3"].readSound(),
        resourcesVfs["music/monkey_island_puzzler.mp3"].readSound(),
    )
    var stopMusic: (() -> Unit)? = null
    var redraw: () -> Unit = {}

    suspend fun resolve(first: Position, second: Position) {
        val lines = mutableSetOf<Pair<Boolean, Set<Position>>>()
        listOf(first, second).forEach { position ->
            mechanics.getHorizontalSurroundings(position).takeIf { it.size >= 3 }
                ?.let { lines += true to it.map { cell -> cell.position }.toSet() }
            mechanics.getVerticalSurroundings(position).takeIf { it.size >= 3 }
                ?.let { lines += false to it.map { cell -> cell.position }.toSet() }
        }
        var multiplier = if (lines.size > 1) 2 else 1
        var matches = (mechanics.getConnectedTiles(first) + mechanics.getConnectedTiles(second))
            .map { it.position }.toSet()
        while (matches.isNotEmpty()) {
            val removed = matches.map { level.field[it] }
            state.scoreWave(removed, multiplier)
            matches.forEach(mechanics::removeTile)
            redraw()
            if (soundEnabled) {
                if (multiplier == 1) clearSound.play()
                else comboSounds[(multiplier - 2).coerceAtMost(comboSounds.lastIndex)].play()
            }
            delay(180)
            mechanics.dropAllToGround()
            redraw()
            delay(180)
            mechanics.insert(mechanics.getNewTileMoves { level.getNextTile(it) })
            redraw()
            delay(180)
            matches = mechanics.findAllMatches()
            multiplier++
        }
    }

    suspend fun selectCell(position: Position) {
        if (busy || gameEnded || settingsOpen) return
        val previous = selected
        if (previous == null) {
            if (level.field[position].isCandy()) selected = position
            redraw()
            return
        }
        selected = null
        val distance = kotlin.math.abs(previous.column - position.column) + kotlin.math.abs(previous.row - position.row)
        if (distance != 1) {
            if (level.field[position].isCandy()) selected = position
            redraw()
            return
        }
        if (!mechanics.isSwapAllowed(previous, position)) {
            busy = true
            mechanics.swapTiles(previous, position)
            redraw()
            delay(120)
            mechanics.swapTiles(previous, position)
            busy = false
            if (soundEnabled) errorSound.play()
            redraw()
            return
        }
        busy = true
        mechanics.swapTiles(previous, position)
        state.consumeMove()
        redraw()
        delay(150)
        resolve(previous, position)
        busy = false
        gameEnded = state.isWon() || state.isLost()
        redraw()
    }

    fun resetLevel() {
        val original = GameField.fromString(LevelData.BOARD)
        for (row in 0 until original.rowSize) for (column in 0 until original.columnsSize) {
            level.field[column, row] = original[column, row]
        }
        state.reset()
        selected = null
        gameEnded = false
    }

    fun createNewBoard() {
        state.reset()
        selected = null
        gameEnded = false
        val candidates = Tile.values().take(5)
        for (row in 0 until level.field.rowSize) for (column in 0 until level.field.columnsSize) {
            var attempts = 0
            do {
                level.field[column, row] = candidates.random()
                attempts++
            } while (mechanics.isInRowWithThree(Position(column, row)) && attempts < 100)
            if (mechanics.isInRowWithThree(Position(column, row))) {
                level.field[column, row] = candidates.first { candidate ->
                    level.field[column, row] = candidate
                    !mechanics.isInRowWithThree(Position(column, row))
                }
            }
        }
    }

    fun toggleMusic() {
        musicEnabled = !musicEnabled
        stopMusic?.invoke()
        stopMusic = null
        if (musicEnabled) {
            var stopCurrentTrack: (() -> Unit)? = null
            val playback = hud.launchImmediately {
                var songIndex = 0
                while (musicEnabled) {
                    val channel = songs[songIndex].play()
                    stopCurrentTrack = { channel.stop() }
                    channel.await()
                    songIndex = (songIndex + 1) % songs.size
                }
            }
            stopMusic = { playback.cancel(); stopCurrentTrack?.invoke() }
        }
    }

    redraw = {
        boardLayer.removeChildren()
        hud.removeChildren()
        dialog.removeChildren()
        boardLayer.image(background).xy(0, 0).size(810, 1440)
        hud.text("COUPS  ${state.movesLeft}", textSize = 32, color = Colors.WHITE).xy(28, 36)
        hud.image(fruits.sliceWithSize(0, 0, 212, 212)).xy(32, 92).size(56, 56)
        hud.text("${state.remaining(Tile.A)}", textSize = 30, color = Colors.WHITE).xy(96, 104)
        hud.image(fruits.sliceWithSize(424, 0, 212, 212)).xy(160, 92).size(56, 56)
        hud.text("${state.remaining(Tile.C)}", textSize = 30, color = Colors.WHITE).xy(224, 104)
        hud.text("SCORE  ${state.score}", textSize = 34, color = Colors.WHITE).xy(530, 38)
        if (state.multiplier > 1) {
            hud.text("x${state.multiplier} COMBO", textSize = 28, color = Colors[0xffdf45]).xy(560, 82)
        }
        hud.image(if (settingsOpen) settingsOpenIcon else settingsIcon).xy(704, 96).size(76, 78)
            .onClick { settingsOpen = !settingsOpen; redraw() }

        for (row in 0 until level.field.rowSize) for (column in 0 until level.field.columnsSize) {
            val position = Position(column, row)
            val point = grid.getPosition(column, row)
            val fill = if (selected == position) Colors[0xffd94a]
                else if ((column + row) % 2 == 0) Colors[0x512d78] else Colors[0x653a8c]
            boardLayer.roundRect(121.0, 121.0, 15.0, fill).xy(point.x, point.y)
            val tile = level.field[position]
            if (tile.isCandy()) {
                boardLayer.image(fruits.sliceWithSize(tile.ordinal * 212, 0, 212, 212))
                    .xy(point.x + 3, point.y + 3).size(115.0, 115.0)
                    .onClick { selectCell(position) }
            }
            boardLayer.roundRect(121.0, 121.0, 15.0, Colors.TRANSPARENT).xy(point.x, point.y)
                .onClick { selectCell(position) }
        }

        if (settingsOpen) {
            val icons = listOf(
                if (soundEnabled) soundOnIcon else soundOffIcon,
                if (musicEnabled) musicOnIcon else musicOffIcon,
                shuffleIcon,
                restartIcon,
            )
            icons.forEachIndexed { index, icon ->
                hud.image(icon).xy(704, 182 + index * 78).size(76, 76).onClick {
                    when (index) {
                        0 -> soundEnabled = !soundEnabled
                        1 -> toggleMusic()
                        2 -> createNewBoard()
                        3 -> resetLevel()
                    }
                    settingsOpen = false
                    redraw()
                }
            }
        }

        if (gameEnded) {
            dialog.roundRect(560.0, 320.0, 28.0, Colors[0xb5327d]).xy(125, 550)
            dialog.text(if (state.isWon()) "Bravo !" else "Perdu !", textSize = 64, color = Colors.WHITE).xy(285, 590)
            dialog.image(restartIcon).xy(260, 710).size(86, 86).onClick { resetLevel(); redraw() }
            dialog.image(shuffleIcon).xy(465, 710).size(86, 86).onClick { createNewBoard(); redraw() }
            dialog.text("Rejouer", textSize = 24, color = Colors.WHITE).xy(260, 800)
            dialog.text("Nouvelle grille", textSize = 24, color = Colors.WHITE).xy(430, 800)
        }
    }
    onMouseDrag { drag ->
        if (drag.start) dragOrigin = grid.getField(drag.sx.toInt(), drag.sy.toInt())
        if (drag.end) {
            val origin = dragOrigin
            dragOrigin = null
            val destination = grid.getField(drag.cx.toInt(), drag.cy.toInt())
            if (origin != null && grid.isOnGrid(origin) && grid.isOnGrid(destination)) {
                selected = origin
                hud.launchImmediately { selectCell(destination) }
            }
        }
    }
    redraw()
}
