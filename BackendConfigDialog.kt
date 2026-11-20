package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.BackendStatus
import com.example.ui.theme.JdmcBrushedSilver
import com.example.ui.theme.JdmcCarbonElevated
import com.example.ui.theme.JdmcCarbonSurface
import com.example.ui.theme.JdmcGunmetal
import com.example.ui.theme.JdmcLightYellow
import com.example.ui.theme.JdmcMetallicSilver
import com.example.ui.theme.JdmcObsidianBlack
import com.example.ui.theme.JdmcSteelOutline
import com.example.util.VideoUrlHelper

@Composable
fun BackendConfigDialog(
    currentUrl: String,
    backendStatus: BackendStatus,
    onSaveUrl: (String) -> Unit,
    onRetest: () -> Unit,
    onDismiss: () -> Unit
) {
    var draftUrl by remember(currentUrl) { mutableStateOf(currentUrl) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JdmcCarbonSurface,
        titleContentColor = JdmcLightYellow,
        textContentColor = JdmcBrushedSilver,
        icon = {
            Icon(
                imageVector = Icons.Filled.Dns,
                contentDescription = null,
                tint = JdmcLightYellow
            )
        },
        title = {
            Text(
                text = "JDMC Backend API Configuration",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Shared access code validation, rate limiting, expiring sessions, Jabatan hierarchy sync, and admin moderation require a live HTTPS JDMC backend server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = JdmcBrushedSilver
                )

                Surface(
                    color = JdmcCarbonElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CURRENT STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = JdmcGunmetal
                        )
                        Text(
                            text = when (backendStatus) {
                                is BackendStatus.Unconfigured -> "Not configured (JDMC_API_BASE_URL is empty)"
                                is BackendStatus.Checking -> "Checking ${backendStatus.baseUrl}..."
                                is BackendStatus.Connected -> "Connected to ${backendStatus.baseUrl}"
                                is BackendStatus.Unreachable -> "Unreachable: ${backendStatus.reason}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = JdmcMetallicSilver
                        )
                    }
                }

                OutlinedTextField(
                    value = draftUrl,
                    onValueChange = {
                        draftUrl = it
                        validationError = null
                    },
                    label = { Text("Backend HTTPS Base URL") },
                    placeholder = { Text("https://api.jdmc-clippers.example.com/") },
                    singleLine = true,
                    isError = validationError != null,
                    supportingText = validationError?.let { msg -> { Text(msg) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backend_url_input")
                )

                Text(
                    text = "API routes: /api/v1/health, /api/v1/jabatan, /api/v1/streamers, /api/v1/clips/published, /api/v1/access/verify, /api/v1/clips, /api/v1/admin/*",
                    style = MaterialTheme.typography.labelSmall,
                    color = JdmcGunmetal
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onRetest,
                    border = BorderStroke(1.dp, JdmcSteelOutline)
                ) {
                    Text("Test", color = JdmcMetallicSilver)
                }
                Button(
                    onClick = {
                        val trimmed = draftUrl.trim()
                        if (trimmed.isNotEmpty() && !VideoUrlHelper.isValidHttpsUrl(trimmed)) {
                            validationError = "Enter a valid https:// URL or leave blank to clear."
                        } else {
                            onSaveUrl(trimmed)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JdmcLightYellow,
                        contentColor = JdmcObsidianBlack
                    ),
                    modifier = Modifier.testTag("save_backend_url_button")
                ) {
                    Text("Save Endpoint")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = JdmcGunmetal)
            }
        }
    )
}
