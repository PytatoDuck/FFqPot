package com.pytato.ffqpot.utils.ffmpeg

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.pytato.ffqpot.R
import com.pytato.ffqpot.utils.VIDEO_FORMATS
import com.pytato.ffqpot.utils.getFileNameWithoutExtension
import com.pytato.ffqpot.utils.getRealPathFromUri
import com.pytato.ffqpot.utils.getVideoDuration
import java.io.File

fun doRemuxCommand(
    context: Context,
    inputUri: Uri,
    outputFileType: String = VIDEO_FORMATS.first(),
    onProgress: (progress: Float, msg: String?) -> Unit,
    onSuccess: (convertedFile: File) -> Unit,
    onFailure: (msg: String, error: StringBuilder?) -> Unit
) {
    require(outputFileType in VIDEO_FORMATS) { "Invalid output file type: $outputFileType" }

    val inputPath = getRealPathFromUri(context, inputUri)
    val inputVideoDuration = inputPath?.let { getVideoDuration(context, it) }
    val inputFileName = inputPath?.let { getFileNameWithoutExtension(it) }

    val appName = context.getString(R.string.app_name)
    val movieDir =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), appName)

    if (!movieDir.exists()) movieDir.mkdirs()

    val outputFileName = "$inputFileName.${outputFileType.lowercase()}"
    val outputFile = File(movieDir, outputFileName)

    val ffmpegCommand =
        "-hide_banner -i \"$inputPath\" -c copy \"${outputFile.absolutePath}\" -stats"

    val persistentLogs = StringBuilder()

    FFmpegKit.executeAsync(ffmpegCommand, { session ->
        val returnCode = session.returnCode
        when {
            ReturnCode.isSuccess(returnCode) -> {
                MediaScannerConnection.scanFile(
                    context, arrayOf(outputFile.absolutePath), arrayOf("video/$outputFileType")
                ) { _, uri -> Log.d("FFmpeg", "File Scanned: $uri") }

                onSuccess(outputFile)
            }

            ReturnCode.isCancel(returnCode) -> {
                onFailure("Remux execution canceled", null)
            }

            else -> {
                onFailure("Remux execution failed", persistentLogs)
            }
        }
    }, { log ->
        val logMessage = log.message.trim()

        if (!logMessage.startsWith("frame=")) persistentLogs.appendLine(logMessage)

        if (logMessage.contains("time=")) {
            val timeString = logMessage.substringAfter("time=").substringBefore(" ").trim()
            val processedTime = parseFFmpegTimeToSeconds(timeString)
            val totalDuration = inputVideoDuration?.div(1000f) ?: 1f

            val progress = (processedTime / totalDuration).coerceIn(0f, 1f)
            onProgress(progress, null)
        }
    }) { stats -> }
}

// 🔥 Helper Function to Parse FFmpeg Time Format (HH:MM:SS.xx) to Seconds
private fun parseFFmpegTimeToSeconds(timeStr: String): Float {
    return try {
        val parts = timeStr.split(":")
        val hours = parts.getOrNull(0)?.toFloatOrNull() ?: 0f
        val minutes = parts.getOrNull(1)?.toFloatOrNull() ?: 0f
        val seconds = parts.getOrNull(2)?.toFloatOrNull() ?: 0f
        (hours * 3600) + (minutes * 60) + seconds
    } catch (e: Exception) {
        0f
    }
}