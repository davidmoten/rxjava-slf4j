package com.github.davidmoten.rx.slf4j;

import java.util.concurrent.atomic.AtomicReference;

import rx.Subscriber;
import rx.subjects.Subject;

public class SingleSubscriptionSubject<T> extends Subject<T, T> {

	private final OnSubscribeImpl<T> onSubscribe;

	public static <T> SingleSubscriptionSubject<T> create() {
		OnSubscribeImpl<T> onSubscribe = new OnSubscribeImpl<T>();
		SingleSubscriptionSubject<T> subject = new SingleSubscriptionSubject<T>(
				onSubscribe);
		return subject;
	}

	public SingleSubscriptionSubject(OnSubscribeImpl<T> onSubscribe) {
		super(onSubscribe);
		this.onSubscribe = onSubscribe;
	}

	@Override
	public void onCompleted() {
		if (subscribed())
			onSubscribe.getSubscriber().get().onCompleted();
	}

	@Override
	public void onError(Throwable e) {
		if (subscribed())
			onSubscribe.getSubscriber().get().onError(e);
	}

	@Override
	public void onNext(T t) {
		if (subscribed())
			onSubscribe.getSubscriber().get().onNext(t);
	}

	private boolean subscribed() {
		return onSubscribe.getSubscriber().get() != null
				&& onSubscribe.getSubscriber().get().isUnsubscribed();
	}

	private static class OnSubscribeImpl<T> implements OnSubscribe<T> {

		private final AtomicReference<Subscriber<? super T>> sub = new AtomicReference<Subscriber<? super T>>();

		@Override
		public void call(Subscriber<? super T> subscriber) {
			sub.set(subscriber);
		}

		public AtomicReference<Subscriber<? super T>> getSubscriber() {
			return sub;
		}

	}

}
