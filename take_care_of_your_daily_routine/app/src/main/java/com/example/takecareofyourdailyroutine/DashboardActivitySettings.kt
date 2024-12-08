package com.example.takecareofyourdailyroutine

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.takecareofyourdailyroutine.databinding.ActivityDashboardHomeBinding
import com.example.takecareofyourdailyroutine.databinding.ActivityDashboardSettingsBinding

class DashboardActivitySettings : AppCompatActivity() {
    lateinit var binding: ActivityDashboardSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTask.setOnClickListener {
            val intent = Intent(this, DashboardTaskActivity::class.java)
            startActivity(intent)
        }
        binding.btnHome.setOnClickListener {
            val intent = Intent(this, DashboardActivityHome::class.java)
            startActivity(intent)
        }
        // Obsługa przycisku wylogowania
        binding.logoutButton.setOnClickListener {
            // Wylogowanie użytkownika, np. usunięcie danych logowania z SharedPreferences
            val sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear() // Usunięcie wszystkich danych logowania
            editor.apply()

            // Przekierowanie do ekranu logowania
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Opcjonalnie zamknij aktywność
            finish()
        }
    }
}


