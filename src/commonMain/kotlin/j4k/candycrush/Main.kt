package j4k.candycrush

import com.soywiz.korge.Korge
import com.soywiz.korge.internal.KorgeInternal
import com.soywiz.korge.internal.KorgeUntested
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onDown
import com.soywiz.korge.input.onOut
import com.soywiz.korge.input.onOver
import com.soywiz.korge.input.onUpAnywhere
import com.soywiz.korge.input.keys
import com.soywiz.korge.view.Image
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.container
import com.soywiz.korge.view.image
import com.soywiz.korge.view.solidRect
import com.soywiz.korge.view.text
import com.soywiz.korau.sound.Sound
import com.soywiz.korau.sound.SoundChannel
import com.soywiz.korau.sound.PlaybackTimes
import com.soywiz.korau.sound.await
import com.soywiz.korau.sound.readSound
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.color.Colors
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import j4k.candycrush.math.PositionGrid
import j4k.candycrush.math.PositionGrid.Position
import j4k.candycrush.model.Level
import j4k.candycrush.model.Tile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val APP_WINDOW_TITLE = "Candy Crush"
private const val APP_WINDOW_WIDTH = 540
private const val APP_WINDOW_HEIGHT = 960
private const val APP_VIRTUAL_WIDTH = 810
private const val APP_VIRTUAL_HEIGHT = 1440

/** Starts Candy Crush in the required portrait window and virtual resolution. */
suspend fun main() = Korge(
    width = APP_WINDOW_WIDTH,
    height = APP_WINDOW_HEIGHT,
    virtualWidth = APP_VIRTUAL_WIDTH,
    virtualHeight = APP_VIRTUAL_HEIGHT,
    title = APP_WINDOW_TITLE,
) {
    CandyCrushGame(this).start()
}

@OptIn(KorgeInternal::class, KorgeUntested::class)
private class CandyCrushGame(private val stage: Stage) {
    private lateinit var level: Level
    private lateinit var progress: GameProgress
    private lateinit var mechanics: GameMechanics
    private lateinit var grid: PositionGrid
    private lateinit var candyViews: List<Image?>
    private lateinit var scoreText: Text
    private lateinit var movesText: Text
    private lateinit var objectiveTexts: List<Text>
    private lateinit var chainText: Text
    private lateinit var gainText: Text
    private lateinit var endOverlay: com.soywiz.korge.view.Container
    private lateinit var endTitle: Text
    private lateinit var settingsButton: Image
    private lateinit var soundButton: Image
    private lateinit var musicButton: Image
    private lateinit var debugModeText: Text
    private val cellBackgrounds = mutableListOf<com.soywiz.korge.view.SolidRect>()
    private lateinit var candySprites: Map<Tile, com.soywiz.korim.bitmap.BitmapSlice<Bitmap>>
    private lateinit var debugSprites: Map<Tile, com.soywiz.korim.bitmap.BitmapSlice<Bitmap>>
    private lateinit var soundOnBitmap: Bitmap
    private lateinit var soundOffBitmap: Bitmap
    private lateinit var musicOnBitmap: Bitmap
    private lateinit var musicOffBitmap: Bitmap
    private lateinit var settingsNormalBitmap: Bitmap
    private lateinit var settingsOpenBitmap: Bitmap
    private lateinit var settingsHoverBitmap: Bitmap
    private lateinit var shuffleHoverBitmap: Bitmap
    private lateinit var shuffleClickBitmap: Bitmap
    private lateinit var restartHoverBitmap: Bitmap
    private lateinit var restartClickBitmap: Bitmap
    private lateinit var backgroundBitmap: Bitmap
    private lateinit var moveIconBitmap: Bitmap
    private lateinit var shuffleBitmap: Bitmap
    private lateinit var restartBitmap: Bitmap
    private lateinit var messageBoxBitmap: Bitmap
    private lateinit var endButtonBitmap: Bitmap
    private val soundEffects = mutableMapOf<String, Sound>()
    private val activeEffects = mutableListOf<SoundChannel>()
    private val comboSounds = mutableListOf<Sound>()
    private val musicTracks = mutableListOf<Sound>()
    private var musicChannel: SoundChannel? = null
    private var musicJob: Job? = null
    private var turnJob: Job? = null
    private var menuOpen = false
    private var soundsEnabled = true
    private var musicEnabled = false
    private var debugLetters = false
    private var resolving = false
    private var selected: Position? = null
    private var gestureStart: Position? = null
    private var highlightedCells: Set<Position> = emptySet()

