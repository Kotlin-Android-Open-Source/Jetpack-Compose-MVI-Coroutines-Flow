plugins {
  kotlin
  kotlinKapt
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(deps.arrow.core)

  implementation(deps.dagger.core)
  kapt(deps.dagger.compiler)

  addUnitTest()
  testImplementation(testUtils)
}
