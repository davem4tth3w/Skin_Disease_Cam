package dmi.developments.skin_disease_cam.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.text.HtmlCompat
import dmi.developments.skin_disease_cam.R

class LibraryPricklyHeatActivity : BottomNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.library_prickly_heat)

        // Back button
        val backBtn = findViewById<ImageButton>(R.id.lib_back_btn)
        backBtn.setOnClickListener {
            // This goes back to the previous screen (LibraryActivity)
            onBackPressedDispatcher.onBackPressed()
        }

        // TextView for home remedies
        val remedyTextView = findViewById<TextView>(R.id.ph_home_remedy)
        remedyTextView.text = HtmlCompat.fromHtml(
            getString(R.string.ph_home_remedy),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }
}
