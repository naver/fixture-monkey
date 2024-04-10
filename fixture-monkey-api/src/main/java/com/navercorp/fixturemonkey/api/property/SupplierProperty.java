package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.16", status = API.Status.EXPERIMENTAL)
public final class SupplierProperty implements Property {

	private final Type type;
	private final AnnotatedType annotatedType;

	public SupplierProperty(Type type, AnnotatedType annotatedType) {
		this.type = type;
		this.annotatedType = annotatedType;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	@Nullable
	@Override
	public String getName() {
		return null;
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Arrays.asList(annotatedType.getAnnotations());
	}

	@Override
	public Object getValue(Object instance) {
		Class<?> actualType = Types.getActualType(instance.getClass());

		if (Supplier.class.isAssignableFrom(actualType)) {
			return instance;
		}

		throw new IllegalArgumentException("given value has no match");
	}
}
