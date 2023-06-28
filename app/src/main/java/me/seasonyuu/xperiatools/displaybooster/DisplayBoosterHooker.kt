package me.seasonyuu.xperiatools.displaybooster

import android.util.Log
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import de.robv.android.xposed.XSharedPreferences

object DisplayBoosterHooker : YukiBaseHooker() {
    const val HOOK_PACKAGE = "com.sonymobile.displaybooster"

    private const val PREF_KEY_ENABLED = "displaybooster_enabled"
    override fun onHook() {
        findClass("com.sonymobile.displaybooster.FpsModeManager").hook {
            this.injectMember {
                method {
                    name { it == "updateFpsMode" }
                }.all()
                beforeHook {
                    field {
                        name { it == "mIsAutoHighRefreshRate" }
                    }.give()?.let {
                        val pref = XSharedPreferences("me.seasonyuu.xperiatools")
                        val enabled = pref.getBoolean(PREF_KEY_ENABLED, false)
                        if (enabled) {
                            it.setBoolean(this.instance, true)
                        }
                    }
                    field {
                        name { it == "mModeLevel" }
                    }.give()?.let {
                        val pref = XSharedPreferences("me.seasonyuu.xperiatools")
                        val enabled = pref.getBoolean(PREF_KEY_ENABLED, false)
                        if (enabled) {
                            it.setInt(this.instance, 0)
                        }
                    }
                }
            }
        }
    }
}