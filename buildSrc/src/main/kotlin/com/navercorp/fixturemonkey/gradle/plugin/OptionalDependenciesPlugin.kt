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

@file:Suppress("KDocUnresolvedReference")

package com.navercorp.fixturemonkey.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * It is the same plugin as Spring Framework [org.springframework.build.optional.OptionalDependenciesPlugin].
 */
class OptionalDependenciesPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val optional = project.configurations.create(OPTIONAL_CONFIGURATION_NAME).apply {
            isCanBeConsumed = false
            isCanBeResolved = false
        }

        project.plugins.withType<JavaBasePlugin> {
            val sourceSets: SourceSetContainer = project.extensions.getByType<JavaPluginExtension>().sourceSets
            sourceSets.configureEach {
                project.configurations.getByName(compileClasspathConfigurationName)
                    .extendsFrom(optional)
                project.configurations.getByName(runtimeClasspathConfigurationName)
                    .extendsFrom(optional)
            }
        }
    }

    companion object {
        const val OPTIONAL_CONFIGURATION_NAME = "optional"
    }
}
