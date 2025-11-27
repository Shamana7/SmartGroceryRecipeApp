package com.shamana.smartgroceryapp.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class OCRResult(
    val text: String,
    val averageConfidence: Float
)

object OCRRepository {

    suspend fun extractTextFromImage(imageFile: java.io.File): OCRResult {
        val originalBitmap: Bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

        val rotated = rotateIfNeeded(originalBitmap, imageFile)
        val gray = toGrayScale(rotated)
        val processed = enhanceContrast(gray)

        val inputImage = InputImage.fromBitmap(processed, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val result = recognizer.process(inputImage).await()

        val confidences = result.textBlocks.flatMap { block ->
            block.lines.mapNotNull { it.confidence }
        }
        val avgConfidence = if (confidences.isNotEmpty()) {
            confidences.average().toFloat()
        } else 0f

        return OCRResult(result.text, avgConfidence)
    }

    private fun rotateIfNeeded(bitmap: Bitmap, imageFile: java.io.File): Bitmap {
        val exif = ExifInterface(imageFile.absolutePath)
        val rotation = when (
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotation == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun toGrayScale(src: Bitmap): Bitmap {
        val config = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(src.width, src.height, config)
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return bmp
    }

    private fun enhanceContrast(src: Bitmap, contrast: Float = 1.2f): Bitmap {
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(src.width, src.height, config)
        val canvas = Canvas(bmp)

        val cm = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, 0f,
                    0f, contrast, 0f, 0f, 0f,
                    0f, 0f, contrast, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }

        canvas.drawBitmap(src, 0f, 0f, paint)
        return bmp
    }
}
