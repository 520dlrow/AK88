package com.mc88.netspeed88

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.TrafficStats
import android.os.SystemClock
import android.widget.RemoteViews
import java.util.Locale

class NetSpeedWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, true)
        }
        scheduleUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = android.content.ComponentName(context, NetSpeedWidget::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        if (intent.action == "com.mc88.netspeed88.UPDATE_WIDGET" || intent.action == Intent.ACTION_SCREEN_ON) {
            for (appWidgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId, false)
            }
            if (intent.action == "com.mc88.netspeed88.UPDATE_WIDGET") {
                scheduleUpdate(context)
            }
        } else if (intent.action == "com.mc88.netspeed88.CLICK_WIDGET") {
            for (appWidgetId in allWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId, true)
            }
        }
    }

    private fun scheduleUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NetSpeedWidget::class.java).apply {
            action = "com.mc88.netspeed88.UPDATE_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerTime = SystemClock.elapsedRealtime() + 3000
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, triggerTime, pendingIntent)
    }

    companion object {
        private var lastRx = 0L
        private var lastTx = 0L
        private var lastTime = 0L

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, forceReset: Boolean) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val clickIntent = Intent(context, NetSpeedWidget::class.java).apply {
                action = "com.mc88.netspeed88.CLICK_WIDGET"
            }
            val clickPendingIntent = PendingIntent.getBroadcast(
                context, 1, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetDownload, clickPendingIntent)
            views.setOnClickPendingIntent(R.id.widgetUpload, clickPendingIntent)

            val currentTime = System.currentTimeMillis()
            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()

            if (lastTime == 0L || forceReset || (currentTime - lastTime) > 10000) {
                lastTime = currentTime
                lastRx = currentRx
                lastTx = currentTx
                views.setTextViewText(R.id.widgetDownload, "0.0 MB/s")
                views.setTextViewText(R.id.widgetUpload, "0.0 MB/s")
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            val timeDiff = currentTime - lastTime
            val rxSpeed = ((currentRx - lastRx) / timeDiff) * 1000
            val txSpeed = ((currentTx - lastTx) / timeDiff) * 1000

            views.setTextViewText(R.id.widgetDownload, formatSpeed(rxSpeed))
            views.setTextViewText(R.id.widgetUpload, formatSpeed(txSpeed))

            lastRx = currentRx
            lastTx = currentTx
            lastTime = currentTime

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun formatSpeed(bytesPerSecond: Long): String {
            if (bytesPerSecond <= 0) return "0 B/s"
            val kb = bytesPerSecond / 1024.0
            val mb = kb / 1024.0

            return when {
                mb >= 1.0 -> String.format(Locale.US, "%.1f MB/s", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
                else -> "$bytesPerSecond B/s"
            }
        }
    }
}
