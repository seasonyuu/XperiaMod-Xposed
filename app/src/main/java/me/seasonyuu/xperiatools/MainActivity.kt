package me.seasonyuu.xperiatools

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.google.android.material.color.MaterialColors
import me.seasonyuu.xperiatools.databinding.ActivityMainBinding
import me.seasonyuu.xperiatools.utils.Utils
import rikka.material.app.MaterialActivity

class MainActivity : MaterialActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(R.style.AppThemeOverlay_Preference, true)
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

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_main, MainFragment())
            .commit()
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

class MainFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main, rootKey)

        val isModuleEnabled = Utils.isModuleActivated()
        findPreference<PreferenceScreen>("container")?.isEnabled = isModuleEnabled

        findPreference<Preference>("restart_system_ui")?.setOnPreferenceClickListener {
            Utils.restartSystemUi()
            true
        }
    }

}