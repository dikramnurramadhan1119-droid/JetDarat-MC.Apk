package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID

private const val TAG = "JdmcAiViewModel"
private const val GEMINI_MODEL_NAME = "gemini-3.8-flash"
private const val AI_TIMEOUT_MS = 60_000L
private const val MAX_IMAGE_DIM_PX = 1024

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val text: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val imageUri: String? = null
)

class JdmcAiViewModel(application: Application) : AndroidViewModel(application) {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = GEMINI_MODEL_NAME,
            systemInstruction = content {
                text(
                    "You are the official JDMC Road Concierge AI for Jet Darat Motorcycle Club (JDMC) " +
                        "in Los Santos on Satu Mimpi RP (FiveM). Club motto: 'Satu Jalan, Satu Mimpi, Satu Keluarga.' " +
                        "Key riders include Gashima (President), Melody (Vice President), Crystal Oda Megumi " +
                        "(The Mother of JDMC), Joko Sancez (Charter Veteran), and Duo Tachibana (Tashima & Gonzales), " +
                        "riding classic RX-King 2-Tak motorcycles. " +
                        "Help riders draft announcements, plan Los Santos / Blaine County convoy routes, " +
                        "write member bios, suggest Media Vault photo captions, and give RX-King 2-Tak garage tips. " +
                        "Match the user's language (Indonesian or English) naturally, concisely, and with respectful biker brotherhood energy."
                )
            }
        )
    }

    private var chatSession: Chat? = null

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    fun setSelectedImage(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun clearConversation() {
        chatSession = null
        _messages.value = emptyList()
        _selectedImageUri.value = null
        _uiState.value = AiUiState.Idle
    }

    fun sendPrompt(rawPrompt: String) {
        val attachedUri = _selectedImageUri.value
        val prompt = rawPrompt.trim().ifBlank {
            if (attachedUri != null) {
                "Analyze this photo and write a cool JDMC Media Vault title and caption."
            } else {
                ""
            }
        }
        if (prompt.isBlank()) return

        val userMessage = AiChatMessage(
            isUser = true,
            text = prompt,
            imageUri = attachedUri?.toString()
        )
        _messages.update { it + userMessage }
        _selectedImageUri.value = null
        _uiState.value = AiUiState.Loading

        viewModelScope.launch {
            val replyId = UUID.randomUUID().toString()
            val accumulated = StringBuilder()
            try {
                val bitmap = if (attachedUri != null) {
                    decodeScaledBitmap(attachedUri)
                } else {
                    null
                }

                val inputContent = content {
                    if (bitmap != null) {
                        image(bitmap)
                    }
                    text(prompt)
                }

                val activeChat = chatSession ?: model.startChat().also { chatSession = it }

                withTimeout(AI_TIMEOUT_MS) {
                    activeChat.sendMessageStream(inputContent).collect { chunk ->
                        val chunkText = chunk.text.orEmpty()
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            val partial = accumulated.toString()
                            _uiState.value = AiUiState.Streaming(partial)
                            _messages.update { current ->
                                val existingIdx = current.indexOfFirst { it.id == replyId }
                                if (existingIdx >= 0) {
                                    current.toMutableList().apply {
                                        this[existingIdx] = this[existingIdx].copy(text = partial)
                                    }
                                } else {
                                    current + AiChatMessage(id = replyId, isUser = false, text = partial)
                                }
                            }
                        }
                    }
                }

                val finalText = accumulated.toString().trim()
                if (finalText.isNotEmpty()) {
                    _uiState.value = AiUiState.Success(finalText)
                } else {
                    _uiState.value = AiUiState.Error("Gemini returned an empty response. Please try again.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini streaming timed out after ${AI_TIMEOUT_MS}ms", e)
                _uiState.value = AiUiState.Error(
                    "Request timed out after 60 seconds. Check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase AI Logic generation failed", e)
                _uiState.value = AiUiState.Error(
                    e.localizedMessage ?: "Gemini AI request failed. Please try again."
                )
            }
        }
    }

    private suspend fun decodeScaledBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = getApplication<Application>().contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_IMAGE_DIM_PX, MAX_IMAGE_DIM_PX)
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        }.onFailure { err ->
            Log.e(TAG, "Failed to decode attached image for Gemini multimodal request", err)
        }.getOrNull()
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
