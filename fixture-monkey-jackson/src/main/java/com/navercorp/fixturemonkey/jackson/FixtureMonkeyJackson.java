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

package com.navercorp.fixturemonkey.jackson;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class FixtureMonkeyJackson {
	private static final List<Module> REGISTERED_MODULES = ObjectMapper.findModules().stream()
		.filter(module -> !module.getModuleName().equalsIgnoreCase("AfterburnerModule"))
		.collect(toList()); // afterburner only support "public setter" for deserializing
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		.registerModules(REGISTERED_MODULES);

	public static ObjectMapper defaultObjectMapper() {
		return OBJECT_MAPPER.copy();
	}
}
