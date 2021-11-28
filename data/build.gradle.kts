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

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
  }
}

dependencies {
  implementation(core)
  implementation(domain)

  implementation(deps.coroutines.core)

  implementation(deps.squareup.retrofit)
  implementation(deps.squareup.moshiKotlin)
  implementation(deps.squareup.converterMoshi)
  implementation(deps.squareup.loggingInterceptor)

  implementation(deps.arrow.core)
  implementation(deps.timber)

  addUnitTest()
  testImplementation(testUtils)

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)
}
