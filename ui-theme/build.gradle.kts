plugins {
  androidLib
  kotlinAndroid
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

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
    useIR = true

    // Opt-in to experimental compose APIs
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"

    // Enable experimental coroutines APIs, including collectAsState()
    freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
  }
  buildFeatures {
    compose = true
  }
  composeOptions {
    kotlinCompilerExtensionVersion = deps.compose.version
  }
}

dependencies {
  implementation(deps.compose.ui)
  implementation(deps.compose.material)
}
