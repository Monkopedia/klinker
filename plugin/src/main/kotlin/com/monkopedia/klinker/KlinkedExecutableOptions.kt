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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

data class KlinkedRunOptions(
    @Input
    @Optional
    var taskName: String? = null,
    /** Arguments passed to executable in run task. */
    @Input
    var args: MutableList<String> = mutableListOf()
) {

    /** Arguments passed to executable in run task. */
    fun args(vararg options: String) {
        args.addAll(options.toList())
    }

    /** Arguments passed to executable in run task. */
    fun args(options: Iterable<String>) {
        args.addAll(options)
    }
}

data class KlinkedExecutableOptions(
    @InputFile
    @Optional
    var compiler: File? = null,
    /** Additional options passed to the linker */
    @Input
    var compilerOpts: MutableList<String> = mutableListOf("-lresolv"),
    /** Additional options passed to the linker */
    @Input
    var linkerOpts: MutableList<String> = mutableListOf(),
    /** Fully qualified main function to run, should have signature (Array<String> -> Int) **/
    @Input
    var targetMain: String = "main",
    @Nested
    @Optional
    var run: KlinkedRunOptions? = null,
) {

    /** Additional options passed to the compiler */
    fun compilerOpts(vararg options: String) {
        compilerOpts.addAll(options.toList())
    }

    /** Additional options passed to the complier */
    fun compilerOpts(options: Iterable<String>) {
        compilerOpts.addAll(options)
    }

    /** Additional options passed to the linker */
    fun linkerOpts(vararg options: String) {
        linkerOpts.addAll(options.toList())
    }

    /** Additional options passed to the linker */
    fun linkerOpts(options: Iterable<String>) {
        linkerOpts.addAll(options)
    }

    fun runTask(config: KlinkedRunOptions.() -> Unit = {}) {
        run = KlinkedRunOptions().also(config)
    }
}
