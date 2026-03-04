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

package com.navercorp.fixturemonkey.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

public class FixtureMonkeyTestSpecs {

	@Data
	@EqualsAndHashCode(exclude = {"strIterator", "strStream"})
	public static class ComplexObject {

		private String str;
		private int integer;
		private int[] intArray;
		private List<String> strList;
		private String[] strArray;
		private SimpleEnum enumValue;
		private SimpleObject object;
		private List<SimpleObject> list;
		private Map<String, SimpleObject> map;
		private Map.Entry<String, SimpleObject> mapEntry;
		private Iterable<String> strIterable;
		private Iterator<String> strIterator;
		private Stream<String> strStream;
		private Supplier<String> strSupplier;
		private Supplier<Supplier<String>> nestedStrSupplier;
	}

	@Data
	public static class MapEntryWrapper {

		private Map.Entry<String, Integer> simpleEntry;
		private Map.Entry<String, SimpleObject> complexEntry;
	}

	public enum SimpleEnum {
		ENUM_1,
		ENUM_2,
		ENUM_3,
		ENUM_4,
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Data
	public static class SimpleObject {

		private String str;
		private char character;
		private Character wrapperCharacter;
		private short primitiveShort;
		private Short wrapperShort;
		private byte primitiveByte;
		private Byte wrapperByte;
		private double primitiveDouble;
		private Double wrapperDouble;
		private float primitiveFloat;
		private Float wrapperFloat;
		private int integer;
		private Integer wrapperInteger;
		private long primitiveLong;
		private Long wrapperLong;
		private boolean primitiveBoolean;
		private Boolean wrapperBoolean;
		private BigInteger bigInteger;
		private BigDecimal bigDecimal;
		private Calendar calendar;
		private Date date;
		private Instant instant;
		private LocalDate localDate;
		private LocalDateTime localDateTime;
		private LocalTime localTime;
		private ZonedDateTime zonedDateTime;
		private MonthDay monthDay;
		private OffsetDateTime offsetDateTime;
		private OffsetTime offsetTime;
		private Period period;
		private Duration duration;
		private Year year;
		private YearMonth yearMonth;
		private ZoneOffset zoneOffset;
		private ZoneId zoneId;
		private Optional<String> optionalString;
		private OptionalInt optionalInt;
		private OptionalLong optionalLong;
		private OptionalDouble optionalDouble;
	}

	@Data
	public static class StringWrapper {

		String value;
	}

	@Data
	public static class IntWrapper {

		Integer value;
	}

	@Data
	public static class StringIntComposite {

		StringWrapper value1;

		IntWrapper value2;
	}

	@Data
	public static class StringPair {

		private String value1;
		private String value2;
	}

	@Data
	public static class StringWrapperPair {

		private StringWrapper value1;
		private StringWrapper value2;
	}

	@Data
	public static class StringListWrapper {

		private List<String> values;
	}

	@Data
	public static class StringWrapperList {

		private List<StringWrapper> values;
	}

	@Data
	public static class StaticFieldObject {

		public static final StaticFieldObject CONSTANT = new StaticFieldObject();
	}

	@Data
	public static class NullableObject {

		@Nullable
		List<String> values;
	}

	public interface Interface {
	}

	@Data
	public static class InterfaceImplementation implements Interface {

		private String value;
	}

	@Data
	public static class InterfaceHolder {

		Interface value;
	}

	@Data
	public static class InterfaceImplHolder {

		InterfaceImplementation value;
	}

	public enum TwoEnum {
		ONE,
		TWO,
	}

	public enum EnumObject {
		ONE,
		TWO,
		THREE,
	}

	@Data
	public static class SelfRecursiveObject {

		String value;

		SelfRecursiveObject recursive;
	}

	@Data
	public static class SelfRecursiveListObject {

		String value;

		List<SelfRecursiveObject> recursives;
	}

	@Data
	public static class SelfRecursiveSupplierObject {

		String value;

		Supplier<SelfRecursiveObject> recursive;
	}

	@Data
	public static class RecursiveLeftObject {

