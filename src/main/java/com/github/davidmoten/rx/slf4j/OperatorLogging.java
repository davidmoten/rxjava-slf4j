package com.github.davidmoten.rx.slf4j;

import static com.github.davidmoten.rx.slf4j.Logging.log;

import org.slf4j.Logger;

import rx.Notification;
import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;

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
        PublishSubjectSingleSubscriber<T> subject = PublishSubjectSingleSubscriber.create();

        // create the logging observable
        Observable<Message<T>> observable = createObservableFromSubject(subject);

        // apply all the logging stream transformations
        for (Func1<Observable<Message<T>>, Observable<Message<T>>> transformation : parameters
                .getTransformations()) {
            observable = transformation.call(observable);
        }

        Action0 unsubscriptionLogger = createUnsubscriptionAction(parameters);
        Action0 subscriptionLogger = createSubscriptionAction(parameters);
        observable = observable
        // add subscription action
                .lift(new OperatorDoOnSubscribe<Message<T>>(subscriptionLogger))
                // add unsubscription action
                .lift(new OperatorDoOnUnsubscribe<Message<T>>(unsubscriptionLogger));

        // create parent subscriber
        Subscriber<T> parent = createParentSubscriber(subject, child);

        // create subscriber for the logging observable
        Subscriber<Message<T>> logSubscriber = createErrorLoggingSubscriber(parameters.getLogger());

        // ensure logSubscriber is unsubscribed when child is unsubscribed
        child.add(logSubscriber);

        // subscribe to the logging observable
        observable.unsafeSubscribe(logSubscriber);

        return parent;
    }

    private static <T> Subscriber<Message<T>> createErrorLoggingSubscriber(final Logger logger) {
        return new Subscriber<Message<T>>() {

            @Override
            public void onCompleted() {
                // ignore
            }

            @Override
            public void onError(Throwable e) {
                logger.error("the logging transformations through an exception: " + e.getMessage(),
                        e);
            }

            @Override
            public void onNext(Message<T> t) {
                // ignore
            }
        };
    }

    private static <T> Observable<Message<T>> createObservableFromSubject(
            PublishSubjectSingleSubscriber<T> subject) {
        return subject.materialize().map(new Func1<Notification<T>, Message<T>>() {

            @Override
            public Message<T> call(Notification<T> n) {
                return new Message<T>(n, "");
            }
        });
    }

    private static <T> Action0 createUnsubscriptionAction(final Parameters<T> p) {
        return new Action0() {
            @Override
            public void call() {
                // log unsubscription if requested
                if (p.getUnsubscribedMessage() != null)
                    log(p.getLogger(), p.getUnsubscribedMessage(), p.getUnsubscribedLevel(), null);
            }

        };
    }

    private static <T> Action0 createSubscriptionAction(final Parameters<T> p) {
        return new Action0() {
            @Override
            public void call() {
                // log subscription if requested
                if (p.getSubscribedMessage() != null)
                    log(p.getLogger(), p.getSubscribedMessage(), p.getSubscribedLevel(), null);
            }
        };
    }

    private static <T> Subscriber<T> createParentSubscriber(
            final PublishSubjectSingleSubscriber<T> subject, final Subscriber<? super T> child) {
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
