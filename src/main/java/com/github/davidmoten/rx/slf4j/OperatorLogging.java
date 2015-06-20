package com.github.davidmoten.rx.slf4j;

import static com.github.davidmoten.rx.slf4j.Logging.log;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.observers.Subscribers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

import com.github.davidmoten.rx.slf4j.Logging.Parameters;
import com.github.davidmoten.rx.slf4j.Logging.Parameters.Message;

public class OperatorLogging<T> implements Operator<T, T> {

	private final Parameters<T> p;

	public OperatorLogging(Parameters<T> parameters) {
		this.p = parameters;
	}

	@Override
	public Subscriber<? super T> call(Subscriber<? super T> child) {
		Subscriber<T> parent = createSubscriber(p.getSubject(), child);
		Subscriber<Message<T>> logSubscriber = Subscribers.empty();
		Subscription unsubscriptionlistener = createUnsubscriptionListener();
		child.add(unsubscriptionlistener);
		logSubscriber.add(unsubscriptionlistener);
		child.add(logSubscriber);
		p.getObservable().unsafeSubscribe(logSubscriber);

		if (p.getSubscribedMessage() != null)
			log(p.getLogger(), p.getSubscribedMessage(),
					p.getSubscribedLevel(), null);
		return parent;
	}

	private Subscription createUnsubscriptionListener() {
		return Subscriptions.create(new Action0() {
			@Override
			public void call() {
				if (p.getUnsubscribedMessage() != null)
					log(p.getLogger(), p.getUnsubscribedMessage(),
							p.getUnsubscribedLevel(), null);
			}

		});
	}

	static <T> Subscriber<T> createSubscriber(final PublishSubject<T> subject,
			final Subscriber<? super T> child) {
		return new Subscriber<T>(child) {

			@Override
			public void onCompleted() {
				subject.onCompleted();
				child.onCompleted();
			}

			@Override
			public void onError(Throwable e) {
				subject.onError(e);
				child.onError(e);
			}

			@Override
			public void onNext(T t) {
				subject.onNext(t);
				child.onNext(t);
			}
		};
	}

}
