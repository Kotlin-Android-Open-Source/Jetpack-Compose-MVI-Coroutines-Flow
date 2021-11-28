@file:Suppress("unused", "ClassName", "SpellCheckingInspection")

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

const val ktlintVersion = "0.40.0"
const val kotlinVersion = "1.5.31"

object appConfig {
  const val applicationId = "com.hoc.flowmvi"

  const val compileSdkVersion = 31
  const val buildToolsVersion = "31.0.0"

  const val minSdkVersion = 21
  const val targetSdkVersion = 30
  const val versionCode = 1
  const val versionName = "1.0"
}

object deps {
  object kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
  }

  object androidx {
    const val appCompat = "androidx.appcompat:appcompat:1.4.0"
    const val coreKtx = "androidx.core:core-ktx:1.7.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.2"
    const val material = "com.google.android.material:material:1.4.0"
    const val activityCompose = "androidx.activity:activity-compose:1.4.0"
  }

  object compose {
    const val version = "1.1.0-beta03"

    const val layout = "androidx.compose.foundation:foundation-layout:$version"
    const val foundation = "androidx.compose.foundation:foundation:$version"
    const val ui = "androidx.compose.ui:ui:$version"
    const val material = "androidx.compose.material:material:$version"
    const val materialIconsExtended = "androidx.compose.material:material-icons-extended:$version"
    const val runtime = "androidx.compose.runtime:runtime:$version"
    const val tooling = "androidx.compose.ui:ui-tooling:$version"
  }

  object lifecycle {
    private const val version = "2.4.0"

    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version" // viewModelScope
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version" // lifecycleScope
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha05"
  }

  object squareup {
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:4.8.1"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.10.0"
  }

  object jetbrains {
    private const val version = "1.5.2"

    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
  }

  object daggerHilt {
    const val version = "2.40.2"
    const val android = "com.google.dagger:hilt-android:$version"
    const val core = "com.google.dagger:hilt-core:$version"
    const val compiler = "com.google.dagger:hilt-compiler:$version"
  }

  object dagger {
    const val version = "2.40.2"
    const val core = "com.google.dagger:dagger:$version"
    const val compiler = "com.google.dagger:dagger-compiler:$version"
  }

  object accompanist {
    private const val version = "0.10.0"
    const val coil = "com.google.accompanist:accompanist-coil:$version"
    const val swiperefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
  }

  object test {
    const val junit = "junit:junit:4.13"
    const val androidxJunit = "androidx.test.ext:junit:1.1.2"
    const val androidXSspresso = "androidx.test.espresso:espresso-core:3.3.0"
  }
}

private typealias PDsS = PluginDependenciesSpec
private typealias PDS = PluginDependencySpec

inline val PDsS.androidApplication: PDS get() = id("com.android.application")
inline val PDsS.androidLib: PDS get() = id("com.android.library")
inline val PDsS.kotlinAndroid: PDS get() = kotlin("android")
inline val PDsS.kotlin: PDS get() = kotlin("jvm")
inline val PDsS.kotlinKapt: PDS get() = kotlin("kapt")
inline val PDsS.daggerHiltAndroid: PDS get() = id("dagger.hilt.android.plugin")

inline val DependencyHandler.domain get() = project(":domain")
inline val DependencyHandler.core get() = project(":core")
inline val DependencyHandler.uiTheme get() = project(":ui-theme")
inline val DependencyHandler.data get() = project(":data")
inline val DependencyHandler.featureMain get() = project(":feature-main")
inline val DependencyHandler.featureAdd get() = project(":feature-add")

fun DependencyHandler.implementationCompose() {
  arrayOf(
    deps.androidx.activityCompose,
    deps.lifecycle.viewModelCompose,
    deps.compose.layout,
    deps.compose.foundation,
    deps.compose.ui,
    deps.compose.material,
    deps.compose.materialIconsExtended,
    deps.compose.runtime,
  ).forEach { add("implementation", it) }

  add("debugImplementation", deps.compose.tooling)
  add("debugImplementation", deps.kotlin.reflect)
}