    suspend fun start() {
        level = Level.defaultLevel()
        progress = GameProgress(level)
        mechanics = GameMechanics(level.field)
        grid = PositionGrid(
            x = (VIRTUAL_WIDTH - BOARD_COLUMNS * CELL_SIZE) / 2,
            y = BOARD_TOP,
            columns = BOARD_COLUMNS,
            rows = BOARD_ROWS,
            tileSize = CELL_SIZE,
        )
        loadAssets()
        buildScene()
        updateBoard()
        updateHud()
    }

    private suspend fun loadAssets() {
        val candySheet = resourcesVfs["images/candy_donuts.png"].readBitmap()
        val spritePositions = mapOf(
            Tile.A to (3 to 2),
            Tile.B to (2 to 1),
            Tile.C to (4 to 0),
            Tile.D to (4 to 1),
            Tile.E to (0 to 3),
        )
        candySprites = spritePositions.mapValues { (_, cell) ->
            candySheet.sliceWithSize(cell.first * SPRITE_SIZE, cell.second * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE)
        }

        val testSheet = resourcesVfs["images/test_fruits.png"].readBitmap()
        debugSprites = listOf(Tile.A, Tile.B, Tile.C, Tile.D, Tile.E).mapIndexed { index, tile ->
            tile to testSheet.sliceWithSize((index % 4) * SPRITE_SIZE, (index / 4) * SPRITE_SIZE, SPRITE_SIZE, SPRITE_SIZE)
        }.toMap()
        soundOnBitmap = resourcesVfs["images/gui_sound_on.png"].readBitmap()
        soundOffBitmap = resourcesVfs["images/gui_sound_off.png"].readBitmap()
        musicOnBitmap = resourcesVfs["images/gui_music_on.png"].readBitmap()
        musicOffBitmap = resourcesVfs["images/gui_music_off.png"].readBitmap()
        settingsNormalBitmap = resourcesVfs["images/gui_settings.png"].readBitmap()
        settingsOpenBitmap = resourcesVfs["images/gui_settings_on.png"].readBitmap()
        settingsHoverBitmap = resourcesVfs["images/gui_settings_hover.png"].readBitmap()
        backgroundBitmap = resourcesVfs["images/background.png"].readBitmap()
        moveIconBitmap = resourcesVfs["images/text_arrows_move.png"].readBitmap()
        shuffleBitmap = resourcesVfs["images/gui_shuffle.png"].readBitmap()
        shuffleHoverBitmap = resourcesVfs["images/gui_shuffle_hover.png"].readBitmap()
        shuffleClickBitmap = resourcesVfs["images/gui_shuffle_click.png"].readBitmap()
        restartBitmap = resourcesVfs["images/gui_restart.png"].readBitmap()
        restartHoverBitmap = resourcesVfs["images/gui_restart_hover.png"].readBitmap()
        restartClickBitmap = resourcesVfs["images/gui_restart_click.png"].readBitmap()
        messageBoxBitmap = resourcesVfs["images/message_box.9.png"].readBitmap()
        endButtonBitmap = resourcesVfs["images/button.9.png"].readBitmap()

        soundEffects["clear"] = resourcesVfs["sounds/clear.mp3"].readSound()
        soundEffects["wrong"] = resourcesVfs["sounds/wrong_move.mp3"].readSound()
        soundEffects["drop"] = resourcesVfs["sounds/drop_ground.mp3"].readSound()
        for (index in 2..6) {
            comboSounds += resourcesVfs["sounds/multi_$index.mp3"].readSound()
        }
        musicTracks += resourcesVfs["music/monkey_drama.mp3"].readSound()
        musicTracks += resourcesVfs["music/monkey_island_puzzler.mp3"].readSound()
    }

