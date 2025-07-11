package com.navercorp.objectfarm.api.node;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.navercorp.objectfarm.api.node.specs.IntefaceObject.Interface;
import com.navercorp.objectfarm.api.node.specs.IntefaceObject.InterfaceImplementation;
import com.navercorp.objectfarm.api.node.specs.IntefaceObject.InterfaceObject;
import com.navercorp.objectfarm.api.nodecandidate.JavaArrayElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaFieldNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaLinearContainerElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecandidate.JavaMapElementNodeCandidateGenerator;
import com.navercorp.objectfarm.api.nodecontext.JavaContainerNodeContext;
import com.navercorp.objectfarm.api.nodecontext.JavaNodeContext;
import com.navercorp.objectfarm.api.nodepromoter.JavaInterfaceNodePromoter;
import com.navercorp.objectfarm.api.nodepromoter.JavaNodePromoter;
import com.navercorp.objectfarm.api.nodepromoter.JvmNodePromoter;
import com.navercorp.objectfarm.api.type.JavaType;
import com.navercorp.objectfarm.api.type.Types;

class JavaNodePromoterTest {
	public static final JavaContainerNodeContext CONTAINER_CONTEXT =
		new JavaContainerNodeContext(3);

	public static final JavaNodeContext CONTEXT = new JavaNodeContext(
		-1L,
		Collections.singletonList(new JavaNodePromoter()),
		Arrays.asList(
			new JavaLinearContainerElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaArrayElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaMapElementNodeCandidateGenerator(CONTAINER_CONTEXT),
			new JavaFieldNodeCandidateGenerator()
		)
	);

	@Test
	void promoteInterface() {
		// given
		JavaNode node = new JavaNode(
			new JavaType(InterfaceObject.class),
			"$",
			CONTEXT
		);

		JvmNodePromoter promoter = new JavaInterfaceNodePromoter(
			type -> {
				if (Types.isAssignable(Interface.class, type.getRawType())) {
					return new JavaType(InterfaceImplementation.class);
				}

				return type;
			}
		);

		// when
		JvmNode actual = promoter.promote(
			node.getCandidateChildren().get(0),
			CONTEXT
		);

		// then
		then(actual.getType().getRawType()).isEqualTo(InterfaceImplementation.class);
	}
}
