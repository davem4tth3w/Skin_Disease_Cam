package dmi.developments.skin_disease_cam.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import dmi.developments.skin_disease_cam.R

class LibraryActivity : BottomNavActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.library)
        setupBottomNav(R.id.nav_library)

        // Navigate to Atopic Dermatitis info page
        val atopicDerm = findViewById<TextView>(R.id.atopic_derm)
        atopicDerm.setOnClickListener {
            val intent = Intent(this, LibraryAtopicDermActivity::class.java)
            startActivity(intent)
        }

        // Navigate to impetigo info page
        val Impetigo = findViewById<TextView>(R.id.impetigo)
        Impetigo.setOnClickListener {
            val intent = Intent(this, LibraryImpetigoActivity::class.java)
            startActivity(intent)
        }

        // Navigate to sarcoptes scabiei info page
        val sarcoptesScabiei = findViewById<TextView>(R.id.sarcoptes_scabiei)
        sarcoptesScabiei.setOnClickListener {
            val intent = Intent(this, LibrarySarcoptesScabieiActivity::class.java)
            startActivity(intent)
        }


        // Navigate to prickly heat info page
        val pricklyHeat = findViewById<TextView>(R.id.prickly_heat)
        pricklyHeat.setOnClickListener {
            val intent = Intent(this, LibraryPricklyHeatActivity::class.java)
            startActivity(intent)
        }


        // Navigate to tinea corporis info page
        val tineaCorp = findViewById<TextView>(R.id.tinea_corp)
        tineaCorp.setOnClickListener {
            val intent = Intent(this, LibraryTineaInfectionActivity::class.java)
            startActivity(intent)
        }


        // Navigate to warts info page
        val Wart = findViewById<TextView>(R.id.wart)
        Wart.setOnClickListener {
            val intent = Intent(this, LibraryWartsActivity::class.java)
            startActivity(intent)
        }


    }
}
