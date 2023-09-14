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

package com.navercorp.fixturemonkey.api.constraint;

import java.math.BigInteger;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.6.8", status = Status.EXPERIMENTAL)
public interface JavaConstraintGenerator {
	BigInteger BIG_INTEGER_MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
	BigInteger BIG_INTEGER_MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
	BigInteger BIG_INTEGER_MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
	BigInteger BIG_INTEGER_MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
	BigInteger BIG_INTEGER_MIN_SHORT = BigInteger.valueOf(Short.MIN_VALUE);
	BigInteger BIG_INTEGER_MAX_SHORT = BigInteger.valueOf(Short.MAX_VALUE);
	BigInteger BIG_INTEGER_MIN_BYTE = BigInteger.valueOf(Byte.MIN_VALUE);
	BigInteger BIG_INTEGER_MAX_BYTE = BigInteger.valueOf(Byte.MAX_VALUE);

	JavaStringConstraint generateStringConstraint(ArbitraryGeneratorContext context);

	JavaIntegerConstraint generateIntegerConstraint(ArbitraryGeneratorContext context);

	JavaDecimalConstraint generateDecimalConstraint(ArbitraryGeneratorContext context);

	JavaContainerConstraint generateContainerConstraint(ArbitraryGeneratorContext context);

	JavaDateTimeConstraint generateDateTimeConstraint(ArbitraryGeneratorContext context);
}
