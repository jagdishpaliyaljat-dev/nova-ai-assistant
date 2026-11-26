package com.personal.novaai.engine.integration

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.personal.novaai.domain.model.PlanStep

class MobileIntegrationHub(private val context: Context) {

    fun executeStep(step: PlanStep): Result<String> {
        val a11y = com.personal.novaai.engine.orchestrator.NovaAccessibilityEngine.instance

        return try {
            when (step.action.uppercase()) {
                "OPEN_APP" -> launchApp(step.target)
                "SEARCH_IN_APP" -> searchApp(step.target, step.parameters["query"]?.toString() ?: "")
                "SEND_MESSAGE" -> openMessaging(step.target, step.parameters["message"]?.toString() ?: "")
                "CALL_CONTACT" -> openDialer(step.target)
                "SET_ALARM" -> setAlarm(
                    (step.parameters["hour"] as? Number)?.toInt() ?: 7,
                    (step.parameters["minute"] as? Number)?.toInt() ?: 0,
                    step.parameters["message"]?.toString() ?: "NOVA AI Alarm"
                )
                "OPEN_URL" -> openUrl(step.parameters["url"]?.toString() ?: step.target)
                "SYSTEM_SETTING" -> openSetting(step.target)
                "TOGGLE", "TURN_ON", "ENABLE" -> {
                    // Turn on Hotspot, Bluetooth, Wi-Fi directly on screen
                    if (a11y != null) {
                        val toggled = a11y.toggleSwitch() || a11y.clickByTextOrToggle(step.target)
                        Result.success(if (toggled) "Toggled ${step.target}" else "Switch clicked")
                    } else {
                        Result.failure(IllegalStateException("Accessibility service not connected"))
                    }
                }
                "CLICK", "TAP" -> {
                    if (a11y != null) {
                        a11y.clickByTextOrToggle(step.target)
                        Result.success("Tapped ${step.target}")
                    } else {
                        Result.failure(IllegalStateException("Accessibility service not connected"))
                    }
                }
                else -> Result.failure(IllegalArgumentException("Unsupported action: ${step.action}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun launchApp(target: String): Result<String> {
        val resolved = resolvePackage(target)
        val intent = when {
            resolved.contains("youtube") -> Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://"))
            resolved == "camera" -> Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            else -> context.packageManager.getLaunchIntentForPackage(resolved)
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(intent)
            return Result.success("App opened: $target")
        } else {
            // Web fallback
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com/search?q=$target"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
            return Result.success("Web view opened for: $target")
        }
    }

    private fun searchApp(target: String, query: String): Result<String> {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.success("Searched '$query' in $target")
    }

    private fun openMessaging(target: String, message: String): Result<String> {
        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(message))
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(sendIntent)
            Result.success("Opened chat for message: $message")
        } catch (e: Exception) {
            // General SMS intent fallback
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            Result.success("Opened SMS composer for: $message")
        }
    }

    private fun openDialer(target: String): Result<String> {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$target")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.success("Dialer opened for: $target")
    }

    private fun setAlarm(hour: Int, minute: Int, message: String): Result<String> {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.success("Alarm set for $hour:$minute")
    }

    private fun openUrl(url: String): Result<String> {
        val parsedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parsedUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.success("Opened URL: $parsedUrl")
    }

    private fun openSetting(settingName: String): Result<String> {
        val lower = settingName.lowercase()
        val action = when {
            lower.contains("hotspot") || lower.contains("tether") -> {
                // Direct Tethering & Portable Hotspot settings
                "android.settings.TETHER_SETTINGS"
            }
            lower.contains("wifi") -> Settings.ACTION_WIFI_SETTINGS
            lower.contains("bluetooth") -> Settings.ACTION_BLUETOOTH_SETTINGS
            lower.contains("display") -> Settings.ACTION_DISPLAY_SETTINGS
            lower.contains("sound") -> Settings.ACTION_SOUND_SETTINGS
            lower.contains("apps") -> Settings.ACTION_APPLICATION_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return Result.success("Opened Settings: $settingName")
    }

    private fun resolvePackage(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("instagram") -> "com.instagram.android"
            lower.contains("chrome") || lower.contains("browser") -> "com.android.chrome"
            lower.contains("setting") -> "com.android.settings"
            lower.contains("camera") -> "camera"
            lower.contains("calculator") -> "com.google.android.calculator"
            lower.contains("maps") -> "com.google.android.apps.maps"
            else -> name
        }
    }
}
