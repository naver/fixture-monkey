/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.gradle.plugin

import org.checkerframework.gradle.plugin.CheckerFrameworkExtension
import org.checkerframework.gradle.plugin.CheckerFrameworkTaskExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

class CheckerFrameworkConventionsPlugin : Plugin<Project> {
    companion object {
        private const val VERIFY_TASK_NAME = "checkerFrameworkMain"
    }

    override fun apply(project: Project) {
        project.plugins.apply("org.checkerframework")

        val checkerFramework = project.extensions.getByType<CheckerFrameworkExtension>().apply {
            checkers = listOf("org.checkerframework.checker.nullness.NullnessChecker")
            extraJavacArgs = listOf(
                "-AsuppressWarnings=initialization,method.invocation,type.arguments"
            )
            excludeTests = true
        }

        project.plugins.withId("java") {
            val main = project.extensions.getByType<SourceSetContainer>().getByName("main")
            val toolchains = project.extensions.getByType<JavaToolchainService>()
            val java = project.extensions.getByType<JavaPluginExtension>()

            val verifyNullness = project.tasks.register<JavaCompile>(VERIFY_TASK_NAME) {
                source(main.allJava)
                classpath = main.compileClasspath
                javaCompiler.set(toolchains.compilerFor(java.toolchain))
                destinationDirectory.set(project.layout.buildDirectory.dir("classes/checker-framework/main"))
                onlyIf { !checkerFramework.skipCheckerFramework }
            }

            project.tasks.withType(JavaCompile::class.java).configureEach {
                if (name != VERIFY_TASK_NAME) {
                    extensions.getByType<CheckerFrameworkTaskExtension>().skipCheckerFramework = true
                }
            }

            project.tasks.named("check") {
                dependsOn(verifyNullness)
            }
        }
    }
}
