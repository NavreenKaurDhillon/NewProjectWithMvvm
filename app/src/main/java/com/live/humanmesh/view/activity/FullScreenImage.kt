package com.live.humanmesh.view.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.live.humanmesh.databinding.ActivityFullScreenImageBinding

class FullScreenImage : AppCompatActivity() {

    private lateinit var binding : ActivityFullScreenImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.extras != null) {
            if (intent.extras?.containsKey("imageUrl") == true) {
                Glide.with(this)
                    .load(intent.getStringExtra("imageUrl"))
                    .into(binding.fullscreenIV)
            }
        }
        binding.toolBar.ivBack.setOnClickListener {
            finish()
        }
    }
}