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
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
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
  testOptions {
    unitTests {
      isReturnDefaultValues = true
      isIncludeAndroidResources = true
    }
  }
}

dependencies {
  implementation(domain)
  implementation(core)
  implementation(coreUi)
  implementation(mviBase)
  implementation(uiTheme)

  implementationCompose()

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.material)

  implementation(deps.coroutines.core)
  implementation(deps.arrow.core)
  implementation(deps.timber)
  implementation(deps.flowExt)

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)

  implementation(deps.coil.compose)
  implementation(deps.accompanist.swiperefresh)
}
