package me.seasonyuu.xperiatools.displaybooster

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import me.seasonyuu.xperiatools.R
import me.seasonyuu.xperiatools.databinding.ActivityDisplaySettingsBinding
import me.seasonyuu.xperiatools.displaybooster.DisplayUtils.Screen4k
import rikka.material.app.MaterialActivity

class DisplaySettingsActivity : MaterialActivity() {
    private val binding: ActivityDisplaySettingsBinding by lazy {
        ActivityDisplaySettingsBinding.inflate(layoutInflater)
    }

    private fun getPreference(): SharedPreferences? {
        return try {
            getSharedPreferences("${packageName}_preferences", Context.MODE_WORLD_READABLE)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.itemUnlockFps.setOnClickListener {
            binding.switchUnlockFps.isChecked = !binding.switchUnlockFps.isChecked
        }
        getPreference()?.let { sp ->
            binding.switchUnlockFps.isChecked =
                sp.getBoolean(DisplayBoosterHooker.PREF_KEY_ENABLED, false)
        }
        binding.enable4k.setOnClickListener {
            DisplayUtils.enable4kMode()
        }
        binding.restoreDisplayMode.setOnClickListener {
            DisplayUtils.restoreDisplayMode()
        }
        binding.switchUnlockFps.setOnCheckedChangeListener { _, isChecked ->
            getPreference()?.let { sp ->
                sp.edit {
                    putBoolean(DisplayBoosterHooker.PREF_KEY_ENABLED, isChecked)
                }
            } ?: run {
                Snackbar.make(binding.root, R.string.module_inactive, Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.refreshDisplayMode.setOnClickListener {
            updateDisplayMode()
        }
        binding.setWmDensity.setOnClickListener {
            DisplayUtils.setWmDensity()
        }
        binding.setWmSize.setOnClickListener {
            DisplayUtils.setWmSize(Screen4k.width(), Screen4k.height())
        }
    }

    override fun onResume() {
        super.onResume()

        updateDisplayMode()
    }

    private fun updateDisplayMode() {
        binding.root.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                display?.mode?.let {
                    binding.tvCurrentDisplayMode.text = getString(
                        R.string.current_display_mode_value,
                        it.physicalWidth,
                        it.physicalHeight,
                        it.refreshRate,
                        bounds.width(),
                        bounds.height()
                    )
                }
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
