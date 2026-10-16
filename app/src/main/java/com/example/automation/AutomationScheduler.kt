package com.example.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AutomationScheduler(private val context: Context) {

    fun scheduleWorkflowAlarm(workflowId: String, triggerTimeMs: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent("com.example.omnia.EXECUTE_AUTOMATION").apply {
                putExtra("WORKFLOW_ID", workflowId)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, workflowId.hashCode(), intent, flags)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
            Log.d("AutomationScheduler", "Scheduled alarm for workflow $workflowId at $triggerTimeMs")
        } catch (e: Exception) {
            Log.e("AutomationScheduler", "Failed to schedule alarm", e)
        }
    }

    fun cancelWorkflowAlarm(workflowId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent("com.example.omnia.EXECUTE_AUTOMATION")
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, workflowId.hashCode(), intent, flags)
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("AutomationScheduler", "Failed to cancel alarm", e)
        }
    }
}