    private fun buildScene() {
        val background = stage.image(backgroundBitmap) {
            scaleX = VIRTUAL_WIDTH.toDouble() / backgroundBitmap.width
            scaleY = VIRTUAL_HEIGHT.toDouble() / backgroundBitmap.height
        }
        background.name = "background"

        val header = stage.solidRect(VIRTUAL_WIDTH.toDouble(), 185.0, Colors["#311348"]) {
            alpha = 0.74
        }
        header.name = "hud-background"

        val movesIcon = stage.image(moveIconBitmap) {
            x = 35.0
            y = 35.0
            scale = 0.55
        }
        movesIcon.name = "moves-icon"
        movesText = stage.text("42", textSize = 48.0, color = Colors.WHITE) {
            x = 100.0
            y = 35.0
        }
        scoreText = stage.text("0", textSize = 58.0, color = Colors.WHITE) {
            x = 620.0
            y = 35.0
        }

        val objectiveIcons = listOf(Tile.A, Tile.C)
        objectiveTexts = objectiveIcons.mapIndexed { index, tile ->
            val x = 190.0 + index * 155.0
            stage.image(candySprites.getValue(tile)) {
                this.x = x
                y = 33.0
                scale = 0.47
            }
            stage.text("0", textSize = 44.0, color = Colors.WHITE) {
                this.x = x + 65.0
                y = 42.0
            }
        }

        chainText = stage.text("", textSize = 54.0, color = Colors["#FFF176"]) {
            x = 610.0
            y = 105.0
        }
        gainText = stage.text("", textSize = 38.0, color = Colors.WHITE) {
            x = 315.0
            y = 112.0
        }
        debugModeText = stage.text("", textSize = 24.0, color = Colors.WHITE) {
            x = 30.0
            y = 190.0
        }

        buildBoard()
        stage.sendChildToFront(gainText)
        bindBoardInput()
        buildSettings()
        buildEndOverlay()
        stage.keys {
            down(com.soywiz.korev.Key.P) { println(level.field) }
            down(com.soywiz.korev.Key.D) {
                debugLetters = !debugLetters
                updateBoard()
            }
            down(com.soywiz.korev.Key.S) { shuffle() }
            down(com.soywiz.korev.Key.R) { restart() }
        }
    }

    private fun buildBoard() {
        val cells = mutableListOf<Image?>()
        for (row in 0 until BOARD_ROWS) {
            for (column in 0 until BOARD_COLUMNS) {
                val point = grid.getPosition(column, row)
                val cellX = point.x
                val cellY = point.y
                val square = stage.solidRect(CELL_SIZE.toDouble(), CELL_SIZE.toDouble(), Colors["#391451"]) {
                    x = cellX
                    y = cellY
                    alpha = 0.67
                }
                cellBackgrounds += square
                val sprite = stage.image(candySprites.getValue(Tile.A)) {
                    x = cellX + CELL_SIZE * 0.075
                    y = cellY + CELL_SIZE * 0.075
                    scale = CANDY_SCALE
                }
                cells += sprite
            }
        }
        candyViews = cells
    }

    private fun bindBoardInput() {
        stage.onDown { event ->
            if (!resolving && !endOverlay.visible && !menuOpen) {
                val position = grid.getField(event.currentPosGlobal.x.toInt(), event.currentPosGlobal.y.toInt())
                if (grid.isOnGrid(position) && level.field[position].isTile()) gestureStart = position
            }
        }
        stage.onUpAnywhere { event ->
            val start = gestureStart
            if (start != null) {
                gestureStart = null
                val destination = grid.getField(event.upPosGlobal.x.toInt(), event.upPosGlobal.y.toInt())
                handleGesture(start, destination)
            }
        }
    }

