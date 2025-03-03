package com.pytato.ffqpot.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log

private const val TAG = "VideoUtils"

fun getVideoThumbnail(context: Context, path: String): Bitmap? {
    val retriever = MediaMetadataRetriever()

    try {
        retriever.setDataSource(context, Uri.parse(path))

        val duration =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                ?: return null
        val frameIntervals = listOf(0.1f, 0.3f, 0.5f, 0.7f, 0.9f)

        for (interval in frameIntervals) {
            val timeUs = (duration * interval * 1000).toLong()
            val bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)

            if (bitmap != null && !isBlackFrame(bitmap)) {
                return bitmap // Return first non-black frame
            }
        }

        return null
    } catch (err: Exception) {
        Log.e(TAG, "Failed to get video thumbnail", err)
        return null
    } finally {
        retriever.release()
    }
}

fun getVideoDuration(context: Context, path: String): Long {
    val retriever = MediaMetadataRetriever()

    try {
        retriever.setDataSource(context, Uri.parse(path))
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        return duration?.toLong() ?: 0L
    } catch (err: Exception) {
        Log.e(TAG, "Failed to get video duration", err)
        return 0L
    } finally {
        retriever.release()
    }
}

private fun isBlackFrame(bitmap: Bitmap, threshold: Int = 10): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    var darkPixelCount = 0
    for (pixel in pixels) {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        val brightness = (r + g + b) / 3 // Average brightness

        if (brightness < threshold) darkPixelCount++
    }

    val darknessRatio = darkPixelCount.toFloat() / pixels.size
    return darknessRatio > 0.85f // If 85%+ of pixels are dark, consider it black
}
