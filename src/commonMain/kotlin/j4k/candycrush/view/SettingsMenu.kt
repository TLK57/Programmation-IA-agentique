package j4k.candycrush.view

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.position

/**
 * The gear button and the column of four buttons shown below it when it is clicked (EF-35):
 * sounds, music, shuffle and restart.
 */
class SettingsMenu(
    private val assets: Assets,
    private val audio: AudioPlayer,
    private val onShuffle: () -> Unit,
    private val onRestart: () -> Unit,
) : Container() {

    private val column = Container()

    private val gear = ImageButton(gui("gui_settings"), gui("gui_settings_hover"), action = ::toggle)

    private val soundButton: ImageButton = ImageButton(soundImage(), action = {
        audio.toggleSounds()
        updateToggleImages()
    })

    private val musicButton: ImageButton = ImageButton(musicImage(), action = {
        audio.toggleMusic()
        updateToggleImages()
    })

    init {
        gear.scale = BUTTON_SCALE
        gear.addTo(this)
        column.addTo(this).position(0.0, BUTTON_SPACING)
        val buttons = listOf(
            soundButton,
            musicButton,
            ImageButton(gui("gui_shuffle"), gui("gui_shuffle_hover"), gui("gui_shuffle_click")) { close(); onShuffle() },
            ImageButton(gui("gui_restart"), gui("gui_restart_hover"), gui("gui_restart_click")) { close(); onRestart() },
        )
        buttons.forEachIndexed { index, button ->
            button.scale = BUTTON_SCALE
            button.addTo(column).position(0.0, index * BUTTON_SPACING)
        }
        column.visible = false
    }

    /** Opens the menu if it is closed, closes it otherwise. */
    fun toggle() {
        column.visible = !column.visible
        gear.normal = gui(if (column.visible) "gui_settings_on" else "gui_settings")
    }

    /** Closes the menu. */
    fun close() {
        if (column.visible) toggle()
    }

    private fun updateToggleImages() {
        soundButton.normal = soundImage()
        musicButton.normal = musicImage()
    }

    private fun soundImage() = gui(if (audio.soundsEnabled) "gui_sound_on" else "gui_sound_off")

    private fun musicImage() = gui(if (audio.musicEnabled) "gui_music_on" else "gui_music_off")

    private fun gui(name: String) = assets.gui.getValue(name)

    private companion object {
        const val BUTTON_SCALE = 0.5
        const val BUTTON_SPACING = 90.0
    }
}
