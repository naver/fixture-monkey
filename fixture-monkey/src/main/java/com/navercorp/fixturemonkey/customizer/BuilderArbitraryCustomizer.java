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

package com.navercorp.fixturemonkey.customizer;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.generator.BuilderFieldArbitraries;

@FunctionalInterface
public interface BuilderArbitraryCustomizer<T, B> extends ArbitraryCustomizer<T> {
	default void customizeBuilderFields(BuilderFieldArbitraries<B> builderFieldArbitraries) {
	}

	@Nullable
	B customizeBuilder(B builder);

	@Nullable
	@Override
	default T customizeFixture(@Nullable T object) {
		return object;
	}
}
