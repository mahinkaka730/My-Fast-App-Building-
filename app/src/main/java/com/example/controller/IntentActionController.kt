package com.example.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val errorMessage: String) : ActionResult()
    data class RequestSmsDraft(val recipient: String = "", val body: String = "") : ActionResult()
    data class TimeReport(val timeInBengali: String) : ActionResult()
}

class IntentActionController(private val context: Context) {

    fun executeCommand(command: String): ActionResult {
        val trimmed = command.trim().lowercase(Locale.ROOT)

        return when {
            // YouTube
            trimmed.contains("youtube খোলো") || trimmed.contains("ইউটিউব খোলো") ||
                    trimmed.contains("open youtube") || trimmed.contains("ইউটিউব ওপেন করো") -> {
                openYouTube()
            }

            // Chrome
            trimmed.contains("chrome খোলো") || trimmed.contains("ক্রোম খোলো") ||
                    trimmed.contains("open chrome") || trimmed.contains("ব্রাউজার খোলো") -> {
                openChrome()
            }

            // Play Store
            trimmed.contains("play store খোলো") || trimmed.contains("প্লে স্টোর খোলো") ||
                    trimmed.contains("open play store") || trimmed.contains("প্লেস্টোর খোলো") -> {
                openPlayStore()
            }

            // Time
            trimmed.contains("সময় বলো") || trimmed.contains("সময় বলো") ||
                    trimmed.contains("কয়টা বাজে") || trimmed.contains("কয়টা বাজে") ||
                    trimmed.contains("tell time") || trimmed.contains("time please") -> {
                getCurrentBengaliTime()
            }

            // Message / SMS
            trimmed.contains("মেসেজ লিখে দাও") || trimmed.contains("মেসেজ পাঠাও") ||
                    trimmed.contains("এসএমএস পাঠাও") || trimmed.contains("send message") ||
                    trimmed.contains("send sms") -> {
                // Extract message body if provided, e.g. "মেসেজ লিখে দাও আমি একটু পরে আসছি"
                val body = extractSmsBody(command)
                ActionResult.RequestSmsDraft(recipient = "", body = body)
            }

            // Web Search
            trimmed.contains("ইন্টারনেটে খুঁজে দাও") || trimmed.contains("গুগলে সার্চ করো") ||
                    trimmed.contains("সার্চ করো") || trimmed.contains("ইন্টারনেটে সার্চ করো") -> {
                val query = extractSearchQuery(command)
                openWebSearch(query)
            }

            // Camera
            trimmed.contains("ক্যামেরা খোলো") || trimmed.contains("open camera") ||
                    trimmed.contains("ছবি তোলো") -> {
                openCamera()
            }

            // Settings
            trimmed.contains("সেটিংস খোলো") || trimmed.contains("ফোন সেটিংস") ||
                    trimmed.contains("open settings") -> {
                openSettings()
            }

            // WhatsApp
            trimmed.contains("হোয়াটসঅ্যাপ খোলো") || trimmed.contains("open whatsapp") -> {
                openAppByPackage("com.whatsapp", "WhatsApp")
            }

            // Phone Dialer
            trimmed.contains("ডায়ালার খোলো") || trimmed.contains("ফোন ডায়ালার") ||
                    trimmed.contains("কল করো") || trimmed.contains("open dialer") -> {
                openDialer()
            }

            // Maps
            trimmed.contains("ম্যাপ খোলো") || trimmed.contains("গুগল ম্যাপ") ||
                    trimmed.contains("open maps") -> {
                openMaps()
            }

            // TikTok
            trimmed.contains("tiktok খোলো") || trimmed.contains("টিকটক খোলো") ||
                    trimmed.contains("open tiktok") || trimmed.contains("টিকটক ওপেন করো") -> {
                openTikTok()
            }

            // General Open app: "open facebook", "open tiktok", etc.
            trimmed.startsWith("open ") -> {
                val appName = trimmed.removePrefix("open ").trim()
                openAnyApp(appName)
            }

            // Bengali open app: "ফেসবুক খোলো", "টিকটক খোলো", ইত্যাদি
            trimmed.endsWith(" খোলো") || trimmed.endsWith(" ওপেন করো") -> {
                val appName = trimmed.substringBeforeLast(" খোলো").substringBeforeLast(" ওপেন করো").trim()
                if (appName.isNotBlank() && appName.length <= 25) {
                    openAnyApp(appName)
                } else {
                    ActionResult.Failure("কমান্ডটি সরাসরি চিহ্নিত হয়নি। এআই উত্তরের জন্য অনুরোধ করা হচ্ছে...")
                }
            }

            else -> {
                ActionResult.Failure("কমান্ডটি সরাসরি চিহ্নিত হয়নি। এআই উত্তরের জন্য অনুরোধ করা হচ্ছে...")
            }
        }
    }

