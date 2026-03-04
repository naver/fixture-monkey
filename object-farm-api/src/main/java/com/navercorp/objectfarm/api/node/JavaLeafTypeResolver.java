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

package com.navercorp.objectfarm.api.node;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.Types;

/**
 * A {@link LeafTypeResolver} that treats Java standard types as leaf types.
 * <p>
 * This includes primitives and types in the {@code java.*} and {@code sun.*} packages.
 */
public final class JavaLeafTypeResolver implements LeafTypeResolver {

	public static final JavaLeafTypeResolver INSTANCE = new JavaLeafTypeResolver();

	private JavaLeafTypeResolver() {
	}

	@Override
	public boolean isLeafType(JvmType jvmType) {
		return Types.isJavaType(jvmType.getRawType());
	}
}
