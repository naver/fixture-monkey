package com.navercorp.fixturemonkey.api.introspector;

import org.apiguardian.api.API;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.arbitraries.StringArbitrary;

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext;

@API(since = "0.6.3", status = API.Status.MAINTAINED)
public final class DefaultJavaArbitraryResolver implements JavaArbitraryResolver {
	@Override
	public Arbitrary<String> strings(StringArbitrary stringArbitrary, ArbitraryGeneratorContext context) {
		return stringArbitrary
			.filter(it -> {
				if (it.trim().isEmpty()) {
					return false;
				}

				return !containsControlCharacters(it);
			});
	}

	private static boolean containsControlCharacters(String value) {
		for (char c : value.toCharArray()) {
			if (Character.isISOControl(c)) {
				return true;
			}
		}

		return false;
	}
}
