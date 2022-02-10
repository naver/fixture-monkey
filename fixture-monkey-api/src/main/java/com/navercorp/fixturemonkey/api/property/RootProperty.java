package com.navercorp.fixturemonkey.api.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public final class RootProperty implements Property {
	private final Type type;
	private final AnnotatedType annotatedType;

	public RootProperty(Type type) {
		this.type = type;
		this.annotatedType = new AnnotatedType() {
			@Override
			public Type getType() {
				return type;
			}

			@Nullable
			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return null;
			}

			@Override
			public Annotation[] getAnnotations() {
				return new Annotation[0];
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return new Annotation[0];
			}
		};
	}

	@Override
	public Type getType() {
		return this.type;
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
	public Object getValue(Object obj) {
		if (Types.getActualType(this.type) == obj.getClass()) {
			return obj;
		}

		throw new IllegalArgumentException(
			"RootProperty obj is not a root type. type: " + this.type + ", objType: " + obj.getClass()
		);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		RootProperty that = (RootProperty)obj;
		return Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}

	@Override
	public String toString() {
		return "RootProperty{"
			+ "type=" + type
			+ ", annotatedType=" + annotatedType + '}';
	}
}
