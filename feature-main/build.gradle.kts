plugins {
  androidLib
  kotlinAndroid
  kotlinKapt
  daggerHiltAndroid
}

hilt {
  enableExperimentalClasspathAggregation = true
}

android {
  compileSdkVersion(appConfig.compileSdkVersion)
  buildToolsVersion(appConfig.buildToolsVersion)

  defaultConfig {
    minSdkVersion(appConfig.minSdkVersion)
    targetSdkVersion(appConfig.targetSdkVersion)
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = deps.compose.version
  }
}

dependencies {
  implementation(domain)
  implementation(core)
  implementation(uiTheme)

  implementationCompose()
  implementation(deps.lifecycle.viewModelCompose)

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.material)

  implementation(deps.jetbrains.coroutinesCore)

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)

  implementation(deps.accompanist.coil)
}
