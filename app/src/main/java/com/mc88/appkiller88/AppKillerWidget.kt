package com.mc88.appkiller88

import android.app.ActivityManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class AppKillerWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.mc88.appkiller88.KILL_ALL") {
            val prefs = context.getSharedPreferences("AK88Prefs", Context.MODE_PRIVATE)
            val selectedApps = prefs.getStringSet("selected_apps", emptySet()) ?: emptySet()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            var killedCount = 0
            for (pkg in selectedApps) {
                am.killBackgroundProcesses(pkg)
                killedCount++
            }
            Toast.makeText(context, "AK88: Killed $killedCount apps", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            val intent = Intent(context, AppKillerWidget::class.java).apply {
                action = "com.mc88.appkiller88.KILL_ALL"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.widgetIcon, pendingIntent)
            views.setOnClickPendingIntent(R.id.widgetText, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
