@file:Suppress("unused", "ClassName", "SpellCheckingInspection")

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

const val ktlintVersion = "0.43.0"
const val kotlinVersion = "1.7.20"

object appConfig {
  const val applicationId = "com.hoc.flowmvi"

  const val compileSdkVersion = 33
  const val buildToolsVersion = "33.0.0"

  const val minSdkVersion = 21
  const val targetSdkVersion = 33
  const val versionCode = 1
  const val versionName = "1.0"
}

object deps {
  object kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
  }

  object androidx {
    const val appCompat = "androidx.appcompat:appcompat:1.5.1"
    const val coreKtx = "androidx.core:core-ktx:1.9.0"
    const val material = "com.google.android.material:material:1.4.0"
    const val activityCompose = "androidx.activity:activity-compose:1.6.1"

    object navigation {
      const val version = "2.5.2"
      const val compose = "androidx.navigation:navigation-compose:$version"
    }
  }

  object compose {
    const val androidxComposeCompiler = "1.3.2"
    const val bom = "androidx.compose:compose-bom:2022.11.00"

    const val layout = "androidx.compose.foundation:foundation-layout"
    const val foundation = "androidx.compose.foundation:foundation"
    const val ui = "androidx.compose.ui:ui"
    const val material = "androidx.compose.material:material"
    const val material3 = "androidx.compose.material3:material3"
    const val materialIconsExtended = "androidx.compose.material:material-icons-extended"
    const val runtime = "androidx.compose.runtime:runtime"
    const val tooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
  }

  object lifecycle {
    private const val version = "2.6.0-alpha01"

    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version" // viewModelScope
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version" // lifecycleScope
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
    const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
    const val runtimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version" // lifecycleScope
  }

  object squareup {
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:4.8.1"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.12.0"
  }

  object coroutines {
    private const val version = "1.6.4"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
  }

  object arrow {
    private const val version = "1.1.3"
    const val core = "io.arrow-kt:arrow-core:$version"
  }

  object daggerHilt {
    const val version = "2.43.2"
    const val android = "com.google.dagger:hilt-android:$version"
    const val core = "com.google.dagger:hilt-core:$version"
    const val compiler = "com.google.dagger:hilt-compiler:$version"
    const val navigationCompose = "androidx.hilt:hilt-navigation-compose:1.0.0"
  }

  object dagger {
    const val version = "2.40.2"
    const val core = "com.google.dagger:dagger:$version"
    const val compiler = "com.google.dagger:dagger-compiler:$version"
  }

  object accompanist {
    const val swiperefresh = "com.google.accompanist:accompanist-swiperefresh:0.28.0"
  }

  object coil {
    const val compose = "io.coil-kt:coil-compose:2.2.2"
  }

  const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5"

  const val viewBindingDelegate = "com.github.hoc081098:ViewBindingDelegate:1.2.0"
  const val flowExt = "io.github.hoc081098:FlowExt:0.5.0"
  const val timber = "com.jakewharton.timber:timber:5.0.1"

  object test {
    const val junit = "junit:junit:4.13.2"
    const val androidxJunit = "androidx.test.ext:junit:1.1.2"
    const val androidXSspresso = "androidx.test.espresso:espresso-core:3.3.0"

    const val mockk = "io.mockk:mockk:1.12.5"
    const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion"
  }
}

private typealias PDsS = PluginDependenciesSpec
private typealias PDS = PluginDependencySpec

inline val PDsS.androidApplication: PDS get() = id("com.android.application")
inline val PDsS.androidLib: PDS get() = id("com.android.library")
inline val PDsS.kotlinAndroid: PDS get() = kotlin("android")
inline val PDsS.kotlin: PDS get() = kotlin("jvm")
inline val PDsS.kotlinKapt: PDS get() = kotlin("kapt")
inline val PDsS.kotlinParcelize: PDS get() = id("kotlin-parcelize")
inline val PDsS.daggerHiltAndroid: PDS get() = id("dagger.hilt.android.plugin")

inline val DependencyHandler.domain get() = project(":domain")
inline val DependencyHandler.core get() = project(":core")
inline val DependencyHandler.uiTheme get() = project(":ui-theme")
inline val DependencyHandler.coreUi get() = project(":core-ui")
inline val DependencyHandler.data get() = project(":data")
inline val DependencyHandler.featureMain get() = project(":feature-main")
inline val DependencyHandler.featureAdd get() = project(":feature-add")
inline val DependencyHandler.featureSearch get() = project(":feature-search")
inline val DependencyHandler.mviBase get() = project(":mvi-base")
inline val DependencyHandler.mviTesting get() = project(":mvi-testing")
inline val DependencyHandler.testUtils get() = project(":test-utils")

fun DependencyHandler.implementationCompose(
  includeMaterial2: Boolean = false,
) {
  arrayOf(
    platform(deps.compose.bom),
    // activity compose
    deps.androidx.activityCompose,
    // navigation compose
    deps.androidx.navigation.compose,
    // lifecycle compose
    deps.lifecycle.viewModelCompose,
    deps.lifecycle.runtimeCompose,
    // hilt navigation compose
    deps.daggerHilt.navigationCompose,
    // compose
    deps.compose.layout,
    deps.compose.foundation,
    deps.compose.ui,
    *(
      if (includeMaterial2) arrayOf(deps.compose.material)
      else emptyArray()
      ),
    deps.compose.material3,
    deps.compose.materialIconsExtended,
    deps.compose.runtime,
    deps.compose.uiToolingPreview,
  ).forEach { add("implementation", it) }

  add("debugImplementation", deps.compose.tooling)
  add("debugImplementation", deps.kotlin.reflect)
}

fun DependencyHandler.addUnitTest(testImplementation: Boolean = true) {
  val configName = if (testImplementation) "testImplementation" else "implementation"

  add(configName, deps.test.junit)
  add(configName, deps.test.mockk)
  add(configName, deps.test.kotlinJUnit)
  add(configName, deps.coroutines.test)
}

val Project.isCiBuild: Boolean
  get() = providers.environmentVariable("CI")
    .forUseAtConfigurationTime()
    .orNull == "true"
