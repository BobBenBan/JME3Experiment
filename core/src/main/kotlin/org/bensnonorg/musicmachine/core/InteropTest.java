package org.bensnonorg.musicmachine.core;

import org.bensnonorg.musicmachine.core.DelayedExecutor;

public class InteropTest {
	public static void main(String[] args) {
		var delayedExecutor = new DelayedExecutor();
		delayedExecutor.queue(() -> {
			System.out.println("hi");
		});
		delayedExecutor.run();
	}
}
