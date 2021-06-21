package com.navercorp.fixturemonkey;

import java.util.function.Supplier;

public class ArbitrarySupports {
	public static void unique(Runnable runnable) {
		boolean isUniqueScope = ArbitraryGeneratorThreadLocal.isUniqueScope();
		try {
			ArbitraryGeneratorThreadLocal.setUniqueScope(true);
			runnable.run();
		} finally {
			if (!isUniqueScope) {
				ArbitraryGeneratorThreadLocal.closeUniqueScope();
			}
		}
	}

	public static <T> T uniqueAndGet(Supplier<T> supplier) {
		boolean isUniqueScope = ArbitraryGeneratorThreadLocal.isUniqueScope();
		try {
			ArbitraryGeneratorThreadLocal.setUniqueScope(true);
			return supplier.get();
		} finally {
			if (!isUniqueScope) {
				ArbitraryGeneratorThreadLocal.closeUniqueScope();
			}
		}
	}
}
