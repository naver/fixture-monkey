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

package com.navercorp.fixturemonkey.adapter.nodecandidate;

import static java.util.stream.Collectors.toMap;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.type.TypeCache;
import com.navercorp.objectfarm.api.node.JvmNodeContext;
import com.navercorp.objectfarm.api.nodecandidate.JavaNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidate;
import com.navercorp.objectfarm.api.nodecandidate.JvmNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.MethodInvocationCreationMethod;
import com.navercorp.objectfarm.api.type.JvmType;
import com.navercorp.objectfarm.api.type.JvmTypes;

/**
 * Generates node candidates from no-argument methods of Java interfaces.
 * <p>
 * This generator mirrors the logic of
 * {@link com.navercorp.fixturemonkey.api.generator.NoArgumentInterfaceJavaMethodPropertyGenerator}
 * but produces {@link JvmNodeCandidate} objects for the object-farm tree infrastructure.
 *
 * @since 1.1.0
 */
@API(since = "1.1.17", status = Status.EXPERIMENTAL)
public final class InterfaceMethodNodeCandidateGenerator implements JvmNodeCandidateGenerator {
	@Override
	public boolean isSupported(JvmType jvmType) {
		return jvmType.getRawType().isInterface();
	}

	@Override
	public boolean isSupported(JvmType jvmType, JvmNodeContext context) {
		if (!isSupported(jvmType)) {
			return false;
		}

		// Exclude container-like interfaces - they are handled by container generators
		if (context.isContainerType(jvmType)) {
			return false;
		}

		return true;
	}

	@Override
	public List<JvmNodeCandidate> generateNextNodeCandidates(JvmType jvmType) {
		Class<?> rawType = jvmType.getRawType();

		Map<Method, String> propertyNamesByGetter = TypeCache.getPropertyDescriptorsByPropertyName(rawType)
			.values()
			.stream()
			.filter(pd -> pd.getReadMethod() != null)
			.collect(toMap(PropertyDescriptor::getReadMethod, PropertyDescriptor::getName));

		Method[] methods = rawType.getMethods();
		List<JvmNodeCandidate> candidates = new ArrayList<>();
		Set<String> seenNames = new HashSet<>();

		for (Method method : methods) {
			if (method.getParameterCount() != 0) {
				continue;
			}

			String name = propertyNamesByGetter.get(method);
			if (name == null) {
				name = resolvePropertyName(method.getName());
			}

			if (!seenNames.add(name)) {
				continue;
			}

			List<Annotation> annotations = Arrays.asList(method.getAnnotations());
			JvmType returnType = JvmTypes.resolveJvmType(jvmType, method.getGenericReturnType(), annotations);

			candidates.add(new JavaNodeCandidate(returnType, name, new MethodInvocationCreationMethod(method)));
		}

		return Collections.unmodifiableList(candidates);
	}

	/**
	 * Resolves a property name from a method name using JavaBeans conventions.
	 * Strips "get"/"is" prefixes and decapitalizes the first character.
	 * Falls back to the raw method name if no prefix matches.
	 */
	private static String resolvePropertyName(String methodName) {
		if (methodName.startsWith("get") && methodName.length() > 3) {
			return decapitalize(methodName.substring(3));
		}
		if (methodName.startsWith("is") && methodName.length() > 2) {
			return decapitalize(methodName.substring(2));
		}
		return methodName;
	}

	private static String decapitalize(String name) {
		if (name.isEmpty()) {
			return name;
		}
		// JavaBeans spec: if first two chars are uppercase, return as-is (e.g., "URL" stays "URL")
		if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
			return name;
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}
}
