package com.android.sample.gethandsdirty.frames.common

interface OnFrameChangeListener {
    fun onFrameChange(dropFrame: Int)
}

abstract class OnFpsChangeListener : OnFrameChangeListener {
    var sumFrameCost = 0f
    var sumFrames = 0
    var lastCost = 0L
    override fun onFrameChange(dropFrame: Int) {
        lastCost = ((dropFrame + 1) * 16.6667f).toLong()
        sumFrameCost += lastCost
        sumFrames += 1
        if (sumFrameCost > 200) {
            val fps = (1000f * sumFrames / sumFrameCost).toInt()
            onFpsChange(fps.coerceAtMost(60))
            sumFrameCost = 0f
            sumFrames = 0
        }
    }

    abstract fun onFpsChange(fps: Int)
}