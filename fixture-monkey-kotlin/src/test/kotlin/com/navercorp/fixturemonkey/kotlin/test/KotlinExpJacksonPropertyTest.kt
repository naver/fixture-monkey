package com.navercorp.fixturemonkey.kotlin.test

import com.fasterxml.jackson.annotation.JsonProperty
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.LabMonkey
import com.navercorp.fixturemonkey.jackson.plugin.JacksonPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.setExp
import com.navercorp.fixturemonkey.kotlin.setExpGetter
import net.jqwik.api.Property
import net.jqwik.kotlin.api.any
import org.assertj.core.api.BDDAssertions.then

class KotlinExpJacksonPropertyTest {
    private val sut: LabMonkey = FixtureMonkey.labMonkeyBuilder()
        .plugin(JacksonPlugin())
        .plugin(KotlinPlugin())
        .build()

    @Property
    fun setExpJsonPropertyName() {
        val stringValue = String.any().sample()
        val actual = sut.giveMeBuilder<JsonPropertyDataValue>()
            .setExp(JsonPropertyDataValue::stringValue, stringValue)
            .sample()
        then(actual.stringValue).isEqualTo(stringValue)
    }

    @Property
    fun setExpGetterJsonPropertyName() {
        val intValue = Int.any().sample()
        val actual = sut.giveMeBuilder<JsonPropertyDataValue>()
            .setExpGetter(JsonPropertyDataValue::getInt, intValue)
            .sample()
        then(actual.intValue).isEqualTo(intValue)
    }
}

data class JsonPropertyDataValue(
    var intValue: Int,

    @field:JsonProperty("string")
    val stringValue: String,
) {
    @JsonProperty("intValue")
    fun getInt() = this.intValue
}
