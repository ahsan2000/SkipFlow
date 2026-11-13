package com.ahsan.skipflow

import android.app.Instrumentation
import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.content.Context
import android.media.AudioManager

/** Debug-test APK only: exercises real STREAM_MUSIC changes without generating sound. */
class VolumeInstrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val audio = targetContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val prefs = targetContext.getSharedPreferences("volume_device_test",Context.MODE_PRIVATE)
        val original = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val baseline = minOf(3,audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        val controller = AdVolumeController(targetContext,prefs)
        val result = Bundle()
        var success = false
        fun expect(level: Int) {
            val until = SystemClock.elapsedRealtime()+2000
            while (audio.getStreamVolume(AudioManager.STREAM_MUSIC)!=level && SystemClock.elapsedRealtime()<until) SystemClock.sleep(50)
            check(audio.getStreamVolume(AudioManager.STREAM_MUSIC)==level) { "Expected media level $level, got ${audio.getStreamVolume(AudioManager.STREAM_MUSIC)}" }
        }
        try {
            check(!audio.isVolumeFixed && audio.mode==AudioManager.MODE_NORMAL && baseline>1) { "Requires adjustable media volume and no active call" }
            prefs.edit().clear().commit()
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,baseline,0); expect(baseline)
            controller.observe(true,0); expect(1)
            controller.observe(true,500); expect(1)
            controller.observe(false,1000); expect(1)
            controller.observe(false,2600); expect(baseline)
            controller.observe(true,3000); expect(1)
            controller.stop(); expect(baseline)
            controller.observe(true,4000); expect(1)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,2,0); expect(2)
            controller.observe(true,4500)
            controller.stop(); expect(2)
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,baseline,0); expect(baseline)
            controller.observe(true,5000); expect(1)
            // Simulate service recreation: second adapter reads the persisted snapshot.
            AdVolumeController(targetContext,prefs).recover(); expect(baseline)
            result.putString("stream","PASS: real Android media volume lowered to 1, restored after ad end and stop; manual override respected; process recovery restored snapshot.")
            success = true
        } catch (t: Throwable) {
            result.putString("stream","FAIL: ${t.stackTraceToString()}")
        } finally {
            prefs.edit().clear().commit()
            audio.setStreamVolume(AudioManager.STREAM_MUSIC,original,0)
            try { expect(original) } catch (t: Throwable) { success=false; result.putString("restore_error",t.message) }
        }
        finish(if(success) Activity.RESULT_OK else Activity.RESULT_CANCELED,result)
    }
}
