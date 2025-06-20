package com.navercorp.objectfarm.api.node.specs;

public class IntefaceObject {
	public interface Interface {
		void method();
	}

	public static class InterfaceImplementation implements Interface {
		@Override
		public void method() {
			// Implementation of the method
		}
	}

	public static class InterfaceObject {
		Interface anInterface;
	}
}
