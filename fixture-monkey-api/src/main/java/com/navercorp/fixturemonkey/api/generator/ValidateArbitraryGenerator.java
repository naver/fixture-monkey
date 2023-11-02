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

						if (javaStringConstraint.getMinSize() != null) {
							if (string == null) {
								return true;
							}
							return BigInteger.valueOf(string.length()).compareTo(javaStringConstraint.getMinSize())
								>= 0;
						}

						if (javaStringConstraint.getMaxSize() != null) {
							if (string == null) {
								return true;
							}
							return BigInteger.valueOf(string.length()).compareTo(javaStringConstraint.getMaxSize())
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

						if (value.compareTo(BigDecimal.ZERO) < 0) {
							if (javaDecimalConstraint.getNegativeMin() != null) {
								if (value.compareTo(javaDecimalConstraint.getNegativeMin()) == 0
									&& Boolean.FALSE.equals(javaDecimalConstraint.getNegativeMinInclusive())) {
									return false;
								}

								if (value.compareTo(javaDecimalConstraint.getNegativeMin()) < 0) {
									return false;
								}
							}

							if (javaDecimalConstraint.getNegativeMax() != null) {
								if (value.compareTo(javaDecimalConstraint.getNegativeMax()) == 0
									&& Boolean.FALSE.equals(javaDecimalConstraint.getNegativeMaxInclusive())) {
									return false;
								}

								if (value.compareTo(javaDecimalConstraint.getNegativeMax()) > 0) {
									return false;
								}
							}
						}

						if (value.compareTo(BigDecimal.ZERO) > 0) {
							if (javaDecimalConstraint.getPositiveMin() != null) {
								if (value.compareTo(javaDecimalConstraint.getPositiveMin()) == 0
									&& Boolean.FALSE.equals(javaDecimalConstraint.getPositiveMinInclusive())) {
									return false;
								}

								if (value.compareTo(javaDecimalConstraint.getPositiveMin()) < 0) {
									return false;
								}
							}

							if (javaDecimalConstraint.getPositiveMax() != null) {
								if (value.compareTo(javaDecimalConstraint.getPositiveMax()) == 0
									&& Boolean.FALSE.equals(javaDecimalConstraint.getPositiveMaxInclusive())) {
									return false;
								}

								if (value.compareTo(javaDecimalConstraint.getPositiveMax()) > 0) {
									return false;
								}
							}
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
					if (javaDateTimeConstraint.getMin() != null) {
						if (offsetTime.isBefore(javaDateTimeConstraint.getMin().atOffset(ZONE_OFFSET).toOffsetTime())) {
							return false;
						}
					}

					if (javaDateTimeConstraint.getMax() != null) {
						if (offsetTime.isAfter(javaDateTimeConstraint.getMax().atOffset(ZONE_OFFSET).toOffsetTime())) {
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
					if (javaDateTimeConstraint.getMin() != null) {
						if (localDateTime.isBefore(javaDateTimeConstraint.getMin())) {
							return false;
						}
					}

					if (javaDateTimeConstraint.getMax() != null) {
						if (localDateTime.isAfter(javaDateTimeConstraint.getMax())) {
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
					if (javaDateConstraint.getMin() != null) {
						if (!localDate.isAfter(javaDateConstraint.getMin().toLocalDate())) {
							return false;
						}
					}

					if (javaDateConstraint.getMax() != null) {
						if (!localDate.isBefore(javaDateConstraint.getMax().toLocalDate())) {
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
						if (value.compareTo(BigInteger.ZERO) < 0) {
							if (javaIntegerConstraint.getNegativeMin() != null) {
								if (value.compareTo(javaIntegerConstraint.getNegativeMin()) < 0) {
									return false;
								}
							}

							if (javaIntegerConstraint.getNegativeMax() != null) {
								if (value.compareTo(BigInteger.ZERO) < 0
									&& value.compareTo(javaIntegerConstraint.getNegativeMax()) > 0) {
									return false;
								}
							}
						}

						if (value.compareTo(BigInteger.ZERO) > 0) {
							if (javaIntegerConstraint.getPositiveMin() != null) {
								if (value.compareTo(javaIntegerConstraint.getPositiveMin()) < 0) {
									return false;
								}
							}

							if (javaIntegerConstraint.getPositiveMax() != null) {
								if (value.compareTo(javaIntegerConstraint.getPositiveMax()) > 0) {
									return false;
								}
							}
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

					if (javaContainerConstraint.getMinSize() != null) {
						DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(it);
						if (decomposableJavaContainer.getSize() < javaContainerConstraint.getMinSize()) {
							throw new ContainerSizeFilterMissException(
								"Container size is should not be less than " + javaContainerConstraint.getMinSize()
							);
						}
					}

					if (javaContainerConstraint.getMaxSize() != null) {
						DecomposableJavaContainer decomposableJavaContainer = decomposedContainerValueFactory.from(it);
						if (decomposableJavaContainer.getSize() > javaContainerConstraint.getMaxSize()) {
							throw new ContainerSizeFilterMissException(
								"Container size is should not be greater than " + javaContainerConstraint.getMaxSize()
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
