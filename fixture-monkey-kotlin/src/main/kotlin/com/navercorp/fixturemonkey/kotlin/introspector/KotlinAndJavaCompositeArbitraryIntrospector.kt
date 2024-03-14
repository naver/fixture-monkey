package com.navercorp.fixturemonkey.kotlin.introspector

import com.navercorp.fixturemonkey.api.generator.ArbitraryGeneratorContext
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospector
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.type.Types
import com.navercorp.fixturemonkey.kotlin.type.isKotlinType
import org.slf4j.LoggerFactory

class KotlinAndJavaCompositeArbitraryIntrospector(
    private val kotlinIntrospector: ArbitraryIntrospector,
    private val javaIntrospector: ArbitraryIntrospector,
) : ArbitraryIntrospector {
    override fun introspect(context: ArbitraryGeneratorContext): ArbitraryIntrospectorResult {
        val type = Types.getActualType(context.resolvedType)
        try {
            return if (type.isKotlinType()) {
                kotlinIntrospector.introspect(context)
            } else {
                javaIntrospector.introspect(context)
            }
        } catch (e: Exception) {
            LOGGER.warn("Given type $type is failed to generated due to the exception.", e)
            return ArbitraryIntrospectorResult.NOT_INTROSPECTED
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KotlinAndJavaCompositeArbitraryIntrospector::class.java)
    }
}
