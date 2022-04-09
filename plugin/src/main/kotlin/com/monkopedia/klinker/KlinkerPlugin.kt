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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.gradle.dsl.AbstractKotlinNativeBinaryContainer
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.StaticLibrary

class KlinkerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
    }
}

fun AbstractKotlinNativeBinaryContainer.klinkedExecutable(
    config: KlinkedExecutableOptions.() -> Unit = {}
) {
    staticLib {
        klinkedExecutable(config)
    }
}

fun StaticLibrary.klinkedExecutable(config: KlinkedExecutableOptions.() -> Unit = {}) {
    val binary = outputFile
    val options = KlinkedExecutableOptions().also(config)
    val buildTypeName = buildType.name.toLowerCase().capitalize()
    val taskName = "link${buildTypeName}ExecutableKlinker"
    val kotlinSourceDir = File(project.buildDir, "gen/klinker/$baseName/kotlin")
    val cppSourceDir = File(project.buildDir, "gen/klinker/$baseName/cpp")
    val outputFile = File(project.buildDir, "bin/klinker/${baseName}$buildTypeName/$baseName")
    cppSourceDir.mkdirs()
    kotlinSourceDir.mkdirs()
    val linkTask = project.tasks.create(taskName, LinkTask::class.java) { task ->
        task.group = "Build"
        task.description = "Uses klinker to link an executable '$buildTypeName' " +
            "for a target '${target.konanTarget}'."
        task.binary = binary
        task.cppDir = cppSourceDir
        task.configuration = options
        task.output = outputFile
        task.dependsOn(compilation.binariesTaskName)
    }
    val kotlinSourceGeneration =
        project.tasks.create(
            "generateKotlinSource$baseName$buildTypeName",
            GenerateKotlinTask::class.java
        ) { task ->
            task.targetMain = options.targetMain
            task.outputDirectory = kotlinSourceDir
        }
    project.kotlinExtension.sourceSets.apply {
        val klinkerSources = create("klinker$baseName$buildTypeName") { sources ->
            sources.kotlin.apply {
                srcDir(kotlinSourceDir)
            }
        }
        compilation.compileKotlinTask.dependsOn(kotlinSourceGeneration)
        compilation.defaultSourceSet.dependsOn(klinkerSources)
    }
    // TODO: Use gradle plugin for cpp compilation?
    val cppSourceGeneration =
        project.tasks.create(
            "generateCppSource$baseName$buildTypeName",
            GenerateCppTask::class.java
        ) { task ->
            task.targetModule = target.konanTarget.family.staticPrefix + baseName
            task.outputDirectory = cppSourceDir
        }
    linkTask.dependsOn(cppSourceGeneration)
    options.run?.let { runOptions ->
        val created = project.tasks.create(
            runOptions.taskName ?: "run${buildTypeName}ExecutableKlinker",
            Exec::class.java
        ) { task ->
            task.group = "Application"
            task.description = "Uses klinker to run an executable '$buildTypeName' " +
                "for a target '${target.konanTarget}'."
            task.executable = outputFile.absolutePath
            task.args = runOptions.args
            task.dependsOn(taskName)
        }
        if (project.tasks.findByName("run") == null) {
            project.tasks.create("run") { task ->
                task.group = "Application"
                task.description = created.description
                task.dependsOn(created)
            }
        }
    }
}
