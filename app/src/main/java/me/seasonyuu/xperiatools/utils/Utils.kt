package me.seasonyuu.xperiatools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.highcapable.yukihookapi.YukiHookAPI
import me.seasonyuu.xperiatools.BuildConfig
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
    fun isModuleActivated( ): Boolean {
        return YukiHookAPI.Status.isModuleActive
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
