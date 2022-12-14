plugins {
  androidLib
  kotlinAndroid
  nocopyPlugin
  kotlinKapt
  daggerHiltAndroid
}

hilt {
  enableExperimentalClasspathAggregation = true
}

android {
  namespace = "com.hoc.flowmvi.ui.search"
  compileSdk = appConfig.compileSdkVersion
  buildToolsVersion = appConfig.buildToolsVersion

  defaultConfig {
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
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
    kotlinCompilerExtensionVersion = deps.compose.androidxComposeCompiler
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
  implementation(deps.immutableCollections)

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.coroutines.core)
  implementation(deps.arrow.core)
  implementation(deps.flowExt)
  implementation(deps.timber)

  addUnitTest()

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)

  implementation(deps.coil.compose)
}
