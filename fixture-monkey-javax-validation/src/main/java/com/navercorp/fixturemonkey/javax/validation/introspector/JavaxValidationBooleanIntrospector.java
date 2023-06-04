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

package com.navercorp.fixturemonkey.javax.validation.introspector;

import static com.navercorp.fixturemonkey.javax.validation.matcher.JavaxMatchers.JAVAX_PACKAGE_MATCHER;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitraries;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectDelegator;
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult;
import com.navercorp.fixturemonkey.api.introspector.BooleanIntrospector;
import com.navercorp.fixturemonkey.api.matcher.Matchers;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class JavaxValidationBooleanIntrospector extends ArbitraryIntrospectDelegator {
	public JavaxValidationBooleanIntrospector() {
		super(
			property -> JAVAX_PACKAGE_MATCHER.match(property) && Matchers.BOOLEAN_TYPE_MATCHER.match(property),
			new BooleanIntrospector()
		);
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		if (context.findAnnotation(AssertTrue.class).isPresent()) {
			return new ArbitraryIntrospectorResult(Arbitraries.of(true));
		}

		if (context.findAnnotation(AssertFalse.class).isPresent()) {
			return new ArbitraryIntrospectorResult(Arbitraries.of(false));
		}

		return super.introspect(context);
	}
}
