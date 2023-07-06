package me.seasonyuu.xperiatools.displaybooster

import android.util.Log
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current

object DisplayHooker : YukiBaseHooker() {
    override fun onHook() {
        findClass("com.android.server.wm.WindowManagerService").hook {
            injectMember {
                method {
                    name { it == "setForcedDisplaySize" }
                }.all()
                afterHook {
                    this.args.forEachIndexed { index, arg ->
                        Log.d("WM_SIZE", "arg#$index: $arg")
                    }
                    field {
                        name { it == "mRoot" }
                    }.get(instance).let { mRoot ->
                        // com.android.server.wm.RootWindowContainer
                        val displayId = args[0] as? Int ?: return@let
                        val width = args[1] as? Int ?: return@let
                        val height = args[2] as? Int ?: return@let
                        Log.d("WM_SIZE", "mRoot: ${mRoot.current()?.name}")
                        mRoot.current()?.method {
                            name { it == "getDisplayContent" }
                            paramCount(1)
                        }?.let { getDisplayContent ->
                            getDisplayContent.invoke<Any>(displayId)
                                ?.let { displayContent ->
                                    displayContent.current {
                                        method {
                                            name { it == "setMaxUiWidth" }
                                            paramCount(1)
                                        }.invoke<Void>(width)
                                    }
                                    displayContent.current().method {
                                        name { it == "updateBaseDisplayMetrics" }
                                        paramCount(5)
                                    }.let { updateBaseDisplayMetrics ->
                                        if (width == 1644) {
                                            val scale = 640f / 420f
                                            updateBaseDisplayMetrics.invoke<Void>(
                                                width,
                                                height,
                                                630f,
                                                428f * scale,
                                                427f * scale
                                            )
                                        } else {

                                        }
                                    }
                                }
                        } ?: run {
                            Log.w("WM_SIZE", "Cannot getDisplayContent")
                        }
                    }
                }
            }
        }
    }
}