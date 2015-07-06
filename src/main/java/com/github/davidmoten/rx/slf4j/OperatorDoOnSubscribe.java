package com.github.davidmoten.rx.slf4j;

import rx.Observable.Operator;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * This operator modifies an {@link rx.Observable} so a given action is invoked
 * when the {@link rx.Observable} is subscribed.
 * 
 * @param <T>
 *            The type of the elements in the {@link rx.Observable} that this
 *            operator modifies
 */
public class OperatorDoOnSubscribe<T> implements Operator<T, T> {
    private final Action0 subscribe;

    /**
     * Constructs an instance of the operator with the callback that gets
     * invoked when the modified Observable is subscribed
     * 
     * @param subscribe
     *            the action that gets invoked when the modified
     *            {@link rx.Observable} is subscribed
     */
    public OperatorDoOnSubscribe(Action0 subscribe) {
        this.subscribe = subscribe;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        subscribe.call();
        // Pass through since this operator is for notification only, there is
        // no change to the stream whatsoever.
        return new Subscriber<T>(child) {
            @Override
            public void onNext(T t) {
                child.onNext(t);
            }

            @Override
            public void onError(Throwable e) {
                child.onError(e);
            }

            @Override
            public void onCompleted() {
                child.onCompleted();
            }
        };
    }
}