    fun openYouTube(query: String? = null): ActionResult {
        return try {
            val intent = if (!query.isNullOrBlank()) {
                Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
            }
            context.startActivity(intent)
            ActionResult.Success("YouTube সফলভাবে খোলা হয়েছে।")
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult.Success("ব্রাউজারে YouTube খোলা হয়েছে।")
            } catch (err: Exception) {
                ActionResult.Failure("YouTube খুলতে ব্যর্থ: ${err.localizedMessage ?: "অজানা ত্রুটি"}")
            }
        }
    }

    fun openChrome(url: String = "https://www.google.com"): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.android.chrome")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Google Chrome খোলা হয়েছে।")
        } catch (e: Exception) {
            try {
                // Fallback to default browser
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                ActionResult.Success("ডিফল্ট ব্রাউজার খোলা হয়েছে।")
            } catch (err: Exception) {
                ActionResult.Failure("ব্রাউজার খুলতে সমস্যা হয়েছে: ${err.localizedMessage}")
            }
        }
    }

    fun openPlayStore(): ActionResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.android.vending")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
            ActionResult.Success("Google Play Store খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("Play Store খুলতে ব্যর্থ: ${e.localizedMessage}")
        }
    }

    fun searchPlayStore(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${Uri.encode(query)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("Play Store-এ '$query' সার্চ করা হচ্ছে...")
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(query)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult.Success("ব্রাউজারে Play Store সার্চ খোলা হয়েছে।")
            } catch (err: Exception) {
                ActionResult.Failure("Play Store সার্চ খুলতে সমস্যা: ${err.localizedMessage}")
            }
        }
    }

    fun openWebSearch(query: String): ActionResult {
        val q = if (query.isBlank()) "KAKA AI" else query
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra("query", q)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("ইন্টারনেটে '$q' খোঁজা হচ্ছে...")
        } catch (e: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(q)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionResult.Success("ব্রাউজারে গুগল সার্চ খোলা হয়েছে।")
            } catch (err: Exception) {
                ActionResult.Failure("সার্চ করতে সমস্যা: ${err.localizedMessage}")
            }
        }
    }

    fun openCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("ক্যামেরা খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("ক্যামেরা খুলতে ব্যর্থ: ${e.localizedMessage}")
        }
    }

    fun openSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("ডিভাইস সেটিংস খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("সেটিংস খুলতে ব্যর্থ: ${e.localizedMessage}")
        }
    }

    fun openDialer(phoneNumber: String? = null): ActionResult {
        return try {
            val uri = if (!phoneNumber.isNullOrBlank()) Uri.parse("tel:$phoneNumber") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("ফোন ডায়ালার খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("ডায়ালার খুলতে ব্যর্থ: ${e.localizedMessage}")
        }
    }

    fun openMaps(query: String = ""): ActionResult {
        return try {
            val uri = if (query.isNotBlank()) Uri.parse("geo:0,0?q=${Uri.encode(query)}") else Uri.parse("geo:0,0")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("গুগল ম্যাপ খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("ম্যাপ খুলতে ব্যর্থ: ${e.localizedMessage}")
        }
    }

    fun openTikTok(): ActionResult {
        val packages = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
        for (pkg in packages) {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return ActionResult.Success("TikTok অ্যাপটি সফলভাবে খোলা হয়েছে।")
            }
        }
        // Fallback to web browser
        return try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            ActionResult.Success("ডিভাইসে TikTok অ্যাপ নেই, ব্রাউজারে TikTok খোলা হয়েছে।")
        } catch (e: Exception) {
            ActionResult.Failure("TikTok খুলতে ব্যর্থ: ${e.localizedMessage ?: "অজানা ত্রুটি"}")
        }
    }

    fun openAnyApp(query: String): ActionResult {
        val lower = query.trim().lowercase(Locale.ROOT)

        // Known common mappings
        val knownPackages = mapOf(
            "tiktok" to "com.zhiliaoapp.musically",
            "টিকটক" to "com.zhiliaoapp.musically",
            "facebook" to "com.facebook.katana",
            "ফেসবুক" to "com.facebook.katana",
            "fb" to "com.facebook.katana",
            "instagram" to "com.instagram.android",
            "ইনস্টাগ্রাম" to "com.instagram.android",
            "insta" to "com.instagram.android",
            "whatsapp" to "com.whatsapp",
            "হোয়াটসঅ্যাপ" to "com.whatsapp",
            "telegram" to "org.telegram.messenger",
            "টেলিগ্রাম" to "org.telegram.messenger",
            "messenger" to "com.facebook.orca",
            "মেসেঞ্জার" to "com.facebook.orca",
            "spotify" to "com.spotify.music",
            "স্পটিফাই" to "com.spotify.music",
            "twitter" to "com.twitter.android",
            "x" to "com.twitter.android",
            "টুইটার" to "com.twitter.android",
            "gmail" to "com.google.android.gm",
            "জিমেইল" to "com.google.android.gm",
            "youtube" to "com.google.android.youtube",
            "ইউটিউব" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "ক্রোম" to "com.android.chrome"
        )

        if (lower == "tiktok" || lower == "টিকটক") {
            return openTikTok()
        }

        if (knownPackages.containsKey(lower)) {
            val pkg = knownPackages[lower]!!
            val appTitle = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            val res = openAppByPackage(pkg, appTitle)
            if (res is ActionResult.Success) return res
        }

        // Search installed apps by label or package
        try {
            val pm = context.packageManager
            val installed = pm.getInstalledApplications(0)
            for (app in installed) {
                val label = pm.getApplicationLabel(app).toString().lowercase(Locale.ROOT)
                if (label.contains(lower) || app.packageName.lowercase(Locale.ROOT).contains(lower)) {
                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        val displayLabel = pm.getApplicationLabel(app).toString()
                        return ActionResult.Success("'$displayLabel' খোলা হয়েছে।")
                    }
                }
            }
        } catch (_: Exception) {}

        // Fallback: If app not found on device, give user option to search Play Store
        return searchPlayStore(query)
    }

    private fun openAppByPackage(packageName: String, appName: String): ActionResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                ActionResult.Success("$appName সফলভাবে খোলা হয়েছে।")
            } else {
                ActionResult.Failure("আপনার ফোনে $appName অ্যাপটি ইনস্টল করা নেই।")
            }
        } catch (e: Exception) {
            ActionResult.Failure("$appName খুলতে সমস্যা: ${e.localizedMessage}")
        }
    }

    fun sendSmsIntent(recipientNumber: String, messageText: String): ActionResult {
        return try {
            val uri = Uri.parse("smsto:${Uri.encode(recipientNumber.trim())}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult.Success("মেসেজটি আপনার এসএমএস অ্যাপে প্রস্তুত করা হয়েছে। পর্যালোচনা করে সেন্ড করুন।")
        } catch (e: Exception) {
            ActionResult.Failure("মেসেজ অ্যাপ খুলতে সমস্যা: ${e.localizedMessage}")
        }
    }

    fun getCurrentBengaliTime(): ActionResult.TimeReport {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        val formatted = timeFormat.format(now) // e.g., "3:45 PM"

        val hourMinute = formatted.substringBefore(" ")
        val amPm = formatted.substringAfter(" ")
        val parts = hourMinute.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val bengaliPeriod = when {
            amPm == "AM" && hour in 4..5 -> "ভোর"
            amPm == "AM" && hour in 6..11 -> "সকাল"
            amPm == "AM" && hour == 12 -> "মধ্যরাত"
            amPm == "AM" && hour in 1..3 -> "রাত"
            amPm == "PM" && hour == 12 -> "দুপুর"
            amPm == "PM" && hour in 1..3 -> "দুপুর"
            amPm == "PM" && hour in 4..5 -> "বিকাল"
            amPm == "PM" && hour in 6..7 -> "সন্ধ্যা"
            else -> "রাত"
        }

        val bengaliHour = toBengaliDigits(hour.toString())
        val bengaliMinute = toBengaliDigits(String.format(Locale.ROOT, "%02d", minute))

        val bengaliTimeText = if (minute == 0) {
            "এখন $bengaliPeriod $bengaliHour টা বাজে।"
        } else {
            "এখন $bengaliPeriod $bengaliHour টা বেজে $bengaliMinute মিনিট।"
        }

        return ActionResult.TimeReport(bengaliTimeText)
    }

    private fun extractSmsBody(command: String): String {
        val keywords = listOf("মেসেজ লিখে দাও", "মেসেজ লিখো", "মেসেজ পাঠাও", "এসএমএস পাঠাও", "send message", "send sms")
        var result = command
        for (kw in keywords) {
            if (result.contains(kw, ignoreCase = true)) {
                result = result.substringAfter(kw).trim()
                break
            }
        }
        return result.removePrefix(":").removePrefix("।").trim()
    }

    private fun extractSearchQuery(command: String): String {
        val keywords = listOf("ইন্টারনেটে খুঁজে দাও", "গুগলে সার্চ করো", "সার্চ করো", "ইন্টারনেটে সার্চ করো", "খুঁজে দাও")
        var result = command
        for (kw in keywords) {
            if (result.contains(kw, ignoreCase = true)) {
                result = result.substringAfter(kw).trim()
                break
            }
        }
        return result.removePrefix(":").removePrefix("।").trim()
    }

    private fun toBengaliDigits(input: String): String {
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bengaliDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}
