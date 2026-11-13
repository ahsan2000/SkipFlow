package com.ahsan.skipflow

/** Conservative visible-player signals, separate from controls that are safe to click. */
object AdSignals {
    private val adIds = setOf("ad_progress_text", "ad_duration_remaining", "ad_remaining", "ad_countdown", "ad_skip_button", "ad_skip_button_text", "skip_ad_button", "skip_ad_button_text", "skip_ad_button_container")
    private val playerIds = setOf("player", "player_view", "player_container", "player_fragment", "watch_player", "watch_player_fragment", "watch_while_player", "player_overlays", "player_overlay", "player_page", "player_root")
    private val countdown = Regex("(?:skip ads? in|you can skip (?:this )?ad in) \\d+(?: seconds?)?[.!]?", RegexOption.IGNORE_CASE)
    private val sequence = Regex("ads? \\d+ of \\d+", RegexOption.IGNORE_CASE)
    fun isPlayer(pkg: String, id: String?): Boolean = SkipPolicy.supports(pkg) && id?.substringBefore(":id/") == pkg && id.substringAfter(":id/", "") in playerIds
    fun isStrongAd(pkg: String, id: String?, text: String?, description: String?): Boolean {
        if (!SkipPolicy.supports(pkg)) return false
        if (SkipPolicy.matches(pkg,id,text,description)) return true
        if (id?.substringBefore(":id/") == pkg && id.substringAfter(":id/", "") in adIds) return true
        return listOfNotNull(text,description).any { countdown.matches(it.trim()) || sequence.matches(it.trim()) }
    }
    fun isPlayerBadge(pkg: String, id: String?, text: String?, description: String?): Boolean {
        if (!SkipPolicy.supports(pkg)) return false
        // Do not treat a song/video title, search result, or recommendation as an ad.
        val local = id?.substringAfter(":id/", "")?.lowercase().orEmpty()
        if (listOf("title", "search", "description", "comment", "channel", "artist").any { it in local }) return false
        if (id?.substringBefore(":id/") == pkg && local in setOf("ad_badge", "ad_attribution", "ad_attribution_label")) return true
        return listOfNotNull(text,description).any { it.trim().lowercase() in setOf("ad", "ads", "advertisement", "sponsored", "اشتہار", "اشتہارات") }
    }
}

/** One snapshot per ad break, including back-to-back ads. User volume changes win. */
class AdVolumeSession(private val endGraceMs: Long = 1500) {
    var inAd: Boolean = false; private set
    var original: Int? = null; private set
    var lowered: Int? = null; private set
    private var clearSince: Long? = null

    fun observe(ad: Boolean, now: Long, current: Int): Int? {
        if (!ad) {
            if (!inAd) return null
            val since = clearSince ?: now.also { clearSince = it }
            return if (now - since >= endGraceMs) finish(current) else null
        }
        clearSince = null
        if (inAd) {
            if (lowered != null && current != lowered) { original = null; lowered = null }
            return null
        }
        inAd = true
        // Never raise already-muted audio or change a volume already at level 1.
        if (current <= 1) return null
        original = current
        lowered = 1
        return 1
    }
    fun finish(current: Int): Int? {
        val restore = original?.takeIf { current == lowered }
        inAd = false; original = null; lowered = null; clearSince = null
        return restore
    }
    /** Keep the current break suppressed when the user switches audio outputs. */
    fun relinquish() { original = null; lowered = null }
}
