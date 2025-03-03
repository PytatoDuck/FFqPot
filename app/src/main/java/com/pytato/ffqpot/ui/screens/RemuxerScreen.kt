package com.pytato.ffqpot.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pytato.ffqpot.ui.components.DropdownMenuPicker
import com.pytato.ffqpot.ui.components.VideoPicker
import com.pytato.ffqpot.utils.VIDEO_FORMATS
import com.pytato.ffqpot.utils.ffmpeg.doRemuxCommand

@Composable
fun RemuxerScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        var isRunning by remember { mutableStateOf(false) }
        var isComplete by remember { mutableStateOf(false) }
        var progress by remember { mutableFloatStateOf(0f) }
        var errMsg by remember { mutableStateOf<String?>(null) }
        var errLogs by remember { mutableStateOf<StringBuilder?>(null) }

        var selectedFormat by remember { mutableStateOf(VIDEO_FORMATS[0]) }
        var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

        DropdownMenuPicker(
            "Select Format",
            VIDEO_FORMATS,
            selectedFormat
        ) { format -> selectedFormat = format }

        VideoPicker() { videoUri ->
            selectedVideoUri = videoUri
            errMsg = null
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            if (isComplete and !isRunning and (errMsg == null)) {
                Text("✅ Remux Complete!")
                Spacer(Modifier.width(16.dp))
            }

            if ((selectedVideoUri != null) and (!isRunning)) {
                Button(
                    onClick = {
                        isRunning = true
                        isComplete = false
                        errMsg = null

                        doRemuxCommand(
                            context = context,
                            selectedVideoUri!!,
                            selectedFormat,
                            onProgress = { progressFloat, msg -> progress = progressFloat },
                            onSuccess = { outputFile ->
                                isRunning = false
                                isComplete = true
                            },
                            onFailure = { msg, err ->
                                isRunning = false
                                errMsg = msg
                                errLogs = err
                            }
                        )
                    },
                ) {
                    Text("Remux", Modifier.padding(horizontal = 8.dp))
                }
            }
        }

        if (isRunning) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!errMsg.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                BasicTextField(
                    value = "$errMsg\n\n\n${errLogs}",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.errorContainer)
                )
            }
        }
    }
}
