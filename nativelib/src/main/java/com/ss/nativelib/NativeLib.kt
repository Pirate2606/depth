package com.ss.nativelib


class NativeLib(modelName: String) {
    private val depthAnything = DepthAnything(modelName)

    fun predictDepth(inputImage: ByteArray): Boolean {
        val result = depthAnything.predict(inputImage)
        return result
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
