package ai.openclaw.app.widget

import ai.openclaw.app.R
import ai.openclaw.app.VoiceTalkOverlayActivity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/**
 * Android AppWidgetProvider for the Voice Talk Home Screen Widget.
 * Launches VoiceTalkOverlayActivity to immediately activate Voice Talk mode in a floating translucent overlay.
 */
class VoiceTalkAppWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    for (appWidgetId in appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId)
    }
  }

  companion object {
    const val ACTION_START_VOICE_TALK = "ai.openclaw.app.action.START_VOICE_TALK"

    fun updateAppWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
    ) {
      val intent =
        Intent(context, VoiceTalkOverlayActivity::class.java).apply {
          action = ACTION_START_VOICE_TALK
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

      val pendingIntent =
        PendingIntent.getActivity(
          context,
          0,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

      val views =
        RemoteViews(context.packageName, R.layout.widget_voice_talk).apply {
          setOnClickPendingIntent(R.id.widget_voice_talk_root, pendingIntent)
          setOnClickPendingIntent(R.id.widget_voice_talk_mic_icon, pendingIntent)
        }

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
