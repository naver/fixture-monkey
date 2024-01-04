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

import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.SpotBugsExtension
import com.github.spotbugs.snom.SpotBugsPlugin
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.io.File

class SpotbugsConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(SpotBugsPlugin::class.java)
        project.extensions.getByType<SpotBugsExtension>().apply {
            ignoreFailures.set(false)
            reportLevel.set(Confidence.HIGH)
        }

        project.tasks.withType<SpotBugsTask>().configureEach {
            reports {
                register("xml") { this.required.set(true) }
                register("html") { this.required.set(false) }
                register("text") { this.required.set(false) }
            }
        }

        project.tasks.register("printSpotbugsMain") {
            doLast {
                val mainResult = File("${project.layout.buildDirectory}/reports/spotbugs/main.text")
                if (mainResult.exists()) {
                    mainResult.readLines().forEach { println(it) }
                }
            }
        }
        project.tasks.getByName("spotbugsMain").finalizedBy("printSpotbugsMain")
    }
}
