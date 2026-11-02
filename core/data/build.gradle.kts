plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.ksp)
  alias(libs.plugins.hilt)
  alias(libs.plugins.room)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "tech.nikku.learningapp.core.data"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

room {
  schemaDirectory("$projectDir/schemas")
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  api(project(":core:domain"))

  // Room
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.room.paging)
  ksp(libs.room.compiler)

  // Hilt
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  // DataStore & WorkManager
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.work.runtime.ktx)

  // Media & Paging
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.paging.runtime)

  // Serialization & Coroutines
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.coroutines.core)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
  testImplementation(libs.robolectric)
  testImplementation(libs.room.testing)
  testImplementation(libs.androidx.work.testing)
  testImplementation(libs.androidx.test.core)
}
