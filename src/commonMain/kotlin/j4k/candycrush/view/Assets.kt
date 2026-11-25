package j4k.candycrush.view

import com.soywiz.korau.sound.Sound
import com.soywiz.korau.sound.readMusic
import com.soywiz.korau.sound.readSound
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.bitmap.NinePatchBmpSlice
import com.soywiz.korim.bitmap.readNinePatch
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import j4k.candycrush.model.Tile

/** Every image, font and sound used by the game, loaded once at start. */
class Assets private constructor(
    val background: Bitmap,
    val candySheet: Bitmap,
    val debugSheet: Bitmap,
    val movesIcon: Bitmap,
    val messageBox: NinePatchBmpSlice,
    val button: NinePatchBmpSlice,
    val bigFont: BitmapFont,
    val smallFont: BitmapFont,
    val gui: Map<String, Bitmap>,
    val sounds: GameSounds,
) {
    /** Image of [tile] in the candy sheet (or in the letter sheet when [debug] is true). */
    fun tileImage(tile: Tile, debug: Boolean = false): BmpSlice {
        val (sheet, cell, sheetColumns) =
            if (debug) Triple(debugSheet, DEBUG_CELLS.getValue(tile), DEBUG_SHEET_COLUMNS)
            else Triple(candySheet, CANDY_CELLS[tile] ?: DEFAULT_CANDY_CELL, CANDY_SHEET_COLUMNS)
        val index = cell - 1
        return sheet.sliceWithSize(
            (index % sheetColumns) * SHEET_CELL_SIZE,
            (index / sheetColumns) * SHEET_CELL_SIZE,
            SHEET_CELL_SIZE,
            SHEET_CELL_SIZE,
        )
    }

    /** Sounds of the game. */
    class GameSounds(val clear: Sound, val wrongMove: Sound, val combos: List<Sound>, val music: List<Sound>)

    companion object {
        /** Size in pixels of one cell of the candy sheets. */
        const val SHEET_CELL_SIZE = 212
        private const val CANDY_SHEET_COLUMNS = 5
        private const val DEBUG_SHEET_COLUMNS = 4
        private const val DEFAULT_CANDY_CELL = 1

        /** Cell numbers (1-based, left to right then top to bottom) of each candy in `candy_donuts.png`. */
        private val CANDY_CELLS = mapOf(Tile.A to 14, Tile.B to 8, Tile.C to 5, Tile.D to 10, Tile.E to 16)

        /** Cell numbers of each tile in `test_fruits.png`. */
        private val DEBUG_CELLS = Tile.values().associateWith { it.ordinal + 1 }

        private val GUI_IMAGES = listOf(
            "gui_settings", "gui_settings_hover", "gui_settings_on",
            "gui_sound_on", "gui_sound_off", "gui_music_on", "gui_music_off",
            "gui_shuffle", "gui_shuffle_hover", "gui_shuffle_click",
            "gui_restart", "gui_restart_hover", "gui_restart_click",
        )

        private val COMBO_SOUNDS = (2..6).map { "sounds/multi_$it.mp3" }
        private val MUSIC = listOf("music/monkey_drama.mp3", "music/monkey_island_puzzler.mp3")

        /** Loads every resource from `src/commonMain/resources`. */
        suspend fun load(): Assets = Assets(
            background = image("background"),
            candySheet = image("candy_donuts"),
            debugSheet = image("test_fruits"),
            movesIcon = image("text_arrows_move"),
            messageBox = resourcesVfs["images/message_box.9.png"].readNinePatch(),
            button = resourcesVfs["images/button.9.png"].readNinePatch(),
            bigFont = resourcesVfs["fonts/candy.fnt"].readBitmapFont(),
            smallFont = resourcesVfs["fonts/candy-small.fnt"].readBitmapFont(),
            gui = GUI_IMAGES.associateWith { image(it) },
            sounds = GameSounds(
                clear = resourcesVfs["sounds/clear.mp3"].readSound(),
                wrongMove = resourcesVfs["sounds/wrong_move.mp3"].readSound(),
                combos = COMBO_SOUNDS.map { resourcesVfs[it].readSound() },
                music = MUSIC.map { resourcesVfs[it].readMusic() },
            ),
        )

        private suspend fun image(name: String): Bitmap = resourcesVfs["images/$name.png"].readBitmap()
    }
}
