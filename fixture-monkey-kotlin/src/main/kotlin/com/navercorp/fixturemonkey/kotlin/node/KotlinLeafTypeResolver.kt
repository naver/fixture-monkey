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

import com.navercorp.objectfarm.api.node.LeafTypeResolver
import com.navercorp.objectfarm.api.type.JvmType

/**
 * A [LeafTypeResolver] that treats Kotlin standard library types as leaf types.
 *
 * This includes types in the `kotlin.*` packages (e.g., `kotlin.Unit`).
 */
class KotlinLeafTypeResolver private constructor() : LeafTypeResolver {
    override fun isLeafType(jvmType: JvmType): Boolean {
        val pkg = jvmType.rawType.`package` ?: return false
        return pkg.name.startsWith("kotlin")
    }

    companion object {
        @JvmField
        val INSTANCE = KotlinLeafTypeResolver()
    }
}
