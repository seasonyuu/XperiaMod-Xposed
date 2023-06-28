package me.seasonyuu.xperiatools.netspeed

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import me.seasonyuu.xperiatools.databinding.ActivitySbIconModeBinding


class StatusBarIconModeActivity : AppCompatActivity() {
    private val binding: ActivitySbIconModeBinding by lazy {
        ActivitySbIconModeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.lightStatusBar.setOnClickListener {
            binding.cbLightStatusBar.isChecked = !binding.cbLightStatusBar.isChecked
        }
        binding.cbLightStatusBar.setOnCheckedChangeListener { _, enable ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = enable
            }
        }
        binding.lowProfile.setOnClickListener {
            binding.cbLowProfile.isChecked = !binding.cbLowProfile.isChecked
        }
        binding.cbLowProfile.setOnCheckedChangeListener { _, enable ->
            // into low profile mode
            window.decorView.systemUiVisibility = if (enable) {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LOW_PROFILE
            } else {
                0
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}