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

package com.navercorp.fixturemonkey.adapter;

import java.beans.ConstructorProperties;
import java.lang.reflect.AnnotatedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGenerator;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.matcher.AssignableTypeMatcher;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.ElementProperty;
import com.navercorp.fixturemonkey.api.type.Types;

public class ValueProjectionAssembleSpecs {

	public static class StringValue {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class Product {

		private String name;
		private int price;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPrice() {
			return price;
		}

		public void setPrice(int price) {
			this.price = price;
		}
	}

	public static class Order {

		private String orderId;
		private Product product;
		private int quantity;

		public String getOrderId() {
			return orderId;
		}

		public void setOrderId(String orderId) {
			this.orderId = orderId;
		}

		public Product getProduct() {
			return product;
		}

		public void setProduct(Product product) {
			this.product = product;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}
	}

	public static class StringListHolder {

		private List<String> values;

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public static class NestedListHolder {

		private List<List<String>> nestedValues;

		public List<List<String>> getNestedValues() {
			return nestedValues;
		}

		public void setNestedValues(List<List<String>> nestedValues) {
			this.nestedValues = nestedValues;
		}
	}

	public static class NestedStringListHolder {

		private List<StringValue> values;

		public List<StringValue> getValues() {
			return values;
		}

		public void setValues(List<StringValue> values) {
			this.values = values;
		}
	}

	public static class InterfaceHolder {

		private List<String> items; // Interface type field

		public List<String> getItems() {
			return items;
		}

		public void setItems(List<String> items) {
			this.items = items;
		}
	}

	public static class GenericWrapper<T> {

		private T value;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}

	public static class MapHolder {

		private java.util.Map<String, Integer> mapping;

		public java.util.Map<String, Integer> getMapping() {
			return mapping;
		}

		public void setMapping(java.util.Map<String, Integer> mapping) {
			this.mapping = mapping;
		}
	}

	public static class SimpleObject {

		private String str;
		private Integer integer;
		private Instant instant;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public Integer getInteger() {
			return integer;
		}

		public void setInteger(Integer integer) {
			this.integer = integer;
		}

		public Instant getInstant() {
			return instant;
		}

		public void setInstant(Instant instant) {
			this.instant = instant;
		}
	}

	public static class InstantObject {

		private Instant instant;

		public Instant getInstant() {
			return instant;
		}

		public void setInstant(Instant instant) {
			this.instant = instant;
		}
	}

	public static class NestedStringList {

		private List<StringValue> values;

		public List<StringValue> getValues() {
			return values;
		}

		public void setValues(List<StringValue> values) {
			this.values = values;
		}
	}

	public static class SimpleObjectChild extends SimpleObject {

		private String childStr;

		public String getChildStr() {
			return childStr;
		}

		public void setChildStr(String childStr) {
			this.childStr = childStr;
		}
	}

	interface Interface {
		String getValue();
	}

	public static class InterfaceImplementation implements Interface {

		private String value;

		@Override
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			InterfaceImplementation that = (InterfaceImplementation)obj;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class InterfaceFieldImplementationValue {

		private InterfaceImplementation value;

		public InterfaceImplementation getValue() {
			return value;
		}

		public void setValue(InterfaceImplementation value) {
			this.value = value;
		}
	}

	public static class SimpleStringObject {

		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			SimpleStringObject that = (SimpleStringObject)obj;
			return Objects.equals(str, that.str);
		}

		@Override
		public int hashCode() {
			return Objects.hash(str);
		}
	}

	public static class NestedSimpleObject {

		private SimpleStringObject object;

		public SimpleStringObject getObject() {
			return object;
		}

		public void setObject(SimpleStringObject object) {
			this.object = object;
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static class OptionalStringObject {

		private Optional<String> optionalString;

		public Optional<String> getOptionalString() {
			return optionalString;
		}

		public void setOptionalString(Optional<String> optionalString) {
			this.optionalString = optionalString;
		}
	}

	public static class StringMapHolder {

		private Map<String, String> mapping;

		public Map<String, String> getMapping() {
			return mapping;
		}

		public void setMapping(Map<String, String> mapping) {
			this.mapping = mapping;
		}
	}

	public static class StringPair {

		private String value1;
		private String value2;

		public String getValue1() {
			return value1;
		}

		public void setValue1(String value1) {
			this.value1 = value1;
		}

		public String getValue2() {
			return value2;
		}

		public void setValue2(String value2) {
			this.value2 = value2;
		}
	}

	public static class IntValue {

		private Integer value;

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}
	}

