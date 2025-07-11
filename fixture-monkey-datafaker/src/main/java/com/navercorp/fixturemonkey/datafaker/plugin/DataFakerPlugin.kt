package com.navercorp.fixturemonkey.datafaker.plugin

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.datafaker.generator.DataFakerStringObjectPropertyGenerator
import com.navercorp.fixturemonkey.datafaker.introspector.DataFakerArbitraryIntrospector
import com.navercorp.fixturemonkey.datafaker.support.DataFakerFieldResolver

class DataFakerPlugin : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        optionsBuilder.insertFirstArbitraryContainerPropertyGenerator(
            { ctx ->
                val prop = ctx
                val actualType = Types.getActualType(prop.type) as Class<*>

                DataFakerFieldResolver.isFakerTargetField(
                    actualType,
                    prop.name
                )
            },
            DataFakerStringObjectPropertyGenerator()
        ).insertFirstArbitraryIntrospector(
            { ctx ->
                val prop = ctx
                val actualType = Types.getActualType(prop.type) as Class<*>

                DataFakerFieldResolver.isFakerTargetField(
                    actualType,
                    prop.name
                )
            },
            DataFakerArbitraryIntrospector()
        )
    }
}
