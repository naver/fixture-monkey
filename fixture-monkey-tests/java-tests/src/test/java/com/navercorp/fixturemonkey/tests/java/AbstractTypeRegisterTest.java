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

package com.navercorp.fixturemonkey.tests.java;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;

class AbstractTypeRegisterTest {
	private static final String PINNED = "PINNED";
	private static final int SAMPLE_COUNT = 50;

	@Test
	void registeredBuilderBeneathInterfacePropertyIsUsed() {
		FixtureMonkey sut = builder()
			.register(Label.class, it -> it.giveMeBuilder(pinnedLabel()))
			.build();

		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(InterfaceHolder.class).getValue().getLabel().getText())
			.collect(Collectors.toSet());

		then(actual).containsExactly(PINNED);
	}

	@Test
	void registeredBuilderBeneathAbstractClassPropertyIsUsed() {
		FixtureMonkey sut = builder()
			.register(Label.class, it -> it.giveMeBuilder(pinnedLabel()))
			.build();

		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(AbstractClassHolder.class).getValue().getLabel().getText())
			.collect(Collectors.toSet());

		then(actual).containsExactly(PINNED);
	}

	@Test
	void registeredBuilderBeneathInterfaceContainerElementIsUsed() {
		FixtureMonkey sut = builder()
			.register(Label.class, it -> it.giveMeBuilder(pinnedLabel()))
			.build();

		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(InterfaceListHolder.class))
			.flatMap(it -> it.getValues().stream())
			.map(it -> it.getLabel().getText())
			.collect(Collectors.toSet());

		then(actual).containsExactly(PINNED);
	}

	@Test
	void registeredBuilderForInterfaceTypeIsUsedForPropertyOfThatType() {
		FirstValue pinned = new FirstValue();
		pinned.setLabel(pinnedLabel());

		FixtureMonkey sut = builder()
			.register(Value.class, it -> it.giveMeBuilder(pinned))
			.build();

		Set<Value> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(InterfaceHolder.class).getValue())
			.collect(Collectors.toSet());

		then(actual).containsExactly(pinned);
	}

	@Test
	void registeredBuilderForImplementationTypeBeneathInterfacePropertyIsUsed() {
		FirstValue pinned = new FirstValue();
		pinned.setLabel(pinnedLabel());

		FixtureMonkey sut = builder()
			.register(FirstValue.class, it -> it.giveMeBuilder(pinned))
			.build();

		Set<Value> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(InterfaceHolder.class).getValue())
			.filter(FirstValue.class::isInstance)
			.collect(Collectors.toSet());

		then(actual).containsExactly(pinned);
	}

	@Test
	void interfacePropertyVariesItsImplementationAcrossSamples() {
		FixtureMonkey sut = builder().build();

		Set<Class<?>> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(InterfaceHolder.class).getValue().getClass())
			.collect(Collectors.toSet());

		then(actual).hasSize(2);
	}

	@Test
	void registeredBuilderIsUsedForConcretePropertyType() {
		FixtureMonkey sut = builder()
			.register(Label.class, it -> it.giveMeBuilder(pinnedLabel()))
			.build();

		Set<String> actual = IntStream.range(0, SAMPLE_COUNT)
			.mapToObj(it -> sut.giveMeOne(ConcreteHolder.class).getValue().getLabel().getText())
			.collect(Collectors.toSet());

		then(actual).containsExactly(PINNED);
	}

	private FixtureMonkeyBuilder builder() {
		return FixtureMonkey.builder()
			.defaultNotNull(true)
			.plugin(new InterfacePlugin()
				.interfaceImplements(Value.class, Arrays.asList(FirstValue.class, SecondValue.class))
				.abstractClassExtends(AbstractValue.class,
					Arrays.asList(FirstAbstractValue.class, SecondAbstractValue.class)));
	}

	private Label pinnedLabel() {
		Label label = new Label();
		label.setText(PINNED);
		return label;
	}

	public static class Label {
		private String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return java.util.Objects.equals(text, ((Label)obj).text);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(text);
		}
	}

	public interface Value {
		Label getLabel();
	}

	public static class FirstValue implements Value {
		private Label label;

		@Override
		public Label getLabel() {
			return label;
		}

		public void setLabel(Label label) {
			this.label = label;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return java.util.Objects.equals(label, ((FirstValue)obj).label);
		}

		@Override
		public int hashCode() {
			return java.util.Objects.hash(label);
		}
	}

	public static class SecondValue implements Value {
		private Label label;

		@Override
		public Label getLabel() {
			return label;
		}

		public void setLabel(Label label) {
			this.label = label;
		}
	}

	public abstract static class AbstractValue {
		public abstract Label getLabel();
	}

	public static class FirstAbstractValue extends AbstractValue {
		private Label label;

		@Override
		public Label getLabel() {
			return label;
		}

		public void setLabel(Label label) {
			this.label = label;
		}
	}

	public static class SecondAbstractValue extends AbstractValue {
		private Label label;

		@Override
		public Label getLabel() {
			return label;
		}

		public void setLabel(Label label) {
			this.label = label;
		}
	}

	public static class InterfaceHolder {
		private Value value;

		public Value getValue() {
			return value;
		}

		public void setValue(Value value) {
			this.value = value;
		}
	}

	public static class AbstractClassHolder {
		private AbstractValue value;

		public AbstractValue getValue() {
			return value;
		}

		public void setValue(AbstractValue value) {
			this.value = value;
		}
	}

	public static class InterfaceListHolder {
		private List<Value> values;

		public List<Value> getValues() {
			return values;
		}

		public void setValues(List<Value> values) {
			this.values = values;
		}
	}

	public static class ConcreteHolder {
		private FirstValue value;

		public FirstValue getValue() {
			return value;
		}

		public void setValue(FirstValue value) {
			this.value = value;
		}
	}
}
