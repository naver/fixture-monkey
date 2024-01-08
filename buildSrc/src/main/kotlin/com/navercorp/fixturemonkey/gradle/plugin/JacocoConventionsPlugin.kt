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
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

class JacocoConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(JacocoPlugin::class.java)

        project.extensions.configure<JacocoPluginExtension> {
            toolVersion = "0.8.11"
            reportsDirectory.set(File("${project.layout.buildDirectory}/reports/jacoco"))
        }

        project.tasks.named<JacocoReport>("jacocoTestReport") {
            reports {
                xml.required.set(true)
                xml.outputLocation.set(File("${project.layout.buildDirectory}/reports/jacoco/jacoco.xml"))
                csv.required.set(false)
                html.required.set(true)
                html.outputLocation.set(File("${project.layout.buildDirectory}/reports/jacoco/jacoco.html"))
            }
        }

        project.tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
            violationRules {
                rule {
                    limit {
                        counter = "LINE"
                    }
                }
            }
        }

        project.tasks.named("check") { dependsOn("jacocoTestCoverageVerification") }
    }
}
