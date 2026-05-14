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

package com.navercorp.fixturemonkey.api.generator;

import static com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary.NOT_GENERATED;
import static com.navercorp.fixturemonkey.api.type.Types.isDateTimeType;
import static com.navercorp.fixturemonkey.api.type.Types.isDateType;
import static com.navercorp.fixturemonkey.api.type.Types.isDecimalType;
import static com.navercorp.fixturemonkey.api.type.Types.isIntegerType;
import static com.navercorp.fixturemonkey.api.type.Types.isTimeType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.constraint.JavaConstraintGenerator;
import com.navercorp.fixturemonkey.api.constraint.JavaContainerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDateTimeConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaDecimalConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaIntegerConstraint;
import com.navercorp.fixturemonkey.api.constraint.JavaStringConstraint;
import com.navercorp.fixturemonkey.api.container.DecomposableJavaContainer;
import com.navercorp.fixturemonkey.api.container.DecomposedContainerValueFactory;
import com.navercorp.fixturemonkey.api.exception.ContainerSizeFilterMissException;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.9", status = Status.MAINTAINED)
public final class ValidateArbitraryGenerator implements ArbitraryGenerator {
	private static final ZoneOffset ZONE_OFFSET = OffsetTime.now().getOffset();

	private final JavaConstraintGenerator constraintGenerator;
	private final DecomposedContainerValueFactory decomposedContainerValueFactory;

	public ValidateArbitraryGenerator(
		JavaConstraintGenerator constraintGenerator,
		DecomposedContainerValueFactory decomposedContainerValueFactory
	) {
		this.constraintGenerator = constraintGenerator;
		this.decomposedContainerValueFactory = decomposedContainerValueFactory;
	}

	@Override
	public CombinableArbitrary<?> generate(ArbitraryGeneratorContext context) {
		CombinableArbitrary<?> generated = context.getGenerated();

		if (generated == NOT_GENERATED) {
			return NOT_GENERATED;
		}

		Class<?> type = Types.getActualType(context.getResolvedType());
		if (type == String.class) {
			JavaStringConstraint javaStringConstraint =
				constraintGenerator.generateStringConstraint(context);

			if (javaStringConstraint != null) {
				generated = generated.filter(
					it -> {
						String string = (String)it;
						if (javaStringConstraint.isNotNull() && string == null) {
							return false;
						}

						if (javaStringConstraint.isNotBlank()) {
							if (string == null) {
								return false;
							}
							return !isBlank(string);
						}

						BigInteger minSize = javaStringConstraint.getMinSize();
						if (minSize != null) {
							if (string == null) {
								return true;
							}
							return BigInteger.valueOf(string.length()).compareTo(minSize)
								>= 0;
						}

						BigInteger maxSize = javaStringConstraint.getMaxSize();
						if (maxSize != null) {
							if (string == null) {
								return true;
							}
							return BigInteger.valueOf(string.length()).compareTo(maxSize)
								<= 0;
						}

						return true;
					}
				);
			}
		}

		if (isDecimalType(type)) {
			JavaDecimalConstraint javaDecimalConstraint = constraintGenerator.generateDecimalConstraint(context);
			if (javaDecimalConstraint != null) {
				generated = generated.filter(
					it -> {
						if (it == null) {
							return true;
						}

						BigDecimal value = toBigDecimal(it);

						BigDecimal min = javaDecimalConstraint.getMin();
						if (min != null) {
							if (value.compareTo(min) == 0
								&& Boolean.FALSE.equals(javaDecimalConstraint.getMinInclusive())) {
								return false;
							}

							return value.compareTo(min) >= 0;
						}

						BigDecimal max = javaDecimalConstraint.getMax();
						if (max != null) {
							if (value.compareTo(max) == 0
								&& Boolean.FALSE.equals(javaDecimalConstraint.getMaxInclusive())) {
								return false;
							}

							return value.compareTo(max) <= 0;
						}

						return true;
					}
				);
			}
		}

		if (isTimeType(type)) {
			JavaDateTimeConstraint javaDateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context);

			if (javaDateTimeConstraint != null) {
				generated = generated.filter(it -> {
					if (it == null) {
						return true;
					}

					OffsetTime offsetTime = toOffsetTime(it);
					LocalDateTime min = javaDateTimeConstraint.getMin();
					if (min != null) {
						if (offsetTime.isBefore(min.atOffset(ZONE_OFFSET).toOffsetTime())) {
							return false;
						}
					}

					LocalDateTime max = javaDateTimeConstraint.getMax();
					if (max != null) {
						if (offsetTime.isAfter(max.atOffset(ZONE_OFFSET).toOffsetTime())) {
							return false;
						}
					}
					return true;
				});
			}
		}

		if (isDateTimeType(type)) {
			JavaDateTimeConstraint javaDateTimeConstraint = constraintGenerator.generateDateTimeConstraint(context);
			if (javaDateTimeConstraint != null) {
				generated = generated.filter(it -> {
					if (it == null) {
						return true;
					}

					LocalDateTime localDateTime = toLocalDateTime(it);
					LocalDateTime min = javaDateTimeConstraint.getMin();
					if (min != null) {
						if (localDateTime.isBefore(min)) {
							return false;
						}
					}

					LocalDateTime max = javaDateTimeConstraint.getMax();
					if (max != null) {
						if (localDateTime.isAfter(max)) {
							return false;
						}
					}
					return true;
				});
			}
		}

