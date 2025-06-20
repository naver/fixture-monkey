package com.navercorp.objectfarm.api.node.specs;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImmutableObject {
	public static class StringObject {
		String value;
	}

	public static class SimpleObject {
		String string;
		int integer;
		List<String> list;
		StringObject obj;
	}

	public static class ListObject {
		List<String> values;
	}

	public static class ArrayObject {
		String[] values;
	}

	public static class ListWildcardObject {
		List<? extends String> values;
	}

	public static class SetObject {
		Set<String> values;
	}

	public static class ObjectListObject {
		List<StringObject> values;
	}

	public static class MapObject {
		Map<String, Integer> values;
	}
}
