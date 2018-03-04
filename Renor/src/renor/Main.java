package renor;

import java.net.Proxy;

import renor.util.Session;

public class Main {
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack", "true");

		Renor renor = new Renor(new Session("Voidx", "69"), 854, 480, false, null, Proxy.NO_PROXY, "Indev");

		Thread.currentThread().setName("Renor main thread");
		renor.run();
	}
}
