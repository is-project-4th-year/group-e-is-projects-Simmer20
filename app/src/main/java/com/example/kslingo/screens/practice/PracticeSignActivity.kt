package com.example.kslingo.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kslingo.R
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.framework.image.BitmapImageBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PracticeSignActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var targetSignText: TextView
    private lateinit var signDescription: TextView
    private lateinit var detectedSignText: TextView
    private lateinit var confidenceBar: ProgressBar
    private lateinit var confidenceText: TextView
    private lateinit var nextButton: Button
    private lateinit var toolbar: Toolbar

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var signRecognitionHelper: SignRecognitionHelper
    private lateinit var handLandmarker: HandLandmarker

    private val signList = listOf("A", "B", "C", "D", "E", "F", "G", "M", "N", "S") // Your signs
    private var currentSignIndex = 0
    private var isSignCorrect = false

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice_sign)

        initViews()
        setupToolbar()
        setupSignRecognition()
        loadNextSign()

        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }

        nextButton.setOnClickListener {
            currentSignIndex++
            if (currentSignIndex < signList.size) {
                loadNextSign()
            } else {
                // Practice session complete
                finish()
            }
        }
    }

    private fun initViews() {
        previewView = findViewById(R.id.previewView)
        targetSignText = findViewById(R.id.targetSignText)
        signDescription = findViewById(R.id.signDescription)
        detectedSignText = findViewById(R.id.detectedSignText)
        confidenceBar = findViewById(R.id.confidenceBar)
        confidenceText = findViewById(R.id.confidenceText)
        nextButton = findViewById(R.id.nextButton)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSignRecognition() {
        signRecognitionHelper = SignRecognitionHelper(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
            .setNumHands(1)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                processHandLandmarks(result)
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(this, options)
    }

    private fun loadNextSign() {
        val currentSign = signList[currentSignIndex]
        targetSignText.text = currentSign
        signDescription.text = getSignDescription(currentSign)
        isSignCorrect = false
        nextButton.isEnabled = false
    }

    private fun getSignDescription(sign: String): String {
        // Add descriptions for each sign
        return when (sign) {
            "A" -> "Make a fist with thumb on the side"
            "B" -> "Fingers extended together, palm forward, thumb across palm"
            "C" -> "Curve your hand to form a C shape"
            // Add more descriptions
            else -> "Perform the sign as shown"
        }
    }

    @ExperimentalGetImage
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
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
            val landmarkArray = FloatArray(63)
            landmarks.forEachIndexed { index, landmark ->
                landmarkArray[index * 3] = landmark.x()
                landmarkArray[index * 3 + 1] = landmark.y()
                landmarkArray[index * 3 + 2] = landmark.z()
            }

            val (predictedSign, confidence) = signRecognitionHelper.recognizeSign(landmarkArray)
            val confidencePercent = (confidence * 100).toInt()

            runOnUiThread {
                detectedSignText.text = predictedSign
                confidenceBar.progress = confidencePercent
                confidenceText.text = "Confidence: $confidencePercent%"

                // Check if correct sign detected with good confidence
                val targetSign = signList[currentSignIndex]
                if (predictedSign.equals(targetSign, ignoreCase = true) && confidence > 0.8) {
                    if (!isSignCorrect) {
                        isSignCorrect = true
                        detectedSignText.setTextColor(getColor(R.color.green))
                        nextButton.isEnabled = true
                    }
                } else {
                    detectedSignText.setTextColor(getColor(R.color.light_green))
                }
            }
        } else {
            runOnUiThread {
                detectedSignText.text = "No hand detected"
                confidenceBar.progress = 0
                confidenceText.text = "Confidence: 0%"
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
