package com.example.food2you.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.food2you.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}