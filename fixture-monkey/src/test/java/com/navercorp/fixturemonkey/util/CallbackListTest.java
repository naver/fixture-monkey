package com.navercorp.fixturemonkey.util;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.CallbackList;

public class CallbackListTest {
	@Test
	void add() {
		// given
		StringBuilder sb = new StringBuilder();
		List<String> sut = new CallbackList<>(new ArrayList<>(), sb::append);

		// when
		sut.add("a");
		sut.add("b");
		sut.add("c");

		then(sb.toString()).isEqualTo("abc");
	}

	@Test
	void addWithIndex() {
		// given
		StringBuilder sb = new StringBuilder();
		List<String> sut = new CallbackList<>(new ArrayList<>(), sb::append);

		// when
		sut.add(0, "a");

		then(sb.toString()).isEqualTo("a");
	}

	@Test
	void set() {
		// given
		StringBuilder sb = new StringBuilder();
		List<String> sut = new CallbackList<>(new ArrayList<>(), sb::append);

		// when
		sut.add("b");
		sut.set(0, "a");

		then(sb.toString()).isEqualTo("ba");
	}

	@Test
	void addAll() {
		// given
		StringBuilder sb = new StringBuilder();
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");
		List<String> sut = new CallbackList<>(new ArrayList<>(), sb::append);

		// when
		sut.addAll(list);

		then(sb.toString()).isEqualTo("abc");
	}

	@Test
	void addAllWithIndex() {
		// given
		StringBuilder sb = new StringBuilder();
		List<String> list = new ArrayList<>();
		list.add("a");
		List<String> sut = new CallbackList<>(new ArrayList<>(), sb::append);

		// when
		sut.addAll(0, list);

		then(sb.toString()).isEqualTo("a");
	}
}
