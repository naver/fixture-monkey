package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.instantiator.instantiateBy
import com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class JakartaValidationTest {
	@Test
	fun instantiateByConstructorWithJakartaValidationConstraint() {
		// given
		data class ValidatedObject(
			@field:Size(min = 1, max = 10)
			val name: String,
		)

		// when
		val actual = SUT.giveMeKotlinBuilder<ValidatedObject>()
			.instantiateBy {
				constructor<ValidatedObject> {
					parameter<String>("name")
				}
			}
			.sample()
			.name

		// then
		then(actual.length).isBetween(1, 10)
	}

	@Test
	fun instantiateByConstructorWithNotBlankConstraint() {
		// given
		data class ValidatedObject(
			@field:NotBlank
			val name: String,
		)

		// when
		val actual = SUT.giveMeKotlinBuilder<ValidatedObject>()
			.instantiateBy {
				constructor<ValidatedObject> {
					parameter<String>("name")
				}
			}
			.sample()
			.name

		// then
		then(actual).isNotBlank
	}

	@Test
	fun instantiateByConstructorWithNumericRangeConstraint() {
		// given
		data class ValidatedObject(
			@field:Min(1)
			@field:Max(100)
			val age: Int,
		)

		// when
		val actual = SUT.giveMeKotlinBuilder<ValidatedObject>()
			.instantiateBy {
				constructor<ValidatedObject> {
					parameter<Int>("age")
				}
			}
			.sample()
			.age

		// then
		then(actual).isBetween(1, 100)
	}

	@Test
	fun instantiateByConstructorWithMultipleConstraints() {
		// given
		data class ValidatedObject(
			@field:Size(min = 1, max = 10)
			val name: String,
			@field:Min(0)
			@field:Max(100)
			val age: Int,
		)

		// when
		val actual = SUT.giveMeKotlinBuilder<ValidatedObject>()
			.instantiateBy {
				constructor<ValidatedObject> {
					parameter<String>("name")
					parameter<Int>("age")
				}
			}
			.sample()

		// then
		then(actual.name.length).isBetween(1, 10)
		then(actual.age).isBetween(0, 100)
	}

	@Test
	fun instantiateByConstructorWithoutInstantiateRespectsConstraint() {
		// given
		data class ValidatedObject(
			@field:Size(min = 1, max = 10)
			val name: String,
		)

		// when
		val actual = SUT.giveMeOne<ValidatedObject>().name

		// then
		then(actual.length).isBetween(1, 10)
	}

	@Test
	fun instantiateBySecondaryConstructorWithJakartaValidationConstraint() {
		// given
		class ValidatedObject(
			@field:Size(min = 1, max = 10)
			val name: String,
			val value: Int,
		) {
			constructor(name: String) : this(name, 0)
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<ValidatedObject>()
			.instantiateBy {
				constructor<ValidatedObject> {
					parameter<String>("name")
				}
			}
			.sample()
			.name

		// then
		then(actual.length).isBetween(1, 10)
	}

	@Test
	fun privateBackingFieldWithPublicComputedProperty() {
		// given
		class ObjectWithPrivateBacking(
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<ObjectWithPrivateBacking>()
			.instantiateBy {
				constructor<ObjectWithPrivateBacking> {
					parameter<String>("_value")
				}
			}
			.sample()

		// then
		then(actual.value).isNotNull
	}

	@Test
	fun privateBackingFieldWithValidationConstraint() {
		// given
		class ObjectWithPrivateBacking(
			@field:Size(min = 1, max = 10)
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<ObjectWithPrivateBacking>()
			.instantiateBy {
				constructor<ObjectWithPrivateBacking> {
					parameter<String>("_value")
				}
			}
			.sample()

		// then
		then(actual.value.length).isBetween(1, 10)
	}

	@Test
	fun privateBackingFieldSetValueByParameterName() {
		// given
		class ObjectWithPrivateBacking(
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<ObjectWithPrivateBacking>()
			.instantiateBy {
				constructor<ObjectWithPrivateBacking> {
					parameter<String>("_value")
				}
			}
			.set("_value", "hello")
			.sample()

		// then
		then(actual.value).isEqualTo("hello")
	}

	@Test
	fun privateBackingFieldWithoutInstantiateBy() {
		// given
		class ObjectWithPrivateBacking(
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeOne<ObjectWithPrivateBacking>()

		// then
		then(actual.value).isNotNull
	}

	@Test
	fun privateBackingFieldWithValidationConstraintWithoutInstantiateBy() {
		// given
		class ObjectWithPrivateBacking(
			@field:Size(min = 1, max = 10)
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeOne<ObjectWithPrivateBacking>()

		// then
		then(actual.value.length).isBetween(1, 10)
	}

	@Test
	fun privateValInstantiateByFieldTargetSize() {
		// given
		class Obj(
			@field:Size(min = 2, max = 5)
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<Obj>()
			.instantiateBy { constructor<Obj> { parameter<String>("_value") } }
			.sample()
			.value

		// then
		then(actual.length).isBetween(2, 5)
	}

	@Test
	fun privateValInstantiateByMultiplePrivateFields() {
		// given
		class Obj(
			@field:Size(min = 1, max = 8)
			private val _name: String,
			@field:Min(10)
			@field:Max(99)
			private val _age: Int,
		) {
			val name: String get() = _name
			val age: Int get() = _age
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<Obj>()
			.instantiateBy {
				constructor<Obj> {
					parameter<String>("_name")
					parameter<Int>("_age")
				}
			}
			.sample()

		// then
		then(actual.name.length).isBetween(1, 8)
		then(actual.age).isBetween(10, 99)
	}

	@Test
	fun privateValInstantiateBySetOverridesConstraint() {
		// given
		class Obj(
			@field:Size(min = 1, max = 5)
			private val _value: String,
		) {
			val value: String get() = _value
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<Obj>()
			.instantiateBy { constructor<Obj> { parameter<String>("_value") } }
			.set("_value", "hi")
			.sample()
			.value

		// then
		then(actual).isEqualTo("hi")
	}

	@Test
	fun privateValInstantiateByMixedPrivateAndPublic() {
		// given
		class Obj(
			@field:Size(min = 1, max = 5)
			private val _secret: String,
			@field:NotBlank
			val visible: String,
		) {
			val secret: String get() = _secret
		}

		// when
		val actual = SUT.giveMeKotlinBuilder<Obj>()
			.instantiateBy {
				constructor<Obj> {
					parameter<String>("_secret")
					parameter<String>("visible")
				}
			}
			.sample()

		// then
		then(actual.secret.length).isBetween(1, 5)
		then(actual.visible).isNotBlank
	}

	companion object {
		private val SUT = FixtureMonkey.builder()
			.plugin(KotlinPlugin())
			.plugin(JakartaValidationPlugin())
			.build()
	}
}
