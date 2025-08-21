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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;
import com.navercorp.objectfarm.api.type.Reflections;

public final class JavaFieldNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		if (rawType.isPrimitive() || rawType.isArray() || rawType.isEnum()) {
			return Collections.emptyList();
		}

		return Reflections.findFields(rawType).stream()
			.map(field -> {
				List<Annotation> annotations = Arrays.stream(field.getAnnotations())
					.collect(Collectors.toList());

				JvmType javaType = JvmTypes.resolveJvmType(jvmType, field.getGenericType(), annotations);

				return new JavaNodeCandidate(
					javaType,
					field.getName()
				);
			})
			.collect(Collectors.toList());
	}
}
