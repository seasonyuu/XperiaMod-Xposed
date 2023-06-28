package me.seasonyuu.xperiatools.netspeed

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.UserManager
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import de.robv.android.xposed.XSharedPreferences
import me.seasonyuu.xperiatools.BuildConfig
import java.text.DecimalFormat

@SuppressLint("HandlerLeak", "AppCompatCustomView")
class TrafficView(context: Context?) : TextView(context, null, 0) {
    var mPositionCallback: PositionCallback? = null
    private var mAttached: Boolean
    private var uploadSpeed: Long = 0
    private var downloadSpeed: Long = 0
    private var totalTxBytes: Long = 0
    private var totalRxBytes: Long = 0
    private var lastUpdateTime: Long = 0
    private var justLaunched = true
    private var loggedZero = false
    private var networkType: String? = null
    private var networkState = false
    private var bootCompleted = false
    var iconTint = DEFAULT_ICON_TINT
    private var prefForceUnit = Common.DEF_FORCE_UNIT
    private var prefMinUnit = Common.DEF_MIN_UNIT
    private var prefUnitMode = Common.DEF_UNIT_MODE
    private var prefSpeedWay = Common.DEF_SPEED_WAY
    private var prefFontSize = Common.DEF_FONT_SIZE
    private var prefSuffix = Common.DEF_SUFFIX
    private var prefDisplay = Common.DEF_DISPLAY
    private var prefSwapSpeeds = Common.DEF_SWAP_SPEEDS
    private var prefUpdateInterval = Common.DEF_UPDATE_INTERVAL
    private var prefFontColor = Common.DEF_FONT_COLOR
    private var prefColor = Common.DEF_COLOR
    private var prefHideBelow = Common.DEF_HIDE_BELOW
    private var prefMinWidth = Common.DEF_MIN_WIDTH
    private var prefShowSuffix = Common.DEF_SHOW_SUFFIX
    private var prefUnitFormat: Set<String>? = Common.DEF_UNIT_FORMAT
    private var prefNetworkType: Set<String?>? = Common.DEF_NETWORK_TYPE
    private var prefNetworkSpeed: Set<String>? = Common.DEF_NETWORK_SPEED
    private var prefFontStyle: Set<String>? = Common.DEF_FONT_STYLE
    private var prefPreventFontAlphaChanging = Common.DEF_PREVENT_FONT_ALPHA_CHANGING

    @SuppressLint("NewApi")
    fun refreshColor() {
        if (prefFontColor) {
            setTextColor(prefColor)
        } else {
            setTextColor(iconTint)
        }
    }

    override fun setAlpha(alpha: Float) {
        if (!prefPreventFontAlphaChanging) super.setAlpha(alpha)
    }

