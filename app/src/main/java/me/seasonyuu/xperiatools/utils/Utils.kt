package me.seasonyuu.xperiatools.utils

import android.annotation.SuppressLint
import android.view.View
import com.highcapable.yukihookapi.YukiHookAPI
import java.io.DataOutputStream

object Utils {
    fun <T : View?> findViewById(root: View, id: String?, packageName: String?): T? {
        var result: T? = null
        result = root.findViewById<View>(root.resources.getIdentifier(id, "id", packageName)) as T
        if (result == null) result =
            root.findViewById<View>(root.resources.getIdentifier(id, "id", null)) as T
        return result
    }

    @Suppress("DEPRECATION")
    @SuppressLint("WorldReadableFiles")
    fun isModuleActivated(): Boolean {
        return YukiHookAPI.Status.isModuleActive
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

    fun enable4kMode() {
        try {
            val command =
                "cmd display set-user-preferred-display-mode 1644 3840 120.00001\nsleep 2s\nwm size 1644x3840"
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

    fun restartSystemUi() {
        try {
            val command = "pkill -f com.android.systemui"
            val p = Runtime.getRuntime().exec("su")
            val stream = p.outputStream
            val outputStream = DataOutputStream(stream)
            outputStream.write(command.toByteArray())
            outputStream.flush()
            outputStream.close()
            stream.close()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }
}
