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

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

class AnonymousInstanceTestSpecs {
	public interface Interface {
		String string();

		Integer integer();
	}

	public interface InterfaceWithParams {
		String string(String str);

		Integer integer(Integer integer);
	}

	public interface InterfaceWithConstant {
		String value = "constant";
	}

	public interface ContainerInterface {
		List<String> list();

		Map<String, Integer> map();
	}

	public interface AnnotatedInterface {
		@NotEmpty
		String string();
	}

	public interface GetterInterface {
		String getValue();
	}
}
