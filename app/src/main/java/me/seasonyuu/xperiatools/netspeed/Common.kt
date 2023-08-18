package me.seasonyuu.xperiatools.netspeed

import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log

object Common {
    private val TAG = Common::class.java.simpleName
    private const val PKG_NAME = "me.seasonyuu.xperiatools"
    const val ACTION_SETTINGS_CHANGED = "$PKG_NAME.changed"
    const val KEY_ENABLE_NETSPEED_INDICATOR = "enable_netspeed_indicator"
    const val KEY_HIDE_BELOW = "hide_below"
    const val KEY_SHOW_SUFFIX = "show_suffix"
    const val KEY_UNIT_MODE = "unit_mode"
    const val KEY_UNIT_FORMAT = "unit_format"
    const val KEY_FORCE_UNIT = "force_unit"
    const val KEY_NETWORK_TYPE = "network_type"
    const val KEY_NETWORK_SPEED = "network_speed"
    const val KEY_FONT_SIZE = "font_size"
    const val KEY_SUFFIX = "suffix"
    const val KEY_DISPLAY = "display"
    const val KEY_SWAP_SPEEDS = "swap_speeds"
    const val KEY_UPDATE_INTERVAL = "update_interval"
    const val KEY_FONT_COLOR = "font_color"
    const val KEY_COLOR = "color"
    const val KEY_FONT_STYLE = "font_style"
    const val KEY_ENABLE_LOG = "enable_logging_beta"
    const val KEY_MIN_UNIT = "min_unit"
    const val KEY_GET_SPEED_WAY = "get_speed_way"
    const val KEY_MIN_WIDTH = "min_width"
    const val KEY_PREVENT_FONT_ALPHA_CHANGING = "prevent_font_alpha_changing"

    val DEF_NETWORK_TYPE = hashSetOf("0", "1")
    val DEF_NETWORK_SPEED = hashSetOf("U", "D")
    val DEF_UNIT_FORMAT = hashSetOf("Sp", "KM", "Bb")

    const val DEF_HIDE_BELOW = 0
    const val DEF_SHOW_SUFFIX = false
    const val DEF_UNIT_MODE = 3 //Decimal bytes
    const val DEF_FORCE_UNIT = 0
    const val DEF_FONT_SIZE = 8f
    const val DEF_SUFFIX = 1
    const val DEF_DISPLAY = 0
    const val DEF_SWAP_SPEEDS = false
    const val DEF_UPDATE_INTERVAL = 1000
    const val DEF_FONT_COLOR = false
    const val DEF_COLOR = Color.LTGRAY
    val DEF_FONT_STYLE = HashSet<String>() // keep empty, means normal style
    const val DEF_ENABLE_LOG = false
    const val DEF_MIN_UNIT = 0
    const val DEF_SPEED_WAY = 0
    const val DEF_MIN_WIDTH = 0
    const val DEF_PREVENT_FONT_ALPHA_CHANGING = false
    const val BIG_UP_TRIANGLE = " ▲ " // \u25B2
    const val BIG_DOWN_TRIANGLE = " ▼ " // \u25BC
    const val SMALL_UP_TRIANGLE = " ▴ " // \u25B4
    const val SMALL_DOWN_TRIANGLE = " ▾ " // \u25BE
    const val BIG_UP_HOLLOW_TRIANGLE = " △ " // \u25B3
    const val BIG_DOWN_HOLLOW_TRIANGLE = " ▽ " // \u25BD
    const val SMALL_UP_HOLLOW_TRIANGLE = " ▵ " // \u25B5
    const val SMALL_DOWN_HOLLOW_TRIANGLE = " ▿ " // \u25BF
    const val ARROW_UP = " ↑ " // \u2191
    const val ARROW_DOWN = " ↓ " // \u2193
    const val ARROW_UP_HOLLOW = " ⇧ " // \u2313
    const val ARROW_DOWN_HOLLOW = " ⇩ " // \u21E9

    fun getPrefInt(pref: SharedPreferences?, key: String, defValue: Int): Int {
        pref ?: return defValue
        try {
            val value = pref.getString(key, defValue.toString())
            return value!!.toInt()
        } catch (e: Exception) {
            Log.w(TAG, "Key: $key. Def: $defValue. Exception ignored: ", e)
        }
        return defValue
    }

    fun getRealInt(pref: SharedPreferences, key: String?, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    fun getPrefFloat(pref: SharedPreferences, key: String, defValue: Float): Float {
        try {
            val value = pref.getString(key, defValue.toString())
            return value!!.toFloat()
        } catch (e: Exception) {
            Log.w(TAG, "Key: $key. Def: $defValue. Exception ignored: ", e)
        }
        return defValue
    }

    fun formatUnit(prefUnitMode: Int, prefUnitFactor: Int, prefUnitFormat: Set<String?>?): String {
        val binaryMode = prefUnitMode == 0 || prefUnitMode == 1
        val bitMode = prefUnitMode == 0 || prefUnitMode == 2
        var factor = "K"
        if (prefUnitFactor == 3) factor = "M" else if (prefUnitFactor == 1) factor = ""
        val unit = StringBuilder()
        prefUnitFormat?.let {
            if (prefUnitFormat.contains("KM")) {
                unit.append(factor)
                if (binaryMode) {
                    if (prefUnitFormat.contains("i") && factor.isNotEmpty()) {
                        unit.append("i")
                    }
                }
            }
            if (prefUnitFormat.contains("Bb")) {
                if (bitMode) unit.append("b") //Bits
                else unit.append("B") //Bytes
            }
            if (prefUnitFormat.contains("p")) {
                if (bitMode) unit.append("p") else unit.append("/")
            }
            if (prefUnitFormat.contains("s")) {
                unit.append("s")
            }
            if (prefUnitFormat.contains("Sp")) {
                if (unit.isNotEmpty()) {
                    unit.insert(0, " ")
                }
            }
        }
        return unit.toString()
    }

    fun throwException(e: Exception?) {
        if (e != null && e is RuntimeException) throw (e as RuntimeException?)!! else throw RuntimeException(
            e
        )
    }
}
