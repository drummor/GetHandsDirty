package com.android.sample.gethandsdirty.timeconsuming.syncbarrier

import android.os.*
import java.lang.reflect.Field

object SyncBarrierDetector {
    private const val CHECK_STRICTLY_MAX_COUNT = 3
    private var barrierCount = 0

    fun detect() {
        val mainQueue: MessageQueue = Looper.getMainLooper().queue
        val field: Field = mainQueue.javaClass.getDeclaredField("mMessages")
        field.isAccessible = true
        val mMessage: Message = field.get(mainQueue) as Message //通过反射得到当前正在等待执行的Message
        if (mMessage != null) {
            val whenTime = mMessage.getWhen() - SystemClock.uptimeMillis()
            if (whenTime < -3000L && mMessage.target == null) { //target == null则为sync barrier
                val token = mMessage.arg1
                startCheckLeaking(token)
            }
        }
    }

    private fun startCheckLeaking(token: Int) {
        var checkCount = 0
        barrierCount = 0
        while (checkCount < CHECK_STRICTLY_MAX_COUNT) {
            checkCount++
            val latestToken: Int = getSyncBarrierToken()
            if (token != latestToken) {     //token变了，不是同一个barrier，return
                break
            }
            if (detectSyncBarrierOnce()) {
                //发生了sync barrier泄漏
             //   Looper.getMainLooper().queue.removeSyncBarrier(token) //手动remove泄漏的sync barrier
                break
            }
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun getSyncBarrierToken(): Int {
     return   1

    }

    private fun detectSyncBarrierOnce(): Boolean {
        val mainHandler: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.arg1 === 0) {
                    barrierCount++ //收到了异步消息，count++
                } else if (msg.arg1 === 1) {
                    barrierCount = 0 //收到了同步消息，说明同步屏障不在, count设置为0
                }
            }
        }
        val asyncMessage: Message = Message.obtain().apply {
            this.isAsynchronous = true
            this.target = mainHandler
            this.arg1 = 0
        }

        val syncNormalMessage: Message = Message.obtain().apply {
            arg1 = 1
        }

        mainHandler.sendMessage(asyncMessage) //发送一个异步消息
        mainHandler.sendMessage(syncNormalMessage) //发送一个同步消息
        return barrierCount > 3
    }
}