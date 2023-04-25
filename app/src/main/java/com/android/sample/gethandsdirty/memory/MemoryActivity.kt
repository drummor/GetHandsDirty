package com.android.sample.gethandsdirty.memory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.android.sample.gethandsdirty.R

class MemoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)
        findViewById<Button>(R.id.bt_memory).setOnClickListener {
            val info = MemoryCheck.fetchMemoryInfo()
            Log.d("drummor", " ->> $info")
        }
    }
    
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MemoryActivity::class.java)
            context.startActivity(starter)
        }
    }
}