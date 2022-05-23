package com.navercorp.fixturemonkey.test;

import static com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.SUT;
import static org.assertj.core.api.BDDAssertions.then;

import net.jqwik.api.Property;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.LabMonkey;
import com.navercorp.fixturemonkey.customizer.ExpressionSpec;
import com.navercorp.fixturemonkey.customizer.MapSpec;
import com.navercorp.fixturemonkey.test.MapSpecTestSpecs.MapObject;
import com.navercorp.fixturemonkey.test.SimpleManipulatorTestSpecs.ListListString;

public class MapSpecTest {
	private static final LabMonkey SUT = LabMonkey.labMonkey();

	@Property
	void setStringMapKeyValue() {

		MapObject actual = SUT.giveMeBuilder(MapObject.class)
			.map("strMap", new MapSpec())
			.sample();

		// then(actual.getStrMap()).
	}

	// @Property
	// void giveMeSpecListListElementPostCondition() {
	// 	// when
	// 	ListListString actual = SUT.giveMeBuilder(ListListString.class)
	// 		.spec(new ExpressionSpec().list("values", it -> {
	// 			it.ofSize(1);
	// 			it.listElement(0, nestedIt -> {
	// 				nestedIt.ofSize(1);
	// 				nestedIt.setElementPostCondition(0, String.class, postConditioned -> postConditioned.length() > 5);
	// 			});
	// 		}))
	// 		.sample();
	//
	// 	then(actual.getValues()).hasSize(1);
	// 	then(actual.getValues().get(0)).hasSize(1);
	// 	then(actual.getValues().get(0).get(0).length()).isGreaterThan(5);
	// }
}
