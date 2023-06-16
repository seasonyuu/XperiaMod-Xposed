package me.seasonyuu.xperiatools

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.highcapable.yukihookapi.BuildConfig
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

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
            loadApp("com.sonymobile.displaybooster") {
                findClass("com.sonymobile.displaybooster.FpsModeManager").hook {
                    this.injectMember {
                        method {
                            name { it == "updateFpsMode" }
                        }.all()
                        beforeHook {
                            Log.d("displaybooster", "beforehook")
                            field {
                                name { it == "mIsAutoHighRefreshRate" }
                            }.give()?.let {
                                Log.d(
                                    "displaybooster",
                                    "change mIsAutoHighRefreshRate ${it.getBoolean(this.instance)}"
                                )
                                it.setBoolean(this.instance, true)
                            }
                            field {
                                name { it == "mModeLevel" }
                            }.give()?.let {
                                it.setInt(this.instance, 0)
                                Log.d(
                                    "displaybooster",
                                    "change mModeLevel ${it.getInt(this.instance)}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}