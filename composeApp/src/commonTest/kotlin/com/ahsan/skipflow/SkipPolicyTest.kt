package com.ahsan.skipflow
import kotlin.test.*
class SkipPolicyTest {
 @Test fun matchesAdControlsOnly() {
  assertTrue(SkipPolicy.matches(SkipPolicy.YOUTUBE, null, "Skip ad", null))
  assertTrue(SkipPolicy.matches(SkipPolicy.MUSIC, null, null, "Skip ads"))
  assertTrue(SkipPolicy.matches(SkipPolicy.YOUTUBE, SkipPolicy.YOUTUBE+":id/skip_ad_button", null, null))
  for (label in listOf("Skip", "Next", "Skip ad in 5", "Skip trial", "Learn more")) assertFalse(SkipPolicy.matches(SkipPolicy.YOUTUBE, null, label, null))
  assertFalse(SkipPolicy.matches("com.other.app", null, "Skip ad", null))
  assertFalse(SkipPolicy.matches(SkipPolicy.YOUTUBE, "com.other.app:id/skip_ad_button", null, null))
 }
}
