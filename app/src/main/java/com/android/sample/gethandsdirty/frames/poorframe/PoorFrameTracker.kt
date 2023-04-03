package com.android.sample.gethandsdirty.frames.poorframe

import android.view.Choreographer
import android.view.Choreographer.FrameCallback
import com.android.sample.gethandsdirty.choreographer.NANOS_PER_MS
import com.android.sample.gethandsdirty.frames.common.OnFrameChangeListener
import java.util.concurrent.CopyOnWriteArrayList

class PoorFrameTracker {
    private var mLastFrameTime = -1L
    private var active = true
    fun stop() {
        this.active = false
    }

    fun init() {
        active = true
        Choreographer.getInstance().postFrameCallback(object : FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (mLastFrameTime == -1L) {
                    mLastFrameTime = frameTimeNanos
                }
                val dropFrame =
                    ((frameTimeNanos - mLastFrameTime) / NANOS_PER_MS / 16.6667f).toInt()
                notifyListener(dropFrame)
                if (active) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
                mLastFrameTime = frameTimeNanos
            }
        })
    }

    private val listenerList = CopyOnWriteArrayList<OnFrameChangeListener>()

    fun register(listener: OnFrameChangeListener) {
        listenerList.add(listener)
    }

    fun notifyListener(dropFrame: Int) {
        listenerList.forEach {
            it.onFrameChange(dropFrame)
        }
    }

    companion object {
        fun getInstance(): PoorFrameTracker {
            return PoorFrameTracker().apply {
                this.init()
            }
        }
    }
}


