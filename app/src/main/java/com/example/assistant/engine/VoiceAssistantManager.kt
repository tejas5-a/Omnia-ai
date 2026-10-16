package com.example.assistant.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.assistant.model.AssistantState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timeoutJob: Job? = null

    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState

    private val _listeningRemainingSeconds = MutableStateFlow(10)
    val listeningRemainingSeconds: StateFlow<Int> = _listeningRemainingSeconds

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript

    var onCommandRecognized: ((String) -> Unit)? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.language = Locale.getDefault()
            isTtsReady = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _assistantState.value = AssistantState.SPEAKING
                }

                override fun onDone(utteranceId: String?) {
                    if (_assistantState.value == AssistantState.SPEAKING) {
                        _assistantState.value = AssistantState.IDLE
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _assistantState.value = AssistantState.IDLE
                }
            })
        }
    }

    fun startListening(timeoutSeconds: Int = 10) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _spokenText.value = "Speech recognition is unavailable on this device."
            _assistantState.value = AssistantState.ERROR
            return
        }

        stopListening()

        _assistantState.value = AssistantState.LISTENING
        _listeningRemainingSeconds.value = timeoutSeconds

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _assistantState.value = AssistantState.LISTENING
                    _lastTranscript.value = ""
                    startListeningTimeout(timeoutSeconds)
                }

                override fun onBeginningOfSpeech() {
                    cancelTimeoutTimer()
                }

                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    cancelTimeoutTimer()
                    _assistantState.value = AssistantState.PROCESSING
                }

                override fun onError(error: Int) {
                    cancelTimeoutTimer()
                    _assistantState.value = AssistantState.IDLE
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        else -> "Listening error ($error)."
                    }
                    _spokenText.value = "$errorMessage Tap mic to try again."
                }

                override fun onResults(results: Bundle?) {
                    cancelTimeoutTimer()
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull().orEmpty()
                    if (text.isNotBlank()) {
                        _lastTranscript.value = text
                        onCommandRecognized?.invoke(text)
                    } else {
                        _assistantState.value = AssistantState.IDLE
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        _lastTranscript.value = partial
                        cancelTimeoutTimer()
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        startListeningTimeout(timeoutSeconds)

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            cancelTimeoutTimer()
            _assistantState.value = AssistantState.ERROR
            _spokenText.value = "Failed to start speech recognition."
        }
    }

    private fun startListeningTimeout(timeoutSeconds: Int) {
        cancelTimeoutTimer()
        _listeningRemainingSeconds.value = timeoutSeconds
        timeoutJob = scope.launch {
            for (sec in timeoutSeconds downTo 1) {
                _listeningRemainingSeconds.value = sec
                delay(1000L)
                if (_assistantState.value != AssistantState.LISTENING) {
                    return@launch
                }
            }
            _listeningRemainingSeconds.value = 0
            if (_assistantState.value == AssistantState.LISTENING) {
                _spokenText.value = "Listening timed out after ${timeoutSeconds}s."
                stopListening()
                _assistantState.value = AssistantState.IDLE
            }
        }
    }

    fun cancelTimeoutTimer() {
        timeoutJob?.cancel()
        timeoutJob = null
    }

    fun stopListening() {
        cancelTimeoutTimer()
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            // Ignore
        }
        if (_assistantState.value == AssistantState.LISTENING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (isTtsReady && text.isNotBlank()) {
            _spokenText.value = text
            _assistantState.value = AssistantState.SPEAKING
            val utteranceId = "MAX_UTTERANCE_${System.currentTimeMillis()}"
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            onComplete?.invoke()
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun setState(state: AssistantState) {
        _assistantState.value = state
    }

    fun destroy() {
        cancelTimeoutTimer()
        scope.cancel()
        stopListening()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