    private fun buildSettings() {
        settingsButton = stage.image(settingsNormalBitmap) {
            x = 710.0
            y = 190.0
            scale = 0.58
        }
        settingsButton.onOver { settingsButton.bitmap = wholeBitmap(settingsHoverBitmap) }
        settingsButton.onOut {
            settingsButton.bitmap = wholeBitmap(if (menuOpen) settingsOpenBitmap else settingsNormalBitmap)
        }
        settingsButton.onClick {
            menuOpen = !menuOpen
            settingsButton.bitmap = wholeBitmap(if (menuOpen) settingsOpenBitmap else settingsNormalBitmap)
            setSettingsVisible(menuOpen)
        }

        soundButton = createIconButton(soundOnBitmap, 710.0, 280.0) {
            soundsEnabled = !soundsEnabled
            soundButton.bitmap = wholeBitmap(if (soundsEnabled) soundOnBitmap else soundOffBitmap)
            if (!soundsEnabled) {
                activeEffects.forEach { it.stop() }
                activeEffects.clear()
            }
        }
        musicButton = createIconButton(musicOffBitmap, 710.0, 365.0) {
            musicEnabled = !musicEnabled
            musicButton.bitmap = wholeBitmap(if (musicEnabled) musicOnBitmap else musicOffBitmap)
            if (musicEnabled) startMusic() else stopMusic()
        }
        createIconButton(shuffleBitmap, 710.0, 450.0, shuffleHoverBitmap, shuffleClickBitmap) {
            shuffle()
        }.name = "shuffle-button"
        createIconButton(restartBitmap, 710.0, 535.0, restartHoverBitmap, restartClickBitmap) {
            restart()
        }.name = "restart-button"
        setSettingsVisible(false)
    }

    private fun createIconButton(
        bitmap: Bitmap,
        x: Double,
        y: Double,
        hoverBitmap: Bitmap? = null,
        clickBitmap: Bitmap? = null,
        action: () -> Unit,
    ): Image {
        val icon = stage.image(bitmap) {
            this.x = x
            this.y = y
            scale = 0.53
        }
        icon.onOver { if (hoverBitmap != null) icon.bitmap = wholeBitmap(hoverBitmap) }
        icon.onOut { icon.bitmap = wholeBitmap(bitmap) }
        icon.onDown { if (clickBitmap != null) icon.bitmap = wholeBitmap(clickBitmap) }
        icon.onUpAnywhere { icon.bitmap = wholeBitmap(if (hoverBitmap != null) hoverBitmap else bitmap) }
        icon.onClick { action() }
        return icon
    }

    private fun buildEndOverlay() {
        endOverlay = stage.container {
            visible = false
        }
        val overlayShade = endOverlay.solidRect(
            VIRTUAL_WIDTH.toDouble(),
            VIRTUAL_HEIGHT.toDouble(),
            Colors["#170923"],
        )
        overlayShade.alpha = 0.62
        val panel = endOverlay.image(messageBoxBitmap) {
            x = 155.0
            y = 510.0
            scaleX = 500.0 / messageBoxBitmap.width
            scaleY = 385.0 / messageBoxBitmap.height
        }
        panel.name = "end-panel"
        endTitle = endOverlay.text("Bravo !", textSize = 70.0, color = Colors.WHITE) {
            x = 255.0
            y = 565.0
        }
        endOverlay.image(endButtonBitmap) {
            x = 225.0
            y = 690.0
            scaleX = 350.0 / endButtonBitmap.width
            scaleY = 76.0 / endButtonBitmap.height
            onClick { restart() }
        }
        endOverlay.text("REJOUER", textSize = 34.0, color = Colors.WHITE) {
            x = 310.0
            y = 708.0
        }
        endOverlay.image(endButtonBitmap) {
            x = 225.0
            y = 790.0
            scaleX = 350.0 / endButtonBitmap.width
            scaleY = 76.0 / endButtonBitmap.height
            onClick { shuffle() }
        }
        endOverlay.text("NOUVELLE GRILLE", textSize = 28.0, color = Colors.WHITE) {
            x = 258.0
            y = 812.0
        }
    }

    private fun setSettingsVisible(visible: Boolean) {
        soundButton.visible = visible
        musicButton.visible = visible
        stage.getChildByName("shuffle-button")?.visible = visible
        stage.getChildByName("restart-button")?.visible = visible
    }

    private fun handleGesture(start: Position, destination: Position) {
        if (!grid.isOnGrid(destination) || resolving || endOverlay.visible || menuOpen) return
        if (start == destination) {
            val previous = selected
            if (previous == null || previous == start) {
                selected = if (previous == null) start else null
                updateBoard()
            } else {
                selected = null
                attemptSwap(previous, start)
            }
            return
        }
        selected = null
        attemptSwap(start, destination)
    }

