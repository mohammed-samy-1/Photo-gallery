package com.plcoding.androidstorage.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.plcoding.androidstorage.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MODE_PRIVATE
    }
}