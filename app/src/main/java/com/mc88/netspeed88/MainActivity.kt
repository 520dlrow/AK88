package com.mc88.netspeed88

import android.net.TrafficStats
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var tvDownload: TextView
    private lateinit var tvUpload: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var lastRx = 0L
    private var lastTx = 0L
    private var lastTime = 0L

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNetworkSpeed()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDownload = findViewById(R.id.tvDownload)
        tvUpload = findViewById(R.id.tvUpload)
        
        findViewById<LinearLayout>(R.id.rootLayout).setOnClickListener {
            lastRx = TrafficStats.getTotalRxBytes()
            lastTx = TrafficStats.getTotalTxBytes()
            lastTime = System.currentTimeMillis()
            tvDownload.text = "0.0 B/s"
            tvUpload.text = "0.0 B/s"
        }

        resetValues()
    }

    override fun onResume() {
        super.onResume()
        resetValues()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun resetValues() {
        lastRx = TrafficStats.getTotalRxBytes()
        lastTx = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
    }

    private fun updateNetworkSpeed() {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastTime
        if (timeDiff <= 0) return

        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()

        val rxSpeed = ((currentRx - lastRx) / timeDiff) * 1000
        val txSpeed = ((currentTx - lastTx) / timeDiff) * 1000

        tvDownload.text = formatSpeed(rxSpeed)
        tvUpload.text = formatSpeed(txSpeed)

        lastRx = currentRx
        lastTx = currentTx
        lastTime = currentTime
    }

    private fun formatSpeed(bytesPerSecond: Long): String {
        if (bytesPerSecond <= 0) return "0.0 B/s"
        val kb = bytesPerSecond / 1024.0
        val mb = kb / 1024.0

        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
            else -> "${bytesPerSecond} B/s"
        }
    }
}
