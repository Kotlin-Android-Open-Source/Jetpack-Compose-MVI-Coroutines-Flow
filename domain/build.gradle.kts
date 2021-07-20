plugins {
  kotlin
  kotlinKapt
}

dependencies {
  implementation(deps.jetbrains.coroutinesCore)

  val dagger = "2.38"
  implementation("com.google.dagger:dagger:$dagger")
  kapt("com.google.dagger:dagger-compiler:$dagger")
}
