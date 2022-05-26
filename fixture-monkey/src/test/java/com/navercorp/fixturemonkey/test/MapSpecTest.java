package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.builder.ExpressionSpec;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.test.MapSpecTestSpecs.MapObject;

public class MapSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();
	private static final ArbitraryTraverser traverser = SUT.giveMeTraverser();

	@Property(tries = 1)
	void mapSetKeyValue() {
		Map<String, String> map = new HashMap<>();
		map.put("key", "value");

		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.set("strMap", map)
			.spec(new ExpressionSpec(traverser).map("strMap", m -> {
				m.setKey("key", "newKey");
				m.setValue("newKey", "newValue");
			}))
			.sample();

		then(actual.getStrMap().get("newKey")).isEqualTo("newValue");
	}

	@Property(tries = 1)
	void mapSetNestedKeyValue() {
		Map<Map<String, String>, Map<String, String>> mapKeyValueMap = new HashMap<>();
		Map<String, String> keyMap = new HashMap<>();
		Map<String, String> valueMap = new HashMap<>();
		keyMap.put("KeyKey", "KeyVal");
		valueMap.put("ValueKey", "ValueValue");
		mapKeyValueMap.put(keyMap, valueMap);

		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.set("mapKeyValueMap", mapKeyValueMap)
			.spec(new ExpressionSpec(traverser).map("mapKeyValueMap", m -> {
				m.setValue(keyMap, k -> {
					k.setValue("ValueKey", "newValueValue"); // 값의 값
					k.setKey("ValueKey", "newValueKey"); // 값의 키
				});
				m.setKey(keyMap, v -> {
					v.setValue("KeyKey", "newKeyValue"); // 키의 값
					v.setKey("KeyKey", "newKeyKey"); // 키의 키
				});
			}))
			.sample();

		Map<String, String> actualKeyMap = actual.getMapKeyValueMap().keySet().iterator().next();
		Map<String, String> actualValueMap = actual.getMapKeyValueMap().get(actualKeyMap);

		then(actualKeyMap.get("newKeyKey")).isEqualTo("newKeyValue");
		then(actualValueMap.get("newValueKey")).isEqualTo("newValueValue");
	}

	// @Property(tries = 1)
	// void mapSetEmptyMapMakeNewEntry() {
	// 	MapObject actual = SUT.giveMeBuilder(MapObject.class)
	// 		.spec(new ExpressionSpec(traverser).map("strMap", m -> {
	// 			m.setKey("key", "key");
	// 			m.setValue("key", "value");
	// 		}))
	// 		.sample();
	//
	// 	then(actual.getStrMap().get("key")).isEqualTo("value");
	// }

	// MapObject actual = SUT.giveMeBuilder(MapObject.class)
	// 	.set("mapKeyValueMap", mapKeyValueMap)
	// 	.setMap("mapKeyValueMap", m -> {
	// 		m.setKey(keyMap, v -> {
	// 			v.setValue("KeyKey", "newKeyValue"); // 키의 값
	// 			v.setKey("newKeyKey"); // 키의 키
	// 		});
	// 		m.setValue(keyMap, k -> {
	// 			k.setValue("ValueKey", "newValueValue"); // 값의 값
	// 			k.setKey("newValueKey"); // 값의 키
	// 		});
	// 	})
	// 	.sample();
}
