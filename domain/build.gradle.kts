plugins {
  kotlin
  kotlinKapt
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.arrow.core)

  implementation(deps.dagger.core)
  kapt(deps.dagger.compiler)

  addUnitTest()
  testImplementation(testUtils)
}
