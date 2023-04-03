package com.android.sample.gethandsdirty.frames.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes

class CostArrayAdapter<T>(context: Context, @LayoutRes resource: Int, list: List<T>) :
    ArrayAdapter<T>(context, resource, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Thread.sleep(10)
        return super.getView(position, convertView, parent)
    }
}