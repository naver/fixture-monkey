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

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KFixtureMonkey
import net.jqwik.api.Arbitrary
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

val SUT: FixtureMonkey = KFixtureMonkey.create()

class PrimaryConstructorArbitraryGeneratorTestSpecs() : DomainContextBase() {
    @Provide
    fun primaryConstructor(): Arbitrary<PrimaryConstructor> = SUT.giveMeArbitrary(PrimaryConstructor::class.java)

    @Provide
    fun intValue(): Arbitrary<IntValue> = SUT.giveMeArbitrary(IntValue::class.java)

    @Provide
    fun nested(): Arbitrary<Nested> = SUT.giveMeArbitrary(Nested::class.java)

    @Provide
    fun dataValue(): Arbitrary<DataValue> = SUT.giveMeArbitrary(DataValue::class.java)

    @Provide
    fun varValue(): Arbitrary<VarValue> = SUT.giveMeArbitrary(VarValue::class.java)

    @Provide
    fun nullableValue(): Arbitrary<NullableValue> = SUT.giveMeArbitrary(NullableValue::class.java)

    @Provide
    fun defaultValue(): Arbitrary<DefaultValue> = SUT.giveMeArbitrary(DefaultValue::class.java)

    @Provide
    fun secondaryConstructor(): Arbitrary<SecondaryConstructor> = SUT.giveMeArbitrary(SecondaryConstructor::class.java)
}

class PrimaryConstructor(
    val intValue: Int,
    @field:NotBlank
    val stringValue: String
)

class IntValue(
    @field:Positive
    val intValue: Int
)

class Nested(
    val nested: IntValue
)

data class DataValue(
    val intValue: Int,
    val stringValue: String
)

class VarValue(
    var intValue: Int,
    var stringValue: String
)

class NullableValue(
    val intValue: Int,
    val stringValue: String?
)

class DefaultValue(
    val intValue: Int,
    val stringValue: String = "default_value"
)

class SecondaryConstructor(
    val intValue: Int,
    val stringValue: String
) {
    constructor(another: String) : this(0, "default_value")
}

interface InterfaceClass {
    fun test()
}
