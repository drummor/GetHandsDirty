package com.android.sample.gethandsdirty.choreographer

import android.os.Looper
import android.view.Choreographer
import java.util.concurrent.Executors

abstract class DisplayEventReceiver {

    private val lock: Object = Object()
    private var choreographer: Choreographer? = null

    init {
        Executors.newSingleThreadExecutor().submit {
            synchronized(lock) {
                Looper.prepare()
                choreographer = Choreographer.getInstance()
                lock.notifyAll()
            }
            Looper.loop()
        }
    }

    abstract fun onVsync(frameTimeNanos: Long, frame: Int)


    fun scheduleVsyncLocked() {
        choreographer?.postFrameCallback {
            onVsync(it, 0)
        } ?: kotlin.run {
            synchronized(lock) {
                if (choreographer == null) {
                    lock.wait() //doNothing
                }
                scheduleVsyncLocked()
            }
        }
    }
}

