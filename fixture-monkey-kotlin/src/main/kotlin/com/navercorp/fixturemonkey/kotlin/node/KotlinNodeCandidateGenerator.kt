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

package com.navercorp.fixturemonkey.kotlin.node

import com.navercorp.fixturemonkey.api.type.KotlinTypeDetector
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator
import com.navercorp.objectfarm.api.type.JvmType

/**
 * A [JvmNodeCandidateGenerator] that only activates for Kotlin types.
 * Delegates actual candidate generation to the wrapped [delegate] generator.
 *
 * When registered as a custom generator with higher priority than the default
 * [JavaFieldNodeCandidateGenerator][com.navercorp.objectfarm.api.nodecandidate.JavaFieldNodeCandidateGenerator],
 * this ensures Kotlin types use constructor-based generation while Java types
 * fall back to field-based generation.
 *
 * @since 1.1.17
 */
class KotlinNodeCandidateGenerator(
    private val delegate: JvmNodeCandidateGenerator
) : JvmNodeCandidateGenerator {

    override fun isSupported(jvmType: JvmType): Boolean =
        KotlinTypeDetector.isKotlinType(jvmType.rawType)

    override fun generateNextNodeCandidates(jvmType: JvmType): List<JvmNodeCandidate> =
        delegate.generateNextNodeCandidates(jvmType)
}
