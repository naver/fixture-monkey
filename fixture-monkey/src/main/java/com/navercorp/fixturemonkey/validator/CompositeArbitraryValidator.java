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

package com.navercorp.fixturemonkey.validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class CompositeArbitraryValidator implements ArbitraryValidator<Object> {
	private final Map<Class<?>, ArbitraryValidator> validators;
	private final ArbitraryValidator defaultValidator;

	public CompositeArbitraryValidator() {
		this(Collections.emptyMap());
	}

	public CompositeArbitraryValidator(Map<Class<?>, ArbitraryValidator> validators) {
		this.validators = new HashMap<>(validators);
		this.defaultValidator = findDefaultValidator();
	}

	private static ArbitraryValidator findDefaultValidator() {
		return new DefaultArbitraryValidator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object arbitrary) {
		ArbitraryValidator validator = this.validators.get(arbitrary.getClass());
		if (validator != null) {
			validator.validate(arbitrary);
		}

		this.defaultValidator.validate(arbitrary);
	}
}
