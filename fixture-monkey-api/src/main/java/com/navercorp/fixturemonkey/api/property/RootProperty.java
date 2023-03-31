package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class RootProperty implements Property {
	private final AnnotatedType annotatedType;

	public RootProperty(AnnotatedType annotatedType) {
		this.annotatedType = annotatedType;
	}

	@Override
	public Type getType() {
		return this.getAnnotatedType().getType();
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return this.annotatedType;
	}

	@Override
	public String getName() {
		return "$";
	}

	@Override
	public List<Annotation> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public Object getValue(Object instance) {
		if (Types.getActualType(this.annotatedType.getType()) == instance.getClass()) {
			return instance;
		}

		throw new IllegalArgumentException(
			"RootProperty obj is not a root type. annotatedType: " + this.annotatedType + ", objType: "
				+ instance.getClass()
		);
	}

	@Override
	public String toString() {
		return "RootProperty{"
			+ "annotatedType=" + annotatedType + '}';
	}
}
