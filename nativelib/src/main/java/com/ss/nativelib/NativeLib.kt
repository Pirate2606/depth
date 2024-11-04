package com.ss.nativelib

import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NativeLib(context: Context, modelName: String) {
    private val depthAnything = DepthAnything(context, modelName)

    suspend fun predictDepth(inputImage: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val (depthImage, _) = depthAnything.predict(inputImage)
        depthImage
    }
}