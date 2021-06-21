package com.navercorp.fixturemonkey.customizer;

import com.navercorp.fixturemonkey.generator.ArbitraryGenerator;

public interface WithFixtureCustomizer {
	ArbitraryGenerator withFixtureCustomizers(ArbitraryCustomizers arbitraryCustomizers);
}
