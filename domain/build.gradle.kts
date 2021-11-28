plugins {
  kotlin
  kotlinKapt
}

dependencies {
  implementation(deps.jetbrains.coroutinesCore)

  implementation(deps.dagger.core)
  kapt(deps.dagger.compiler)
}