	public static class StringAndInt {

		private StringValue value1;
		private IntValue value2;

		public StringValue getValue1() {
			return value1;
		}

		public void setValue1(StringValue value1) {
			this.value1 = value1;
		}

		public IntValue getValue2() {
			return value2;
		}

		public void setValue2(IntValue value2) {
			this.value2 = value2;
		}
	}

	public static class ListStringObject {

		private List<String> values;

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public static class StringArrayHolder {

		private String[] values;

		public String[] getValues() {
			return values;
		}

		public void setValues(String[] values) {
			this.values = values;
		}
	}

	public static class IntArrayHolder {

		private int[] values;

		public int[] getValues() {
			return values;
		}

		public void setValues(int[] values) {
			this.values = values;
		}
	}

	public static class SimpleListObject {

		private String str;
		private List<String> strList;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public List<String> getStrList() {
			return strList;
		}

		public void setStrList(List<String> strList) {
			this.strList = strList;
		}
	}

	public static class SimpleIntObject {

		private String str;
		private int integer;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}

		public int getInteger() {
			return integer;
		}

		public void setInteger(int integer) {
			this.integer = integer;
		}
	}

	public static class StaticFieldObject {

		public static final StaticFieldObject CONSTANT = new StaticFieldObject();
	}

	public static class GenericValue<T> {

		private T value;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}

	public static class GenericStringWrapperValue {

		private GenericValue<String> value;

		public GenericValue<String> getValue() {
			return value;
		}

		public void setValue(GenericValue<String> value) {
			this.value = value;
		}
	}

	public static class GenericWrapperValue<T> {

		private GenericValue<T> value;

		public GenericValue<T> getValue() {
			return value;
		}

		public void setValue(GenericValue<T> value) {
			this.value = value;
		}
	}

	public static class GenericChildValue extends GenericValue<String> {
	}

	public static class GenericTwoValue<T, U> {

		private T value;
		private U value2;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public U getValue2() {
			return value2;
		}

		public void setValue2(U value2) {
			this.value2 = value2;
		}
	}

	public static class GenericChildTwoValue extends GenericTwoValue<String, Integer> {
	}

	public static class ObjectValue {

		private Object value;

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	public static class ParentValue {

		private String parentValue;

		public String getParentValue() {
			return parentValue;
		}

		public void setParentValue(String parentValue) {
			this.parentValue = parentValue;
		}
	}

	public static class ChildValue extends ParentValue {

		private String childValue;

		public String getChildValue() {
			return childValue;
		}

		public void setChildValue(String childValue) {
			this.childValue = childValue;
		}
	}

	public static class NullableObject {

		@Nullable
		private List<String> values;

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public static class ComplexObject {

		private List<String> strList;
		private List<SimpleObject> list;
		private Map<String, SimpleObject> map;

		public List<String> getStrList() {
			return strList;
		}

		public void setStrList(List<String> strList) {
			this.strList = strList;
		}

		public List<SimpleObject> getList() {
			return list;
		}

		public void setList(List<SimpleObject> list) {
			this.list = list;
		}

		public Map<String, SimpleObject> getMap() {
			return map;
		}

		public void setMap(Map<String, SimpleObject> map) {
			this.map = map;
		}
	}

	enum TwoEnum {
		ONE,
		TWO,
	}

	public static class UniqueArbitraryGenerator implements ArbitraryGenerator {

		private static final Set<Object> UNIQUE = new HashSet<>();

		private final ArbitraryGenerator delegate;

		public UniqueArbitraryGenerator(ArbitraryGenerator delegate) {
			this.delegate = delegate;
		}

		@Override
		public CombinableArbitrary generate(ArbitraryGeneratorContext context) {
			return delegate
				.generate(context)
				.filter(obj -> {
					if (!UNIQUE.contains(obj)) {
						UNIQUE.add(obj);
						return true;
					}
					return false;
				});
		}
	}

	@lombok.Getter
	@lombok.Builder
	public static class BuilderInteger {

		private int value;
	}

	@lombok.Getter
	@lombok.Builder
	public static class CustomBuilderMethodInteger {

		private int value;

		public static CustomBuilderMethodIntegerBuilder customBuilder() {
			return new CustomBuilderMethodIntegerBuilder();
		}
	}

	@lombok.Getter
	@lombok.Builder
	public static class CustomBuildMethodInteger {

		private int value;

		public static class CustomBuildMethodIntegerBuilder {

