package com.navercorp.fixturemonkey.api.introspector;

import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Builders.BuilderCombinator;

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo;
import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;
import com.navercorp.fixturemonkey.api.generator.ArbitraryProperty;
import com.navercorp.fixturemonkey.api.generator.ContainerProperty;
import com.navercorp.fixturemonkey.api.matcher.Matcher;
import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty;
import com.navercorp.fixturemonkey.api.property.TupleLikeElementsProperty.TupleLikeElementsType;

@API(since = "0.4.0", status = Status.MAINTAINED)
public final class TupleLikeElementsIntrospector implements ArbitraryIntrospector, Matcher {
	@Override
	public boolean match(Property property) {
		return property.getClass() == TupleLikeElementsProperty.class;
	}

	@Override
	public ArbitraryIntrospectorResult introspect(ArbitraryGeneratorContext context) {
		ArbitraryProperty property = context.getArbitraryProperty();
		ContainerProperty containerProperty = property.getContainerProperty();
		if (containerProperty == null) {
			throw new IllegalArgumentException(
				"container property should not null. type : " + property.getObjectProperty().getProperty().getName()
			);
		}
		ArbitraryContainerInfo containerInfo = containerProperty.getContainerInfo();
		if (containerInfo == null) {
			return ArbitraryIntrospectorResult.EMPTY;
		}

		List<Arbitrary<?>> childrenArbitraries = context.getElementArbitraries();
		BuilderCombinator<TupleLikeElementsType> builderCombinator = Builders.withBuilder(TupleLikeElementsType::new);
		for (Arbitrary<?> child : childrenArbitraries) {
			builderCombinator = builderCombinator.use(child).in((elements, value) -> {
				elements.add(value);
				return elements;
			});
		}

		return new ArbitraryIntrospectorResult(
			builderCombinator.build(TupleLikeElementsType::getList)
		);
	}
}
