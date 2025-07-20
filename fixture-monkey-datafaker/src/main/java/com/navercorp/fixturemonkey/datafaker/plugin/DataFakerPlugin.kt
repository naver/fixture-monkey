package com.navercorp.fixturemonkey.datafaker.plugin

import com.navercorp.fixturemonkey.api.matcher.Matcher
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.datafaker.generator.DataFakerObjectPropertyGenerator
import com.navercorp.fixturemonkey.datafaker.introspector.DataFakerArbitraryIntrospector
import com.navercorp.fixturemonkey.datafaker.support.DataFakerFieldResolver

class DataFakerPlugin : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        val dataFakerMatcher = Matcher { property ->
            val actualType = Types.getActualType(property.type) as Class<*>
            DataFakerFieldResolver.isFakerTargetField(actualType, property.name)
        }

        optionsBuilder.insertFirstArbitraryObjectPropertyGenerator(
            dataFakerMatcher,
            DataFakerObjectPropertyGenerator()
        )
        
        optionsBuilder.insertFirstArbitraryIntrospector(
            dataFakerMatcher,
            DataFakerArbitraryIntrospector()
        )
    }
}
