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

package com.navercorp.objectfarm.api.nodecandidate;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.JvmType;

/**
 * Default {@link JvmNodeCandidateFactory} implementation for Java types.
 * <p>
 * Handles {@link Map.Entry} types by creating {@link JavaMapEntryNodeCandidate} with
 * key/value children, and all other types as plain {@link JavaNodeCandidate}.
 *
 * @since 1.1.17
 */
public final class JavaNodeCandidateFactory implements JvmNodeCandidateFactory {
	public static final JavaNodeCandidateFactory INSTANCE = new JavaNodeCandidateFactory();

	@Override
	public JvmNodeCandidate create(JvmType type, String name, @Nullable CreationMethod creationMethod) {
		if (Map.Entry.class.isAssignableFrom(type.getRawType())) {
			return createMapEntryCandidate(type, name, creationMethod);
		}
		return new JavaNodeCandidate(type, name, creationMethod);
	}

	private JvmNodeCandidate createMapEntryCandidate(
		JvmType type,
		String name,
		@Nullable CreationMethod creationMethod
	) {
		List<? extends JvmType> typeVars = type.getTypeVariables();

		JvmType keyType;
		JvmType valueType;
		if (typeVars != null && typeVars.size() >= 2) {
			keyType = typeVars.get(0);
			valueType = typeVars.get(1);
		} else {
			keyType = new JavaType(Object.class);
			valueType = new JavaType(Object.class);
		}

		JvmNodeCandidate keyCandidate = new JavaNodeCandidate(keyType, "key");
		JvmNodeCandidate valueCandidate = new JavaNodeCandidate(valueType, "value");

		return new JavaMapEntryNodeCandidate(type, name, keyCandidate, valueCandidate, creationMethod);
	}
}
