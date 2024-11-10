package com.ss.nativelib

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.R.color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import androidx.core.graphics.get
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer


class DepthAnything(modelName: String) {

    private val ortEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession
    private val inputName: String
    private val inputDim: Int
    private val outputDim: Int
    private val rotateTransform = Matrix().apply { postRotate(90f) }

    init {
        val modelBytes = File(modelName).readBytes()
        ortSession = ortEnvironment.createSession(modelBytes)
        inputName = ortSession.inputNames.iterator().next()

        when {
            modelName.contains("_256") -> {
                inputDim = 256
                outputDim = 252
            }
            modelName.contains("_512") -> {
                inputDim = 512
                outputDim = 504
            }
            else -> throw IllegalArgumentException("Unsupported model size")
        }
    }

    private fun byteArrayToBitmap(imageData: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    }

    fun predict(imageData: ByteArray): Boolean {
        val inputImage = byteArrayToBitmap(imageData)
        val resizedImage = Bitmap.createScaledBitmap(inputImage, inputDim, inputDim, true)
        val imagePixels = convertBitmapToByteBuffer(resizedImage)

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            imagePixels,
            longArrayOf(1, inputDim.toLong(), inputDim.toLong(), 3),
            OnnxJavaType.UINT8
        )

        val outputs = ortSession.run(mapOf(inputName to inputTensor))
        val outputTensor = outputs[0] as OnnxTensor

        var depthMap = Bitmap.createBitmap(outputDim, outputDim, Bitmap.Config.ALPHA_8)
        depthMap.copyPixelsFromBuffer(outputTensor.byteBuffer)
        depthMap = Bitmap.createBitmap(depthMap, 0, 0, outputDim, outputDim, rotateTransform, false)
        depthMap = Bitmap.createScaledBitmap(depthMap, inputImage.width, inputImage.height, true)
        depthMap = flipBitmapHorizontally(depthMap)

        saveAlpha8Bitmap(depthMap, "/storage/emulated/0/Android/data/com.naitan.Parallax/files/Documents/depthMap.png")
        return true
    }

    private fun flipBitmapHorizontally(originalBitmap: Bitmap): Bitmap {
        // Create a matrix for the transformation
        val matrix = Matrix().apply {
            postScale(-1f, 1f) // Flip horizontally
        }

        // Create and return the new flipped bitmap
        return Bitmap.createBitmap(
            originalBitmap,
            0, 0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
    }

    private fun saveAlpha8Bitmap(alpha8Bitmap: Bitmap, filePath: String) {
        val width = alpha8Bitmap.width
        val height = alpha8Bitmap.height
        val rgbBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                val alpha = alpha8Bitmap.getPixel(x, y)
                val color = Color.argb((alpha shr 24) and 0xFF, (alpha shr 16) and 0xFF,
                    (alpha shr 8) and 0xFF, alpha and 0xFF)
                rgbBitmap.setPixel(x, y, color)
            }
        }

        val file = File(filePath)
        val outputStream = FileOutputStream(file)
        rgbBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocate(1 * bitmap.width * bitmap.height * 3)
        imgData.rewind()
        for (i in 0 until bitmap.width) {
            for (j in 0 until bitmap.height) {
                imgData.put(Color.red(bitmap[i, j]).toByte())
                imgData.put(Color.blue(bitmap[i, j]).toByte())
                imgData.put(Color.green(bitmap[i, j]).toByte())
            }
        }
        imgData.rewind()
        return imgData
    }
}
