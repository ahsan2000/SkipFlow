package com.ahsan.skipflow

import kotlin.test.*
class AdVolumePolicyTest {
    @Test fun savesOnceAcrossConsecutiveAdsAndRestoresAfterStableEnd() {
        val session = AdVolumeSession()
        assertEquals(1,session.observe(true,0,8))
        assertEquals(8,session.original)
        assertNull(session.observe(true,500,1))
        assertNull(session.observe(false,1000,1))
        assertNull(session.observe(true,1600,1)) // next ad in same break
        assertEquals(8,session.original)
        assertNull(session.observe(false,2000,1))
        assertNull(session.observe(false,3400,1))
        assertEquals(8,session.observe(false,3500,1))
        assertNull(session.finish(8))
    }
    @Test fun manualVolumeChangesAreNotOverwrittenOrForcedBackDown() {
        val session = AdVolumeSession()
        assertEquals(1,session.observe(true,0,9))
        assertNull(session.observe(true,200,4))
        assertNull(session.observe(true,400,4))
        assertNull(session.finish(4))
        assertEquals(1,session.observe(true,2000,4)) // new break can take a new snapshot
    }
    @Test fun pauseExitAndDisableRestoreExactlyOnce() {
        val session = AdVolumeSession()
        assertEquals(1,session.observe(true,0,6))
        assertEquals(6,session.finish(1))
        assertNull(session.finish(6))
        assertFalse(session.inAd)
    }
    @Test fun mutedOrAlreadyQuietAudioIsNeverRaised() {
        for (level in 0..1) {
            val session = AdVolumeSession()
            assertNull(session.observe(true,0,level))
            assertNull(session.finish(level))
        }
    }
    @Test fun routeChangeDoesNotApplyOldOutputVolumeToNewOutput() {
        val session = AdVolumeSession()
        session.observe(true,0,12)
        session.relinquish()
        assertNull(session.observe(true,1000,5))
        assertNull(session.finish(5))
    }
    @Test fun conservativeAdSignalsRejectTitlesAndUnrelatedApps() {
        val pkg = SkipPolicy.YOUTUBE
        assertTrue(AdSignals.isStrongAd(pkg,"$pkg:id/ad_progress_text",null,null))
        assertFalse(AdSignals.isStrongAd(pkg,"$pkg:id/ad_badge",null,null))
        assertTrue(AdSignals.isPlayerBadge(pkg,"$pkg:id/ad_badge",null,null))
        assertTrue(AdSignals.isStrongAd(SkipPolicy.MUSIC,null,"Ad 1 of 2",null))
        assertTrue(AdSignals.isStrongAd(pkg,null,"Skip ad in 5",null))
        assertFalse(AdSignals.isStrongAd(pkg,null,"Amazing ad tutorial",null))
        assertFalse(AdSignals.isStrongAd("other.app",null,"Skip ad",null))
        assertFalse(AdSignals.isStrongAd(pkg,"other.app:id/ad_badge",null,null))
        assertFalse(AdSignals.isPlayerBadge(pkg,"$pkg:id/title","Sponsored",null))
        assertTrue(AdSignals.isPlayerBadge(pkg,null,"Ad",null))
        assertTrue(AdSignals.isPlayer(pkg,"$pkg:id/player_view"))
        assertFalse(AdSignals.isPlayer(pkg,"$pkg:id/player_title"))
    }
}
