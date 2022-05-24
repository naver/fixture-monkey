package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.resolver.ArbitraryTraverser;
import com.navercorp.fixturemonkey.test.MapSpecTestSpecs.MapObject;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.ListListString;

public class MapSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Property
	void setStringMapKeyValue() {

		// MapObject actual = SUT.giveMeBuilder(MapObject.class)
		// 	// .spec(new ExpressionSpec()
		// 	// 	.map("strMap",
		// 	// 		(it) -> it.setKey()))
		// 	.map("stMap", new MapSpec())
		// 	.sample();

		// then(actual.getStrMap()).
	}

	@Property
	void giveMeSpecListListElementPostCondition() {
		// when
		FixtureMonkey SUT = FixtureMonkey.create();
		ListListString actual = SUT.giveMeBuilder(ListListString.class)
			.spec(new ExpressionSpec().list("values", it -> {
				it.ofSize(1);
				it.listElement(0, nestedIt -> {
					nestedIt.ofSize(1);
					nestedIt.setElement(0, "hello!");
					// nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
				});
			}))
			.sample();

		System.out.println(actual);

		then(actual.getValues()).hasSize(1);
		then(actual.getValues().get(0)).hasSize(1);
		then(actual.getValues().get(0).get(0).length()).isGreaterThan(5);
	}

	// @Property
	// void MapSpec() {
	// 	// Map<Map<String, String>, Map<Integer, Integer>> mapKeyValueMap
	//
	// 	MapObject actual = SUT.giveMeBuilder(MapObject.class)
	// 		.spec(new ExpressionSpec().map("mapKeyValueMap", map -> {
	// 			map.setKey(key, new HashMap<String, String>()); //입력된 key에 맞는 키 전체(Map<String, String>)를 set
	// 			map.setValue(key, new HashMap<String, String>()); //입력된 key에 맞는 값 전체(Map<Integer, Integer>)를 set
	//
	// 			//listElement처럼 입력한 key에 맞는 entry의 key값 선택
	// 			map.mapKey(key, nestedMap -> {
	// 				nestedMap.setKey(key, "Key"); // 키의 키
	// 				nestedMap.setValue(key, "value"); //키의 값
	// 			});
	//
	// 			//listElement처럼 입력한 key에 맞는 entry의 value값 선택
	// 			map.mapValue(key, nestedMap -> {
	// 				nestedMap.setKey(key, "Key"); // 값의 키
	// 				nestedMap.setValue(key, "value"); // 값의 값
	// 			});
	// 		}))
	// 		.sample();
	// }

	void MapSpec2() {
		// Map<Map<String, String>, Map<Integer, Integer>> mapKeyValueMap
		ArbitraryTraverser traverser = SUT.giveMeTraverser();

		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.spec(new ExpressionSpec(traverser).map("mapKeyValueMap", map -> {
				map.setKey(key, new HashMap<String, String>()); //입력된 key에 맞는 키 전체(Map<String, String>)를 set
				map.setValue(key, new HashMap<Integer, Integer>()); //입력된 key에 맞는 값 전체(Map<Integer, Integer>)를 set
				// map.setKey(key, mapKey -> {
				// 	mapKey.setKey(key, "key"); // 키의 키
				// 	mapKey.setValue(key, "value"); // 키의 값
				// });
				// map.setValue(key, mapValue -> {
				// 	mapValue.setKey(key, "key"); // 값의 키
				// 	mapValue.setValue(key, "value"); // 값의 값
				// });
			}
			.sample();
	}
}
