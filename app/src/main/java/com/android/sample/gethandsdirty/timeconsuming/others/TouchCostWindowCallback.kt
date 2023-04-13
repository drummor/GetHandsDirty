package com.android.sample.gethandsdirty.timeconsuming.others

import android.view.MotionEvent
import android.view.Window

class TouchCostWindowCallback(private val windowCallback: Window.Callback) :
    Window.Callback by windowCallback {
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val start = System.currentTimeMillis()
        val rst = windowCallback.dispatchTouchEvent(event)
        val cost = System.currentTimeMillis() - start
        // 监控
        return rst
    }
}