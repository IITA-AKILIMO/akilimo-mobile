package com.iita.akilimo.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iita.akilimo.R
import com.iita.akilimo.databinding.ActivityMySurveyBinding


class MySurveyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMySurveyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}