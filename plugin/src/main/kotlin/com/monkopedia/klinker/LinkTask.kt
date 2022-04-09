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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

internal open class LinkTask : DefaultTask() {
    @InputFile
    lateinit var binary: File

    @InputDirectory
    lateinit var cppDir: File

    @OutputFile
    lateinit var output: File

    @Nested
    lateinit var configuration: KlinkedExecutableOptions

    @OptIn(ExperimentalStdlibApi::class)
    @TaskAction
    fun run() {
        output.parentFile.mkdirs()
        val result = project.exec { exec ->
            val workingDir = project.projectDir
            exec.workingDir = workingDir
            exec.executable = configuration.compiler?.absolutePath ?: "clang++"
            exec.args = buildList {
                add("-o")
                add(output.toRelativeString(workingDir))

                add("-I")
                add(binary.parentFile.toRelativeString(workingDir))

                addAll(configuration.compilerOpts)
                configuration.linkerOpts.forEach { linkerOpt ->
                    add("-Xlinker")
                    add(linkerOpt)
                }

                cppDir.listFiles { f -> f.extension == "cpp" || f.extension == "c" }?.forEach { f ->
                    add(f.toRelativeString(workingDir))
                }
                add(binary.toRelativeString(workingDir))
                println("Executing with arguments $this")
            }
        }
        result.rethrowFailure()
        println("Executing link")
    }
}
