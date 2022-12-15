import java.util.Properties

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

  signingConfigs {
    create("release") {
      val keystoreProperties = Properties().apply {
        load(
          rootProject.file("keystore/key.properties")
            .apply { check(exists()) }
            .reader()
        )
      }

      keyAlias = keystoreProperties["keyAlias"] as String
      keyPassword = keystoreProperties["keyPassword"] as String
      storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
        .apply { check(exists()) }
      storePassword = keystoreProperties["storePassword"] as String

      // Optional, specify signing versions used
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )

      signingConfig = signingConfigs.getByName("release")
      isDebuggable = false
    }
  }

  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = deps.compose.androidxComposeCompiler
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

  implementation(deps.androidx.appCompat)
  implementationCompose()

  implementation(uiTheme)
  implementation(domain)
  implementation(data)
  implementation(core)
  implementation(coreUi)
  implementation(featureMain)
  implementation(featureAdd)
  implementation(featureSearch)

  implementation(deps.coroutines.android)
  implementation(deps.timber)

  implementation(deps.daggerHilt.android)
  kapt(deps.daggerHilt.compiler)

  testImplementation(deps.test.junit)
  androidTestImplementation(deps.test.androidxJunit)
  androidTestImplementation(deps.test.androidXSspresso)
}
