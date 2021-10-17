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

package com.navercorp.fixturemonkey.kotlin.test

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.domains.Domain
import org.assertj.core.api.BDDAssertions.then

class PrimaryConstructorArbitraryGeneratorTest {
    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithPrimaryConstructor(@ForAll actual: PrimaryConstructor) {
        then(actual.stringValue).isNotBlank
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithNestedOne(@ForAll actual: Nested) {
        then(actual.nested.intValue).isPositive
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeDataClass(@ForAll actual: DataValue) {
        then(actual.intValue).isNotNull
        then(actual.stringValue).isNotNull
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithVarValue(@ForAll actual: VarValue) {
        then(actual.intValue).isNotNull
        then(actual.stringValue).isNotNull
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithNullable(@ForAll actual: NullableValue) {
        then(actual.intValue).isNotNull
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithDefaultValue(@ForAll actual: DefaultValue) {
        then(actual.stringValue).isNotEqualTo("default_value")
    }

    @Property
    @Domain(PrimaryConstructorArbitraryGeneratorTestSpecs::class)
    fun giveMeClassWithSecondaryConstructor(@ForAll actual: SecondaryConstructor) {
        then(actual.stringValue).isNotEqualTo("default_value")
    }

    @Property
    fun giveMeInterfaceClass() {
        // when
        val actual = SUT.giveMeOne(InterfaceClass::class.java)

        then(actual).isNull()
    }
}
