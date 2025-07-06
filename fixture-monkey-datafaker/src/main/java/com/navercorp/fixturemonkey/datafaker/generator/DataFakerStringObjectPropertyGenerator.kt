package com.navercorp.fixturemonkey.datafaker.generator

import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.api.generator.ContainerProperty
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGenerator
import com.navercorp.fixturemonkey.api.generator.ContainerPropertyGeneratorContext

class DataFakerStringObjectPropertyGenerator : ContainerPropertyGenerator {
    override fun generate(ctx: ContainerPropertyGeneratorContext): ContainerProperty {
        return ContainerProperty(
            emptyList(),
            ArbitraryContainerInfo(0, 0)
        )
    }
}
