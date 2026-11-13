package com.ahsan.skipflow

object SkipPolicy {
 const val YOUTUBE = "com.google.android.youtube"
 const val MUSIC = "com.google.android.apps.youtube.music"
 private val ids = setOf("skip_ad_button", "skip_ad_button_text", "skip_ad_button_container", "ad_skip_button", "ad_skip_button_text")
 private val labels = setOf("skip ad", "skip ads", "skip advertisement", "skip advertisements", "اشتہار چھوڑیں", "اشتہارات چھوڑیں")
 fun supports(pkg: String) = pkg == YOUTUBE || pkg == MUSIC
 fun matches(pkg: String, id: String?, text: String?, description: String?): Boolean {
  if (!supports(pkg)) return false
  if (id?.substringBefore(":id/") == pkg && id.substringAfter(":id/", "") in ids) return true
  return listOfNotNull(text, description).any { it.trim().lowercase() in labels }
 }
}
