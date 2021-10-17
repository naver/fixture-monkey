package com.navercorp.fixturemonkey.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.List;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.PropertyNameResolver;

class DefaultPropertyNameResolverTest {
	@Test
	void resolve() {
		PropertyNameResolver sut = new DefaultPropertyNameResolver();
		then(sut.resolve(getNameProperty("name"))).isEqualTo("name");
	}

	private Property getNameProperty(String name) {
		return new Property() {
			@Override
			public AnnotatedType getAnnotatedType() {
				return null;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public List<Annotation> getAnnotations() {
				return null;
			}

			@Nullable
			@Override
			public Object getValue(Object obj) {
				return null;
			}
		};
	}
}
