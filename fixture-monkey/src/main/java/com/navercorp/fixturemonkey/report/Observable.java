package com.navercorp.fixturemonkey.report;

public interface Observable {
//	public void register(Observer obj);
//	public void unregister(Observer obj);

	public void notify(Integer builder, DebugInfo debugInfo);
}
