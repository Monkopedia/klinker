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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

internal open class GenerateKotlinTask : DefaultTask() {
    @Input
    lateinit var targetMain: String

    @OutputDirectory
    lateinit var outputDirectory: File

    @TaskAction
    fun run() {
        if (!outputDirectory.exists()) outputDirectory.mkdirs()
        val output = File(outputDirectory, "klinker_main.kt")
        val targetList = targetMain.split(".")
        val targetName = targetList.last()
        val targetImport = if (targetList.size > 1) "import $targetMain" else ""
        output.writeText(
            """
            |@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
            |import kotlinx.cinterop.*
            |$targetImport
            |
            |fun klinker_main(args: NativePtr) = $targetName(
            |    sequence {
            |        val ptr = interpretCPointer<CPointerVar<ByteVar>>(args) ?: error("Invalid ${'$'}args")
            |        var i = 0
            |        while (ptr[i].rawValue != NativePtr.NULL) {
            |            yield(ptr[i]?.toKString() ?: "")
            |            i++
            |        }
            |    }.toList().let { it.subList(1, it.size) }
            |)
        """.trimMargin()
        )
    }
}
