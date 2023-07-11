package me.seasonyuu.xperiatools.displaybooster

import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import java.io.DataOutputStream

object DisplayUtils {
    @Suppress("MemberVisibilityCanBePrivate")
    const val DEFAULT_4K_DENSITY = 630

    val Screen4k = Rect(0, 0, 1644, 3840)

    enum class FpsMode {
        Fps60,
        Fps120
    }

    fun restoreDisplayMode() {
        try {
            val command =
                "cmd display clear-user-preferred-display-mode\nwm size reset\nwm density reset"
            val p = Runtime.getRuntime().exec("su")
            val stream = p.outputStream
            val outputStream = DataOutputStream(stream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setWmDensity(density: Int = DEFAULT_4K_DENSITY) {
        try {
            val command =
                "wm density $density"
            val p = Runtime.getRuntime().exec("su")
            val stream = p.outputStream
            val outputStream = DataOutputStream(stream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setWmSize(width: Int, height: Int) {
        try {
            val command =
                "wm size ${width}x$height"
            val p = Runtime.getRuntime().exec("su")
            val stream = p.outputStream
            val outputStream = DataOutputStream(stream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun enable4kMode(fpsMode: FpsMode = FpsMode.Fps120) {
        try {
            val fps = if (fpsMode == FpsMode.Fps60) "60.000004" else "120.00001"
            val command = "cmd display set-user-preferred-display-mode 1644 3840 $fps"
            Log.d("Display", "cmd:[$command]")
            val p = Runtime.getRuntime().exec("su")
            val stream = p.outputStream
            val outputStream = DataOutputStream(stream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}