package com.github.davidmoten.rx.slf4j;

import static com.github.davidmoten.rx.slf4j.Logging.log;
import rx.Notification;
import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observers.Subscribers;
import rx.subscriptions.Subscriptions;

import com.github.davidmoten.rx.slf4j.Logging.Parameters;
import com.github.davidmoten.rx.slf4j.Logging.Parameters.Message;
import com.github.davidmoten.rx.subjects.PublishSubjectSingleSubscriber;

public class OperatorLogging<T> implements Operator<T, T> {

	private final Parameters<T> parameters;

	public OperatorLogging(Parameters<T> parameters) {
		this.parameters = parameters;
	}

	@Override
	public Subscriber<? super T> call(Subscriber<? super T> child) {
		// create the subject and an observable from the subject that
		// materializes the notifications from the subject
		PublishSubjectSingleSubscriber<T> subject = PublishSubjectSingleSubscriber
				.create();
		Observable<Message<T>> observable = createObservableFromSubject(subject);
		// apply all the logging stream transformations
		for (Func1<Observable<Message<T>>, Observable<Message<T>>> transformation : parameters
				.getTransformations()) {
			observable = transformation.call(observable);
		}
		Subscriber<T> parent = createSubscriber(subject, child);
		Subscriber<Message<T>> logSubscriber = Subscribers.empty();
		Subscription unsubscriptionlistener = createUnsubscriptionListener(parameters);
		child.add(unsubscriptionlistener);
		logSubscriber.add(unsubscriptionlistener);
		child.add(logSubscriber);
		observable.unsafeSubscribe(logSubscriber);

		if (parameters.getSubscribedMessage() != null)
			log(parameters.getLogger(), parameters.getSubscribedMessage(),
					parameters.getSubscribedLevel(), null);
		return parent;
	}

	private static <T> Observable<Message<T>> createObservableFromSubject(
			PublishSubjectSingleSubscriber<T> subject) {
		return subject.materialize().map(
				new Func1<Notification<T>, Message<T>>() {

					@Override
					public Message<T> call(Notification<T> n) {
						return new Message<T>(n, "");
					}
				});
	}

	private static <T> Subscription createUnsubscriptionListener(
			final Parameters<T> p) {
		return Subscriptions.create(new Action0() {
			@Override
			public void call() {
				if (p.getUnsubscribedMessage() != null)
					log(p.getLogger(), p.getUnsubscribedMessage(),
							p.getUnsubscribedLevel(), null);
			}

		});
	}

	private static <T> Subscriber<T> createSubscriber(
			final PublishSubjectSingleSubscriber<T> subject,
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
