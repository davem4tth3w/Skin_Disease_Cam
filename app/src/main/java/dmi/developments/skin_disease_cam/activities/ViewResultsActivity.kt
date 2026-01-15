package dmi.developments.skin_disease_cam.activities

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import dmi.developments.skin_disease_cam.R

class ViewResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_record)

        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val imageView = findViewById<ImageView>(R.id.view_img)
        val diseaseName = findViewById<TextView>(R.id.skin_disease_name)
        val remediesText = findViewById<TextView>(R.id.remedies)

        // Get passed data
        val imagePath = intent.getStringExtra("imagePath")
        val skindisease = intent.getStringExtra("skindisease")
        val remedies = intent.getStringExtra("remedies")

        // Display values
        diseaseName.text = skindisease ?: "Unknown Disease"
        remediesText.text = remedies ?: "No remedies available"

        // Display values
        diseaseName.text = skindisease
        remediesText.text = HtmlCompat.fromHtml(remedies, HtmlCompat.FROM_HTML_MODE_LEGACY) // Renders bold, line breaks, etc.

        // Load the image from URI
        imagePath?.let {
            imageView.setImageURI(Uri.parse(it))
        }

        // Back button
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
