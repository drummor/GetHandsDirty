package com.android.sample.gethandsdirty.frames.srollframe

import android.view.Choreographer
import android.view.Window
import com.android.sample.gethandsdirty.frames.common.Constants.TIME_MILLIS_TO_NANO
import com.android.sample.gethandsdirty.frames.common.OnFrameChangeListener
import java.util.concurrent.CopyOnWriteArrayList

class ScrollFrameMonitor(private val window: Window) {
    init {
        window.decorView.viewTreeObserver.addOnScrollChangedListener {
            startCollectScrollFrame()
        }
    }

    private var lastFrameTime = 0L
    private var isScroll = false
    private var pendingCallback = false
    private val listenerList = CopyOnWriteArrayList<OnFrameChangeListener>()


    fun registerListener(lsn: OnFrameChangeListener) {
        listenerList.add(lsn)
    }

    private fun notifyListener(dropFrame: Int) {
        listenerList.forEach {
            it.onFrameChange(dropFrame)
        }
    }

    private fun startCollectScrollFrame() {
        isScroll = true
        tryScheduleNextCallback()
    }

    private fun tryScheduleNextCallback() {
        if (!pendingCallback) {
            pendingCallback = true
            Choreographer.getInstance().postFrameCallback(FrameCallback())
        }
    }

    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            pendingCallback = false
            if (isScroll) {
                if (lastFrameTime != 0L) {
                    val dropFrame =
                        (((frameTimeNanos - lastFrameTime) / TIME_MILLIS_TO_NANO / 16.6667f)).toInt()
                    notifyListener(dropFrame)
                }
                isScroll = false
                tryScheduleNextCallback()
                lastFrameTime = frameTimeNanos
            } else {
                lastFrameTime = 0
            }
        }
    }

    companion object {
        private val map = HashMap<Window, ScrollFrameMonitor>()
        fun getInstance(window: Window): ScrollFrameMonitor {
            if (map[window] == null) {
                val monitor = ScrollFrameMonitor(window)
                map[window] = monitor;
            }
            return map[window]!!
        }
    }
}

