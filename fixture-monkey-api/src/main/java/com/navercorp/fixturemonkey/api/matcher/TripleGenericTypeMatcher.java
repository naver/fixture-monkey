package com.navercorp.fixturemonkey.api.matcher;

import java.lang.reflect.AnnotatedType;
import java.util.List;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import com.navercorp.fixturemonkey.api.property.Property;
import com.navercorp.fixturemonkey.api.type.Types;

@API(since = "0.6.0", status = Status.MAINTAINED)
public final class TripleGenericTypeMatcher implements Matcher {
	@Override
	public boolean match(Property property) {
		List<AnnotatedType> genericsTypes = Types.getGenericsTypes(property.getAnnotatedType());
		return genericsTypes.size() == 3;
	}
}
