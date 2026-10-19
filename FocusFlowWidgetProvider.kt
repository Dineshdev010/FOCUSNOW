package com.focusflow.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import com.focusflow.app.data.FocusFlowRepository

class FocusFlowWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, FocusFlowWidgetProvider::class.java)
            val allIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allIds != null && allIds.isNotEmpty()) {
                onUpdate(context, appWidgetManager, allIds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val repo = FocusFlowRepository(context)
        val streak = repo.getStreakDays()
        val reelsBlocked = repo.getReelsBlockedCount()
        val totalMins = repo.getTotalFocusMinutes()
        val savedHoursStr = String.format(java.util.Locale.US, "%.1f hrs saved", (reelsBlocked * 3 + totalMins) / 60.0)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_focus_flow)

            views.setTextViewText(R.id.widget_streak, "🔥 $streak Day Streak")
            views.setTextViewText(R.id.widget_time_saved, savedHoursStr)
            views.setTextViewText(R.id.widget_traps_blocked, "$reelsBlocked Traps Blocked Today")

            // Main Click Intent -> Opens Main App Home
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAVIGATE_TAB", "Home")
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingOpen)

            // "Focus ⚡" Action Click Intent -> Opens Focus Tab and starts session
            val openFocusIntent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAVIGATE_TAB", "Focus")
                putExtra("START_FOCUS_SESSION", true)
            }
            val pendingFocus = PendingIntent.getActivity(
                context,
                1,
                openFocusIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_action_btn, pendingFocus)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, FocusFlowWidgetProvider::class.java)
                val ids = appWidgetManager.getAppWidgetIds(thisWidget)
                if (ids != null && ids.isNotEmpty()) {
                    val provider = FocusFlowWidgetProvider()
                    provider.onUpdate(context, appWidgetManager, ids)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun pinWidgetToHomeScreen(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, FocusFlowWidgetProvider::class.java)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val pinnedWidgetCallbackIntent = Intent(context, FocusFlowWidgetProvider::class.java).apply {
                        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    }
                    val successCallback = PendingIntent.getBroadcast(
                        context,
                        0,
                        pinnedWidgetCallbackIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    return appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                }
            }
            return false
        }
    }
}
