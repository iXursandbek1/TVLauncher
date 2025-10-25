package uz.khursandbek.tvlauncher.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {
    fun isSystemApp(info: ResolveInfo): Boolean {
        return (info.activityInfo.applicationInfo.flags and
                ApplicationInfo.FLAG_SYSTEM) != 0
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isGame(info: ResolveInfo): Boolean {
        return info.activityInfo.applicationInfo.category ==
                ApplicationInfo.CATEGORY_GAME
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isVideoApp(info: ResolveInfo): Boolean {
        return info.activityInfo.applicationInfo.category ==
                ApplicationInfo.CATEGORY_VIDEO
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isMusicApp(info: ResolveInfo): Boolean {
        return info.activityInfo.applicationInfo.category ==
                ApplicationInfo.CATEGORY_AUDIO
    }

    fun getInstallTime(context: Context, packageName: String): Long {
        return try {
            context.packageManager
                .getPackageInfo(packageName, 0)
                .firstInstallTime
        } catch (e: Exception) {
            0L
        }
    }

    fun getUpdateTime(context: Context, packageName: String): Long {
        return try {
            context.packageManager
                .getPackageInfo(packageName, 0)
                .lastUpdateTime
        } catch (e: Exception) {
            0L
        }
    }

    fun formatAppSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$bytes bytes"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getVersionName(context: Context, packageName: String): String {
        return try {
            context.packageManager
                .getPackageInfo(packageName, 0)
                .versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun isLaunchable(context: Context, packageName: String): Boolean {
        return context.packageManager.getLaunchIntentForPackage(packageName) != null
    }

    fun getRecentlyInstalledApps(
        context: Context,
        apps: List<ResolveInfo>,
        daysAgo: Int = 30
    ): List<ResolveInfo> {
        val thirtyDaysAgo = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)

        return apps.filter { app ->
            val installTime = getInstallTime(context, app.activityInfo.packageName)
            installTime > thirtyDaysAgo
        }.sortedByDescending { app ->
            getInstallTime(context, app.activityInfo.packageName)
        }
    }


    fun getRecentlyUpdatedApps(
        context: Context,
        apps: List<ResolveInfo>,
        daysAgo: Int = 7
    ): List<ResolveInfo> {
        val sevenDaysAgo = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000L)

        return apps.filter { app ->
            val updateTime = getUpdateTime(context, app.activityInfo.packageName)
            updateTime > sevenDaysAgo
        }.sortedByDescending { app ->
            getUpdateTime(context, app.activityInfo.packageName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun filterByCategory(apps: List<ResolveInfo>, category: Int): List<ResolveInfo> {
        return apps.filter {
            it.activityInfo.applicationInfo.category == category
        }
    }

    fun searchApps(
        context: Context,
        apps: List<ResolveInfo>,
        query: String
    ): List<ResolveInfo> {
        val lowerQuery = query.lowercase()
        return apps.filter { app ->
            val appName = app.loadLabel(context.packageManager).toString().lowercase()
            val packageName = app.activityInfo.packageName.lowercase()
            appName.contains(lowerQuery) || packageName.contains(lowerQuery)
        }
    }
}