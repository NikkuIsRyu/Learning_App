package tech.nikku.learningapp.feature.shared.player

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsController @Inject constructor(
  @ApplicationContext private val context: Context
) {

  private var tts: TextToSpeech? = null

  private val _isReady = MutableStateFlow(false)
  val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

  private val _isAvailable = MutableStateFlow(true)
  val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private var pendingUtteranceCallback: (() -> Unit)? = null

  init {
    initTts()
  }

  private fun initTts() {
    tts = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        _isReady.value = true
        setupProgressListener()
      } else {
        _isReady.value = false
        _isAvailable.value = false
        _statusMessage.value = "Text-to-Speech service is not available on this device."
      }
    }
  }

  private fun setupProgressListener() {
    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {
        _isSpeaking.value = true
      }

      override fun onDone(utteranceId: String?) {
        _isSpeaking.value = false
        pendingUtteranceCallback?.invoke()
        pendingUtteranceCallback = null
      }

      @Deprecated("Deprecated in Java")
      override fun onError(utteranceId: String?) {
        _isSpeaking.value = false
        pendingUtteranceCallback = null
      }

      override fun onError(utteranceId: String?, errorCode: Int) {
        _isSpeaking.value = false
        pendingUtteranceCallback = null
      }
    })
  }

  fun speak(
    text: String,
    locale: Locale = Locale.JAPANESE,
    speed: Float = 1.0f,
    onDone: (() -> Unit)? = null
  ) {
    val engine = tts
    if (engine == null || !_isReady.value) {
      _statusMessage.value = "TTS engine is initializing. Please try again in a moment."
      onDone?.invoke()
      return
    }

    val availability = engine.isLanguageAvailable(locale)
    if (availability == TextToSpeech.LANG_MISSING_DATA || availability == TextToSpeech.LANG_NOT_SUPPORTED) {
      _isAvailable.value = false
      _statusMessage.value = "Voice data for ${locale.displayLanguage} is missing on device. Install voice data in system settings."
      onDone?.invoke()
      return
    }

    try {
      engine.language = locale
      engine.setSpeechRate(speed.coerceIn(0.5f, 1.5f))
      this.pendingUtteranceCallback = onDone
      val utteranceId = UUID.randomUUID().toString()
      engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    } catch (e: Exception) {
      _statusMessage.value = "Unable to play speech: ${e.localizedMessage}"
      onDone?.invoke()
    }
  }

  fun stop() {
    tts?.stop()
    _isSpeaking.value = false
    pendingUtteranceCallback = null
  }

  fun release() {
    stop()
    tts?.shutdown()
    tts = null
    _isReady.value = false
  }
}
