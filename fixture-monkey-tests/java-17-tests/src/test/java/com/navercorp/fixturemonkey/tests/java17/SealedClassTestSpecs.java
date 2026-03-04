package com.navercorp.fixturemonkey.tests.java17;

public final class SealedClassTestSpecs {
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

	// Generic sealed interface
	public sealed interface GenericSealedShape<T> permits GenericCircle, GenericSquare {
		T getLabel();
	}

	public record GenericCircle<T>(T label, double radius) implements GenericSealedShape<T> {
		@Override
		public T getLabel() {
			return label;
		}
	}

	public record GenericSquare<T>(T label, double side) implements GenericSealedShape<T> {
		@Override
		public T getLabel() {
			return label;
		}
	}

	public record GenericSealedShapeContainer(
		GenericSealedShape<String> stringShape,
		GenericSealedShape<Integer> intShape
	) {
	}
}
