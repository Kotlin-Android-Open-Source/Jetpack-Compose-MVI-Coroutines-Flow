plugins {
  androidApplication
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
    applicationId = appConfig.applicationId
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
}

dependencies {
  implementation(
    fileTree(
      mapOf(
        "dir" to "libs",
        "include" to listOf("*.jar")
      )
    )
  )

  implementation(domain)
  implementation(data)
  implementation(core)
  implementation(coreUi)
  implementation(featureMain)
  implementation(featureAdd)

  implementation(deps.coroutines.android)
  implementation(deps.timber)

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidxJunit)
  androidTestImplementation(deps.test.androidXSspresso)
}