		if (isDateType(type)) {
			JavaDateTimeConstraint javaDateConstraint = constraintGenerator.generateDateTimeConstraint(context);
			if (javaDateConstraint != null) {
				generated = generated.filter(it -> {
					if (it == null) {
						return true;
					}

					LocalDate localDate = toLocalDate(it);
					LocalDateTime min = javaDateConstraint.getMin();
					if (min != null) {
						if (!localDate.isAfter(min.toLocalDate())) {
							return false;
						}
					}

					LocalDateTime max = javaDateConstraint.getMax();
					if (max != null) {
						if (!localDate.isBefore(max.toLocalDate())) {
							return false;
						}
					}
					return true;
				});
			}
		}

		if (isIntegerType(type)) {
			JavaIntegerConstraint javaIntegerConstraint = constraintGenerator.generateIntegerConstraint(context);
			if (javaIntegerConstraint != null) {
				generated = generated.filter(
					it -> {
						if (it == null) {
							return true;
						}

						BigInteger value = toBigInteger(it);

						BigInteger min = javaIntegerConstraint.getMin();
						if (min != null
							&& value.compareTo(min) < 0) {
							return false;
						}

						BigInteger max = javaIntegerConstraint.getMax();
						if (max != null
							&& value.compareTo(max) > 0) {
							return false;
						}

						return true;
					}
				);
			}
		}

		if (context.getArbitraryProperty().isContainer()) {
			JavaContainerConstraint javaContainerConstraint =
				constraintGenerator.generateContainerConstraint(context);

			if (javaContainerConstraint != null) {
				generated = generated.filter(it -> {
					if (it == null) {
						return true;
					}

					if (javaContainerConstraint.isNotEmpty()) {
						DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(it);
						if (decomposableJavaContainer.getSize() == 0) {
							throw new ContainerSizeFilterMissException("Container size is should not be 0.");
						}
					}

					Integer minSize = javaContainerConstraint.getMinSize();
					if (minSize != null) {
						DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(it);
						if (decomposableJavaContainer.getSize() < minSize) {
							throw new ContainerSizeFilterMissException(
								"Container size is should not be less than " + minSize
							);
						}
					}

					Integer maxSize = javaContainerConstraint.getMaxSize();
					if (maxSize != null) {
						DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(it);
						if (decomposableJavaContainer.getSize() > maxSize) {
							throw new ContainerSizeFilterMissException(
								"Container size is should not be greater than " + maxSize
							);
						}
					}

					return true;
				});
			}
		}
		return generated;
	}

	private LocalDate toLocalDate(Object value) {
		if (value instanceof Year) {
			return ((Year)value).atMonthDay(MonthDay.of(1, 1));
		} else if (value instanceof YearMonth) {
			return ((YearMonth)value).atDay(1);
		} else if (value instanceof LocalDate) {
			return (LocalDate)value;
		} else if (value instanceof MonthDay) {
			return Year.now().atMonthDay((MonthDay)value);
		}

		throw new IllegalArgumentException("Given type is not convertible to LocalDate. " + value.getClass());
	}

	private LocalDateTime toLocalDateTime(Object value) {
		if (Calendar.class.isAssignableFrom(value.getClass())) {
			return LocalDateTime.ofInstant(((Calendar)value).toInstant(), ZoneId.systemDefault());
		} else if (Date.class.isAssignableFrom(value.getClass())) {
			return LocalDateTime.ofInstant(((Date)value).toInstant(), ZoneId.systemDefault());
		} else if (Instant.class.isAssignableFrom(value.getClass())) {
			return LocalDateTime.ofInstant((Instant)value, ZoneId.systemDefault());
		} else if (LocalDateTime.class.isAssignableFrom(value.getClass())) {
			return (LocalDateTime)value;
		} else if (ZonedDateTime.class.isAssignableFrom(value.getClass())) {
			return ((ZonedDateTime)value).toLocalDateTime();
		} else if (OffsetDateTime.class.isAssignableFrom(value.getClass())) {
			return ((OffsetDateTime)value).toLocalDateTime();
		}

		throw new IllegalArgumentException("Given type is not convertible to LocalDateTime. " + value.getClass());
	}

	private OffsetTime toOffsetTime(Object value) {
		if (value instanceof LocalTime) {
			return OffsetTime.of((LocalTime)value, ZONE_OFFSET);
		} else {
			if (value instanceof OffsetTime) {
				return (OffsetTime)value;
			}
		}
		throw new IllegalArgumentException("Given type is not convertible to OffsetTime. " + value.getClass());
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof Float || value instanceof Double) {
			return new BigDecimal(value.toString());
		} else {
			if (value instanceof BigDecimal) {
				return (BigDecimal)value;
			}
		}
		throw new IllegalArgumentException("Given type is not convertible to BigDecimal. " + value.getClass());
	}

	private BigInteger toBigInteger(Object value) {
		if (value instanceof Long || value instanceof Short || value instanceof Byte || value instanceof Integer) {
			return new BigInteger(value.toString());
		} else {
			if (value instanceof BigInteger) {
				return (BigInteger)value;
			}
		}
		throw new IllegalArgumentException("Given type is not convertible to BigInteger. " + value.getClass());
	}

	private boolean isBlank(String value) {
		char[] charArray = value.toCharArray();
		int length = 0;
		for (char c : charArray) {
			if (Character.isWhitespace(c)) {
				length++;
			}
		}
		return value.length() == length;
	}
}
