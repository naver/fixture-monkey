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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;

public final class JavaRecordNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	@Override
	public boolean isSupported(JvmType jvmType) {
		return jvmType.getRawType().isRecord();
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();
		RecordComponent[] components = rawType.getRecordComponents();
		if (components.length == 0) {
			return Collections.emptyList();
		}

		Constructor<?> canonicalConstructor = findCanonicalConstructor(rawType, components);

		List<JvmNodeCandidate> candidates = new ArrayList<>(components.length);
		for (int i = 0; i < components.length; i++) {
			List<Annotation> annotations = mergeAnnotations(components[i]);

			JvmType fieldType = JvmTypes.resolveJvmType(
				jvmType, components[i].getGenericType(), annotations
			);

			CreationMethod creationMethod = new ConstructorParamCreationMethod(canonicalConstructor, i);

			candidates.add(
				JavaNodeCandidateFactory.INSTANCE.create(fieldType, components[i].getName(), creationMethod)
			);
		}
		return candidates;
	}

	private static Constructor<?> findCanonicalConstructor(Class<?> recordType, RecordComponent[] components) {
		Class<?>[] componentTypes = Arrays.stream(components)
			.map(RecordComponent::getType)
			.toArray(Class[]::new);

		try {
			return recordType.getDeclaredConstructor(componentTypes);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
				"Canonical constructor not found for record: " + recordType.getName(), e
			);
		}
	}

	private static List<Annotation> mergeAnnotations(RecordComponent component) {
		List<Annotation> annotations = new ArrayList<>(Arrays.asList(component.getAnnotations()));
		Set<Class<? extends Annotation>> seen = annotations
			.stream()
			.map(Annotation::annotationType)
			.collect(Collectors.toSet());

		Method accessor = component.getAccessor();
		for (Annotation ann : accessor.getAnnotations()) {
			if (seen.add(ann.annotationType())) {
				annotations.add(ann);
			}
		}

		return annotations;
	}
}
