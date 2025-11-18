package com.example.kslingo.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kslingo.R
import com.example.kslingo.screens.practice.SignRecognitionHelper
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SignRecognitionActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultTextView: TextView
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var signRecognitionHelper: SignRecognitionHelper
    private lateinit var handLandmarker: HandLandmarker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_recognition)

        previewView = findViewById(R.id.previewView)
        resultTextView = findViewById(R.id.resultTextView)

        signRecognitionHelper = SignRecognitionHelper(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize MediaPipe Hand Landmarker
        setupHandLandmarker()

        // Request camera permission and start
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupHandLandmarker() {
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
            .setNumHands(1)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, image ->
                processHandLandmarks(result)
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(this, options)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    @ExperimentalGetImage
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val bitmap = imageProxy.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()

            handLandmarker.detectAsync(mpImage, imageProxy.imageInfo.timestamp)
        }
        imageProxy.close()
    }

    private fun processHandLandmarks(result: HandLandmarkerResult) {
        if (result.landmarks().isNotEmpty()) {
            val landmarks = result.landmarks()[0]

            // Convert to flat array (21 landmarks x 3 cords = 63 values)
            val landmarkArray = FloatArray(63)
            landmarks.forEachIndexed { index, landmark ->
                landmarkArray[index * 3] = landmark.x()
                landmarkArray[index * 3 + 1] = landmark.y()
                landmarkArray[index * 3 + 2] = landmark.z()
            }

            // Run inference
            val (predictedSign, confidence) = signRecognitionHelper.recognizeSign(landmarkArray)

            // Update UI
            runOnUiThread {
                resultTextView.text = "Sign: $predictedSign (${(confidence * 100).toInt()}%)"
            }
        } else {
            runOnUiThread {
                resultTextView.text = "No hand detected"
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        signRecognitionHelper.close()
        handLandmarker.close()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
}