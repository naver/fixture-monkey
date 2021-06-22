package com.navercorp.fixturemonkey.generator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.ArbitraryCustomizers;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;

public class BeanArbitraryGenerator extends AbstractArbitraryGenerator
	implements WithFixtureCustomizer {
	public static final BeanArbitraryGenerator INSTANCE = new BeanArbitraryGenerator();
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ArbitraryCustomizers arbitraryCustomizers;

	public BeanArbitraryGenerator() {
		this(new ArbitraryCustomizers());
	}

	private BeanArbitraryGenerator(ArbitraryCustomizers arbitraryCustomizers) {
		this.arbitraryCustomizers = arbitraryCustomizers;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	protected <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes) {
		Class<T> clazz = type.getType();
		if (clazz.isInterface()) {
			return Arbitraries.just(null);
		}

		FieldArbitraries fieldArbitraries = new FieldArbitraries(
			toArbitrariesByFieldName(nodes, ArbitraryNode::getFieldName, (node, arbitrary) -> arbitrary)
		);

		this.arbitraryCustomizers.customizeFields(clazz, fieldArbitraries);

		Combinators.BuilderCombinator builderCombinator = Combinators.withBuilder(
			() -> ReflectionUtils.newInstance(clazz));
		for (Map.Entry<String, Arbitrary> entry : fieldArbitraries.entrySet()) {
			String fieldName = entry.getKey();
			builderCombinator = builderCombinator.use(entry.getValue()).in((b, v) -> {
				try {
					if (v != null) {
						BeanUtils.setProperty(b, fieldName, v);
					}
				} catch (IllegalAccessException | InvocationTargetException e) {
					log.warn(e,
						() -> "set bean property is failed. field: " + fieldName + " value: " + v
					);
				}
				return b;
			});
		}

		return builderCombinator.build(b -> this.arbitraryCustomizers.customizeFixture(clazz, (T)b));
	}

	@Override
	public ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers) {
		if (this.arbitraryCustomizers == arbitraryCustomizers) {
			return this;
		}
		return new BeanArbitraryGenerator(arbitraryCustomizers);
	}
}
