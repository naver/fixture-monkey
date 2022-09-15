package com.navercorp.fixturemonkey;

public interface Observable {
//	public void register(Observer obj);
//	public void unregister(Observer obj);

	public void notify(Object obj, Object o);
}
