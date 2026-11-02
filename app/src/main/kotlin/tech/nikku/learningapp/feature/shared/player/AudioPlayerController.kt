package tech.nikku.learningapp.feature.shared.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.nikku.learningapp.core.domain.model.DialogueTurn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayerController @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
  private var positionPollJob: Job? = null

  private var exoPlayer: ExoPlayer? = null

  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _currentPositionMs = MutableStateFlow(0L)
  val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

  private val _durationMs = MutableStateFlow(0L)
  val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

  private val _playbackSpeed = MutableStateFlow(1.0f)
  val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

  private var onPlaybackEnded: (() -> Unit)? = null

  private fun ensurePlayer(): ExoPlayer {
    val existing = exoPlayer
    if (existing != null) return existing

    val newPlayer = ExoPlayer.Builder(context).build().apply {
      playbackParameters = PlaybackParameters(_playbackSpeed.value)
      addListener(object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
          _isPlaying.value = playing
          if (playing) {
            startPollingPosition()
          } else {
            stopPollingPosition()
          }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
          if (playbackState == Player.STATE_ENDED) {
            _isPlaying.value = false
            stopPollingPosition()
            onPlaybackEnded?.invoke()
          }
          if (playbackState == Player.STATE_READY) {
            _durationMs.value = duration.coerceAtLeast(0L)
          }
        }
      })
    }
    exoPlayer = newPlayer
    return newPlayer
  }

  fun playUri(uri: Uri, startMs: Long? = null, endMs: Long? = null, onEnded: (() -> Unit)? = null) {
    val player = ensurePlayer()
    this.onPlaybackEnded = onEnded

    val mediaItemBuilder = MediaItem.Builder().setUri(uri)
    if (startMs != null || endMs != null) {
      val clipBuilder = MediaItem.ClippingConfiguration.Builder()
      if (startMs != null && startMs >= 0) {
        clipBuilder.setStartPositionMs(startMs)
      }
      if (endMs != null && endMs > (startMs ?: 0L)) {
        clipBuilder.setEndPositionMs(endMs)
      }
      mediaItemBuilder.setClippingConfiguration(clipBuilder.build())
    }

    player.setMediaItem(mediaItemBuilder.build())
    player.prepare()
    player.play()
  }

  fun playTurn(uri: Uri, turn: DialogueTurn, onEnded: (() -> Unit)? = null) {
    playUri(uri, turn.startMs, turn.endMs, onEnded)
  }

  fun pause() {
    exoPlayer?.pause()
  }

  fun resume() {
    exoPlayer?.play()
  }

  fun stop() {
    exoPlayer?.stop()
    _isPlaying.value = false
    stopPollingPosition()
  }

  fun seekTo(positionMs: Long) {
    exoPlayer?.seekTo(positionMs)
    _currentPositionMs.value = positionMs
  }

  fun setSpeed(speed: Float) {
    val clamped = speed.coerceIn(0.5f, 1.5f)
    _playbackSpeed.value = clamped
    exoPlayer?.playbackParameters = PlaybackParameters(clamped)
  }

  private fun startPollingPosition() {
    positionPollJob?.cancel()
    positionPollJob = coroutineScope.launch {
      while (isActive) {
        exoPlayer?.let { player ->
          _currentPositionMs.value = player.currentPosition
        }
        delay(50L)
      }
    }
  }

  private fun stopPollingPosition() {
    positionPollJob?.cancel()
    positionPollJob = null
  }

  fun release() {
    stopPollingPosition()
    exoPlayer?.release()
    exoPlayer = null
    _isPlaying.value = false
  }
}
