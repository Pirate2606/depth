package com.ss.nativelib

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.runBlocking

class NativeLib(modelName: String) {
    private val depthAnything = DepthAnything(modelName)

    fun predictDepth(): String {
//        val (depthImage, _) = depthAnything.predict(inputImage)
        return "Hello"
    }

    companion object {
        private var instance: NativeLib? = null

        @JvmStatic
        fun getInstance(modelName: String): NativeLib {
            if (instance == null) {
                instance = NativeLib(modelName)
            }
            return instance!!
        }
    }
}
