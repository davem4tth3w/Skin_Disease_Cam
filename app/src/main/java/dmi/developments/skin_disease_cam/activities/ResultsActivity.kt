package dmi.developments.skin_disease_cam.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import dmi.developments.skin_disease_cam.R

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.results)

        val imageView = findViewById<ImageView>(R.id.preview_img)
        val skinDiseaseTextView = findViewById<TextView>(R.id.skin_disease_name)
        val remediesTextView = findViewById<TextView>(R.id.remedies)
        val backButton = findViewById<ImageButton>(R.id.back_btn)

        // Get data passed from previous activity
        val imagePath = intent.getStringExtra("imagePath")
        val skindisease = intent.getStringExtra("skindisease") ?: "Unknown"
        val remedies = intent.getStringExtra("remedies") ?: "No remedies found"

        // Show image preview
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        }

        // Display skin disease & remedies
        skinDiseaseTextView.text = skindisease
        remediesTextView.text = HtmlCompat.fromHtml(remedies, HtmlCompat.FROM_HTML_MODE_LEGACY)


        // When "back_btn" is clicked, open CameraActivity
        backButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }
}