		String value;

		RecursiveRightObject recursive;
	}

	@Data
	public static class RecursiveRightObject {

		int value;

		RecursiveLeftObject recursive;
	}

	@Setter
	@Getter
	public static class GenericValue<T> {

		T value;
	}

	@Setter
	@Getter
	public static class GenericStringWrapper {

		GenericValue<String> value;
	}

	@Setter
	@Getter
	public static class GenericWrapper<T> {

		GenericValue<T> value;
	}

	@Setter
	@Getter
	public static class GenericStringChild extends GenericValue<String> {
	}

	@Setter
	@Getter
	public static class GenericPair<T, U> {

		T value;

		U value2;
	}

	@Setter
	@Getter
	public static class GenericStringIntChild extends GenericPair<String, Integer> {
	}

	@Setter
	@Getter
	public static class ObjectWrapper {

		Object value;
	}

	@Setter
	@Getter
	public static class Parent {

		String parentValue;
	}

	@Setter
	@Getter
	public static class Child extends Parent {

		String childValue;
	}

	@Setter
	@Getter
	public static class GenericSimpleChild extends GenericValue<SimpleObject> {

		String childValue;
	}

	@Setter
	@Getter
	public static class NestedStringListWrapper {

		List<StringListWrapper> values;
	}

	@Setter
	@Getter
	public static class DoubleNestedStringListWrapper {

		List<NestedStringListWrapper> values;
	}

	@Getter
	public static class ConstructorJavaTypeObject {

		private final String string;
		private final int primitiveInteger;
		private final float primitiveFloat;
		private final long primitiveLong;
		private final double primitiveDouble;
		private final byte primitiveByte;
		private final char primitiveCharacter;
		private final short primitiveShort;
		private final boolean primitiveBoolean;
		private final Integer wrapperInteger;

		public ConstructorJavaTypeObject(
			int primitiveInteger,
			float primitiveFloat,
			long primitiveLong,
			double primitiveDouble,
			byte primitiveByte,
			char primitiveCharacter,
			short primitiveShort,
			boolean primitiveBoolean
		) {
			this.string = "first";
			this.primitiveInteger = primitiveInteger;
			this.primitiveFloat = primitiveFloat;
			this.primitiveLong = primitiveLong;
			this.primitiveDouble = primitiveDouble;
			this.primitiveByte = primitiveByte;
			this.primitiveCharacter = primitiveCharacter;
			this.primitiveShort = primitiveShort;
			this.primitiveBoolean = primitiveBoolean;
			this.wrapperInteger = null;
		}

		public ConstructorJavaTypeObject() {
			this.string = "second";
			this.primitiveInteger = 1;
			this.primitiveFloat = -1;
			this.primitiveLong = 1;
			this.primitiveDouble = 1.0;
			this.primitiveByte = 1;
			this.primitiveCharacter = 1;
			this.primitiveShort = 2;
			this.primitiveBoolean = false;
			this.wrapperInteger = null;
		}

		public ConstructorJavaTypeObject(String str) {
			this.string = str;
			this.primitiveInteger = 1;
			this.primitiveFloat = -1;
			this.primitiveLong = 1;
			this.primitiveDouble = 1.0;
			this.primitiveByte = 1;
			this.primitiveCharacter = 1;
			this.primitiveShort = 2;
			this.primitiveBoolean = false;
			this.wrapperInteger = null;
		}

		public static ConstructorJavaTypeObject from() {
			return new ConstructorJavaTypeObject("factory");
		}

		public static ConstructorJavaTypeObject from(String str) {
			return new ConstructorJavaTypeObject("factory");
		}
	}

	@Getter
	public static class ConstructorGenericObject<T> {

		private final T value;

		public ConstructorGenericObject(T value) {
			this.value = value;
		}
	}

	@Getter
	public static class ConstructorTwoGenericObject<T, U> {

		private final T tValue;
		private final U uValue;

		public ConstructorTwoGenericObject(T tValue, U uValue) {
			this.tValue = tValue;
			this.uValue = uValue;
		}
	}

