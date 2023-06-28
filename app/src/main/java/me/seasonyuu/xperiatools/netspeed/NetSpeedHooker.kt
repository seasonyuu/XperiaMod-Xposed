package me.seasonyuu.xperiatools.netspeed

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker

object NetSpeedHooker : YukiBaseHooker() {
    const val HOOK_PACKAGE = "com.android.systemui"

    private var mTrafficView: TrafficView? = null
    private val positionCallback: PositionCallback = PositionCallbackTiramisu()

    @Suppress("MemberVisibilityCanBePrivate")
    const val APPEARANCE_LOW_PROFILE_BARS = 1 shl 2

    override fun onHook() {
        findClass("$HOOK_PACKAGE.statusbar.StatusBarStateControllerImpl").hook {
            injectMember {
                method {
                    name {
                        it == "setSystemBarAttributes"
                    }
                }.all()
                afterHook {
                    val view = mTrafficView ?: return@afterHook
                    (args[0] as? Int)?.let { appearance ->
                        if (appearance and APPEARANCE_LOW_PROFILE_BARS != 0) {
                            view.animate().alpha(0f).setDuration(350).start()
                        } else {
                            view.animate().alpha(1f).setDuration(250).start()
                        }
                    }
                }
            }
        }
        findClass("$HOOK_PACKAGE.statusbar.phone.DarkIconDispatcherImpl").hook {
            injectMember {
                method {
                    name {
                        it == "applyIconTint"
                    }
                    afterHook {
                        field {
                            name { it == "mIconTint" }
                        }.give()?.getInt(this.instance)?.let { iconTint ->
                            mTrafficView?.let {
                                it.iconTint = iconTint
                                it.refreshColor()
                            }
                        }
                    }
                }
            }
        }
        findClass("$HOOK_PACKAGE.statusbar.phone.PhoneStatusBarView").hook {
            injectMember {
                method {
                    name {
                        it == "onAttachedToWindow"
                    }
                }.all()
                afterHook {
                    (this.instance as? View)?.let { root ->
                        val clock: View? = root.findViewById(
                            root.resources.getIdentifier(
                                "clock",
                                "id",
                                HOOK_PACKAGE
                            )
                        )
                        if (clock != null) {
                            val trafficView = TrafficView(root.context)

                            if (clock is TextView) {
                                val color = clock.currentTextColor
                                trafficView.iconTint = color
                                trafficView.refreshColor()
                            }
                            trafficView.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL

                            trafficView.mPositionCallback = positionCallback
                            positionCallback.setup(root, trafficView)

                            mTrafficView = trafficView
                        }
                    }
                }
            }
        }
    }
}