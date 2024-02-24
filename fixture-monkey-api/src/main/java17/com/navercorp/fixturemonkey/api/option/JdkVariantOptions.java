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

package com.navercorp.fixturemonkey.api.option;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.generator.ObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.generator.SealedTypeObjectPropertyGenerator;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "1.0.14", status = Status.INTERNAL)
public final class JdkVariantOptions {
	private static final ObjectPropertyGenerator SEALED_TYPE_OBJECT_PROPERTY_GENERATOR =
		new SealedTypeObjectPropertyGenerator();

	public void apply(FixtureMonkeyOptionsBuilder optionsBuilder) {
		optionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
			p -> Types.getActualType(p.getType()).isSealed(),
			SEALED_TYPE_OBJECT_PROPERTY_GENERATOR
		);
	}
}
