package me.seasonyuu.xperiatools

import android.os.Build
import androidx.annotation.RequiresApi
import com.highcapable.yukihookapi.BuildConfig
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import me.seasonyuu.xperiatools.displaybooster.DisplayBoosterHooker
import me.seasonyuu.xperiatools.netspeed.NetSpeedHooker

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
object HookEntry : IYukiHookXposedInit {
    override fun onInit() {
        configs {
            isDebug = BuildConfig.DEBUG
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onHook() {
        encase {
            loadApp(DisplayBoosterHooker.HOOK_PACKAGE, DisplayBoosterHooker)
            loadApp(NetSpeedHooker.HOOK_PACKAGE, NetSpeedHooker)
        }
    }
}