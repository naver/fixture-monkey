package com.navercorp.fixturemonkey.api.expression;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@API(since = "0.4.0", status = Status.EXPERIMENTAL)
@FunctionalInterface
public interface ExpressionGenerator {
	String generate();
}
