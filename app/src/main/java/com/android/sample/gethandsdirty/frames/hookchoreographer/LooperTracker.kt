package com.android.sample.gethandsdirty.frames.hookchoreographer

import android.os.Looper
import android.util.Log
import android.util.SparseIntArray
import android.view.Choreographer
import com.android.sample.gethandsdirty.frames.hookchoreographer.utils.ReflectUtils
import java.lang.reflect.Method

class LooperTracker : LooperMonitor.LooperListener {
    private val tag = "MatrixFrameTracker"

    var frameIntervalNanos = 16666667L
    private val CALLBACK_INPUT = 0
    private val addCallbackLockedMethodName = "addCallbackLocked"

    private lateinit var callbackQueueLock: Any
    private lateinit var callbackQueues: Array<Any>
    private var addInputQueueMethod: Method? = null
    private lateinit var vsyncReceiver: Any


    private var isVsyncFrame: Boolean = false
    private var startTime = 0L

    companion object {
        private val frameTracker: LooperTracker = LooperTracker()
        fun getFrameTracker(): LooperTracker {
            frameTracker.init()
            return frameTracker
        }
    }

    private var isInit = false
    fun init() {
        if (isInit) {
            return
        }
        isInit = true
        initReflect()
        addInputCallBack {
            isVsyncFrame = true
        }
        LooperMonitor.getInstance(Looper.getMainLooper()).registerListener(this)
    }

    private fun initReflect() {
        val choreographer = Choreographer.getInstance()
        frameIntervalNanos =
            ReflectUtils.reflectObject(choreographer, "mFrameIntervalNanos", 16666667L)
        Log.d(tag, "反射的结果：$frameIntervalNanos")
        vsyncReceiver =

            ReflectUtils
                .reflectObject<Any>(choreographer, "mDisplayEventReceiver", null)
        callbackQueueLock = ReflectUtils
            .reflectObject(choreographer, "mLock", Any())
        callbackQueues = ReflectUtils
            .reflectObject<Array<Any>>(choreographer, "mCallbackQueues", null)
        if (null != callbackQueues) {
            addInputQueueMethod = ReflectUtils.reflectMethod(
                callbackQueues[CALLBACK_INPUT],
                addCallbackLockedMethodName,
                Long::class.javaPrimitiveType,
                Any::class.java,
                Any::class.java
            )
        }
    }

    private fun addInputCallBack(callback: Runnable) {
        try {
            synchronized(callbackQueueLock) {
                addInputQueueMethod!!.invoke(
                    callbackQueues[CALLBACK_INPUT],
                    -1,
                    Runnable { callback.run() },
                    null
                )
            }
        } catch (e: java.lang.Exception) {
        }
    }

    private fun getIntendedFrameTimeNs(defaultValue: Long): Long {
        try {
            return ReflectUtils.reflectObject(vsyncReceiver, "mTimestampNanos", defaultValue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultValue
    }

    override fun dispatchStart(originString: String) {
        Log.d(tag, originString)
        startTime = System.nanoTime()
    }

    override fun dispatchEnd(originString: String) {
        if (isVsyncFrame) {
            val endNs = System.nanoTime()
            val intendedFrameTimeNs = getIntendedFrameTimeNs(startTime)
            val jit = endNs - intendedFrameTimeNs
            val dropFrame = ((jit) / frameIntervalNanos).toInt()
            notifyListener(intendedFrameTimeNs, endNs, dropFrame)
            addInputCallBack { isVsyncFrame = true }
        }
        isVsyncFrame = false
    }


    private fun notifyListener(startNs: Long, endNs: Long, dropFrame: Int) {
        listenerList.forEach { l ->
            l.onFrame(startNs, endNs, dropFrame)
        }
    }

    private val listenerList = ArrayList<FrameListener>();
    fun register(listener: FrameListener) {
        this.listenerList.add(listener)
    }

    interface FrameListener {
        fun onFrame(startNs: Long, endNs: Long, dropFrame: Int)
    }

    class FPSListener : FrameListener {
        private val ary = SparseIntArray()
        fun getTotalInfo(): SparseIntArray {
            return ary;
        }

        override fun onFrame(startNs: Long, endNs: Long, droppedFrames: Int) {
            val duration = ((endNs - startNs) / 1000000f).toInt()
            var count = ary.get(duration)
            ary.put(duration, ++count)
        }
    }

    enum class DropStatus(var index: Int) {
        DROPPED_FROZEN(4),
        DROPPED_HIGH(3),
        DROPPED_MIDDLE(2),
        DROPPED_NORMAL(1),
        DROPPED_BEST(0);
    }
}