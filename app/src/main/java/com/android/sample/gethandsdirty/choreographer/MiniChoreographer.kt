package com.android.sample.gethandsdirty.choreographer


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock

const val NANOS_PER_MS = 1000000

class MiniChoreographer(looper: Looper) {
    companion object {
        private val sThreadInstance = object : ThreadLocal<MiniChoreographer>() {
            override fun initialValue(): MiniChoreographer {
                val looper = Looper.myLooper()
                    ?: throw IllegalStateException("The current thread must have a looper!")
                return MiniChoreographer(looper)
            }
        }

        fun getInstance(): MiniChoreographer {
            return sThreadInstance.get()
        }

        const val CALLBACK_INPUT = 0
        private const val CALLBACK_INSETS_ANIMATION = 1
        private const val CALLBACK_TRAVERSAL = 2
        private const val CALLBACK_LAST = CALLBACK_TRAVERSAL

        private const val MSG_DO_FRAME = 0
        private const val MSG_DO_SCHEDULE_CALLBACK = 1
    }

    private var mHandler: FrameHandler = FrameHandler(looper)
    private var mDisplayEventReceiver: DisplayEventReceiver = FrameDisplayEventReceiver()
    private val mCallbackQueues = Array(CALLBACK_LAST + 1) { CallbackQueue() }
    private val mLock = Object()

    fun postCallback(
        callbackType: Int = CALLBACK_INPUT, delayMillis: Long = 0, action: FrameCallback,
    ) {
        synchronized(mLock) {
            //两件事件：
            //1.根据事件类型放到对应的队列里
            //2.申请垂直同步,如果时间未到通过handler发送一个延时消息处理
            val now = SystemClock.uptimeMillis()
            val dueTime = now + delayMillis
            mCallbackQueues[callbackType].addCallbackLocked(dueTime, action)
            if (dueTime <= now) {
                scheduleFrameLocked()
            } else {
                mHandler.obtainMessage(MSG_DO_SCHEDULE_CALLBACK, action).apply {
                    this.arg1 = callbackType
                    this.isAsynchronous = true
                }.let { mHandler.sendMessageAtTime(it, dueTime) }
            }
        }
    }

    private fun scheduleFrameLocked() {
        mDisplayEventReceiver.scheduleVsyncLocked()
    }

    /*
    取出对应类型符合时间条件的callback处理
     */
    private fun doCallbacks(callbackType: Int, frameTimeNanos: Long) {
        var callbacks: CallbackRecord?
        val now = System.nanoTime()
        callbacks = mCallbackQueues[callbackType].extractDueCallbacksLocked(now / NANOS_PER_MS)
        if (callbacks == null) {
            return
        }
        var callback = callbacks
        while (callback != null) {
            callback.run(frameTimeNanos)
            callback = callback.next
        }
    }

    private fun obtainCallbackLocked(dueTime: Long, action: FrameCallback): CallbackRecord {
        return CallbackRecord().apply {
            this.dueTime = dueTime
            this.action = action
        }
    }

    private var mLastFrameTimeNanos = 0L
    private fun doFrame(mTimestampNanos: Long, mFrame: Int) {
        if (mTimestampNanos < mLastFrameTimeNanos) {
            //垂直信号的时间小于已经处理过的最小时间了，申请监听一个脉冲
            scheduleFrameLocked()
            return
        }
        mLastFrameTimeNanos = mTimestampNanos

        //处理input事件
        doCallbacks(CALLBACK_INPUT, mTimestampNanos)
        //处理动画，我们平时使用choreographer postCallback就是这个类型的
        doCallbacks(CALLBACK_INSETS_ANIMATION, mTimestampNanos)
        //处理绘制，通常是ViewRootImpl设置的监听
        doCallbacks(CALLBACK_TRAVERSAL, mTimestampNanos)
    }

    private inner class FrameHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_DO_FRAME -> doFrame(System.nanoTime(), 0)
                MSG_DO_SCHEDULE_CALLBACK -> scheduleFrameLocked()
            }
        }
    }

    inner class FrameDisplayEventReceiver : DisplayEventReceiver(), Runnable {
        private var mTimestampNanos: Long = 0
        private var mFrame: Int = 0

        //监听了垂直信号开始处理
        override fun onVsync(timestampNanos: Long, frame: Int) {
            mTimestampNanos = timestampNanos
            mFrame = frame
            val msg = Message.obtain(mHandler, this)
            msg.isAsynchronous = true
            mHandler.sendMessageAtTime(msg, timestampNanos / 1000000)
        }

        override fun run() {
            doFrame(mTimestampNanos, mFrame)
        }
    }

    inner class CallbackQueue {
        private var mHead: CallbackRecord? = null

        //取出符合条件的callback
        fun extractDueCallbacksLocked(now: Long): CallbackRecord? {
            val callbacks = mHead
            if ((callbacks?.dueTime ?: 0) > now) {//未找到比改时间小的直接返回
                return null
            }
            var last = callbacks
            var next = last?.next
            while (next != null) {
                if (next.dueTime > now) {
                    last?.next = null
                    break
                }
                last = next
                next = next.next
            }
            mHead = next
            return callbacks
        }

        fun addCallbackLocked(dueTime: Long, action: FrameCallback) {
            val callback: CallbackRecord = obtainCallbackLocked(dueTime, action)
            var entry = mHead
            if (entry == null) {
                mHead = callback //如果当前为空直接将该recode设置为头
                return
            }
            if (dueTime < entry.dueTime) { //如果不为空且小于头部的时间，就将该record设置为头
                callback.next = entry
                mHead = callback
                return
            }
            while (entry?.next != null) {//依次往后找 直到找到不必起时间小的插入其中
                if (dueTime < entry.next!!.dueTime) {
                    callback.next = entry.next
                    break
                }
                entry = entry.next
            }
            entry?.next = callback
        }
    }

    inner class CallbackRecord {
        var next: CallbackRecord? = null
        var dueTime: Long = 0
        var action: FrameCallback? = null
        fun run(time: Long) {
            action?.doFrame(time)
        }
    }
}

interface FrameCallback {
    fun doFrame(frameTimeNanos: Long)
}
