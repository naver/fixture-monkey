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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

class MavenPublishConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply(PublishingPlugin::class.java)
        project.plugins.apply(SigningPlugin::class.java)
        project.plugins.apply(MavenPublishPlugin::class.java)

        project.extensions.getByType<PublishingExtension>().apply {
            publications {
                register<MavenPublication>("fixtureMonkey") {
                    from(project.components["java"])

                    pom {
                        name.set("fixture-monkey")
                        description.set("The easiest way to generate controllable arbitrary test objects")
                        url.set("http://github.com/naver/fixture-monkey")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("ah.jo")
                                name.set("SeongAh Jo")
                                email.set("ah.jo@navercorp.com")
                            }
                            developer {
                                id.set("mhyeon-lee")
                                name.set("Myeonghyeon Lee")
                                email.set("mheyon.lee@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com/naver/fixture-monkey.git")
                            developerConnection.set("scm:git:ssh://github.com/naver/fixture-monkey.git")
                            url.set("http://github.com/naver/fixture-monkey")
                        }
                    }
                }
            }
        }

        project.extensions.getByType<SigningExtension>().apply {
            val signingKeyId: String? by project
            val signingKey: String? by project
            val signingPassword: String? by project

            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            if ((project.version as? String)?.endsWith("SNAPSHOT") == false) {
                sign(project.extensions.getByType<PublishingExtension>().publications["fixtureMonkey"])
            }
        }
    }
}
