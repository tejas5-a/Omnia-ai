package com.example.assistant.engine

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import com.example.assistant.model.AssistantCommand
import com.example.assistant.model.CommandExecutionResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceControlExecutor(private val context: Context) {

    fun execute(command: AssistantCommand): CommandExecutionResult {
        return when (command) {
            is AssistantCommand.ToggleFlashlight -> toggleFlashlight(command.enable)
            is AssistantCommand.SetVolume -> setVolume(command.levelPercent)
            is AssistantCommand.GetVolume -> getVolume()
            is AssistantCommand.GetBatteryStatus -> getBatteryStatus()
            is AssistantCommand.GetDateTime -> getDateTime()
            is AssistantCommand.CreateAlarm -> createAlarm(command.hour, command.minute, command.label)
            is AssistantCommand.CreateTimer -> createTimer(command.durationSeconds, command.label)
            is AssistantCommand.OpenApp -> openApp(command.appName)
            is AssistantCommand.OpenSettings -> openSettings(command.settingsType)
            else -> CommandExecutionResult(
                isSuccess = false,
                message = "Unsupported command for DeviceControlExecutor",
                spokenResponse = "I can't perform that system action directly."
            )
        }
    }

    private fun toggleFlashlight(enable: Boolean): CommandExecutionResult {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull()
            if (cameraManager != null && cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                val statusText = if (enable) "turned on" else "turned off"
                CommandExecutionResult(
                    isSuccess = true,
                    message = "Flashlight $statusText",
                    spokenResponse = "Flashlight $statusText."
                )
            } else {
                CommandExecutionResult(
                    isSuccess = false,
                    message = "Flashlight unavailable on this device",
                    spokenResponse = "Sorry, flashlight is not available on this device."
                )
            }
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Flashlight error: ${e.localizedMessage}",
                spokenResponse = "I couldn't control the flashlight. Please check camera permissions."
            )
        }
    }

    private fun setVolume(percent: Int): CommandExecutionResult {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVolume = (maxVolume * (percent / 100f)).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, AudioManager.FLAG_SHOW_UI)
                CommandExecutionResult(
                    isSuccess = true,
                    message = "Media volume set to $percent%",
                    spokenResponse = "Volume set to $percent percent."
                )
            } else {
                CommandExecutionResult(
                    isSuccess = false,
                    message = "Audio service unavailable",
                    spokenResponse = "Audio settings are unavailable right now."
                )
            }
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Volume setting failed: ${e.localizedMessage}",
                spokenResponse = "Could not adjust the volume."
            )
        }
    }

    private fun getVolume(): CommandExecutionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager != null) {
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val percent = if (max > 0) (current * 100 / max) else 0
            return CommandExecutionResult(
                isSuccess = true,
                message = "Current volume is $percent%",
                spokenResponse = "Your volume is currently at $percent percent."
            )
        }
        return CommandExecutionResult(false, "Audio service unavailable", "Unable to read volume.")
    }

    private fun getBatteryStatus(): CommandExecutionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val isCharging = bm?.isCharging ?: false

        val response = if (level >= 0) {
            val stateText = if (isCharging) "and currently charging" else ""
            "Your battery is at $level percent $stateText."
        } else {
            "Battery status unavailable."
        }

        return CommandExecutionResult(
            isSuccess = level >= 0,
            message = response,
            spokenResponse = response
        )
    }

    private fun getDateTime(): CommandExecutionResult {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)

        val spoken = "It is $timeStr on $dateStr."
        return CommandExecutionResult(
            isSuccess = true,
            message = spoken,
            spokenResponse = spoken
        )
    }

    private fun createAlarm(hour: Int, minute: Int, label: String): CommandExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            CommandExecutionResult(
                isSuccess = true,
                message = "Alarm set for $formattedTime",
                spokenResponse = "Setting an alarm for $formattedTime."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Could not open Alarm app",
                spokenResponse = "Failed to launch the Clock app to set an alarm."
            )
        }
    }

    private fun createTimer(seconds: Int, label: String): CommandExecutionResult {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            val mins = seconds / 60
            val spokenMins = if (mins > 0) "$mins minute${if (mins > 1) "s" else ""}" else "$seconds seconds"
            CommandExecutionResult(
                isSuccess = true,
                message = "Timer set for $spokenMins",
                spokenResponse = "Setting a timer for $spokenMins."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Timer intent failed",
                spokenResponse = "Could not start timer."
            )
        }
    }

    private fun openApp(appName: String): CommandExecutionResult {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(0)

        // Simple fuzzy match for app label or package name
        var targetPackage: String? = null
        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
            if (label.contains(appName.lowercase(Locale.ROOT))) {
                targetPackage = app.packageName
                break
            }
        }

        if (targetPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return CommandExecutionResult(
                    isSuccess = true,
                    message = "Opening $appName",
                    spokenResponse = "Opening $appName."
                )
            }
        }

        return CommandExecutionResult(
            isSuccess = false,
            message = "App '$appName' not found",
            spokenResponse = "I couldn't find an application named $appName on this device."
        )
    }

    private fun openSettings(settingsType: String): CommandExecutionResult {
        val action = when (settingsType.lowercase(Locale.ROOT)) {
            "wifi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "hotspot" -> Settings.ACTION_WIRELESS_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandExecutionResult(
                isSuccess = true,
                message = "Opening $settingsType settings",
                spokenResponse = "Opening $settingsType settings."
            )
        } catch (e: Exception) {
            CommandExecutionResult(
                isSuccess = false,
                message = "Unable to open settings",
                spokenResponse = "Could not open system settings."
            )
        }
    }
}
