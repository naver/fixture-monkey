package com.navercorp.fixturemonkey.datafaker.plugin

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.datafaker.generator.DataFakerObjectPropertyGenerator
import com.navercorp.fixturemonkey.datafaker.introspector.DataFakerArbitraryIntrospector

class DataFakerPlugin : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        val dataFakerIntrospector = DataFakerArbitraryIntrospector()

        optionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
            dataFakerIntrospector,
            DataFakerObjectPropertyGenerator()
        )
        
        optionsBuilder.insertFirstArbitraryIntrospector(
            dataFakerIntrospector,
            dataFakerIntrospector
        )
    }
}