    @Suppress("DEPRECATION")
    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                Log.d(TAG, "Receiver receive ${intent.action}")
                when (intent.action) {
                    ConnectivityManager.CONNECTIVITY_ACTION -> {
                        Log.i(TAG, "Connectivity changed")
                        updateConnectionInfo()
                        updateViewVisibility()
                    }

                    Intent.ACTION_BOOT_COMPLETED -> {
                        bootCompleted = true
                        loadPreferences()
                        updateViewVisibility()
                        update()
                    }

                    Intent.ACTION_SCREEN_ON -> {
                        mTrafficHandler.postAtTime(
                            updateTrafficRunnable,
                            SystemClock.uptimeMillis() / 1000 * 1000 + 1000
                        )
                    }

                    Intent.ACTION_SCREEN_OFF -> {
                        mTrafficHandler.removeCallbacks(updateTrafficRunnable)
                    }

                    Intent.ACTION_USER_PRESENT -> {
                        loadPreferences()
                    }

                    Common.ACTION_SETTINGS_CHANGED -> {
                        Log.i(TAG, "Settings changed")
                        if (intent.hasExtra(Common.KEY_FORCE_UNIT)) {
                            prefForceUnit =
                                intent.getIntExtra(Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT)
                        }
                        if (intent.hasExtra(Common.KEY_MIN_UNIT)) {
                            prefMinUnit =
                                intent.getIntExtra(Common.KEY_MIN_UNIT, Common.DEF_MIN_UNIT)
                        }
                        if (intent.hasExtra(Common.KEY_MIN_WIDTH)) {
                            prefMinWidth =
                                intent.getIntExtra(Common.KEY_MIN_WIDTH, Common.DEF_MIN_WIDTH)
                            minimumWidth = prefMinWidth
                        }
                        if (intent.hasExtra(Common.KEY_UNIT_MODE)) {
                            prefUnitMode =
                                intent.getIntExtra(Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE)
                        }
                        if (intent.hasExtra(Common.KEY_UNIT_FORMAT)) {
                            @Suppress("DEPRECATION", "UNCHECKED_CAST")
                            prefUnitFormat =
                                intent.getSerializableExtra(Common.KEY_UNIT_FORMAT) as Set<String>?
                        }
                        if (intent.hasExtra(Common.KEY_HIDE_BELOW)) {
                            prefHideBelow =
                                intent.getIntExtra(Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW)
                        }
                        if (intent.hasExtra(Common.KEY_SHOW_SUFFIX)) {
                            prefShowSuffix =
                                intent.getBooleanExtra(
                                    Common.KEY_SHOW_SUFFIX,
                                    Common.DEF_SHOW_SUFFIX
                                )
                        }
                        if (intent.hasExtra(Common.KEY_FONT_SIZE)) {
                            prefFontSize =
                                intent.getFloatExtra(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE)
                        }
                        if (intent.hasExtra(Common.KEY_SUFFIX)) {
                            prefSuffix = intent.getIntExtra(Common.KEY_SUFFIX, Common.DEF_SUFFIX)
                        }
                        if (intent.hasExtra(Common.KEY_NETWORK_TYPE)) {
                            @Suppress("DEPRECATION", "UNCHECKED_CAST")
                            prefNetworkType =
                                intent.getSerializableExtra(Common.KEY_NETWORK_TYPE) as Set<String?>?
                        }
                        if (intent.hasExtra(Common.KEY_NETWORK_SPEED)) {
                            @Suppress("DEPRECATION", "UNCHECKED_CAST")
                            prefNetworkSpeed =
                                intent.getSerializableExtra(Common.KEY_NETWORK_SPEED) as Set<String>?
                        }
                        if (intent.hasExtra(Common.KEY_DISPLAY)) {
                            prefDisplay = intent.getIntExtra(Common.KEY_DISPLAY, Common.DEF_DISPLAY)
                        }
                        if (intent.hasExtra(Common.KEY_SWAP_SPEEDS)) {
                            prefSwapSpeeds =
                                intent.getBooleanExtra(
                                    Common.KEY_SWAP_SPEEDS,
                                    Common.DEF_SWAP_SPEEDS
                                )
                        }
                        if (intent.hasExtra(Common.KEY_UPDATE_INTERVAL)) {
                            prefUpdateInterval = intent.getIntExtra(
                                Common.KEY_UPDATE_INTERVAL,
                                Common.DEF_UPDATE_INTERVAL
                            )
                        }
                        if (intent.hasExtra(Common.KEY_FONT_COLOR)) {
                            prefFontColor =
                                intent.getBooleanExtra(Common.KEY_FONT_COLOR, Common.DEF_FONT_COLOR)
                        }
                        if (intent.hasExtra(Common.KEY_COLOR)) {
                            prefColor = intent.getIntExtra(Common.KEY_COLOR, Common.DEF_COLOR)
                        }
                        if (intent.hasExtra(Common.KEY_FONT_STYLE)) {
                            @Suppress("DEPRECATION", "UNCHECKED_CAST")
                            prefFontStyle =
                                intent.getSerializableExtra(Common.KEY_FONT_STYLE) as Set<String>?
                        }
                        if (intent.hasExtra(Common.KEY_PREVENT_FONT_ALPHA_CHANGING)) {
                            prefPreventFontAlphaChanging = intent.getBooleanExtra(
                                Common.KEY_PREVENT_FONT_ALPHA_CHANGING,
                                Common.DEF_PREVENT_FONT_ALPHA_CHANGING
                            )
                        }
                        updateViewVisibility()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onReceive failed: ", e)
                Common.throwException(e)
            }
        }
    }


    private val updateTrafficRunnable = object : Runnable {
        override fun run() {
            try {
                // changing values must be fetched together and only once
                val lastUpdateTimeNew = SystemClock.elapsedRealtime()
                val way =
                    if (prefSpeedWay == 0) TrafficStats.GRAVITY_BOX_WAY else TrafficStats.OLD_WAY
                val totalBytes = TrafficStats.getTotalBytes(way)
                val totalTxBytesNew = totalBytes[1]
                val totalRxBytesNew = totalBytes[0]
                val elapsedTime = lastUpdateTimeNew - lastUpdateTime
                if (elapsedTime == 0L) {
                    Log.w(TAG, "Elapsed time is zero")
                    uploadSpeed = 0
                    downloadSpeed = 0
                } else {
                    uploadSpeed = (totalTxBytesNew - totalTxBytes) * 1000 / elapsedTime
                    downloadSpeed = (totalRxBytesNew - totalRxBytes) * 1000 / elapsedTime
                }
                if (!loggedZero || uploadSpeed != 0L || downloadSpeed != 0L) {
//					Log.d(TAG, totalTxBytes, ",", totalTxBytesNew, ";", totalRxBytes, ",", totalRxBytesNew, ";",
//							lastUpdateTime, ",", lastUpdateTimeNew, ";", uploadSpeed, ",", downloadSpeed);
                    loggedZero = uploadSpeed == 0L && downloadSpeed == 0L
                }
                totalTxBytes = totalTxBytesNew
                totalRxBytes = totalRxBytesNew
                lastUpdateTime = lastUpdateTimeNew
                val spanString = SpannableString(createText())
                if (prefFontStyle?.contains("B") == true) {
                    spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
                }
                if (prefFontStyle?.contains("I") == true) {
                    spanString.setSpan(StyleSpan(Typeface.ITALIC), 0, spanString.length, 0)
                }
                text = spanString
                setTextSize(TypedValue.COMPLEX_UNIT_SP, prefFontSize)
                refreshColor()
                update()

                mTrafficHandler.postAtTime(this, SystemClock.uptimeMillis() / 1000 * 1000 + 1000)
            } catch (e: Exception) {
                Log.e(TAG, "handleMessage failed: ", e)
                Common.throwException(e)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val mTrafficHandler: Handler = Handler(Looper.getMainLooper())

    private val isReady: Boolean
        get() {
            val manager = context.getSystemService(Context.USER_SERVICE) as UserManager
            return manager.isUserUnlocked
        }

    @Suppress("DEPRECATION")
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onAttachedToWindow() {
        try {
            super.onAttachedToWindow()
            Log.d(TAG, "onAttachedToWindow")
            if (!mAttached) {
                mAttached = true
                val filter = IntentFilter()
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
                filter.addAction(Common.ACTION_SETTINGS_CHANGED)
                filter.addAction(Intent.ACTION_BOOT_COMPLETED)
                filter.addAction(Intent.ACTION_USER_UNLOCKED)
                filter.addAction(Intent.ACTION_SCREEN_ON)
                filter.addAction(Intent.ACTION_SCREEN_OFF)
                filter.addAction(Intent.ACTION_USER_PRESENT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(
                        mIntentReceiver,
                        filter,
                        null,
                        handler,
                        Context.RECEIVER_EXPORTED
                    )
                } else {
                    context.registerReceiver(
                        mIntentReceiver,
                        filter,
                        null,
                        handler
                    )
                }
            }
            mTrafficHandler.postAtTime(
                updateTrafficRunnable,
                SystemClock.uptimeMillis() / 1000 * 1000 + 1000
            )
            updateViewVisibility()
        } catch (e: Exception) {
            Log.e(TAG, "onAttachedToWindow failed: ", e)
            Common.throwException(e)
        }
    }

    override fun onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow()
            if (mAttached) {
                context.unregisterReceiver(mIntentReceiver)
                mAttached = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "onDetachedFromWindow failed: ", e)
            Common.throwException(e)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateConnectionInfo() {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null) {
            Log.d(TAG, networkInfo.toString())
            networkState = networkInfo.isAvailable
            networkType = networkInfo.type.toString()
            Log.i(TAG, "Network type: $networkType")
        } else {
            networkState = false
        }
        Log.d(TAG, "Network State: $networkState")
    }

    private fun updateTraffic() {
        Log.d(TAG, "updateTraffic")
        if (justLaunched) {
            // get the values for the first time
            lastUpdateTime = SystemClock.elapsedRealtime()
            val way = if (prefSpeedWay == 0) TrafficStats.GRAVITY_BOX_WAY else TrafficStats.OLD_WAY
            val totalBytes = TrafficStats.getTotalBytes(way)
            totalTxBytes = totalBytes[1]
            totalRxBytes = totalBytes[0]

            // don't get the values again
            justLaunched = false
            minimumWidth = prefMinWidth
        }
        mTrafficHandler.post(updateTrafficRunnable)
    }

    private fun createText(): String {
        val uploadSuffix: String
        val downloadSuffix: String
        val strUploadValue: String
        val strDownloadValue: String
        when (prefSuffix) {
            0 -> {
                downloadSuffix = " "
                uploadSuffix = downloadSuffix
            }

            1 -> {
                uploadSuffix = Common.BIG_UP_TRIANGLE
                downloadSuffix = Common.BIG_DOWN_TRIANGLE
            }

            2 -> {
                uploadSuffix = Common.BIG_UP_HOLLOW_TRIANGLE
                downloadSuffix = Common.BIG_DOWN_HOLLOW_TRIANGLE
            }

            3 -> {
                uploadSuffix = Common.SMALL_UP_TRIANGLE
                downloadSuffix = Common.SMALL_DOWN_TRIANGLE
            }

            4 -> {
                uploadSuffix = Common.SMALL_UP_HOLLOW_TRIANGLE
                downloadSuffix = Common.SMALL_DOWN_HOLLOW_TRIANGLE
            }

            5 -> {
                uploadSuffix = Common.ARROW_UP
                downloadSuffix = Common.ARROW_DOWN
            }

            6 -> {
                uploadSuffix = Common.ARROW_UP_HOLLOW
                downloadSuffix = Common.ARROW_DOWN_HOLLOW
            }

            else -> {
                downloadSuffix = " "
                uploadSuffix = downloadSuffix
            }
        }
        val showUploadSpeed = prefNetworkSpeed!!.contains("U")
        val showDownloadSpeed = prefNetworkSpeed!!.contains("D")
        var showInExactPosition = showUploadSpeed && showDownloadSpeed
        strUploadValue = if (showUploadSpeed) {
            formatSpeed(if (uploadSpeed > 0) uploadSpeed else 0, uploadSuffix)
        } else {
            ""
        }
        strDownloadValue = if (showDownloadSpeed) {
            formatSpeed(if (downloadSpeed > 0) downloadSpeed else 0, downloadSuffix)
        } else {
            ""
        }
        var delimiter: String
        if (prefDisplay == 0) {
            delimiter = "\n"
        } else {
            delimiter = " "
            showInExactPosition = false // irrelevant in one-line mode
        }
        val showBothSpeeds = strUploadValue.isNotEmpty() && strDownloadValue.isNotEmpty()
        if (!showBothSpeeds && !showInExactPosition) {
            delimiter = ""
        }
        return if (prefSwapSpeeds) {
            strDownloadValue + delimiter + strUploadValue
        } else {
            strUploadValue + delimiter + strDownloadValue
        }
    }

    private fun formatSpeed(transferSpeedBytes: Long, transferSuffix: String): String {
        val unitFactor: Float
        var transferSpeed = transferSpeedBytes
        when (prefUnitMode) {
            0 -> {
                transferSpeed *= 8
                unitFactor = 1024f
            }

            1 -> unitFactor = 1024f
            2 -> {
                transferSpeed *= 8
                unitFactor = 1000f
            }

            3 -> unitFactor = 1000f
            else -> unitFactor = 1000f
        }
        var tempPrefUnit = prefForceUnit
        var megaTransferSpeed = transferSpeed.toFloat() / (unitFactor * unitFactor)
        var kiloTransferSpeed = transferSpeed.toFloat() / unitFactor
        val transferValue: Float
        val transferDecimalFormat: DecimalFormat
        if (prefForceUnit == 0) { // Auto mode
            tempPrefUnit = if (megaTransferSpeed >= 1) {
                3
            } else if (kiloTransferSpeed >= 1) {
                2
            } else {
                1
            }
            if (prefMinUnit != 0) {
                if (prefMinUnit == 1) {
                    if (transferSpeed < 1) {
                        transferSpeed = 0
                        tempPrefUnit = 1
                    }
                } else if (prefMinUnit == 2) {
                    if (kiloTransferSpeed < 1) {
                        kiloTransferSpeed = 0f
                        tempPrefUnit = 2
                    }
                } else if (prefMinUnit == 3) {
                    if (megaTransferSpeed < 1) {
                        tempPrefUnit = 3
                    } else if (megaTransferSpeed < 0.1) {
                        megaTransferSpeed = 0f
                        tempPrefUnit = 3
                    }
                }
            }
        }
        when (tempPrefUnit) {
            3 -> {
                transferValue = megaTransferSpeed
                transferDecimalFormat =
                    if (transferValue < 0.1) formatWithoutDecimal else formatWithDecimal
            }

            2 -> {
                transferValue = kiloTransferSpeed
                transferDecimalFormat = formatWithoutDecimal
            }

            1 -> {
                transferValue = transferSpeed.toFloat()
                transferDecimalFormat = formatWithoutDecimal
            }

            else -> {
                transferValue = transferSpeed.toFloat()
                transferDecimalFormat = formatWithoutDecimal
            }
        }
        var strTransferValue: String = if (transferValue < prefHideBelow) {
            ""
        } else {
            transferDecimalFormat.format(transferValue.toDouble())
        }
        if (strTransferValue.isNotEmpty()) {
            strTransferValue += Common.formatUnit(prefUnitMode, tempPrefUnit, prefUnitFormat)
            strTransferValue += transferSuffix
        } else if (prefShowSuffix) {
            strTransferValue += transferSuffix
        }
        return strTransferValue
    }

    private fun update() {
        mTrafficHandler.removeCallbacks(mRunnable)
        mTrafficHandler.postDelayed(mRunnable, prefUpdateInterval.toLong())
    }

    private val mRunnable = Runnable {
        try {
            mTrafficHandler.sendEmptyMessage(0)
        } catch (e: Exception) {
            Log.e(TAG, "run failed: ", e)
            Common.throwException(e)
        }
    }

    init {
        loadPreferences()
        updateConnectionInfo()
        updateViewVisibility()
        mAttached = false
    }

    private fun updateViewVisibility() {
        if (isReady && networkState && prefNetworkType!!.contains(networkType)) {
            if (mAttached) {
                updateTraffic()
            }
            visibility = VISIBLE
        } else {
//            setVisibility(View.GONE);
        }
        //		Log.d(TAG, "Visibility (visible: ", View.VISIBLE, "; gone: ", View.GONE, "): ", getVisibility());
    }

    private fun loadPreferences() {
        try {
            val prefs = XSharedPreferences(
                BuildConfig.APPLICATION_ID,
                "${BuildConfig.APPLICATION_ID}_preferences"
            )

            // fetch all preferences first
            val localPrefForceUnit =
                prefs.getString(Common.KEY_FORCE_UNIT, Common.DEF_FORCE_UNIT.toString())
                    ?: Common.DEF_FORCE_UNIT.toString()
            val localPrefMinUnit =
                prefs.getString(Common.KEY_MIN_UNIT, Common.DEF_MIN_UNIT.toString())
                    ?: Common.DEF_MIN_UNIT.toString()
            val localPrefUnitMode =
                prefs.getString(Common.KEY_UNIT_MODE, Common.DEF_UNIT_MODE.toString())
                    ?: Common.DEF_UNIT_MODE.toString()
            val localPrefUnitFormat =
                prefs.getStringSet(Common.KEY_UNIT_FORMAT, Common.DEF_UNIT_FORMAT)
            val localPrefHideBelow =
                prefs.getInt(Common.KEY_HIDE_BELOW, Common.DEF_HIDE_BELOW)
            val localPrefShowSuffix =
                prefs.getBoolean(Common.KEY_SHOW_SUFFIX, Common.DEF_SHOW_SUFFIX)
            val localPrefFontSize =
                prefs.getFloat(Common.KEY_FONT_SIZE, Common.DEF_FONT_SIZE)
            val localPrefSuffix = prefs.getString(Common.KEY_SUFFIX, Common.DEF_SUFFIX.toString())
                ?: Common.DEF_SUFFIX.toString()
            val localPrefNetworkType =
                prefs.getStringSet(Common.KEY_NETWORK_TYPE, Common.DEF_NETWORK_TYPE)
            val localPrefNetworkSpeed =
                prefs.getStringSet(Common.KEY_NETWORK_SPEED, Common.DEF_NETWORK_SPEED)
            val localPrefDisplay =
                prefs.getString(Common.KEY_DISPLAY, Common.DEF_DISPLAY.toString())
                    ?: Common.DEF_DISPLAY.toString()
            val localPrefSwapSpeeds =
                prefs.getBoolean(Common.KEY_SWAP_SPEEDS, Common.DEF_SWAP_SPEEDS)
            val localPrefUpdateInterval =
                prefs.getInt(Common.KEY_UPDATE_INTERVAL, Common.DEF_UPDATE_INTERVAL)
            val localPrefFontColor =
                prefs.getBoolean(Common.KEY_FONT_COLOR, Common.DEF_FONT_COLOR)
            val localPrefColor = prefs.getInt(Common.KEY_COLOR, Common.DEF_COLOR)
            val localPrefFontStyle =
                prefs.getStringSet(Common.KEY_FONT_STYLE, Common.DEF_FONT_STYLE)
            val localSpeedWay =
                prefs.getString(Common.KEY_GET_SPEED_WAY, Common.DEF_SPEED_WAY.toString())
                    ?: Common.DEF_SPEED_WAY.toString()
            val localMinWidth = prefs.getInt(Common.KEY_MIN_WIDTH, Common.DEF_MIN_WIDTH)

//			only when all are fetched, set them to fields
            prefForceUnit = localPrefForceUnit.toInt()
            prefMinUnit = localPrefMinUnit.toInt()
            prefUnitMode = localPrefUnitMode.toInt()
            prefUnitFormat = localPrefUnitFormat
            prefSpeedWay = localSpeedWay.toInt()
            prefHideBelow = localPrefHideBelow
            prefShowSuffix = localPrefShowSuffix
            prefFontSize = localPrefFontSize
            prefSuffix = localPrefSuffix.toInt()
            prefNetworkType = localPrefNetworkType
            prefNetworkSpeed = localPrefNetworkSpeed
            prefDisplay = localPrefDisplay.toInt()
            prefSwapSpeeds = localPrefSwapSpeeds
            prefUpdateInterval = localPrefUpdateInterval
            prefFontColor = localPrefFontColor
            prefColor = localPrefColor
            prefFontStyle = localPrefFontStyle
            prefMinWidth = localMinWidth
        } catch (e: Exception) {
            Log.e(TAG, "loadPreferences failure ignored, using defaults. Exception: ", e)
        }
    }

    companion object {
        private val TAG = TrafficView::class.java.simpleName
        private val formatWithDecimal = DecimalFormat(" ##0.0")
        private val formatWithoutDecimal = DecimalFormat(" ##0")
        private const val DEFAULT_ICON_TINT = Color.WHITE
    }
}
