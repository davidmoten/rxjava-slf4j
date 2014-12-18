package com.github.davidmoten.rx.slf4j;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.Observable;
import rx.functions.Action1;

public class Logging2Test {

	@Test
	public void testNoErrorsAndCompletes() {
		Observable
				.range(1, 1000)
				.doOnNext(new Action1<Integer>() {

					@Override
					public void call(Integer n) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
						}
					}
				})
				.doOnNext(
						Logging2.rate("msgsPerSecond=", 100, TimeUnit.SECONDS)
								.count("count=").count("n=")
								.every(200, TimeUnit.MILLISECONDS)
								.count("div200=").value("v=").memory().log())
				.subscribe();
	}
}
