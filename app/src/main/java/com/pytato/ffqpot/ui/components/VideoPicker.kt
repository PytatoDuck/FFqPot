package com.pytato.ffqpot.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pytato.ffqpot.utils.VIDEO_FORMATS
import com.pytato.ffqpot.utils.getFileName
import com.pytato.ffqpot.utils.getFileSize
import com.pytato.ffqpot.utils.getRealPathFromUri
import com.pytato.ffqpot.utils.getVideoDuration
import com.pytato.ffqpot.utils.getVideoThumbnail
import java.util.concurrent.TimeUnit

data class VideoDisplayInfo(
    val thumbnail: Bitmap?,
    val filename: String,
    val size: Long,
    val duration: Long, // In milliseconds
    val resolution: Pair<Int, Int>,
    val path: String,
    val uri: Uri
) {
    val formattedDuration: String
        get() = duration.let {
            val hours = TimeUnit.MILLISECONDS.toHours(it)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(it) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(it) % 60

            if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else String.format("%02d:%02d", minutes, seconds)
        }

    fun formattedFileSize(context: Context): String {
        return Formatter.formatFileSize(context, size) ?: "0"
    }
}

@Composable
private fun VideoPickerButton(
    modifier: Modifier,
    context: Context,
    label: String = "Select Video",
    onVideoSelected: (selectedVideo: VideoDisplayInfo) -> Unit,
) {
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data

            uri?.let { videoUri ->
                val filePath = getRealPathFromUri(context, videoUri)
                val fileExtension =
                    filePath?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()

                // Validate file extension
                if (fileExtension in VIDEO_FORMATS) {
                    filePath?.let {
                        val thumbnail = getVideoThumbnail(context, it)
                        val fileName = getFileName(it)
                        val fileSize = getFileSize(it)
                        val videoDuration = getVideoDuration(context, it)
                        val videoResolution = Pair(thumbnail?.width ?: 0, thumbnail?.height ?: 0)

                        // Callback
                        onVideoSelected(
                            VideoDisplayInfo(
                                thumbnail = thumbnail,
                                filename = fileName,
                                size = fileSize,
                                duration = videoDuration,
                                resolution = videoResolution,
                                path = filePath,
                                uri = videoUri
                            )
                        )
                    }
                } else {
                    Toast.makeText(context, "Unspported video format", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Button(
        onClick = {
            val intent =
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                ).apply { type = "video/*" }
            videoPickerLauncher.launch(intent)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VideoCard(
    context: Context,
    videoInfo: VideoDisplayInfo,
    onVideoSelected: (selectedVideo: VideoDisplayInfo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Filename
            Text(
                text = videoInfo.filename,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        // .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Image(
                        bitmap = videoInfo.thumbnail!!.asImageBitmap(),
                        contentDescription = "Selected video thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    // Metadata
                    DetailItem(
                        label = "Duration",
                        value = videoInfo.formattedDuration
                    )
                    DetailItem(
                        label = "Resolution",
                        value = "${videoInfo.resolution.first} × ${videoInfo.resolution.second}"
                    )
                    DetailItem(
                        label = "Size",
                        value = videoInfo.formattedFileSize(context)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Path
                    Text(
                        text = videoInfo.path,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            VideoPickerButton(
                Modifier,
                context,
                "Choose a Different Video"
            ) { selectedVideo -> onVideoSelected(selectedVideo) }
        }
    }
}

@Composable
fun VideoPicker(onVideoSelected: (selectedVideoUri: Uri) -> Unit) {
    val context = LocalContext.current
    var selectedVideoInfo by remember { mutableStateOf<VideoDisplayInfo?>(null) }

    if (selectedVideoInfo != null) {
        VideoCard(context, selectedVideoInfo!!) { newSelectedVideo ->
            selectedVideoInfo = newSelectedVideo
            onVideoSelected(selectedVideoInfo!!.uri)
        }
    } else {
        VideoPickerButton(Modifier.padding(24.dp), context) { newSelectedVideo ->
            selectedVideoInfo = newSelectedVideo
            onVideoSelected(selectedVideoInfo!!.uri)
        }
    }
}
