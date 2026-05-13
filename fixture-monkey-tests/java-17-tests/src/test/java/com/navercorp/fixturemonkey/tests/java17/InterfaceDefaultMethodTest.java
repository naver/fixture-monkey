package com.navercorp.fixturemonkey.tests.java17;

import static com.navercorp.fixturemonkey.tests.TestEnvironment.TEST_COUNT;
import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class InterfaceDefaultMethodTest {
	private static final FixtureMonkey SUT = FixtureMonkey.builder()
		.plugin(new InterfacePlugin())
		.build();

	@Test
	void defaultMethod() {
		String actual = SUT.giveMeOne(DefaultMethodInterface.class).defaultMethod();

		then(actual).isEqualTo("test");
	}

	@Test
	void defaultMethodDependsOnAbstractMethod() {
		DependentDefaultMethodInterface instance = SUT.giveMeOne(DependentDefaultMethodInterface.class);

		String actual = instance.defaultMethod();

		then(actual).isEqualTo("test-" + instance.value());
	}

	@RepeatedTest(TEST_COUNT)
	void defaultMethodReturningThis() {
		SelfReturningDefaultMethodInterface instance = SUT.giveMeOne(SelfReturningDefaultMethodInterface.class);

		SelfReturningDefaultMethodInterface actual = instance.self();

		then(actual).isSameAs(instance);
	}

	@RepeatedTest(TEST_COUNT)
	void defaultMethodCallingAbstractMethodWithArgument() {
		ConcreteSelfTypedWither instance = SUT.giveMeOne(ConcreteSelfTypedWither.class);

		ConcreteSelfTypedWither actual = instance.withDefaultContent();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void abstractMethodWithArgumentReturningSelfTypeReturnsProxy() {
		ConcreteSelfTypedWither instance = SUT.giveMeOne(ConcreteSelfTypedWither.class);

		ConcreteSelfTypedWither actual = instance.withContent("new-content");

		then(actual).isSameAs(instance);
	}

	@RepeatedTest(TEST_COUNT)
	void defaultMethodCallingNoArgAbstractMethod() {
		ConcreteSelfTypedWither instance = SUT.giveMeOne(ConcreteSelfTypedWither.class);

		ConcreteSelfTypedWither actual = instance.copyViaDefault();

		then(actual).isNotNull();
	}

	@RepeatedTest(TEST_COUNT)
	void noArgAbstractMethodReturningSelfType() {
		ConcreteSelfTypedWither instance = SUT.giveMeOne(ConcreteSelfTypedWither.class);

		ConcreteSelfTypedWither actual = instance.copy();

		then(actual).isNotNull();
	}

	public interface DefaultMethodInterface {
		default String defaultMethod() {
			return "test";
		}
	}

	public interface DependentDefaultMethodInterface {
		String value();

		default String defaultMethod() {
			return "test-" + value();
		}
	}

	public interface SelfReturningDefaultMethodInterface {
		String value();

		default SelfReturningDefaultMethodInterface self() {
			return this;
		}
	}

	public interface SelfTypedWither<T extends SelfTypedWither<T>> {
		T withContent(String content);

		T copy();

		default T withDefaultContent() {
			return withContent("x");
		}

		default T copyViaDefault() {
			return copy();
		}
	}

	public interface ConcreteSelfTypedWither extends SelfTypedWither<ConcreteSelfTypedWither> {
	}
}
