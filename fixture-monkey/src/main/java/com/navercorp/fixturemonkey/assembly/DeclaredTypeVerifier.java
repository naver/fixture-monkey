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

package com.navercorp.fixturemonkey.assembly;

import org.jspecify.annotations.Nullable;

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary;
import com.navercorp.fixturemonkey.api.type.Types;
import com.navercorp.objectfarm.api.expression.PathExpression;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.WildcardRawType;

final class DeclaredTypeVerifier {
	private DeclaredTypeVerifier() {
	}

	static void verifyAssignable(@Nullable Object value, JvmType declaredType, PathExpression path) {
		if (value == null || value == LazyResolver.RECURSION_BLOCKED || value instanceof CombinableArbitrary) {
			return;
		}
		Class<?> declaredRawType = declaredType.getRawType();
		if (isAnyTypeMarker(declaredRawType) || Types.isAssignable(value.getClass(), declaredRawType)) {
			return;
		}
		throw new IllegalArgumentException(
			"The value is not assignable to the property type.\n"
				+ "path: " + path.toExpression()
				+ ", property type: " + declaredRawType.getTypeName()
				+ ", value type: " + value.getClass().getTypeName()
		);
	}

	private static boolean isAnyTypeMarker(Class<?> type) {
		return type == WildcardRawType.class
			|| type == Types.GeneratingWildcardType.class
			|| type == Types.UnidentifiableType.class;
	}
}