	@Getter
	public static class SimpleContainerObject {

		private final List<ConstructorJavaTypeObject> list;

		public SimpleContainerObject(List<ConstructorJavaTypeObject> list) {
			this.list = list;
		}
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Getter
	public static class ConstructorContainerObject {

		private final int[] primitiveArray;
		private final String[] array;
		private final List<String> list;
		private final List<ConstructorJavaTypeObject> complexList;
		private final java.util.Set<String> set;
		private final java.util.Set<ConstructorJavaTypeObject> complexSet;
		private final Map<String, Integer> map;
		private final Map<String, ConstructorJavaTypeObject> complexMap;
		private final Map.Entry<String, Integer> mapEntry;
		private final Map.Entry<String, ConstructorJavaTypeObject> complexMapEntry;
		private final Optional<String> optional;
		private final OptionalInt optionalInt;
		private final OptionalLong optionalLong;
		private final OptionalDouble optionalDouble;

		public ConstructorContainerObject(
			List<String> list,
			List<ConstructorJavaTypeObject> complexList,
			java.util.Set<String> set,
			java.util.Set<ConstructorJavaTypeObject> complexSet,
			Map<String, Integer> map,
			Map<String, ConstructorJavaTypeObject> complexMap,
			Map.Entry<String, Integer> mapEntry,
			Map.Entry<String, ConstructorJavaTypeObject> complexMapEntry,
			Optional<String> optional,
			OptionalInt optionalInt,
			OptionalLong optionalLong,
			OptionalDouble optionalDouble
		) {
			this.primitiveArray = new int[] {1};
			this.array = new String[] {"test"};
			this.list = list;
			this.complexList = complexList;
			this.set = set;
			this.complexSet = complexSet;
			this.map = map;
			this.complexMap = complexMap;
			this.mapEntry = mapEntry;
			this.complexMapEntry = complexMapEntry;
			this.optional = optional;
			this.optionalInt = optionalInt;
			this.optionalLong = optionalLong;
			this.optionalDouble = optionalDouble;
		}
	}

	@Data
	public static class MutableJavaTypeObject {

		private String string;
		private int primitiveInteger;
		private float primitiveFloat;
		private long primitiveLong;
		private double primitiveDouble;
		private byte primitiveByte;
		private char primitiveCharacter;
		private short primitiveShort;
		private boolean primitiveBoolean;
		private Integer wrapperInteger;
		private Float wrapperFloat;
		private Long wrapperLong;
		private Double wrapperDouble;
		private Byte wrapperByte;
		private Character wrapperCharacter;
		private Short wrapperShort;
		private Boolean wrapperBoolean;
	}

	@Getter
	public static class ConstructorOnlyObject {

		private final String value;
		private final int number;

		@java.beans.ConstructorProperties({"value", "number"})
		public ConstructorOnlyObject(String value, int number) {
			this.value = value;
			this.number = number;
		}
	}

	@Data
	public static class NestedListObject {

		private long id;
		private long stockQuantity;
		private List<Entry> entries;
		private List<Item> items;

		@Data
		public static class Entry {

			private String code;
			private long stockQuantity;
			private List<String> tags;
		}

		@Data
		public static class Item {

			private long id;
			private long stockQuantity;
		}
	}

	@Data
	public static class ThreeLevelObject {

		private String id;
		private long price;
		private String category;
		private List<ThreeLevelMid> mids;
	}

	@Data
	public static class ThreeLevelMid {

		private String code;
		private String memo;
		private long price;
		private long quantity;
		private List<ThreeLevelLeaf> leaves;
	}

	@Data
	public static class ThreeLevelLeaf {

		private String id;
		private List<String> names;
		private List<String> values;
	}

	@Data
	public static class CompositeObject {

		private NestedListObject left;
		private ThreeLevelObject right;
	}

	@Data
	public static class DeepObject {

		private String id;
		private String name;
		private BigDecimal amount;
		private String type;
		private String status;
		private Boolean flag;
		private List<Option> options;
		private List<Addon> addons;
		private Policy policy;

