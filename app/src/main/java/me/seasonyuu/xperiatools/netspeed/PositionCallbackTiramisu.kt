package me.seasonyuu.xperiatools.netspeed

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import me.seasonyuu.xperiatools.utils.Utils

class PositionCallbackTiramisu : PositionCallback {
    private var mSystemIconArea: ViewGroup? = null

    override fun setup(root: View, trafficView: View) {
        mSystemIconArea = Utils.findViewById(root, "system_icon_area", NetSpeedHooker.HOOK_PACKAGE)
        mSystemIconArea?.addView(trafficView, 0)
    }
}