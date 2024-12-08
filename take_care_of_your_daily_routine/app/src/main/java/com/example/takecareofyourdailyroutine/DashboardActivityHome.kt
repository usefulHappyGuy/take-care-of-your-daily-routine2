package com.example.takecareofyourdailyroutine

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.takecareofyourdailyroutine.databinding.ActivityDashboardHomeBinding

class DashboardActivityHome : AppCompatActivity() {

    lateinit var binding: ActivityDashboardHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTask.setOnClickListener {
            val intent = Intent(this, DashboardTaskActivity::class.java)
            startActivity(intent)
        }
        binding.btnSettings.setOnClickListener {
            val intent = Intent(this, DashboardActivitySettings::class.java)
            startActivity(intent)
        }
    }
}