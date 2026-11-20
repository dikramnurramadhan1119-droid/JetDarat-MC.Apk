package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.JdmcAmberAlert
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGoldBorder
import com.example.ui.theme.JdmcGoldContainer
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.AiUiState

private data class JdmcQuickPrompt(
    val label: String,
    val promptText: String
)

private val JDMC_QUICK_PROMPTS = listOf(
    JdmcQuickPrompt(
        label = "Convoy Dispatch",
        promptText = "Buatkan pengumuman touring malam JDMC RX-King 2-Tak di Los Santos untuk Satu Mimpi RP."
    ),
    JdmcQuickPrompt(
        label = "Los Santos Route",
        promptText = "Suggest a scenic midnight RX-King convoy route across Los Santos and Blaine County with 2 pit stops."
    ),
    JdmcQuickPrompt(
        label = "Member Bio",
        promptText = "Write a short, badass JDMC member biography for an active Satu Mimpi RP rider."
    ),
    JdmcQuickPrompt(
        label = "RX-King Tips",
        promptText = "Give a quick pre-ride inspection checklist for classic RX-King 2-Tak motorcycles before a long night run."
    ),
    JdmcQuickPrompt(
        label = "Caption Photo",
        promptText = "Analyze my attached photo and write a punchy title + caption for JDMC Media Vault."
    )
)

@Composable
fun JdmcAiAssistantScreen(
    uiState: AiUiState,
    messages: List<AiChatMessage>,
    selectedImageUri: Uri?,
    onSelectImage: (Uri?) -> Unit,
    onSendPrompt: (String) -> Unit,
    onResetChat: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    var promptText by remember { mutableStateOf("") }
    var copiedMessageId by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            onSelectImage(uri)
        }
    }

    LaunchedEffect(messages.size, uiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val isBusy = uiState is AiUiState.Loading || uiState is AiUiState.Streaming

    Scaffold(
        containerColor = JdmcObsidianBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(color = JdmcCarbonSurface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("ai_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to HQ",
                                tint = JdmcLightYellow
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = JdmcLightYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "JDMC ROAD AI",
                                style = MaterialTheme.typography.titleMedium,
                                color = JdmcLightYellow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gemini 3.8 Flash • Brotherhood Concierge",
                                style = MaterialTheme.typography.labelSmall,
                                color = JdmcGunmetal
                            )
                        }
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onResetChat,
                            enabled = !isBusy,
                            modifier = Modifier.testTag("ai_reset_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Reset conversation",
                                tint = JdmcMetallicSilver
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("jdmc_ai_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(JDMC_QUICK_PROMPTS) { item ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (!isBusy) {
                                    if (item.label == "Caption Photo" && selectedImageUri == null) {
                                        promptText = item.promptText
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    } else {
                                        onSendPrompt(item.promptText)
                                    }
                                }
                            },
                            label = { Text(item.label) },
                            colors = jdmcFilterChipColors()
                        )
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Surface(
                                color = JdmcCarbonSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, JdmcGoldBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = JdmcLightYellow
                                        )
                                        Text(
                                            text = "Satu Jalan, Satu Mimpi, Satu Keluarga.",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = JdmcLightYellow
                                        )
                                    }
                                    Text(
                                        text = "Ask Gemini for convoy dispatches, Satu Mimpi RP storylines, RX-King 2-Tak checklists, member bios, or attach a bike/convoy photo from your gallery for instant Media Vault captions.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JdmcBrushedSilver
                                    )
                                }
                            }
                        }
                    } else {
                        items(messages, key = { it.id }) { msg ->
                            AiMessageBubble(
                                message = msg,
                                isCopied = copiedMessageId == msg.id,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(msg.text))
                                    copiedMessageId = msg.id
                                }
                            )
                        }
                    }

                    if (uiState is AiUiState.Loading) {
                        item {
                            Surface(
                                color = JdmcCarbonElevated,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, JdmcSteelOutline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = JdmcLightYellow
                                    )
                                    Text(
                                        text = "JDMC Road AI is thinking...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JdmcMetallicSilver
                                    )
                                }
                            }
                        }
                    }

                    if (uiState is AiUiState.Error) {
                        item {
                            Surface(
                                color = JdmcCarbonElevated,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, JdmcAmberAlert),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ai_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "AI Error",
                                        tint = JdmcAmberAlert
                                    )
                                    Text(
                                        text = uiState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JdmcMetallicSilver,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = JdmcCarbonSurface,
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedImageUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(JdmcCarbonElevated, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Attached gallery photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Text(
                                    text = "Photo attached for Gemini multimodal analysis",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JdmcLightYellow,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onSelectImage(null) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove attached photo",
                                        tint = JdmcMetallicSilver
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = !isBusy,
                                border = BorderStroke(1.dp, JdmcGoldBorder),
                                contentPadding = PaddingValues(12.dp),
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("ai_attach_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Attach photo from gallery",
                                    tint = JdmcLightYellow
                                )
                            }

                            OutlinedTextField(
                                value = promptText,
                                onValueChange = { promptText = it },
                                placeholder = { Text("Ask JDMC Road AI...") },
                                maxLines = 3,
                                enabled = !isBusy,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_prompt_input")
                            )

                            Button(
                                onClick = {
                                    val outgoing = promptText
                                    promptText = ""
                                    onSendPrompt(outgoing)
                                },
                                enabled = !isBusy && (promptText.isNotBlank() || selectedImageUri != null),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JdmcLightYellow,
                                    contentColor = JdmcObsidianBlack
                                ),
                                contentPadding = PaddingValues(12.dp),
                                modifier = Modifier
                                    .size(52.dp)
                                    .testTag("ai_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send prompt"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiChatMessage,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isUser) JdmcGoldContainer else JdmcCarbonSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (message.isUser) JdmcGoldBorder else JdmcSteelOutline),
            modifier = Modifier.widthIn(max = 580.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (message.isUser) "YOU" else "JDMC ROAD AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcLightYellow,
                    fontWeight = FontWeight.Bold
                )
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = message.imageUri,
                        contentDescription = "Prompt image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JdmcMetallicSilver
                )
                if (!message.isUser) {
                    TextButton(
                        onClick = onCopy,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy response",
                            tint = JdmcLightYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied" else "Copy",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcLightYellow
                        )
                    }
                }
            }
        }
    }
}
