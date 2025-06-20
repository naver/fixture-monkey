package com.navercorp.objectfarm.api.nodecandidate;

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
				JvmType javaType = JvmTypes.resolveJvmType(jvmType, field.getGenericType());

				return new JavaNodeCandidate(
					javaType,
					field.getName()
				);
			})
			.collect(Collectors.toList());
	}
}
