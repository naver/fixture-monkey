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

package com.navercorp.fixturemonkey.api.matcher;

import java.lang.reflect.Type;
import java.util.UUID;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
public class Matchers {

	public static final Matcher ENUM_TYPE_MATCHER = ctx -> Types.getActualType(ctx.getType()).isEnum();
	public static final Matcher BOOLEAN_TYPE_MATCHER = ctx -> {
		Type type = ctx.getType();
		return type == boolean.class || type == Boolean.class;
	};
	public static final Matcher UUID_TYPE_MATCHER = new ExactTypeMatcher(UUID.class);
}
