package com.example.media

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

enum class PhotoFilter(val bengaliName: String) {
    ORIGINAL("স্বাভাবিক"),
    GRAYSCALE("সাদা-কালো"),
    SEPIA("সেপিয়া"),
    CYBERPUNK("সাইবার ব্লু"),
    WARM("উষ্ণ আভা"),
    INVERT("নেগেটিভ"),
    CONTRAST("হাই কন্ট্রাস্ট"),
    VINTAGE("ভিন্টেজ")
}

object ImageProcessor {

    suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1200): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val original = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (original == null) return@withContext null

                // Scale down if too large to avoid OOM
                if (original.width > maxDimension || original.height > maxDimension) {
                    val scale = maxDimension.toFloat() / maxOf(original.width, original.height)
                    val newW = (original.width * scale).toInt()
                    val newH = (original.height * scale).toInt()
                    Bitmap.createScaledBitmap(original, newW, newH, true)
                } else {
                    original
                }
            } catch (e: Exception) {
                null
            }
        }

    fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun cropToSquare(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        return Bitmap.createBitmap(source, x, y, size, size)
    }

    fun cropTo4x3(source: Bitmap): Bitmap {
        val targetRatio = 4f / 3f
        val currentRatio = source.width.toFloat() / source.height.toFloat()
        return if (currentRatio > targetRatio) {
            val newWidth = (source.height * targetRatio).toInt()
            val x = (source.width - newWidth) / 2
            Bitmap.createBitmap(source, x, 0, newWidth, source.height)
        } else {
            val newHeight = (source.width / targetRatio).toInt()
            val y = (source.height - newHeight) / 2
            Bitmap.createBitmap(source, 0, y, source.width, newHeight)
        }
    }

    fun applyFilter(source: Bitmap, filter: PhotoFilter): Bitmap {
        if (filter == PhotoFilter.ORIGINAL) return source

        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colorMatrix = ColorMatrix().apply {
            when (filter) {
                PhotoFilter.GRAYSCALE -> setSaturation(0f)
                PhotoFilter.SEPIA -> {
                    setSaturation(0f)
                    val sepiaMatrix = ColorMatrix(
                        floatArrayOf(
                            1.2f, 0f, 0f, 0f, 20f,
                            0f, 1.0f, 0f, 0f, 10f,
                            0f, 0f, 0.8f, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(sepiaMatrix)
                }
                PhotoFilter.CYBERPUNK -> {
                    val cyberMatrix = ColorMatrix(
                        floatArrayOf(
                            1.3f, 0f, 0.2f, 0f, 10f,
                            0f, 0.8f, 0.4f, 0f, -10f,
                            0.2f, 0.4f, 1.6f, 0f, 40f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(cyberMatrix)
                }
                PhotoFilter.WARM -> {
                    val warmMatrix = ColorMatrix(
                        floatArrayOf(
                            1.25f, 0f, 0f, 0f, 25f,
                            0f, 1.05f, 0f, 0f, 10f,
                            0f, 0f, 0.75f, 0f, -20f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(warmMatrix)
                }
                PhotoFilter.INVERT -> {
                    val invertMatrix = ColorMatrix(
                        floatArrayOf(
                            -1f, 0f, 0f, 0f, 255f,
                            0f, -1f, 0f, 0f, 255f,
                            0f, 0f, -1f, 0f, 255f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(invertMatrix)
                }
                PhotoFilter.CONTRAST -> {
                    val scale = 1.4f
                    val translate = (-0.5f * scale + 0.5f) * 255f
                    val contrastMatrix = ColorMatrix(
                        floatArrayOf(
                            scale, 0f, 0f, 0f, translate,
                            0f, scale, 0f, 0f, translate,
                            0f, 0f, scale, 0f, translate,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(contrastMatrix)
                }
                PhotoFilter.VINTAGE -> {
                    setSaturation(0.6f)
                    val vintageMatrix = ColorMatrix(
                        floatArrayOf(
                            1.1f, 0.1f, 0.1f, 0f, 10f,
                            0.1f, 0.9f, 0.1f, 0f, 5f,
                            0.1f, 0.1f, 0.7f, 0f, -15f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(vintageMatrix)
                }
                PhotoFilter.ORIGINAL -> {}
            }
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return result
    }

    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "edited_images").apply { mkdirs() }
            val file = File(cacheDir, "by_kaka_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            // Use FileProvider or Uri directly for internal share
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }

    fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
            val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "KAKA AI ফটো এডিটর দিয়ে সম্পাদিত ছবি")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(Intent.createChooser(intent, "ছবি শেয়ার করুন").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Exception) {}
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
