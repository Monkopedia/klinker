# Klinker

[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Maven Central](https://img.shields.io/maven-central/v/com.monkopedia.klinker/plugin/0.1.2)](https://search.maven.org/artifact/com.monkopedia.klinker/plugin/0.1.2/pom)

Klinker is a gradle plugin making it possible to link kotlin native executables with custom
linkers and options. It does this by creating a static library for kotlin compilation, then
generates a c+kotlin wrapper that calls into kotlin to start the app, finally using the specified
compiler to compile and link the c code and kotlin library into a binary.

Currently only supported from gradle kts, untested on groovy.

## Why?

I've encountered that some libraries are configured to be built with clang, and it can be very hard
to convert their build systems to gcc. While there are likely ways to get clang and gcc play nicely
together, I have had the easiest luck with simply compiling kotlin with its toolchain, then linking
separately.

## Usage

Once the plugin is applied, there is an extension method which sets up a klinker target within a
native target (or specifically for a static library for more granular control).

```
import com.monkopedia.klinker.klinkedExecutable
plugins {
    id("com.monkopedia.klinker.plugin")
    kotlin("multiplatform") version "1.6.20"
}

kotlin {
    linuxX64("native") {
        binaries {
            klinkedExecutable {
                compilerArgs("-lcustom_library")
                runTask {
                    args("--flag_passed_to_kotlin")
                }
            }
        }
    }
}
```
