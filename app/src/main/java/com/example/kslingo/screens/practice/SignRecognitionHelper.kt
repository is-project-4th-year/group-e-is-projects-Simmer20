package com.example.kslingo.screens.practice

import android.content.Context
import org.json.JSONArray
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class SignRecognitionHelper(context: Context) {
    private var interpreter: Interpreter
    private var signLabels: List<String>

    init {
        // Load TFLite model
        val modelFile = context.assets.openFd("hand_landmark_model.tflite")
        val inputStream = FileInputStream(modelFile.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFile.startOffset
        val declaredLength = modelFile.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        interpreter = Interpreter(modelBuffer)

        // Load sign labels
        val labelsJson = context.assets.open("sign_labels.json").bufferedReader().use { it.readText() }
        signLabels = JSONArray(labelsJson).let { jsonArray ->
            List(jsonArray.length()) { i -> jsonArray.getString(i) }
        }
    }

    fun recognizeSign(landmarks: FloatArray): Pair<String, Float> {
        // Prepare input (21 landmarks x 3 coordinates = 63 features)
        val inputBuffer = ByteBuffer.allocateDirect(63 * 4).apply {
            order(ByteOrder.nativeOrder())
            landmarks.forEach { putFloat(it) }
            rewind()
        }

        // Prepare output
        val outputBuffer = ByteBuffer.allocateDirect(signLabels.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // Parse results
        outputBuffer.rewind()
        val probabilities = FloatArray(signLabels.size) { outputBuffer.float }

        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
        val confidence = probabilities[maxIndex]

        return Pair(signLabels[maxIndex], confidence)
    }

    fun close() {
        interpreter.close()
    }
}