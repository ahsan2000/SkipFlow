package com.ahsan.skipflow

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaRouter
import android.util.Log

/** Android volume adapter. Recovery contains volume numbers only, never screen contents. */
class AdVolumeController(context: Context, private val prefs: SharedPreferences = context.getSharedPreferences("skipflow",Context.MODE_PRIVATE)) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val router = context.getSystemService(Context.MEDIA_ROUTER_SERVICE) as MediaRouter
    private val session = AdVolumeSession()
    private var route: String? = null
    private fun current() = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
    private fun routeKey(): String {
        val selected = router.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO)
        return "${selected.deviceType}:${selected.name}"
    }
    private fun setVolume(value: Int): Boolean = try {
        audio.setStreamVolume(AudioManager.STREAM_MUSIC,value.coerceIn(0,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)),0)
        true
    } catch (e: SecurityException) { Log.w(TAG,"Media volume change was denied",e); false }
    private fun clearSnapshot() {
        if (prefs.contains(SAVED) || prefs.getBoolean(ACTIVE,false)) prefs.edit().remove(SAVED).remove(ROUTE).putBoolean(ACTIVE,false).commit()
    }
    fun recover() {
        if (!prefs.contains(SAVED)) return
        val before = prefs.getInt(SAVED,0)
        if (prefs.getString(ROUTE,null) != routeKey() || current() != 1 || audio.isVolumeFixed) { clearSnapshot(); return }
        if (setVolume(before)) {
            Log.i(TAG,"Restored media volume to $before after service reconnect")
            clearSnapshot()
        }
    }
    fun observe(ad: Boolean, now: Long) {
        if (audio.isVolumeFixed || audio.mode != AudioManager.MODE_NORMAL) { stop(); return }
        val key = routeKey()
        if (session.inAd && route != key) {
            session.relinquish()
            clearSnapshot()
        }
        route = key
        val command = session.observe(ad,now,current())
        if (command != null) {
            if (session.original != null) {
                // Persist before lowering so a restarted service can recover the original level.
                if (!prefs.edit().putInt(SAVED,session.original!!).putString(ROUTE,key).putBoolean(ACTIVE,true).commit()) {
                    session.relinquish(); return
                }
                if (setVolume(command)) Log.i(TAG,"Ad detected: lowered media volume from ${session.original} to 1")
                else { session.relinquish(); clearSnapshot() }
            } else {
                if (setVolume(command)) {
                    Log.i(TAG,"Ad ended: restored media volume to $command")
                    clearSnapshot()
                }
            }
        }
        if (session.original == null && command == null) clearSnapshot()
    }
    fun stop() {
        val canRestore = route == null || route == routeKey()
        val restore = session.finish(current())
        if (restore != null && canRestore && !audio.isVolumeFixed) {
            if (setVolume(restore)) {
                Log.i(TAG,"Restored media volume to $restore on pause/exit")
                clearSnapshot()
            }
        } else if (!canRestore) clearSnapshot()
        else recover()
        route = null
    }
    companion object {
        private const val TAG = "SkipFlowVolume"
        private const val SAVED = "ad_saved_volume"
        private const val ROUTE = "ad_saved_route"
        const val ACTIVE = "ad_volume_active"
    }
}
