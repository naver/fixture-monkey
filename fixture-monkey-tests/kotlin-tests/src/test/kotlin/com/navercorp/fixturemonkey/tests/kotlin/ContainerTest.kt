/*
 * Fixture Monkey
 *
 * Copyright (c) 2021-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.api.generator.ArbitraryContainerInfo
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class ContainerTest {
    @Test
    fun setShrinkContainerNode() {
        val expected = listOf("a")

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size("$", 3)
            .set("$[0]", "a1")
            .set("$[1]", "b")
            .set("$[2]", "c")
            .set(expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setExpandContainerNode() {
        val expected = listOf("a", "b", "c")

        val actual = SUT.giveMeKotlinBuilder<List<String>>()
            .size("$", 1)
            .set(expected)
            .sample()

        then(actual).isEqualTo(expected)
    }

    @Test
    fun setListKeepsContainerTwoLevelsInsideElement() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<Feed>()
            .set("notes", listOf(Note(Tags(listOf("a", "b")))))
            .sample()

        // then
        then(actual.notes).hasSize(1)
        then(actual.notes[0].tags.values).containsExactly("a", "b")
    }

    @Test
    fun setListKeepsEmptyContainerTwoLevelsInsideElement() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<Feed>()
            .set("notes", listOf(Note(Tags(emptyList()))))
            .sample()

        // then
        then(actual.notes[0].tags.values).isEmpty()
    }

    @Test
    fun setListKeepsEachElementsOwnContainerSize() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<Feed>()
            .set("notes", listOf(Note(Tags(listOf("a", "b"))), Note(Tags(listOf("c", "d", "e", "f")))))
            .sample()

        // then
        then(actual.notes).hasSize(2)
        then(actual.notes[0].tags.values).containsExactly("a", "b")
        then(actual.notes[1].tags.values).containsExactly("c", "d", "e", "f")
    }

    @Test
    fun setListKeepsNullableObjectInsideElement() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<NullableFeed>()
            .set("notes", listOf(NullableNote(Tags(listOf("a", "b"))), NullableNote(null)))
            .sample()

        // then
        then(actual.notes[0].tags!!.values).containsExactly("a", "b")
        then(actual.notes[1].tags).isNull()
    }

    @Test
    fun setArrayKeepsContainerTwoLevelsInsideElement() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<ArrayFeed>()
            .set("notes", arrayOf(Note(Tags(listOf("a", "b")))))
            .sample()

        // then
        then(actual.notes[0].tags.values).containsExactly("a", "b")
    }

    @Test
    fun setMapKeepsContainerTwoLevelsInsideValue() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<MapFeed>()
            .set("notes", mapOf("key" to Note(Tags(listOf("a", "b")))))
            .sample()

        // then
        then(actual.notes["key"]!!.tags.values).containsExactly("a", "b")
    }

    @Test
    fun setRootKeepsContainerTwoLevelsInsideListElement() {
        // when
        val actual = FIXED_CONTAINER_SIZE_SUT.giveMeKotlinBuilder<Feed>()
            .set("$", Feed(listOf(Note(Tags(listOf("a", "b"))))))
            .sample()

        // then
        then(actual.notes[0].tags.values).containsExactly("a", "b")
    }

    data class Tags(val values: List<String>)

    data class Note(val tags: Tags)

    data class Feed(val notes: List<Note>)

    data class NullableNote(val tags: Tags?)

    data class NullableFeed(val notes: List<NullableNote>)

    data class MapFeed(val notes: Map<String, Note>)

    class ArrayFeed(val notes: Array<Note>)

    companion object {
        private val SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

        private val FIXED_CONTAINER_SIZE_SUT: FixtureMonkey = FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .defaultArbitraryContainerInfoGenerator { ArbitraryContainerInfo(3, 3) }
            .build()
    }
} 