    private fun attemptSwap(first: Position, second: Position) {
        if (resolving || !grid.isOnGrid(first) || !grid.isOnGrid(second)) return
        if (abs(first.column - second.column) + abs(first.row - second.row) != 1) return
        if (!level.field[first].isTile() || !level.field[second].isTile()) return
        if (progress.movesRemaining == 0) return
        resolving = true
        turnJob = stage.launch {
            if (!mechanics.isSwapAllowed(first, second)) {
                playEffect("wrong")
                animateSwap(first, second, returnToStart = true)
                resolving = false
                updateBoard()
                turnJob = null
                return@launch
            }
            animateSwap(first, second, returnToStart = false)
            mechanics.swapTiles(first, second)
            progress.beginTurn()
            updateBoard()
            resolveTurn(first, second)
            turnJob = null
        }
    }

    private suspend fun animateSwap(first: Position, second: Position, returnToStart: Boolean) {
        val firstIndex = indexOf(first)
        val secondIndex = indexOf(second)
        val firstSprite = candyViews[firstIndex] ?: return
        val secondSprite = candyViews[secondIndex] ?: return
        val firstX = grid.getPosition(first.column, first.row).x
        val firstY = grid.getPosition(first.column, first.row).y
        val secondX = grid.getPosition(second.column, second.row).x
        val secondY = grid.getPosition(second.column, second.row).y
        val firstOriginX = firstX + CELL_SIZE * 0.075
        val firstOriginY = firstY + CELL_SIZE * 0.075
        val secondOriginX = secondX + CELL_SIZE * 0.075
        val secondOriginY = secondY + CELL_SIZE * 0.075
        animateViews(
            listOf(
                ViewMotion(firstSprite, firstOriginX, firstOriginY, secondOriginX, secondOriginY),
                ViewMotion(secondSprite, secondOriginX, secondOriginY, firstOriginX, firstOriginY),
            ),
            SWAP_MILLISECONDS,
        )
        if (returnToStart) {
            animateViews(
                listOf(
                    ViewMotion(firstSprite, secondOriginX, secondOriginY, firstOriginX, firstOriginY),
                    ViewMotion(secondSprite, firstOriginX, firstOriginY, secondOriginX, secondOriginY),
                ),
                SWAP_MILLISECONDS,
            )
        }
    }

    private suspend fun resolveTurn(first: Position, second: Position) {
        var multiplier = countInitialMatches(first, second).let { if (it > 1) 2 else 1 }
        var matches = collectMatches(listOf(first, second))
        while (matches.isNotEmpty()) {
            val counts = matches.groupingBy { level.field[it] }.eachCount()
            progress.recordWave(counts, multiplier)
            showWaveFeedback(matches, counts.values.sum(), multiplier)
            if (multiplier == 1) playEffect("clear") else playCombo(multiplier)
            fadeMatchedCandies(matches)
            matches.forEach { level.field[it] = Tile.Hole }
            updateHud()
            updateBoard()

            val drops = mechanics.dropAllToGround()
            if (drops.isNotEmpty()) playEffect("drop")
            for (move in drops) animateDrop(move)
            updateBoard()

            val inserts = mechanics.getNewTileMoves { column -> level.getNextTile(column) }
            mechanics.insert(inserts)
            updateBoard()
            inserts.forEach { move ->
                val sprite = candyViews[indexOf(move.target)]
                val point = grid.getPosition(move.target.column, move.target.row)
                if (sprite != null) {
                    sprite.x = point.x + CELL_SIZE * 0.075
                    sprite.y = (BOARD_TOP - CELL_SIZE).toDouble()
                    sprite.alpha = 0.15
                }
            }
            for (move in inserts) animateInsertion(move.target)

            multiplier++
            matches = collectMatches()
        }

        progress.finishTurn()
        updateHud()
        if (progress.outcome != null) showEndScreen()
        resolving = false
    }

    private fun countInitialMatches(first: Position, second: Position): Int {
        val groups = mutableSetOf<Set<Position>>()
        listOf(first, second).forEach { position ->
            val vertical = mechanics.getVerticalSurroundings(position)
            if (vertical.size >= MIN_MATCH_SIZE) groups += vertical.map { it.position }.toSet()
            val horizontal = mechanics.getHorizontalSurroundings(position)
            if (horizontal.size >= MIN_MATCH_SIZE) groups += horizontal.map { it.position }.toSet()
        }
        return groups.size
    }

