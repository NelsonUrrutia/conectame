package com.nelsonurrutia.conectame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Meeting photos (the "Photo" choice in the meeting form), kept inside the app (the gallery original can be moved or deleted) as a 512px
 * square JPEG per meeting: files/photos/<meeting id>.jpg.
 */
object MeetingPhotos {

    private const val SIZE = 512

    fun file(context: Context, meetingId: String): File =
        File(context.filesDir, "photos").apply { mkdirs() }.let { File(it, "$meetingId.jpg") }

    fun load(path: String?): Bitmap? = path?.let { File(it) }?.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path) }

    fun load(context: Context, meeting: Meeting): Bitmap? =
        if (meeting.hasPhoto) load(file(context, meeting.id).path) else null

    /** Copies a photo picked from the gallery into a temporary square file; returns its path. */
    suspend fun importPicked(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val square = centerSquare(decode(context, uri) ?: return@runCatching null)
            val temp = File(context.cacheDir, "picked_${System.currentTimeMillis()}.jpg")
            temp.outputStream().use { square.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            temp.path
        }.getOrNull()
    }

    /** Makes the chosen photo the meeting's photo; returns the new `photo` stamp for [Meeting]. */
    fun save(context: Context, chosenPath: String, meetingId: String): Long {
        val dest = file(context, meetingId)
        val chosen = File(chosenPath)
        if (chosen.path != dest.path) {
            chosen.copyTo(dest, overwrite = true)
            chosen.delete()
        }
        return System.currentTimeMillis()
    }

    fun delete(context: Context, meetingId: String) {
        file(context, meetingId).delete()
    }

    /** Round version of a photo, for the home-screen button. */
    fun circle(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val canvas = Canvas(out)
        canvas.drawOval(RectF(0f, 0f, size.toFloat(), size.toFloat()), paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, Rect(0, 0, size, size), Rect(0, 0, size, size), paint)
        return out
    }

    private fun decode(context: Context, uri: Uri): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Also turns phone photos upright (they're often stored sideways).
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                val smallest = min(info.size.width, info.size.height)
                decoder.setTargetSampleSize(max(1, smallest / SIZE))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val smallest = min(bounds.outWidth, bounds.outHeight)
            val options = BitmapFactory.Options().apply { inSampleSize = max(1, smallest / SIZE) }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        }

    private fun centerSquare(bitmap: Bitmap): Bitmap {
        val side = min(bitmap.width, bitmap.height)
        val square = Bitmap.createBitmap(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side)
        return Bitmap.createScaledBitmap(square, SIZE, SIZE, true)
    }
}
