package dmi.developments.skin_disease_cam.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dmi.developments.skin_disease_cam.R

open class BottomNavActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBottomNav(selectedId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = selectedId

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_camera -> {
                    if (selectedId != R.id.nav_camera) {
                        startActivity(Intent(this, CameraActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_records -> {
                    if (selectedId != R.id.nav_records) {
                        startActivity(Intent(this, RecordsActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_library -> {
                    if (selectedId != R.id.nav_library) {
                        startActivity(Intent(this, LibraryActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
