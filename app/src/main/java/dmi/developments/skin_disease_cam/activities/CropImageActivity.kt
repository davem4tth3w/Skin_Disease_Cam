package dmi.developments.skin_disease_cam.activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageView
import dmi.developments.skin_disease_cam.R
import java.io.File
import java.io.FileOutputStream

class CropImageActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private lateinit var cancelButton: Button
    private lateinit var cropButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crop_image)

        cropImageView = findViewById(R.id.cropper)
        cancelButton = findViewById(R.id.cancel_button)
        cropButton = findViewById(R.id.crop_button)

        // Get the image URI from intent
        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = if (imageUriString != null) Uri.parse(imageUriString) else null

        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri)
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener { finish() }

        cropButton.setOnClickListener {
            try {
                val croppedImage = cropImageView.croppedImage
                if (croppedImage != null) {
                    // Save the cropped bitmap to cache directory
                    val file = File(externalCacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { out ->
                        croppedImage.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }

                    // Pass cropped image URI to PreviewImageActivity
                    val intent = Intent(this, PreviewImageActivity::class.java)
                    intent.putExtra("capturedImageUri", Uri.fromFile(file).toString())
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Cropping failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
