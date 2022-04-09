/*
 * Copyright 2022 Jason Monk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.monkopedia.klinker

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder

/**
 * A simple functional test for the 'com.monkopedia.klinker.plugin' plugin.
 */
class KlinkerPluginFunctionalTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun getProjectDir() = tempFolder.root
    private fun getBuildFile() = getProjectDir().resolve("build.gradle.kts")
    private fun getSettingsFile() = getProjectDir().resolve("settings.gradle.kts")
    private fun getSourceFile() = getProjectDir().resolve("src/nativeMain/kotlin/main.kt")

    @Test
    fun `can run task`() {
        getSourceFile().parentFile.mkdirs()
        getSourceFile().writeText(
            """
            fun main(args: List<String>) = 0.also { println("Executing kotlin code") }
            """.trimIndent()
        )
        // Setup the test build
        getSettingsFile().writeText(
            """
            rootProject.name = "test_project"
            """.trimIndent()
        )
        getBuildFile().writeText(
            """
                |import com.monkopedia.klinker.klinkedExecutable
                |plugins {
                |    id("com.monkopedia.klinker.plugin")
                |    kotlin("multiplatform") version "1.6.20"
                |}
                |
                |repositories {
                |    jcenter()
                |}
                |
                |kotlin {
                |    // Determine host preset.
                |    val hostOs = System.getProperty("os.name")
                |
                |    // Create target for the host platform.
                |    val hostTarget = when {
                |        hostOs == "Mac OS X" -> macosX64("native")
                |        hostOs == "Linux" -> linuxX64("native")
                |        hostOs.startsWith("Windows") -> mingwX64("native")
                |        else -> throw GradleException(
                |            "Host OS '${'$'}hostOs' is not supported in Kotlin/Native ${'$'}project."
                |        )
                |    }
                |
                |    hostTarget.apply {
                |        binaries {
                |            klinkedExecutable()
                |        }
                |    }
                |    sourceSets["nativeMain"].dependencies {
                |        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
                |    }
                |}
            """.trimMargin()
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("linkReleaseExecutableKlinker")
        runner.withProjectDir(getProjectDir())
        try {
            val result = runner.build()

            // Verify the result
            assertTrue(result.output.contains("Executing link"))
            getProjectDir().listRecursive().forEach {
                println("Found: $it")
            }
            assertTrue(File(getProjectDir(), "build/").exists())
        } finally {
            val text = File(
                getProjectDir(),
                "build/bin/native/releaseStatic/libtest_project_api.h"
            ).readText()
            println("Header file:\n$text")
        }
    }
}

private fun File.listRecursive() = sequence<File> {
    listRecursive(this@listRecursive)
}

private suspend fun SequenceScope<File>.listRecursive(file: File) {
    yield(file)
    for (file in (file.listFiles() ?: emptyArray())) {
        listRecursive(file)
    }
}
