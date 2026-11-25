package j4k.candycrush.view

import com.soywiz.korau.sound.Sound
import com.soywiz.korau.sound.SoundChannel
import com.soywiz.korau.sound.await
import com.soywiz.korau.sound.playingOrPaused
import com.soywiz.korio.async.launchImmediately
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Plays sound effects and music. Sounds are enabled and music is disabled at start (EF-35);
 * disabling either stops what is currently playing.
 */
class AudioPlayer(private val scope: CoroutineScope, private val sounds: Assets.GameSounds) {

    /** True when sound effects are enabled. */
    var soundsEnabled = true
        private set

    /** True when music is enabled. */
    var musicEnabled = false
        private set

    private val effectChannels = mutableListOf<SoundChannel>()
    private var musicJob: Job? = null
    private var musicChannel: SoundChannel? = null

    /** Plays the removal sound. */
    fun playClear() = playEffect(sounds.clear)

    /** Plays the invalid swap sound. */
    fun playWrongMove() = playEffect(sounds.wrongMove)

    /** Plays the combo sound matching [multiplier] (×2 to ×5, then the same sound from ×6 on). */
    fun playCombo(multiplier: Int) {
        val index = (multiplier - FIRST_COMBO_MULTIPLIER).coerceIn(0, sounds.combos.lastIndex)
        playEffect(sounds.combos[index])
    }

    /** Toggles sound effects; returns the new state. */
    fun toggleSounds(): Boolean {
        soundsEnabled = !soundsEnabled
        if (!soundsEnabled) {
            effectChannels.forEach { it.stop() }
            effectChannels.clear()
        }
        return soundsEnabled
    }

    /** Toggles music; returns the new state. */
    fun toggleMusic(): Boolean {
        musicEnabled = !musicEnabled
        if (musicEnabled) startMusic() else stopMusic()
        return musicEnabled
    }

    private fun playEffect(sound: Sound) {
        if (!soundsEnabled) return
        effectChannels.removeAll { !it.playingOrPaused }
        effectChannels += sound.play(scope.coroutineContext)
    }

    private fun startMusic() {
        musicJob = scope.launchImmediately {
            while (true) {
                for (track in sounds.music) {
                    val channel = track.play()
                    musicChannel = channel
                    channel.await()
                }
            }
        }
    }

    private fun stopMusic() {
        musicJob?.cancel()
        musicJob = null
        musicChannel?.stop()
        musicChannel = null
    }

    private companion object {
        const val FIRST_COMBO_MULTIPLIER = 2
    }
}
