package com.ahsan.skipflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log

/** Debug-only, DUMP-permission protected regression probe for a blocked service Looper. */
class DebugUiStallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context,intent: Intent) {
        if(context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        Log.i("SkipFlowProbe","MAIN_STALL_START uptime=${SystemClock.uptimeMillis()}")
        SystemClock.sleep(2000)
        Log.i("SkipFlowProbe","MAIN_STALL_END uptime=${SystemClock.uptimeMillis()}")
    }
}
