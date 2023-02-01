import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.4.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:6.12.1")
    classpath("com.google.dagger:hilt-android-gradle-plugin:${deps.daggerHilt.version}")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.44.0")
    classpath("org.jacoco:org.jacoco.core:0.8.8")
    classpath("com.vanniktech:gradle-android-junit-jacoco-plugin:0.17.0-SNAPSHOT")
    classpath("dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.4.0")
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "com.github.ben-manes.versions")

  configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")

      ktlint(ktlintVersion).userData(
        // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
        mapOf(
          "indent_size" to "2",
          "ij_kotlin_imports_layout" to "*",
          "end_of_line" to "lf",
          "charset" to "utf-8"
        )
      )

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces(2)
      endWithNewline()
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")

      ktlint(ktlintVersion).userData(
        mapOf(
          "indent_size" to "2",
          "ij_kotlin_imports_layout" to "*",
          "end_of_line" to "lf",
          "charset" to "utf-8"
        )
      )

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      if (project.findProperty("composeCompilerReports") == "true") {
        freeCompilerArgs = freeCompilerArgs + listOf(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir.absolutePath}/compose_compiler"
        )
      }
      if (project.findProperty("composeCompilerMetrics") == "true") {
        freeCompilerArgs = freeCompilerArgs + listOf(
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir.absolutePath}/compose_compiler"
        )
      }
    }
  }
}

allprojects {
  val javaVersion = JavaVersion.VERSION_11.toString()

  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = javaVersion

      // Opt-in to experimental compose APIs
      freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
      // Enable experimental coroutines APIs, including collectAsState()
      freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
  }

  tasks.withType<JavaCompile> {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
  }

  repositories {
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
