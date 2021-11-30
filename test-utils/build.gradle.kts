plugins {
  kotlin
}

dependencies {
  implementation(deps.coroutines.core)
  implementation(core)
  api(deps.arrow.core)

  addUnitTest(testImplementation = false)
}
