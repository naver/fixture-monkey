package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.data.MapEntry;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.test.MapSpecTestSpecs.MapObject;

public class MapSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Property
	void mapAddKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setMap("strMap", m -> {
				m.addKey("key");
			})
			.sample();

		then(actual.getStrMap().containsKey("key")).isTrue();
	}

	@Property
	void mapAddValue() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setMap("strMap", m -> {
				m.addValue("value");
			})
			.sample();

		then(actual.getStrMap().containsValue("value")).isTrue();
	}

	@Property
	void mapPut() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setMap("strMap", m -> {
				m.put("key", "value");
			})
			.sample();

		then(actual.getStrMap().get("key")).isEqualTo("value");
	}

	@Property()
	void mapAddNestedKey() {
		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.setMap("mapKeyMap", m -> {
				m.addKey(k-> {
					k.addKey("key");
				});
			})
			.sample();

		List<String> keyList = actual.getMapKeyMap().keySet().stream()
			.map(Map::keySet).flatMap(Set::stream).collect(Collectors.toList());
		then(keyList).contains("key");
	}

	// @Property(tries = 10)
	// void mapAddNestedValue() {
	// 	MapObject actual = SUT.giveMeBuilder(MapObject.class)
	// 		.setMap("mapValueMap", m -> {
	// 			m.addValue(k-> {
	// 				k.addValue("value");
	// 			});
	// 		})
	// 		.sample();
	//
	// 	List<String> valueList = actual.getMapKeyMap().entrySet().stream()
	// 		.map(Map.Entry::getValue).map(Map::entrySet).stream().flatMap(Set::stream).collect(Collectors.toList());
	// 	then(valueList).contains("value");
	// }

	// @Property()
	// void mapAddNestedKeyValue() {
	// 	MapObject actual = SUT.giveMeBuilder(MapObject.class)
	// 		.setMap("mapKeyValueMap", m -> {
	// 			m.addKey(k -> {
	// 				k.addKey("KeyKey");
	// 				k.addValue("KeyValue");
	// 				k.put("Key", "Value");
	// 			});
	// 			m.addValue(v -> {
	// 				v.addKey("ValueKey");
	// 				v.addValue("ValueValue");
	// 				v.put("Key","Value");
	// 			});
	// 		})
	// 		.sample();
	//
	// 	System.out.println(actual.getMapKeyValueMap());
	// 	then(actual.getMapKeyValueMap().entrySet());
	// }
}
