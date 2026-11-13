plugins {
 alias(libs.plugins.kotlin.multiplatform)
 alias(libs.plugins.android.application)
 alias(libs.plugins.compose.multiplatform)
 alias(libs.plugins.compose.compiler)
}
kotlin {
 androidTarget()
 jvm("desktop")
 sourceSets {
  commonMain.dependencies { implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3); implementation(compose.ui) }
  commonTest.dependencies { implementation(kotlin("test")) }
  androidMain.dependencies { implementation("androidx.activity:activity-compose:1.11.0") }
 }
}
android {
 sourceSets.getByName("debug").manifest.srcFile("src/androidDebug/AndroidManifest.xml")
 sourceSets.getByName("main").assets.srcDir("src/androidMain/assets")
 namespace = "com.ahsan.skipflow"
 compileSdk = 36
 defaultConfig { applicationId = "com.ahsan.skipflow"; minSdk = 26; targetSdk = 36; versionCode = 8; versionName = "0.5.0"; testInstrumentationRunner = "com.ahsan.skipflow.VolumeInstrumentation" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
