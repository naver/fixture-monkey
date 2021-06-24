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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.navercorp.fixturemonkey.generator.BuilderFieldArbitraries;
import com.navercorp.fixturemonkey.generator.FieldArbitraries;

public final class ArbitraryCustomizers {
	private final Map<Class<?>, ArbitraryCustomizer<?>> customizerMap;

	public ArbitraryCustomizers() {
		this(Collections.emptyMap());
	}

	public ArbitraryCustomizers(Map<Class<?>, ArbitraryCustomizer<?>> customizerMap) {
		this.customizerMap = new HashMap<>(customizerMap);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<ArbitraryCustomizer<T>> getArbitraryCustomizer(Class<T> type) {
		return Optional.ofNullable((ArbitraryCustomizer<T>)this.customizerMap.get(type));
	}

	public <T> void customizeFields(Class<T> type, FieldArbitraries fieldArbitraries) {
		this.getArbitraryCustomizer(type).ifPresent(customizer ->
			customizer.customizeFields(type, fieldArbitraries));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> void customizeBuilderFields(Class<T> type, BuilderFieldArbitraries builderFieldArbitraries) {
		this.getArbitraryCustomizer(type)
			.filter(customizer -> customizer instanceof BuilderArbitraryCustomizer)
			.map(BuilderArbitraryCustomizer.class::cast)
			.ifPresent(customizer ->
				customizer.customizeBuilderFields(builderFieldArbitraries));
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T, B> B customizeBuilder(Class<T> type, B builder) {
		return (B)this.getArbitraryCustomizer(type)
			.filter(customizer -> customizer instanceof BuilderArbitraryCustomizer)
			.map(BuilderArbitraryCustomizer.class::cast)
			.map(customizer -> customizer.customizeBuilder(builder))
			.orElse(builder);
	}

	@Nullable
	public <T> T customizeFixture(Class<T> type, @Nullable T object) {
		return this.getArbitraryCustomizer(type)
			.map(customizer -> customizer.customizeFixture(object))
			.orElse(object);
	}

	public ArbitraryCustomizers mergeWith(Map<Class<?>, ArbitraryCustomizer<?>> customizerMap) {
		if (customizerMap.isEmpty()) {
			return this;
		}

		Map<Class<?>, ArbitraryCustomizer<?>> mergedCustomizer = new HashMap<>(this.customizerMap);
		mergedCustomizer.putAll(customizerMap);

		return new ArbitraryCustomizers(mergedCustomizer);
	}
}