		@Data
		public static class Option {

			private String name;
			private List<String> values;
		}

		@Data
		public static class Addon {

			private String id;
			private String name;
			private BigDecimal price;
		}

		@Data
		public static class Policy {

			private String method;
			private String feeType;
			private String payType;
			private String groupId;
			private BigDecimal fee;
			private Threshold threshold;
			private QuantityRule quantityRule;
			private AreaRule areaRule;

			@Data
			public static class Threshold {

				private BigDecimal limit;
			}

			@Data
			public static class QuantityRule {

				private String type;
				private Long repeat;
			}

			@Data
			public static class AreaRule {

				private boolean enabled;
				private String unit;
				private BigDecimal rate1;
				private BigDecimal rate2;
			}
		}
	}

	@Data
	public static class DeepNestedListObject {

		private List<Item> items;
		private String name;

		@Data
		public static class Item {

			private String name;
			private Detail detail;
		}

		@Data
		public static class Detail {

			private String type;
			private Spec spec;
		}

		public enum Category {
			NOTHING,
			PARCEL,
			DIRECT
		}

		@Data
		public static class Spec {

			private Category category;
			private int fee;
		}
	}

	@Data
	public static class RichObject {

		private String code;
		private int count;
		private BigDecimal total;
		private DeepObject content;
		private DeepObject.Policy policy;
		private List<Discount> discounts;
		private Location location;
		private Metadata metadata;

		@Data
		public static class Discount {

			private String seq;
			private String name;
			private BigDecimal amount;
		}

		@Data
		public static class Location {

			private String name;
			private String zip;
			private String line1;
			private String line2;
		}

		@Data
		public static class Metadata {

			private String accountId;
			private String trackingCode;
			private boolean verified;
		}
	}

	@Data
	public static class Envelope {

		private Body body;

		@Data
		public static class Body {

			private String id;
			private String merchantProductId;
			private String name;
			private BigDecimal basePrice;
			private String taxType;
			private String productUrl;
			private String imageUrl;
			private String giftName;
			private Long stockQuantity;
			private String status;
			private Boolean supplementSupport;
			private Boolean optionSupport;
			private List<OptionGroup> optionGroups;
			private List<AddonInfo> addonInfos;
			private DeliveryInfo deliveryInfo;

			@Data
			public static class OptionGroup {

				private List<OptionSpec> specs;
				private List<Variant> variants;
			}

			@Data
			public static class OptionSpec {

				private String name;
				private String type;
				private List<OptionValue> values;
			}

			@Data
			public static class OptionValue {

				private String id;
				private String text;
				private Boolean active;
			}

			@Data
			public static class Variant {

				private String code;
				private BigDecimal price;
				private Long quantity;
				private Boolean active;
				private List<VariantOption> options;
			}

			@Data
			public static class VariantOption {

				private String name;
				private String id;
			}

			@Data
			public static class AddonInfo {

				private String id;
				private String name;
				private BigDecimal price;
				private Long quantity;
				private Boolean active;
			}

			@Data
			public static class DeliveryInfo {

				private String method;
				private String feeType;
				private String payType;
				private String groupId;
				private Range range;
				private Threshold threshold;
				private QuantityRule quantityRule;
				private AreaRule areaRule;

				@Data
				public static class Range {

					private String type;
					private BigDecimal from;
					private BigDecimal to;
				}

				@Data
				public static class Threshold {

					private BigDecimal limit;
				}

				@Data
				public static class QuantityRule {

					private String type;
					private Long repeat;
				}

				@Data
				public static class AreaRule {

					private boolean enabled;
					private String unit;
					private BigDecimal rate1;
					private BigDecimal rate2;
				}
			}
		}
	}

	@Data
	public static class WrapperObject {

		private Content content;
		private String metadata;

		@Data
		public static class Content {

			private String status;
			private String type;
			private String name;
			private List<ContentItem> items;
		}

		@Data
		public static class ContentItem {

			private String type;
			private String value;
		}
	}
}
