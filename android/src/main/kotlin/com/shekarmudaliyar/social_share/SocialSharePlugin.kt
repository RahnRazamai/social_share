package com.shekarmudaliyar.social_share

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.FileProvider
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.net.URLEncoder

class SocialSharePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var activeContext: Context? = null
    private var context: Context? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "social_share")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        activeContext = activity?.applicationContext ?: context

        when (call.method) {
            "shareWhatsapp" -> shareOnWhatsapp(call, result)
            "shareTwitter" -> shareOnTwitter(call, result)
            "checkInstalledApps" -> checkInstalledApps(result)
            else -> result.notImplemented()
        }
    }

    private fun shareOnWhatsapp(call: MethodCall, result: Result) {
        val content: String? = call.argument("content")
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, content)
        }
        try {
            activity?.startActivity(whatsappIntent)
            result.success("success")
        } catch (ex: ActivityNotFoundException) {
            result.success("error")
        }
    }

    private fun shareOnTwitter(call: MethodCall, result: Result) {
        val text: String? = call.argument("captionText")
        val urlScheme = "http://www.twitter.com/intent/tweet?text=${URLEncoder.encode(text, Charsets.UTF_8.name())}"
        Log.d("", urlScheme)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme))
        try {
            activity?.startActivity(intent)
            result.success("success")
        } catch (ex: ActivityNotFoundException) {
            result.success("error")
        }
    }

    private fun checkInstalledApps(result: Result) {
        val pm: PackageManager = context!!.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val apps = mutableMapOf<String, Boolean>()
        apps["whatsapp"] = packages.any { it.packageName == "com.whatsapp" }
        apps["twitter"] = packages.any { it.packageName == "com.twitter.android" }
        result.success(apps)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
