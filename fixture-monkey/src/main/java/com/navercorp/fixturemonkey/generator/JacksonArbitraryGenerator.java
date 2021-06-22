package com.navercorp.fixturemonkey.generator;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Combinators.BuilderCombinator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.fixturemonkey.JacksonObjectMapper;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JacksonArbitraryGenerator extends AbstractArbitraryGenerator {
	public static final JacksonArbitraryGenerator INSTANCE = new JacksonArbitraryGenerator();

	private final ObjectMapper objectMapper;
	private final ArbitraryCustomizers arbitraryCustomizers;

	public JacksonArbitraryGenerator() {
		this(JacksonObjectMapper.defaultObjectMapper(), new ArbitraryCustomizers());
	}

	public JacksonArbitraryGenerator(ObjectMapper objectMapper) {
		this(objectMapper, new ArbitraryCustomizers());
	}

	private JacksonArbitraryGenerator(ObjectMapper objectMapper, ArbitraryCustomizers arbitraryCustomizers) {
		if (objectMapper.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)) {
			this.objectMapper = objectMapper.copy().configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, false);
		} else {
			this.objectMapper = objectMapper;
		}
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, this::resolveFieldName, this::formatValue)
		);

		this.arbitraryCustomizers.customizeFields(type.getType(), fieldArbitraries);

		BuilderCombinator<Map<String, Object>> builderCombinator = Combinators.withBuilder(HashMap::new);
		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String fieldName = entry.getKey();
			Arbitrary<?> parameterArbitrary = entry.getValue();
			builderCombinator = builderCombinator.use(parameterArbitrary).in((map, value) -> {
				if (value != null) {
					map.put(fieldName, value);
				}
				return map;
			});
		}

		return (Arbitrary<T>)builderCombinator.build(
			map -> {
				Class clazz = type.getType();
				T fixture = (T)objectMapper.convertValue(map, clazz);

				return this.arbitraryCustomizers.customizeFixture(clazz, fixture);
			});
	}

	private <T> Arbitrary<T> formatValue(ArbitraryNode<T> node, Arbitrary<T> arbitrary) {
		ArbitraryType<T> nodeType = node.getType();
		JsonFormat jsonFormat = nodeType.getAnnotation(JsonFormat.class);
		if (jsonFormat != null) {
			return (Arbitrary<T>)arbitrary.map(it -> format(it, jsonFormat));
		}
		return arbitrary;
	}

	private Object format(Object object, JsonFormat jsonFormat) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(jsonFormat.pattern());
		if (object instanceof TemporalAccessor) {
			TemporalAccessor temporalAccessor = (TemporalAccessor)object;
			return dateTimeFormatter.format(temporalAccessor);
		} else if (object instanceof Date) {
			TemporalAccessor dateTemporalAccessor = ((Date)object).toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
			return dateTimeFormatter.format(dateTemporalAccessor);
		} else if (object instanceof Enum && jsonFormat.shape().isNumeric()) {
			return ((Enum)object).ordinal();
		} else {
			return object;
		}
	}

	private <T> String resolveFieldName(ArbitraryNode<T> node) {
		ArbitraryType<T> nodeType = node.getType();
		JsonProperty jsonProperty = nodeType.getAnnotation(JsonProperty.class);
		if (jsonProperty == null) {
			return node.getFieldName();
		} else {
			return jsonProperty.value();
		}
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}

		return new JacksonArbitraryGenerator(objectMapper, arbitraryCustomizers);
	}
}
