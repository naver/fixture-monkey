package com.navercorp.fixturemonkey.tests.kotlin

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class KotlinInterfaceTest {
    private val sut: FixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .build()

    @Test
    fun parentTest() {
        val actual = sut.giveMeOne<Parent>()

        then(actual).isNotNull
    }

    @Test
    fun childTest() {
        val actual = sut.giveMeOne<Child>()

        then(actual).isNotNull
    }

    @Test
    fun parentWithChildCollectionTest() {
        val actual = sut.giveMeOne<ParentWithChildCollection>()

        then(actual).isNotNull
    }

    @Test
    fun parentWithChildListTest() {
        val actual = sut.giveMeOne<ParentWithChildList>()

        then(actual).isNotNull
    }

    @Test
    fun parentWithChildCollectionImplTest() {
        val actual = sut.giveMeOne<ParentWithChildCollectionImpl>()

        then(actual).isNotNull
        then(actual.childCollection).isInstanceOf(List::class.java)
    }

    @Test
    fun parentWithChildCollectionOverridedTypeImplTest() {
        val actual = sut.giveMeOne<ParentWithChildCollectionOverridedTypeImpl>()

        then(actual).isNotNull
        then(actual.childCollection).isInstanceOf(List::class.java)
    }

    interface Parent {
        val string: String
        val integer: Int
    }

    interface Child {
        val string: String
        val integer: Int
    }

    interface ParentWithChildCollection {
        val childCollection: Collection<Child>
    }

    interface ParentWithChildList {
        val childList: List<Child>
    }

    data class ParentWithChildCollectionImpl(
        override val childCollection: Collection<Child>,
        val childList: List<Child>,
        val string: String,
    ) : ParentWithChildCollection

    data class ParentWithChildCollectionOverridedTypeImpl(
        override val childCollection: List<Child>,
    ) : ParentWithChildCollection
}