			public CustomBuildMethodInteger customBuild() {
				return new CustomBuildMethodInteger(value);
			}
		}
	}

	public static class ConstructorOnlyInteger {

		private final int value;

		@java.beans.ConstructorProperties({"value"})
		public ConstructorOnlyInteger(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public static class FactoryMethodInteger {

		private final int value;

		private FactoryMethodInteger(int value) {
			this.value = value;
		}

		public static FactoryMethodInteger of(int value) {
			return new FactoryMethodInteger(value);
		}

		public int getValue() {
			return value;
		}
	}

	public interface GetFixedValue {
		Object get();
	}

	public interface GetFixedValueChild extends GetFixedValue {
	}

	public interface GetterInterface {
		String getValue();
	}

	public static class GetIntegerFixedValue implements GetFixedValue {

		@Override
		public Object get() {
			return 1;
		}
	}

	public static class GetStringFixedValue implements GetFixedValue {

		@Override
		public Object get() {
			return "fixed";
		}
	}

	public static class GetIntegerFixedValueChild implements GetFixedValueChild {

		@Override
		public Object get() {
			return 2;
		}
	}

	public static class GenericGetFixedValue<T extends GetFixedValue> {

		private T value;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}

	public abstract static class AbstractSamePropertyValue {

		private String value;

		public AbstractSamePropertyValue(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class ConcreteSamePropertyValue extends AbstractSamePropertyValue {

		private int intValue;

		@java.beans.ConstructorProperties({"value", "intValue"})
		public ConcreteSamePropertyValue(String value, int intValue) {
			super(value);
			this.intValue = intValue;
		}

		public int getIntValue() {
			return intValue;
		}

		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}
	}

	public abstract static class AbstractNoneValue {

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			return Objects.hash(getClass());
		}
	}

	public static class AbstractNoneConcreteIntValue extends AbstractNoneValue {

		private int intValue;

		public int getIntValue() {
			return intValue;
		}

		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			AbstractNoneConcreteIntValue that = (AbstractNoneConcreteIntValue)obj;
			return intValue == that.intValue;
		}

		@Override
		public int hashCode() {
			return Objects.hash(intValue);
		}
	}

	public static class AbstractNoneConcreteStringValue extends AbstractNoneValue {

		private String stringValue;

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			AbstractNoneConcreteStringValue that = (AbstractNoneConcreteStringValue)obj;
			return Objects.equals(stringValue, that.stringValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(stringValue);
		}
	}

	public abstract static class AbstractValue {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			AbstractValue that = (AbstractValue)obj;
			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class ConcreteStringValue extends AbstractValue {

		private String stringValue;

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			ConcreteStringValue that = (ConcreteStringValue)obj;
			return Objects.equals(stringValue, that.stringValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), stringValue);
		}
	}

	public static class ConcreteIntValue extends AbstractValue {

		private int intValue;

		public int getIntValue() {
			return intValue;
		}

		public void setIntValue(int intValue) {
			this.intValue = intValue;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!super.equals(obj)) {
				return false;
			}
			ConcreteIntValue that = (ConcreteIntValue)obj;
			return intValue == that.intValue;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), intValue);
		}
	}

	public abstract static class SelfRecursiveAbstractValue {

		private final List<SelfRecursiveAbstractValue> recursives;

		@java.beans.ConstructorProperties("recursives")
		public SelfRecursiveAbstractValue(List<SelfRecursiveAbstractValue> recursives) {
			this.recursives = recursives;
		}

		public List<SelfRecursiveAbstractValue> getRecursives() {
			return recursives;
		}
	}

	public static class SelfRecursiveImplementationValue extends SelfRecursiveAbstractValue {

		private final String value;

