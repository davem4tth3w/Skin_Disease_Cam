package dmi.developments.skin_disease_cam.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import android.util.Log
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import dmi.developments.skin_disease_cam.R
import dmi.developments.skin_disease_cam.data.entity.Result
import dmi.developments.skin_disease_cam.viewmodel.ResultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.createBitmap

@AndroidEntryPoint
class PreviewImageActivity : AppCompatActivity() {

    private val resultViewModel: ResultViewModel by viewModels()

    private val labelDisplayMap = mapOf(
        "atopic_dermatitis" to "Atopic Dermatitis (Eczema)",
        "impetigo" to "Impetigo",
        "prickly_heat" to "Prickly Heat (Miliaria)",
        "sarcoptes_scabiei" to "Sarcoptes Scabiei (Scabies)",
        "tinea_corporis" to "Tinea (Fungal) Infection",
        "warts" to "Warts (Verruca Vulgaris)"
    )


    // class-level cached variables
    private var cachedInterpreter: Interpreter? = null
    private var cachedLabels: List<String> = emptyList()
    private var modelLoaded = false

    private val modelPath = "inception_v3_px224_aug_light_dynamic.tflite"
    private val labelsPath = "labels.txt"
    private val imageSize = 224

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_image)

        initModel()

        val imageView = findViewById<ImageView>(R.id.preview_img)
        val cancelButton = findViewById<ImageButton>(R.id.cancel_btn)
        val confirmButton = findViewById<ImageButton>(R.id.check_btn)

        val imageUriString = intent.getStringExtra("capturedImageUri")
        val imageUri = imageUriString?.toUri()

        var croppedFilePath: String? = null

        imageUri?.let {
            try {
                // Load and correct orientation
                val originalBitmap = loadBitmapWithCorrectOrientation(it)

                // Detect whether the image came from camera
                val isFromCamera = it.toString().startsWith("file://") &&
                        it.toString().contains("IMG_")

                val displayBitmap: Bitmap = if (isFromCamera) {
                    // Auto-crop to focus frame for camera captures
                    val croppedFocusBitmap = cropToFocusFrame(originalBitmap)
                    val tempFile = saveTempBitmap(croppedFocusBitmap)
                    croppedFilePath = tempFile.absolutePath
                    croppedFocusBitmap
                } else {
                    // Manual crop flow (from gallery)
                    croppedFilePath = File(it.path ?: "").absolutePath
                    originalBitmap
                }

                imageView.setImageBitmap(displayBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener { finish() }

        confirmButton.setOnClickListener {
            croppedFilePath?.let {
                performClassificationAndSave(Uri.fromFile(File(it)).toString())
            } ?: Toast.makeText(this, "Cropped image not found", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadBitmapWithCorrectOrientation(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image input stream")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val path = getRealPathFromURI(uri)
        val exif = path?.let { ExifInterface(it) }
        val rotationDegrees = when (exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return rotateBitmapIfNeeded(bitmap, rotationDegrees)
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun cropToFocusFrame(originalBitmap: Bitmap): Bitmap {
        val focusDp = 400f
        val density = resources.displayMetrics.density
        val focusPx = (focusDp * density).toInt()

        // Get the actual size of the captured image
        val (width, height) = originalBitmap.width to originalBitmap.height

        // Calculate the center square crop
        val cropSize = focusPx.coerceAtMost(width.coerceAtMost(height)) // safety

        val left = (width - cropSize) / 2
        val top = (height - cropSize) / 2

        // Ensure bounds are safe
        val safeLeft = left.coerceAtLeast(0)
        val safeTop = top.coerceAtLeast(0)
        val safeSize = cropSize.coerceAtMost(width - safeLeft).coerceAtMost(height - safeTop)

        return try {
            Bitmap.createBitmap(originalBitmap, safeLeft, safeTop, safeSize, safeSize)
        } catch (e: Exception) {
            // Fallback: just resize to 210dp equivalent
            val resized = Bitmap.createScaledBitmap(originalBitmap, focusPx, focusPx, true)
            Bitmap.createBitmap(resized, 0, 0, focusPx, focusPx)
        }
    }


    private fun saveTempBitmap(bitmap: Bitmap): File {
        val file = File(externalCacheDir, "cropped_focus_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return file
    }

    private fun performClassificationAndSave(imageUriString: String) {
        val loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_dialog)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog.show()

        lifecycleScope.launch {
            try {
                // Run image classification in background
                val predictedLabel = withContext(Dispatchers.Default) {
                    classifyImage(imageUriString)
                }

                // Get remedies for the predicted skin disease
                val remedies = RemediesActivity.getRemediesFor(predictedLabel)

                // Save result with remedies included
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val result = Result(
                    imagePath = imageUriString,
                    skindisease = predictedLabel,
                    remedies = remedies,
                    timestamp = timestamp
                )

                // Insert into Room database
                resultViewModel.addResult(result)

                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()

                    // Open ResultsActivity showing saved info
                    val intent = Intent(this@PreviewImageActivity, ResultsActivity::class.java)
                    intent.putExtra("imagePath", imageUriString.toUri().path ?: "")
                    intent.putExtra("skindisease", predictedLabel)
                    intent.putExtra("remedies", remedies)
                    startActivity(intent)
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@PreviewImageActivity,
                        "Classification failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    // Call this once in onCreate() to preload model & labels and log any asset errors
    private fun initModel() {
        try {
            cachedLabels = loadLabels() // may be empty list but won't crash
            cachedInterpreter = try {
                val buffer = loadModelFile(modelPath)
                Interpreter(buffer)
            } catch (e: Exception) {
                Log.e("PreviewImageActivity", "Model load failed: ${e.message}", e)
                null
            }

            modelLoaded = cachedInterpreter != null && cachedLabels.isNotEmpty()
            Log.d("PreviewImageActivity", "Model loaded=$modelLoaded labels=${cachedLabels.size}")
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "initModel error", e)
            modelLoaded = false
        }
    }


    // Improved classifyImage using cached interpreter & labels; with detailed logging
    private fun classifyImage(imageUriString: String): String {
        // Quick checks
        if (cachedInterpreter == null) {
            Log.e("PreviewImageActivity", "Interpreter is null. Did you call initModel()?")
            return "Unknown"
        }

        if (cachedLabels.isEmpty()) {
            Log.e("PreviewImageActivity", "Labels list is empty.")
            return "Unknown"
        }

        try {
            val uri = imageUriString.toUri()
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap == null) {
                Log.e("PreviewImageActivity", "Failed to decode bitmap from uri: $imageUriString")
                return "Unknown"
            }

            if (bitmap.width < imageSize || bitmap.height < imageSize) {
                Log.e("PreviewImageActivity", "Bitmap too small: ${bitmap.width}x${bitmap.height}")
                return "Unknown"
            }

            //Preprocess: contrast + sharpening
            val processedBitmap = sharpenBitmap(enhanceBitmapContrast(bitmap))

            //normalization
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                .build()

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(processedBitmap)
            tensorImage = imageProcessor.process(tensorImage)


            // Prepare output - ensure size matches labels count
            val outputArray = Array(1) { FloatArray(cachedLabels.size) }

            // Run inference
            cachedInterpreter!!.run(tensorImage.buffer, outputArray)

            val scores = outputArray[0]
            if (scores.size != cachedLabels.size) {
                Log.e("PreviewImageActivity", "Score size (${scores.size}) != labels size (${cachedLabels.size})")
                return "Unknown"
            }

            //Apply confidence threshold
            val maxIdx = scores.indices.maxByOrNull { scores[it] } ?: 0
            val confidence = scores[maxIdx]
            val predicted = if (confidence >= 0.8f && maxIdx in cachedLabels.indices) {
                cachedLabels[maxIdx]
            } else {
                "Uncertain"
            }

            Log.d("PreviewImageActivity", "Predicted: $predicted (confidence=$confidence) - scores: ${scores.joinToString(",")}")
            return formatLabel(predicted)

        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "classifyImage exception", e)
            return "Unknown"
        }
    }

    // Safer bitmap loader - handles content:// and file:// URIs
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            // First try content resolver
            contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: run {
                // fallback for file paths
                val path = getRealPathFromURI(uri)
                if (path != null) BitmapFactory.decodeFile(path) else null
            }
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "loadBitmapFromUri error for $uri", e)
            null
        }
    }

    // Enhances contrast and brightness of the image (for monitor photos, low light, etc.)
    private fun enhanceBitmapContrast(bitmap: Bitmap): Bitmap {
        val cm = ColorMatrix()
        val contrast = 1.2f
        val brightness = 10f
        cm.set(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }

        val result =
            createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
        Canvas(result).drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }


    private fun sharpenBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)

        // Sharpen kernel
        val kernel = arrayOf(
            floatArrayOf(0f, -1f, 0f),
            floatArrayOf(-1f, 5f, -1f),
            floatArrayOf(0f, -1f, 0f)
        )

        val pixels = IntArray(width * height)
        val output = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var rSum = 0f
                var gSum = 0f
                var bSum = 0f

                for (ky in -1..1) {
                    for (kx in -1..1) {
                        val pixel = pixels[(x + kx) + (y + ky) * width]
                        val factor = kernel[ky + 1][kx + 1]
                        rSum += ((pixel shr 16) and 0xFF) * factor
                        gSum += ((pixel shr 8) and 0xFF) * factor
                        bSum += (pixel and 0xFF) * factor
                    }
                }

                val r = rSum.coerceIn(0f, 255f).toInt()
                val g = gSum.coerceIn(0f, 255f).toInt()
                val b = bSum.coerceIn(0f, 255f).toInt()
                output[x + y * width] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        result.setPixels(output, 0, width, 0, 0, width, height)
        return result
    }



    // Load labels (unchanged but with extra logs)
    private fun loadLabels(): List<String> {
        val labels = mutableListOf<String>()
        try {
            assets.open(labelsPath).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) labels.add(trimmed)
                }
            }
            Log.d("PreviewImageActivity", "Loaded ${labels.size} labels from assets/$labelsPath")
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "Failed to load labels from assets/$labelsPath", e)
        }
        return labels
    }

    // Load model file (unchanged but with try/catch and logging)
    private fun loadModelFile(modelPathInAssets: String): MappedByteBuffer {
        try {
            val afd = assets.openFd(modelPathInAssets)
            FileInputStream(afd.fileDescriptor).use { inputStream ->
                val fileChannel = inputStream.channel
                val startOffset = afd.startOffset
                val declaredLength = afd.declaredLength
                Log.d("PreviewImageActivity", "Mapping model asset $modelPathInAssets offset=$startOffset len=$declaredLength")
                return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            }
        } catch (e: Exception) {
            Log.e("PreviewImageActivity", "Failed to open model asset $modelPathInAssets", e)
            throw e
        }
    }

    // Better getRealPathFromURI supporting content:// queries, fallback to uri.path
    private fun getRealPathFromURI(uri: Uri): String? {
        try {
            if (uri.scheme == "file") {
                return uri.path
            }
            if (uri.scheme == "content") {
                contentResolver.query(uri, arrayOf(android.provider.MediaStore.Images.Media.DATA), null, null, null)
                    ?.use { cursor ->
                        val idx = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                        if (cursor.moveToFirst()) {
                            return cursor.getString(idx)
                        }
                    }
            }
        } catch (e: Exception) {
            Log.w("PreviewImageActivity", "getRealPathFromURI failed, returning uri.path", e)
        }
        return uri.path
    }

    private fun formatLabel(label: String): String {
        if (label == "Uncertain" || label == "Unknown") return label

        // Return mapped friendly name if it exists
        labelDisplayMap[label]?.let { return it }

        // Otherwise, just format underscores into words (fallback)
        return label.split("_").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
    }

}

