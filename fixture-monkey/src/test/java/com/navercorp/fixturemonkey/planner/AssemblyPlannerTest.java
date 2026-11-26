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

package com.navercorp.fixturemonkey.planner;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptions;
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import com.navercorp.fixturemonkey.customizer.Scope;
import com.navercorp.fixturemonkey.customizer.ScopeSet;
import com.navercorp.objectfarm.api.node.JvmNode;
import com.navercorp.objectfarm.api.tree.JvmNodeTree;
import com.navercorp.objectfarm.api.type.ReflectiveJvmType;

class AssemblyPlannerTest {

	private static final ScopeSet EMPTY_SCOPE_SET = new ScopeSet(
		Scope.root(Collections.emptyList(), Collections.emptyMap()),
		Collections.emptyList()
	);

	@Test
	void abstractRootIsLeftForAssemblyLikeAbstractProperty() {
		// given
		FixtureMonkeyOptionsBuilder optionsBuilder = FixtureMonkeyOptions.builder();
		new InterfacePlugin()
			.interfaceImplements(Shape.class, Arrays.<Class<? extends Shape>>asList(Circle.class, Square.class))
			.accept(optionsBuilder);
		FixtureMonkeyOptions options = optionsBuilder.build();
		AssemblyPlanner planner = new AssemblyPlanner(0L);

		// when
		JvmNodeTree rootTree = planner
			.plan(new ReflectiveJvmType(Shape.class), EMPTY_SCOPE_SET, false, options, null)
			.getNodeTree();
		JvmNodeTree holderTree = planner
			.plan(new ReflectiveJvmType(Holder.class), EMPTY_SCOPE_SET, false, options, null)
			.getNodeTree();

		// then
		JvmNode root = rootTree.getRootNode();
		JvmNode property = holderTree.getChildren(holderTree.getRootNode()).get(0);
		then(root.getConcreteType().getRawType()).isEqualTo(Shape.class);
		then(root.getDeclaredType().getRawType()).isEqualTo(Shape.class);
		then(property.getConcreteType().getRawType()).isEqualTo(Shape.class);
		then(property.getDeclaredType().getRawType()).isEqualTo(Shape.class);
	}

	public interface Shape {
	}

	public static class Circle implements Shape {
		private int radius;
	}

	public static class Square implements Shape {
		private int side;
	}

	public static class Holder {
		private Shape shape;
	}
}