		@java.beans.ConstructorProperties({"recursives", "value"})
		public SelfRecursiveImplementationValue(List<SelfRecursiveAbstractValue> recursives, String value) {
			super(recursives);
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static class GetterInterfaceImplementation implements GetterInterface {

		private String value;

		@Override
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class GetterInterfaceImplementation2 implements GetterInterface {

		private String value;

		@Override
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	interface PairInterface<S, T> {
		S getFirst();

		T getSecond();
	}

	public static class Pair<S, T> implements PairInterface<S, T> {

		private final S first;
		private final T second;

		public Pair(S first, T second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public S getFirst() {
			return first;
		}

		@Override
		public T getSecond() {
			return second;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Pair<?, ?> pair = (Pair<?, ?>)obj;
			return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
		}

		@Override
		public int hashCode() {
			return Objects.hash(first, second);
		}
	}

	public static class PairContainerPropertyGenerator implements ContainerPropertyGenerator {

		@Override
		public ContainerProperty generate(ContainerPropertyGeneratorContext context) {
			com.navercorp.fixturemonkey.api.property.Property property = context.getProperty();

			List<AnnotatedType> elementTypes = Types.getGenericsTypes(property.getAnnotatedType());
			if (elementTypes.size() != 2) {
				throw new IllegalArgumentException(
					"Pair elementsTypes must be have 1 generics type for element. "
						+ "propertyType: "
						+ property.getType()
						+ ", elementTypes: "
						+ elementTypes
				);
			}

			AnnotatedType firstElementType = elementTypes.get(0);
			AnnotatedType secondElementType = elementTypes.get(1);
			List<com.navercorp.fixturemonkey.api.property.Property> elementProperties = new ArrayList<>();
			elementProperties.add(new ElementProperty(property, firstElementType, 0, 0));
			elementProperties.add(new ElementProperty(property, secondElementType, 1, 1));

			return new ContainerProperty(elementProperties, new ArbitraryContainerInfo(1, 1));
		}
	}

	public static class PairIntrospector implements ArbitraryIntrospector, Matcher {

		private static final Matcher MATCHER = new AssignableTypeMatcher(Pair.class);

		@Override
		public boolean match(com.navercorp.fixturemonkey.api.property.Property property) {
			return MATCHER.match(property);
		}

		@SuppressWarnings("ConstantConditions")
		@Override
		public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
			ArbitraryProperty property = context.getArbitraryProperty();
			if (!property.isContainer()) {
				return ArbitraryIntrospectorResult.NOT_INTROSPECTED;
			}

			List<CombinableArbitrary<?>> elementCombinableArbitraryList = context.getElementCombinableArbitraryList();

			return new ArbitraryIntrospectorResult(
				CombinableArbitrary.containerBuilder()
					.elements(elementCombinableArbitraryList)
					.build(elements -> new Pair<>(elements.get(0), elements.get(1)))
			);
		}
	}

	public interface GenericHolder<T> {
		T getValue();
	}

	public static class StringGenericHolder implements GenericHolder<String> {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class IntegerGenericHolder implements GenericHolder<Integer> {

		private Integer value;

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}
	}

	public static class GenericHolderContainer {

		private GenericHolder<String> stringHolder;
		private GenericHolder<Integer> integerHolder;

		public GenericHolder<String> getStringHolder() {
			return stringHolder;
		}

		public void setStringHolder(GenericHolder<String> stringHolder) {
			this.stringHolder = stringHolder;
		}

		public GenericHolder<Integer> getIntegerHolder() {
			return integerHolder;
		}

		public void setIntegerHolder(GenericHolder<Integer> integerHolder) {
			this.integerHolder = integerHolder;
		}
	}

	public abstract static class AbstractWithNestedInterface {

		private Interface nestedInterface;

		public Interface getNestedInterface() {
			return nestedInterface;
		}

		public void setNestedInterface(Interface nestedInterface) {
			this.nestedInterface = nestedInterface;
		}
	}

	public static class ConcreteWithNestedInterface extends AbstractWithNestedInterface {

		private String ownValue;

		public String getOwnValue() {
			return ownValue;
		}

		public void setOwnValue(String ownValue) {
			this.ownValue = ownValue;
		}
	}

	public abstract static class SelfRecursiveWithFieldAbstractValue {

		private final String name;
		private final SelfRecursiveWithFieldAbstractValue child;

		@java.beans.ConstructorProperties({"name", "child"})
		public SelfRecursiveWithFieldAbstractValue(String name, SelfRecursiveWithFieldAbstractValue child) {
			this.name = name;
			this.child = child;
		}

		public String getName() {
			return name;
		}

		public SelfRecursiveWithFieldAbstractValue getChild() {
			return child;
		}
	}

	public static class SelfRecursiveWithFieldImplementation extends SelfRecursiveWithFieldAbstractValue {

		private final int extra;

		@java.beans.ConstructorProperties({"name", "child", "extra"})
		public SelfRecursiveWithFieldImplementation(String name, SelfRecursiveWithFieldAbstractValue child, int extra) {
			super(name, child);
			this.extra = extra;
		}

		public int getExtra() {
			return extra;
		}
	}

	public abstract static class AbstractGenericBox<T> {

		private T content;
		private List<T> items;

		public T getContent() {
			return content;
		}

		public void setContent(T content) {
			this.content = content;
		}

		public List<T> getItems() {
			return items;
		}

		public void setItems(List<T> items) {
			this.items = items;
		}
	}

	public static class ConcreteStringBox extends AbstractGenericBox<String> {

		private String label;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

	public interface ValueProvider {
		Object getValue();
	}

	public static class StringValueProvider implements ValueProvider {

		private String value;

		@Override
		public Object getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public static class ValueProviderContainer {

		private ValueProvider provider;

		public ValueProvider getProvider() {
			return provider;
		}

		public void setProvider(ValueProvider provider) {
			this.provider = provider;
		}
	}

	// Minimal reproduction for DirectOrderPayMethod-like structure:
	// A constructor-based class whose fields should be populated but might be null
	public static class PayMethodType {

		private final String payMethodType;

		@ConstructorProperties({"payMethodType"})
		public PayMethodType(String payMethodType) {
			this.payMethodType = payMethodType;
		}

		public String getPayMethodType() {
			return payMethodType;
		}
	}

	public static class SimplePayMethod {

		private final PayMethodType firstPayMethod;
		private final PayMethodType secondPayMethod;

		@ConstructorProperties({"firstPayMethod", "secondPayMethod"})
		public SimplePayMethod(PayMethodType firstPayMethod, PayMethodType secondPayMethod) {
			this.firstPayMethod = firstPayMethod;
			this.secondPayMethod = secondPayMethod;
		}

		public PayMethodType getFirstPayMethod() {
			return firstPayMethod;
		}

		public PayMethodType getSecondPayMethod() {
			return secondPayMethod;
		}

		public boolean isFirstPayMethodChargePoint() {
			return firstPayMethod.getPayMethodType().equals("CHARGE_POINT");
		}
	}

	public static class PreApproval {

		@Nullable
		private final SimplePayMethod payMethod;
		private final String description;

		@ConstructorProperties({"payMethod", "description"})
		public PreApproval(@Nullable SimplePayMethod payMethod, String description) {
			this.payMethod = payMethod;
			this.description = description;
		}

		@Nullable
		public SimplePayMethod getPayMethod() {
			return payMethod;
		}

		public String getDescription() {
			return description;
		}
	}

	public static class OrderContainer {

		private PreApproval preApproval;

		public PreApproval getPreApproval() {
			return preApproval;
		}

		public void setPreApproval(PreApproval preApproval) {
			this.preApproval = preApproval;
		}
	}

	// Case 1: No @ConstructorProperties, no -parameters flag → param names lost
	public static class NoParamNamesPayMethod {

		private final String firstMethod;
		private final String secondMethod;

		public NoParamNamesPayMethod(String firstMethod, String secondMethod) {
			this.firstMethod = firstMethod;
			this.secondMethod = secondMethod;
		}

		public String getFirstMethod() {
			return firstMethod;
		}

		public String getSecondMethod() {
			return secondMethod;
		}
	}

	// Case 2: Only default (no-arg) constructor, fields are final
	public static class DefaultCtorFinalFields {

		private final String value;

		public DefaultCtorFinalFields() {
			this.value = "default";
		}

		public String getValue() {
			return value;
		}
	}

	// Case 3: Multiple constructors — one no-arg, one with args
	public static class MultipleCtorsPayMethod {

		private final String firstMethod;
		private final String secondMethod;

		public MultipleCtorsPayMethod() {
			this.firstMethod = null;
			this.secondMethod = null;
		}

		@ConstructorProperties({"firstMethod", "secondMethod"})
		public MultipleCtorsPayMethod(String firstMethod, String secondMethod) {
			this.firstMethod = firstMethod;
			this.secondMethod = secondMethod;
		}

		public String getFirstMethod() {
			return firstMethod;
		}

		public String getSecondMethod() {
			return secondMethod;
		}
	}

	// Case 4: Wrapper that holds a constructor-based type as field
	public static class WrapperWithCtorChild {

		private NoParamNamesPayMethod payMethod;

		public NoParamNamesPayMethod getPayMethod() {
			return payMethod;
		}

		public void setPayMethod(NoParamNamesPayMethod payMethod) {
			this.payMethod = payMethod;
		}
	}

	public static class WrapperWithMultiCtorChild {

		private MultipleCtorsPayMethod payMethod;

		public MultipleCtorsPayMethod getPayMethod() {
			return payMethod;
		}

		public void setPayMethod(MultipleCtorsPayMethod payMethod) {
			this.payMethod = payMethod;
		}
	}
}
