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

package com.navercorp.fixturemonkey.api.property;

import static org.assertj.core.api.BDDAssertions.then;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.type.TypeReference;

class PropertyCacheTest {
	@Test
	void getRootProperty() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		// when
		RootProperty actual = PropertyCache.getRootProperty(typeReference.getAnnotatedType());

		then(actual.getType()).isEqualTo(typeReference.getType());
	}

	@Test
	void getProperties() {
		// given
		TypeReference<PropertyValue> typeReference = new TypeReference<PropertyValue>() {
		};

		// when
		List<Property> actual = PropertyCache.getProperties(typeReference.getAnnotatedType());

		then(actual).hasSize(1);
		then(actual.get(0)).isExactlyInstanceOf(CompositeProperty.class);
		then(actual.get(0).getType()).isEqualTo(String.class);
		then(actual.get(0).getName()).isEqualTo("name");
	}

	@Test
	void getPropertiesGenerics() {
		// given
		TypeReference<GenericSample<String>> typeReference = new TypeReference<GenericSample<String>>() {
		};

		// when
		List<Property> actual = PropertyCache.getProperties(typeReference.getAnnotatedType());

		then(actual).hasSize(5);

		List<Property> sorted = new ArrayList<>(actual);
		sorted.sort(Comparator.comparing(Property::getName));

		then(sorted.get(0).getName()).isEqualTo("list");
		then(sorted.get(0).getType()).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)sorted.get(0).getType()).getRawType()).isEqualTo(List.class);
		then(((ParameterizedType)sorted.get(0).getType()).getActualTypeArguments()).hasSize(1);
		then(((ParameterizedType)sorted.get(0).getType()).getActualTypeArguments()[0]).isEqualTo(String.class);

		then(sorted.get(1).getName()).isEqualTo("name");
		then(sorted.get(1).getType()).isEqualTo(String.class);

		then(sorted.get(2).getName()).isEqualTo("sample2");
		then(sorted.get(2).getType()).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)sorted.get(2).getType()).getRawType()).isEqualTo(GenericSample2.class);
		then(((ParameterizedType)sorted.get(2).getType()).getActualTypeArguments()).hasSize(1);
		then(((ParameterizedType)sorted.get(2).getType()).getActualTypeArguments()[0]).isEqualTo(String.class);

		then(sorted.get(3).getName()).isEqualTo("samples");
		then(sorted.get(3).getType()).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)sorted.get(3).getType()).getRawType()).isEqualTo(List.class);
		then(((ParameterizedType)sorted.get(3).getType()).getActualTypeArguments()).hasSize(1);
		then(((ParameterizedType)sorted.get(3).getType()).getActualTypeArguments()[0]).isEqualTo(PropertyValue.class);

		then(sorted.get(4).getName()).isEqualTo("test");
		then(sorted.get(4).getType()).isEqualTo(PropertyValue.class);
	}

	@Test
	void getPropertiesGenericsBiGenerics() {
		// given
		TypeReference<BiGenericSample<Integer, String>> typeReference =
			new TypeReference<BiGenericSample<Integer, String>>() {
			};

		// when
		List<Property> actual = PropertyCache.getProperties(typeReference.getAnnotatedType());

		then(actual).hasSize(4);

		List<Property> sorted = new ArrayList<>(actual);
		sorted.sort(Comparator.comparing(Property::getName));

		then(sorted.get(0).getName()).isEqualTo("address");
		then(sorted.get(0).getType()).isEqualTo(String.class);

		then(sorted.get(1).getName()).isEqualTo("name");
		then(sorted.get(1).getType()).isEqualTo(Integer.class);

		then(sorted.get(2).getName()).isEqualTo("sample2");
		then(sorted.get(2).getType()).isInstanceOf(ParameterizedType.class);
		then(((ParameterizedType)sorted.get(2).getType()).getRawType()).isEqualTo(BiGenericSample2.class);
		then(((ParameterizedType)sorted.get(2).getType()).getActualTypeArguments()).hasSize(2);
		then(((ParameterizedType)sorted.get(2).getType()).getActualTypeArguments()[0]).isEqualTo(Integer.class);
		then(((ParameterizedType)sorted.get(2).getType()).getActualTypeArguments()[1]).isEqualTo(String.class);

		then(sorted.get(3).getName()).isEqualTo("test");
		then(sorted.get(3).getType()).isEqualTo(PropertyValue.class);
	}

	@Test
	void getProperty() {
		TypeReference<PropertyValue> typeReference = new TypeReference<PropertyValue>() {
		};
		Optional<Property> actual = PropertyCache.getProperty(typeReference.getAnnotatedType(), "name");
		then(actual).isPresent();
		then(actual.get()).isExactlyInstanceOf(CompositeProperty.class);

		CompositeProperty compositeProperty = (CompositeProperty)actual.get();
		then(compositeProperty.getPrimaryProperty()).isExactlyInstanceOf(PropertyDescriptorProperty.class);
		then(compositeProperty.getSecondaryProperty()).isExactlyInstanceOf(FieldProperty.class);
	}

	@Test
	void getPropertyEmpty() {
		TypeReference<PropertyValue> typeReference = new TypeReference<PropertyValue>() {
		};
		Optional<Property> actual = PropertyCache.getProperty(typeReference.getAnnotatedType(), "test");
		then(actual).isNotPresent();
	}

	@Test
	void getFields() {
		Map<String, Field> actual = PropertyCache.getFields(PropertyValue.class);
		then(actual).hasSize(1);
		then(actual.get("name")).isNotNull();
		then(actual.get("name").getName()).isEqualTo("name");
	}

	@Test
	void getPropertyDescriptors() throws NoSuchMethodException {
		Map<Method, PropertyDescriptor> actual = PropertyCache.getPropertyDescriptors(PropertyValue.class);
		then(actual).hasSize(1);

		Method method = PropertyValue.class.getDeclaredMethod("getName");
		then(actual.get(method)).isNotNull();
		then(actual.get(method).getName()).isEqualTo("name");
	}

	static class GenericSample<T> {
		private GenericSample2<T> sample2;
		private T name;
		private PropertyValue test;
		private List<T> list;
		private List<PropertyValue> samples;

		public GenericSample2<T> getSample2() {
			return this.sample2;
		}

		public T getName() {
			return this.name;
		}

		public PropertyValue getTest() {
			return this.test;
		}

		public List<T> getList() {
			return this.list;
		}

		public List<PropertyValue> getSamples() {
			return this.samples;
		}
	}

	static class GenericSample2<T> {
		private T name;

		public T getName() {
			return this.name;
		}
	}

	static class BiGenericSample<T, R> {
		private BiGenericSample2<T, R> sample2;
		private T name;
		private R address;
		private PropertyValue test;

		public BiGenericSample2<T, R> getSample2() {
			return this.sample2;
		}

		public T getName() {
			return this.name;
		}

		public R getAddress() {
			return this.address;
		}

		public PropertyValue getTest() {
			return this.test;
		}
	}

	static class BiGenericSample2<T, R> {
		private T name;
		private R address;

		public T getName() {
			return this.name;
		}

		public R getAddress() {
			return this.address;
		}
	}
}
