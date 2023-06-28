package me.seasonyuu.xperiatools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import me.seasonyuu.xperiatools.databinding.ActivityMainBinding
import me.seasonyuu.xperiatools.displaybooster.DisplayBoosterHooker
import me.seasonyuu.xperiatools.netspeed.SettingsActivity
import me.seasonyuu.xperiatools.utils.Utils
import rikka.material.app.MaterialActivity

class MainActivity : MaterialActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(R.style.AppThemeOverlay_Preference, true)
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val isModuleEnabled = Utils.isModuleActivated()
        setActivateUi(isModuleEnabled)

        binding.activateStatus.setText(if (isModuleEnabled) R.string.module_active else R.string.module_inactive)
        binding.moduleVersion.text =
            getString(R.string.version_format, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

        binding.itemUnlockFps.setOnClickListener {
            if (Utils.isModuleActivated()) {
                binding.switchUnlockFps.isChecked = !binding.switchUnlockFps.isChecked
            } else {
                Snackbar.make(binding.root, R.string.module_inactive, Snackbar.LENGTH_SHORT).show()
            }
        }
        getPreference()?.let { sp ->
            binding.switchUnlockFps.isChecked =
                sp.getBoolean(DisplayBoosterHooker.PREF_KEY_ENABLED, false)
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
        binding.itemRestartSystemUi.setOnClickListener {
            Utils.restartSystemUi()
        }
        binding.itemNetSpeed.setOnClickListener {
            if (Utils.isModuleActivated()) {
                startActivity(Intent(this, SettingsActivity::class.java))
            } else {
                Snackbar.make(binding.root, R.string.module_inactive, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setActivateUi(isActive: Boolean) {
        val colorOn = if (isActive) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSecondary,
            Color.WHITE
        ) else MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnError,
            Color.WHITE
        )
        val colorSurface = if (isActive) MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnSecondaryContainer,
            Color.WHITE
        ) else MaterialColors.getColor(
            this,
            com.google.android.material.R.attr.colorOnErrorContainer,
            Color.WHITE
        )
        if (isActive) {
            binding.statusIcon.setImageResource(R.drawable.check_circle)
            binding.activateStatus.setText(R.string.module_active)
        } else {
            binding.statusIcon.setImageResource(R.drawable.error_fill1)
            binding.activateStatus.setText(R.string.module_inactive)
        }
        binding.statusIcon.imageTintList = ColorStateList.valueOf(colorOn)
        binding.moduleVersion.setTextColor(colorOn)
        binding.activateStatus.setTextColor(colorOn)
        binding.statusCard.setCardBackgroundColor(colorSurface)
        binding.statusCard.rippleColor = ColorStateList.valueOf(
            Color.argb(50, Color.red(colorOn), Color.green(colorOn), Color.blue(colorOn))
        )
    }
}