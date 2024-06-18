package com.navercorp.fixturemonkey.tests.java17;

final class SealedClassTestSpecs {
	public static sealed class BaseSealedClass permits SealedClass {
	}

	public static sealed class SealedClass extends BaseSealedClass permits SealedClassImpl {
	}

	public static final class SealedClassImpl extends SealedClass {
	}

	public sealed interface BaseSealedInterface permits SealedInterface {
	}

	public sealed interface SealedInterface extends BaseSealedInterface permits SealedInterfaceImpl {
	}

	public record SealedInterfaceImpl(String value) implements SealedInterface {
	}

	public record BaseSealedClassProperty(BaseSealedClass sealedClass, BaseSealedInterface sealedInterface) {
	}

	public record SealedClassProperty(
		SealedClass sealedClass,
		SealedInterface sealedInterface
	) {
	}
}
