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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.kotlin.dsl.configure
import java.io.File

class CheckstyleConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(CheckstylePlugin::class.java)
        project.extensions.configure<CheckstyleExtension> {
            configFile = File("${project.rootDir}/tool/naver-checkstyle-rules.xml")
            configProperties = mapOf("suppressionFile" to "${project.rootDir}/tool/naver-checkstyle-suppressions.xml")
            toolVersion = "8.45.1"
            isIgnoreFailures = false
            maxErrors = 0
            maxWarnings = 0
        }
    }
}