    private fun collectMatches(through: List<Position>? = null): Set<Position> {
        val source = through ?: buildList {
            for (row in 0 until level.field.rowSize) {
                for (column in 0 until level.field.columnsSize) add(Position(column, row))
            }
        }
        val matched = mutableSetOf<Position>()
        source.forEach { position ->
            if (!level.field[position].isTile()) return@forEach
            mechanics.getConnectedTiles(position).forEach { matched += it.position }
        }
        return matched
    }

    private suspend fun showWaveFeedback(matches: Set<Position>, count: Int, multiplier: Int) {
        gainText.text = "+${count * GameProgress.POINTS_PER_CANDY * multiplier}"
        chainText.text = if (multiplier > 1) "x$multiplier" else ""
        if (matches.isNotEmpty()) {
            val center = matches.sortedWith(compareBy<Position> { it.row }.thenBy { it.column })[matches.size / 2]
            val centerPoint = grid.getPosition(center.column, center.row)
            val centerX = centerPoint.x
            val centerY = centerPoint.y
            gainText.x = centerX
            gainText.y = centerY
        }
        updateHud()
        val initialY = gainText.y
        for (frame in 1..10) {
            gainText.y = initialY - 6.0 * frame
            gainText.alpha = 1.0 - frame / 10.0
            delay(100)
        }
        gainText.text = ""
        gainText.alpha = 1.0
        chainText.text = ""
    }

    private suspend fun fadeMatchedCandies(matches: Set<Position>) {
        val sprites = matches.mapNotNull { candyViews[indexOf(it)] }
        val steps = 10
        for (step in 1..steps) {
            val alpha = 1.0 - step.toDouble() / steps
            sprites.forEach { it.alpha = alpha }
            delay(20)
        }
    }

    private suspend fun animateDrop(move: GameMechanics.Move) {
        val sprite = candyViews[indexOf(move.tile)] ?: return
        val source = grid.getPosition(move.tile.column, move.tile.row)
        val target = grid.getPosition(move.target.column, move.target.row)
        animateViews(
            listOf(
                ViewMotion(
                    sprite,
                    source.x + CELL_SIZE * 0.075,
                    source.y + CELL_SIZE * 0.075,
                    target.x + CELL_SIZE * 0.075,
                    target.y + CELL_SIZE * 0.075,
                ),
            ),
            abs(move.tile.row - move.target.row) * DROP_MILLISECONDS_PER_ROW,
        )
    }

    private suspend fun animateInsertion(target: Position) {
        val sprite = candyViews[indexOf(target)] ?: return
        val destination = grid.getPosition(target.column, target.row)
        animateViews(
            listOf(
                ViewMotion(
                    sprite,
                    sprite.x,
                    sprite.y,
                    destination.x + CELL_SIZE * 0.075,
                    destination.y + CELL_SIZE * 0.075,
                ),
            ),
            (target.row + 1) * DROP_MILLISECONDS_PER_ROW,
        )
        sprite.alpha = 1.0
    }

    private suspend fun animateViews(motions: List<ViewMotion>, duration: Long) {
        if (motions.isEmpty()) return
        val frames = (duration / FRAME_MILLISECONDS).toInt().coerceAtLeast(1)
        for (frame in 1..frames) {
            val ratio = frame.toDouble() / frames
            motions.forEach { motion ->
                motion.view.x = motion.startX + (motion.endX - motion.startX) * ratio
                motion.view.y = motion.startY + (motion.endY - motion.startY) * ratio
            }
            delay(FRAME_MILLISECONDS)
        }
    }

    private fun updateBoard() {
        for (row in 0 until level.field.rowSize) {
            for (column in 0 until level.field.columnsSize) {
                val position = Position(column, row)
                val index = indexOf(position)
                val background = cellBackgrounds[index]
                background.color = if (position == selected) Colors["#FFE66D"] else Colors["#391451"]
                val sprite = candyViews[index]
                val tile = level.field[position]
                if (sprite == null) continue
                val point = grid.getPosition(column, row)
                sprite.x = point.x + CELL_SIZE * 0.075
                sprite.y = point.y + CELL_SIZE * 0.075
                sprite.alpha = 1.0
                sprite.visible = tile.isTile()
                if (tile.isTile()) {
                    sprite.bitmap = if (debugLetters) debugSprites.getValue(tile) else candySprites.getValue(tile)
                }
            }
        }
        debugModeText.text = if (debugLetters) "DEBUG: LETTERS" else ""
    }

