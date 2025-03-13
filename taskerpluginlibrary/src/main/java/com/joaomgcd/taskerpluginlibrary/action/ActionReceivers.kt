package com.joaomgcd.taskerpluginlibrary.action

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.joaomgcd.taskerpluginlibrary.extensions.canBindFireService
import com.joaomgcd.taskerpluginlibrary.extensions.mayNeedToStartForeground
import com.joaomgcd.taskerpluginlibrary.extensions.runFromTasker
import com.joaomgcd.taskerpluginlibrary.runner.IntentServiceParallel
import net.dinglisch.android.tasker.TaskerPlugin


class BroadcastReceiverAction : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        resultCode = TaskerPlugin.Setting.RESULT_CODE_PENDING
        try {
            runFromTasker<IntentServiceAction>(context, intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}

class IntentServiceAction : IntentServiceParallel("IntentServiceTaskerAction") {
    override fun onHandleIntent(intent: Intent) {
        if (getAppVersion(ctx, "net.dinglisch.android.taskerm") == "6.4.15") {
            return
        }
        
        val mayNeedToStartForeground: Boolean = intent.mayNeedToStartForeground
        startForegroundIfNeeded(mayNeedToStartForeground)
        val result = TaskerPluginRunnerAction.runFromIntent(this, intent)
        if (!result.hasStartedForeground) {
            startForegroundIfNeeded(mayNeedToStartForeground)
        }
    }

    fun getAppVersion(context: Context, packageName: String): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null // App is not installed
        }
    }
}
