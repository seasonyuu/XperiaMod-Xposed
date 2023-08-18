package me.seasonyuu.xperiatools.netspeed

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.view.WindowCompat
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import com.skydoves.colorpickerpreference.ColorPickerPreference
import me.seasonyuu.xperiatools.R
import me.seasonyuu.xperiatools.databinding.ActivitySettingsBinding
import me.seasonyuu.xperiatools.netspeed.Common.KEY_ENABLE_NETSPEED_INDICATOR
import rikka.material.app.MaterialActivity
import java.util.TreeMap


class SettingsActivity : MaterialActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        super.onApplyUserThemeResource(theme, isDecorView)
        theme.applyStyle(R.style.AppThemeOverlay_Preference, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        // if it's light mode
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightNavigationBars = true
                isAppearanceLightStatusBars = true
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragment.id, SettingsFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    companion object {
        const val TAG = "SettingsFragment"
    }

    private val mPrefs: SharedPreferences? by lazy {
        preferenceManager.sharedPreferences
    }
    private var prefUnitMode = -1
    private var prefForceUnit = -1
    private val networkTypeEntries = LinkedHashSet<String>()
    private val networkTypeValues = LinkedHashSet<String>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        preferenceManager.sharedPreferencesMode = Context.MODE_WORLD_READABLE

        refreshNetworkTypes()
        refreshPreferences(mPrefs, null)
    }

    override fun onResume() {
        super.onResume()

        val settings = findPreference<PreferenceGroup>("settings")
        setAllSummary(settings)

        mPrefs?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        mPrefs?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        prefs ?: return
        key ?: return

        try {
            val intent = Intent()
            Log.i(TAG, "onSharedPreferenceChanged $key")
            refreshPreferences(prefs, key)
            setSummary(findPreference(key))
            intent.setAction(Common.ACTION_SETTINGS_CHANGED)
            when (key) {

                Common.KEY_FORCE_UNIT -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_FORCE_UNIT)
                    findPreference<Preference>(Common.KEY_MIN_UNIT)?.isEnabled = value == 0
                    intent.putExtra(key, value)
                }

                Common.KEY_UNIT_MODE -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_UNIT_MODE)
                    intent.putExtra(key, value)
                }

                Common.KEY_HIDE_BELOW -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_HIDE_BELOW)
                    intent.putExtra(key, value)
                }

                Common.KEY_SHOW_SUFFIX -> {
                    val value = prefs.getBoolean(key, Common.DEF_SHOW_SUFFIX)
                    intent.putExtra(key, value)
                }

                Common.KEY_FONT_SIZE -> {
                    val value = Common.getPrefFloat(prefs, key, Common.DEF_FONT_SIZE)
                    intent.putExtra(key, value)
                }

                Common.KEY_SUFFIX -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_SUFFIX)
                    intent.putExtra(key, value)
                }

                Common.KEY_DISPLAY -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_DISPLAY)
                    intent.putExtra(key, value)
                }

                Common.KEY_SWAP_SPEEDS -> {
                    val value = prefs.getBoolean(key, Common.DEF_SWAP_SPEEDS)
                    intent.putExtra(key, value)
                }

                Common.KEY_UPDATE_INTERVAL -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_UPDATE_INTERVAL)
                    intent.putExtra(key, value)
                }

                Common.KEY_FONT_COLOR -> {
                    val value = prefs.getBoolean(key, Common.DEF_FONT_COLOR)
                    intent.putExtra(key, value)
                }

                Common.KEY_COLOR -> {
                    val value = Common.getRealInt(prefs, key, Common.DEF_COLOR)
                    intent.putExtra(key, value)
                }

                Common.KEY_ENABLE_LOG -> {
                    val value = prefs.getBoolean(key, Common.DEF_ENABLE_LOG)
                    intent.putExtra(key, value)
                }

                Common.KEY_PREVENT_FONT_ALPHA_CHANGING -> {
                    val value = prefs.getBoolean(key, Common.DEF_PREVENT_FONT_ALPHA_CHANGING)
                    intent.putExtra(key, value)
                }

                Common.KEY_NETWORK_TYPE, Common.KEY_NETWORK_SPEED, Common.KEY_UNIT_FORMAT, Common.KEY_FONT_STYLE -> {
                    val mulPref: MultiSelectListPreference? =
                        findPreference(key)
                    val value = mulPref?.values as? HashSet<String>
                    intent.putExtra(key, value)
                }

                Common.KEY_GET_SPEED_WAY -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_SPEED_WAY)
                    intent.putExtra(key, value)
                }

                Common.KEY_MIN_UNIT -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_FORCE_UNIT)
                    intent.putExtra(key, value)
                }

                Common.KEY_MIN_WIDTH -> {
                    val value = Common.getPrefInt(prefs, key, Common.DEF_MIN_WIDTH)
                    intent.putExtra(key, value)
                }

                else -> {
                    intent.setAction(null)
                }
            }
            if (intent.action != null) {
                requireContext().sendBroadcast(intent)
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "onSharedPreferenceChanged failed: ", e)
            Common.throwException(e)
        }
    }

    private fun setAllSummary(group: PreferenceGroup?) {
        group ?: return
        for (i in 0 until group.preferenceCount) {
            val pref = group.getPreference(i)
            if (pref is PreferenceGroup) {
                setAllSummary(pref)
            } else {
                setSummary(pref)
            }
        }
    }

    private fun refreshNetworkTypes() {
        // Get the network types supported by device
        val cm =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val allNetInfo = cm?.allNetworkInfo

        networkTypeEntries.clear()
        networkTypeValues.clear()

        if (allNetInfo == null) {
            Log.e(TAG, "Array containing all network info is null!")
        } else {
            val resNetworkTypeEntries =
                listOf<String>(*resources.getStringArray(R.array.networktype_entries))
            val resNetworkTypeValues =
                listOf<String>(*resources.getStringArray(R.array.networktype_values))
            for (netInfo in allNetInfo) {
                if (netInfo == null) {
                    Log.w(TAG, "Network info object is null.")
                } else {
                    val netInfoType = netInfo.type.toString()
                    val index = resNetworkTypeValues.indexOf(netInfoType)
                    if (index >= 0 && index < resNetworkTypeEntries.size) {
                        networkTypeEntries.add(resNetworkTypeEntries[index])
                        networkTypeValues.add(resNetworkTypeValues[index])
                    }
                }
            }
        }
    }

    private fun setSummary(preference: Preference?) {
        when (preference) {
            is ListPreference -> {
                preference.setSummary(createListPrefSummary(preference))
            }

            is MultiSelectListPreference -> {
                preference.setSummary(createMultiSelectSummary(preference))
            }

            is ColorPickerPreference -> {
                preference.setSummary(createColorPickerSummary(preference))
            }

            is EditTextPreference -> {
                preference.setSummary(createEditTextSummary(preference))
            }
        }
    }

    private fun createListPrefSummary(listPref: ListPreference): String? {
        return if (Common.KEY_UNIT_MODE == listPref.key) {
            try {
                val listValue = listPref.value.toIntOrNull() ?: return listPref.entry?.toString()
                val summaryTexts = resources.getStringArray(R.array.unit_mode_summary)
                String.format("${listPref.entry} (%s)", summaryTexts[listValue])
            } catch (e: Exception) {
                listPref.entry?.toString()
            }
        } else {
            listPref.entry?.toString()
        }
    }

    private fun createEditTextSummary(editPref: EditTextPreference): String? {
        var summaryText: String? = editPref.text

        if (Common.KEY_UPDATE_INTERVAL == editPref.key) {
            summaryText = try {
                summaryText!!.toInt().toString()
            } catch (e: Exception) {
                Common.DEF_UPDATE_INTERVAL.toString()
            }
            summaryText = formatWithUnit(summaryText, getString(R.string.unit_update_interval))
        } else if (Common.KEY_HIDE_BELOW == editPref.key) {
            val value: Int = try {
                summaryText!!.toInt()
            } catch (e: Exception) {
                Common.DEF_HIDE_BELOW
            }
            summaryText = if (value <= 0) {
                getString(R.string.sum0_hide_below)
            } else if (value == 1) {
                getString(R.string.sum1_hide_below)
            } else {
                formatWithUnit(value.toString(), getString(R.string.unit_hide_below))
            }
        } else if (Common.KEY_FONT_SIZE == editPref.key) {
            summaryText = try {
                summaryText!!.toFloat().toString()
            } catch (e: Exception) {
                Common.DEF_FONT_SIZE.toString()
            }
            summaryText = formatWithUnit(summaryText, getString(R.string.unit_font_size))
        } else if (Common.KEY_MIN_WIDTH == editPref.key) {
            val value = Common.getPrefInt(mPrefs, Common.KEY_MIN_WIDTH, Common.DEF_MIN_WIDTH)
            summaryText = if (value != Common.DEF_MIN_WIDTH) {
                formatWithUnit(value.toString(), getString(R.string.min_width_px_summary))
            } else getString(R.string.min_width_summary)
        }

        return summaryText
    }

    private fun formatWithUnit(value: String?, unit: String) = if (unit.contains("%s")) {
        String.format(unit, value)
    } else {
        "$value $unit"
    }

    private fun createMultiSelectSummary(mulPref: MultiSelectListPreference): String {
        val valueSet = mulPref.values

        if (Common.KEY_UNIT_FORMAT == mulPref.key) {
            val formattedUnit = Common.formatUnit(prefUnitMode, prefForceUnit, valueSet)
            return if (formattedUnit.isEmpty()) {
                getString(R.string.unit_format_hidden)
            } else {
                getString(R.string.unit_format_fmt_as) + " #" + formattedUnit
            }
        }

        if (valueSet.size == 0) {
            return getString(R.string.summary_none)
        }

        val selections = TreeMap<Int, String>()
        for (value in valueSet) {
            val index = mulPref.findIndexOfValue(value)
            if (index < 0 || index >= mulPref.entries.size) {
                Log.w(
                    TAG,
                    "Found multi select value without entry: " +
                            value
                )
            } else {
                val entry = mulPref.entries[index] as String
                selections[index] = entry
            }
        }

        val summary = StringBuilder()
        for (entry in selections.values) {
            if (summary.isNotEmpty()) {
                summary.append(", ")
            }
            summary.append(entry)
        }
        return summary.toString()
    }

    private fun createColorPickerSummary(colorPicker: ColorPickerPreference): String {
        val prefs = colorPicker.sharedPreferences
        val key = colorPicker.key

        return if (prefs?.contains(key) == true) {
            val iColor = prefs.getInt(key, 0)

            val summary = String.format("#%08X", iColor).uppercase()
            formatWithUnit(summary, getString(R.string.unit_font_color))
        } else {
            getString(R.string.summary_none)
        }
    }

    private fun refreshPreferences(prefs: SharedPreferences?, key: String?) {
        prefs ?: return

        // When key is null, refresh everything.
        // When a key is provided, refresh only for that key.
        if (key.isNullOrEmpty()) { //only first time
            val mulPref: MultiSelectListPreference? =
                findPreference(Common.KEY_NETWORK_TYPE) as? MultiSelectListPreference?
            mulPref?.entries = networkTypeEntries.toArray(arrayOf<String>())
            mulPref?.entryValues = networkTypeValues.toArray(arrayOf<String>())
        }

        if (Common.KEY_FORCE_UNIT == key) {
            getUnitSettings(prefs)
            setSummary(findPreference(Common.KEY_UNIT_FORMAT))
        }

        if (Common.KEY_UNIT_FORMAT == key) {
            getUnitSettings(prefs)
        }

        if (key.isNullOrEmpty() || key == Common.KEY_HIDE_BELOW
            || key == Common.KEY_SUFFIX
        ) {
            val prefHideBelow =
                Common.getPrefInt(prefs, Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW)
            val prefSuffix = Common.getPrefInt(prefs, Common.KEY_SUFFIX, Common.DEF_SUFFIX)
            findPreference<Preference>(Common.KEY_SHOW_SUFFIX)?.isEnabled =
                prefHideBelow > 0 && prefSuffix != 0
        }

        if (key.isNullOrEmpty() || key == Common.KEY_UNIT_MODE) {
            // Dynamically change the entry texts of "Unit" preference
            getUnitSettings(prefs)
            val resId: Int = when (prefUnitMode) {
                0 -> R.array.unit_entries_binary_bits
                1 -> R.array.unit_entries_binary_bytes
                2 -> R.array.unit_entries_decimal_bits
                3 -> R.array.unit_entries_decimal_bytes
                else -> R.array.unit_entries_decimal_bits
            }
            val unitEntries = resources.getStringArray(resId)
            val prefUnit = findPreference<Preference>(Common.KEY_FORCE_UNIT)
            (prefUnit as ListPreference).entries = unitEntries
            if (!key.isNullOrEmpty()) { // key-specific refresh
                setSummary(prefUnit)
                setSummary(findPreference(Common.KEY_UNIT_FORMAT))
            }
        }

        if (Common.KEY_MIN_WIDTH == key) {
            setSummary(findPreference(key))
        }

        findPreference<Preference>("status_bar_icon_mode")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(requireContext(), StatusBarIconModeActivity::class.java))
                true
            }
        findPreference<SwitchPreferenceCompat>(KEY_ENABLE_NETSPEED_INDICATOR)
            ?.setOnPreferenceChangeListener { _, _ ->
                Snackbar.make(
                    requireView(),
                    R.string.require_restart_system_ui,
                    Snackbar.LENGTH_SHORT
                ).show()
                true
            }
    }

    private fun getUnitSettings(prefs: SharedPreferences) {
        prefUnitMode = Common.getPrefInt(prefs, Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE)
        prefForceUnit = Common.getPrefInt(prefs, Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT)
    }
}