    private fun updateHud() {
        scoreText.text = progress.score.toString()
        movesText.text = progress.movesRemaining.toString()
        objectiveTexts.forEachIndexed { index, label ->
            label.text = progress.remainingFor(if (index == 0) Tile.A else Tile.C).toString()
        }
    }

    private fun showEndScreen() {
        endTitle.text = if (progress.outcome == GameProgress.Outcome.Won) "Bravo !" else "Perdu !"
        endOverlay.visible = true
    }

    private fun restart() {
        cancelTurnAnimations()
        stopMusic()
        musicButton.bitmap = wholeBitmap(musicOffBitmap)
        level = Level.defaultLevel()
        mechanics = GameMechanics(level.field)
        progress = GameProgress(level)
        resolving = false
        selected = null
        endOverlay.visible = false
        updateBoard()
        updateHud()
    }

    private fun shuffle() {
        cancelTurnAnimations()
        stopMusic()
        musicButton.bitmap = wholeBitmap(musicOffBitmap)
        val shuffled = BoardFactory.createMatchFreeField(BOARD_COLUMNS, BOARD_ROWS) { Tile.randomTile() }
        level = Level(
            shuffled.toString(),
            Level.DEFAULT_RESERVE_DATA,
            randomTile = { Tile.randomTile() },
        )
        mechanics = GameMechanics(level.field)
        progress = GameProgress(level)
        selected = null
        endOverlay.visible = false
        updateBoard()
        updateHud()
    }

    private fun startMusic() {
        musicJob?.cancel()
        musicEnabled = true
        musicJob = stage.launch {
            var index = 0
            while (musicEnabled) {
                val track = musicTracks[index % musicTracks.size]
                musicChannel = track.playNoCancel(PlaybackTimes(1))
                musicChannel?.await()
                musicChannel?.stop()
                musicChannel = null
                index++
            }
        }
    }

    private fun stopMusic() {
        musicEnabled = false
        musicJob?.cancel()
        musicJob = null
        musicChannel?.stop()
        musicChannel = null
    }

    private fun cancelTurnAnimations() {
        turnJob?.cancel()
        turnJob = null
        resolving = false
        gestureStart = null
        selected = null
    }

    private fun playEffect(name: String) {
        if (!soundsEnabled) return
        soundEffects[name]?.let { playAndTrack(it) }
    }

    private fun playCombo(multiplier: Int) {
        if (!soundsEnabled || comboSounds.isEmpty()) return
        val sound = comboSounds[(multiplier - 2).coerceAtMost(comboSounds.lastIndex)]
        playAndTrack(sound)
    }

    private fun playAndTrack(sound: Sound) {
        val channel = sound.playNoCancel(PlaybackTimes(1))
        activeEffects += channel
        stage.launch {
            channel.await()
            activeEffects.remove(channel)
        }
    }

    private fun wholeBitmap(bitmap: Bitmap) =
        bitmap.sliceWithSize(0, 0, bitmap.width, bitmap.height)

    private fun indexOf(position: Position): Int = position.row * BOARD_COLUMNS + position.column

    companion object {
        private const val VIRTUAL_WIDTH = APP_VIRTUAL_WIDTH
        private const val VIRTUAL_HEIGHT = APP_VIRTUAL_HEIGHT
        private const val BOARD_COLUMNS = 5
        private const val BOARD_ROWS = 9
        private const val CELL_SIZE = 88
        private const val BOARD_TOP = 320
        private const val SPRITE_SIZE = 212
        private const val CANDY_SCALE = CELL_SIZE * 0.85 / SPRITE_SIZE
        private const val DROP_MILLISECONDS_PER_ROW = 500L
        private const val SWAP_MILLISECONDS = 150L
        private const val FRAME_MILLISECONDS = 16L
        private const val MIN_MATCH_SIZE = 3
    }

    private data class ViewMotion(
        val view: com.soywiz.korge.view.View,
        val startX: Double,
        val startY: Double,
        val endX: Double,
        val endY: Double,
    )
}
