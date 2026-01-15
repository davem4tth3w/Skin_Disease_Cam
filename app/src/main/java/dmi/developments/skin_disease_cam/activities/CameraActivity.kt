package dmi.developments.skin_disease_cam.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dmi.developments.skin_disease_cam.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : BottomNavActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var uploadButton: FloatingActionButton
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // Gallery picker launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d("CameraActivity", "Selected image: $uri")
                // After selecting image, open CropImageActivity
                val intent = Intent(this, CropImageActivity::class.java)
                intent.putExtra("imageUri", uri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_camera)
        setupBottomNav(R.id.nav_camera)

        previewView = findViewById(R.id.previewView)
        captureButton = findViewById(R.id.btn_capture)
        uploadButton = findViewById(R.id.upload_img)

        // Request camera permission
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureButton.setOnClickListener { takePhoto() }
        uploadButton.setOnClickListener { openGallery() }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun openGallery() {
        try {
            pickImageLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e("CameraActivity", "Failed to open gallery", e)
            Toast.makeText(this, "Unable to open gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) startCamera()
            else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Camera binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            externalCacheDir,
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Log.d("CameraActivity", "Photo saved: $savedUri")

                    val intent = Intent(this@CameraActivity, PreviewImageActivity::class.java)
                    intent.putExtra("capturedImageUri", savedUri.toString())
                    startActivity(intent)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(applicationContext, "Capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
