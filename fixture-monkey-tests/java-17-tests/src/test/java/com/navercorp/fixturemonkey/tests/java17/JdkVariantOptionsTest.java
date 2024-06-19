package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.generator.InterfaceObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.BaseSealedClass;
import com.navercorp.fixturemonkey.tests.java17.SealedClassTestSpecs.SealedClass;

class JdkVariantOptionsTest {
	@RepeatedTest(TEST_COUNT)
	@DisplayName("User-added ObjectPropertyGenerators have a higher priority.")
	void sampleSealedClass() {
		// given
		FixtureMonkey sut = FixtureMonkey.builder()
			.objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			.defaultNotNull(true)
			.pushAssignableTypeObjectPropertyGenerator(
				BaseSealedClass.class,
				new InterfaceObjectPropertyGenerator<>(
					List.of(SealedClass.class)
				)
			)
			.build();

		// when
		BaseSealedClass actual = sut.giveMeOne(BaseSealedClass.class);

		then(actual).isInstanceOf(SealedClass.class);
	}
}
