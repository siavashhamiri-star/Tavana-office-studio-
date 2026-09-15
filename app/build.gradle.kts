import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.io.File

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.ava.stagez"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      val keyStoreFile = file(keystorePath)
      if (keyStoreFile.exists() && !System.getenv("STORE_PASSWORD").isNullOrBlank()) {
        storeFile = keyStoreFile
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD") ?: System.getenv("STORE_PASSWORD")
      } else {
        // Safe fallback to debug.keystore for CI and developer builds
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug { signingConfig = signingConfigs.getByName("debugConfig") }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Defensive normalization for .env:
// secrets-gradle-plugin 2.0.1 has a bug where empty values (e.g. GEMINI_API_KEY=)
// return "" without quotes, generating invalid Java `public static final String GEMINI_API_KEY = ;`
val envFile = rootProject.file(".env")
val exampleFile = rootProject.file(".env.example")
if (!envFile.exists() && exampleFile.exists()) {
  envFile.writeText(exampleFile.readText())
} else if (envFile.exists()) {
  var hasGeminiKey = false
  var modified = false
  val sanitizedLines = envFile.readLines().map { line ->
    val trimmed = line.trim()
    if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
      val parts = trimmed.split("=", limit = 2)
      val key = parts[0].trim()
      val value = parts[1].trim().removeSurrounding("\"").removeSurrounding("'")
      if (key == "GEMINI_API_KEY") hasGeminiKey = true
      if (value.isEmpty()) {
        modified = true
        "$key=YOUR_${key}"
      } else {
        line
      }
    } else {
      line
    }
  }.toMutableList()
  if (!hasGeminiKey) {
    sanitizedLines.add("GEMINI_API_KEY=YOUR_GEMINI_API_KEY")
    modified = true
  }
  if (modified) {
    envFile.writeText(sanitizedLines.joinToString("\n") + "\n")
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Convenience task to copy and organize APK and AAB binaries into a root build-artifacts folder
tasks.register("exportArtifacts") {
  description = "Collects generated APK and AAB files into root build-artifacts directory"
  group = "build"
  doLast {
    val exportDir = File("${rootDir}/build-artifacts")
    exportDir.mkdirs()
    val buildOutputsDir = layout.buildDirectory.asFile.get()
    
    // Debug outputs
    val debugApk = File(buildOutputsDir, "outputs/apk/debug/app-debug.apk")
    if (debugApk.exists()) {
      debugApk.copyTo(File(exportDir, "TAVANA-Studio-debug.apk"), overwrite = true)
      logger.lifecycle("Exported Debug APK to build-artifacts/TAVANA-Studio-debug.apk")
    }
    val debugAab = File(buildOutputsDir, "outputs/bundle/debug/app-debug.aab")
    if (debugAab.exists()) {
      debugAab.copyTo(File(exportDir, "TAVANA-Studio-debug.aab"), overwrite = true)
      logger.lifecycle("Exported Debug AAB to build-artifacts/TAVANA-Studio-debug.aab")
    }
    
    // Release outputs
    val releaseApk = File(buildOutputsDir, "outputs/apk/release/app-release.apk")
    if (releaseApk.exists()) {
      releaseApk.copyTo(File(exportDir, "TAVANA-Studio-release.apk"), overwrite = true)
      logger.lifecycle("Exported Release APK to build-artifacts/TAVANA-Studio-release.apk")
    }
    val releaseAab = File(buildOutputsDir, "outputs/bundle/release/app-release.aab")
    if (releaseAab.exists()) {
      releaseAab.copyTo(File(exportDir, "TAVANA-Studio-release.aab"), overwrite = true)
      logger.lifecycle("Exported Release AAB to build-artifacts/TAVANA-Studio-release.aab")
    }
  }
}

