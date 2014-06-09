package com.github.davidmoten.rx.slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Functions;
import rx.subjects.PublishSubject;

public class Logging {

	private static final Logger DEFAULT_LOGGER = LoggerFactory
			.getILoggerFactory().getLogger(Logging.class.getName());

	public enum Level {
		INFO, WARN, DEBUG, TRACE, ERROR;
	}

	public enum CountFormat {
		K_AND_M, COMMAS, PLAIN;
	}

	public static class Parameters<T> {

		private final Logger logger;
		private final String loggerName;
		private final String onCompleteMessage;
		private final String subscribedMessage;
		private final String unsubscribedMessage;
		private final boolean logOnNext;
		private final boolean logOnError;
		private final String onErrorPrefix;
		private final String onErrorSuffix;
		private final String onNextPrefix;
		private final String onNextSuffix;
		private final Level onNextLevel;
		private final Level onErrorLevel;
		private final Level onCompletedLevel;
		private final Level subscribedLevel;
		private final Level unsubscribedLevel;
		private final CountFormat countFormat;
		private final boolean logObject;
		private final boolean logStackTrace;
		private final PublishSubject<T> subject;
		private final Observable<Message<T>> observable;
		private final Func1<T, ?> valueFunction;

		Parameters(Logger logger, String loggerName, String onCompleteMessage,
				String subscribedMessage, String unsubscribedMessage,
				boolean logOnNext, boolean logOnError, String onErrorPrefix,
				String onErrorSuffix, String onNextPrefix, String onNextSuffix,
				Level onNextLevel, Level onErrorLevel, Level onCompletedLevel,
				Level subscribedLevel, Level unsubscribedLevel,
				CountFormat countFormat, boolean logObject,
				Func1<T, ?> valueFunction, boolean logStackTrace,
				PublishSubject<T> subject, Observable<Message<T>> observable) {
			this.logger = logger;
			this.loggerName = loggerName;
			this.onCompleteMessage = onCompleteMessage;
			this.subscribedMessage = subscribedMessage;
			this.unsubscribedMessage = unsubscribedMessage;
			this.logOnNext = logOnNext;
			this.logOnError = logOnError;
			this.onErrorPrefix = onErrorPrefix;
			this.onErrorSuffix = onErrorSuffix;
			this.onNextPrefix = onNextPrefix;
			this.onNextSuffix = onNextSuffix;
			this.onNextLevel = onNextLevel;
			this.onErrorLevel = onErrorLevel;
			this.onCompletedLevel = onCompletedLevel;
			this.subscribedLevel = subscribedLevel;
			this.unsubscribedLevel = unsubscribedLevel;
			this.countFormat = countFormat;
			this.logObject = logObject;
			this.valueFunction = valueFunction;
			this.logStackTrace = logStackTrace;
			this.subject = subject;
			this.observable = observable;
		}

		public Logger getLogger() {
			if (logger != null)
				return logger;
			else if (loggerName != null)
				return LoggerFactory.getLogger(loggerName);
			else {
				return DEFAULT_LOGGER;
			}
		}

		public String getLoggerName() {
			return loggerName;
		}

		public String getOnCompleteMessage() {
			return onCompleteMessage;
		}

		public String getSubscribedMessage() {
			return subscribedMessage;
		}

		public String getUnsubscribedMessage() {
			return unsubscribedMessage;
		}

		public boolean getLogOnNext() {
			return logOnNext;
		}

		public boolean getLogOnError() {
			return logOnError;
		}

		public String getOnErrorPrefix() {
			return onErrorPrefix;
		}

		public String getOnErrorSuffix() {
			return onErrorSuffix;
		}

		public String getOnNextPrefix() {
			return onNextPrefix;
		}

		public String getOnNextSuffix() {
			return onNextSuffix;
		}

		public Level getOnNextLevel() {
			return onNextLevel;
		}

		public Level getOnErrorLevel() {
			return onErrorLevel;
		}

		public Level getOnCompletedLevel() {
			return onCompletedLevel;
		}

		public Level getSubscribedLevel() {
			return subscribedLevel;
		}

		public Level getUnsubscribedLevel() {
			return unsubscribedLevel;
		}

		public CountFormat getCountFormat() {
			return countFormat;
		}

		public boolean getLogObject() {
			return logObject;
		}

		public boolean getLogStackTrace() {
			return logStackTrace;
		}

		public Func1<T, ?> getValueFunction() {
			return valueFunction;
		}

		public Observable<Message<T>> getObservable() {
			return observable;
		}

		public PublishSubject<T> getSubject() {
			return subject;
		}

		public static <T> Builder<T> builder() {
			return new Builder<T>();
		}

		public static class Message<T> {
			private final Notification<T> value;
			private final String message;

			public Message(Notification<T> value, String message) {
				super();
				this.value = value;
				this.message = message;
			}

			public Notification<T> value() {
				return value;
			}

			public String message() {
				return message;
			}

			public Message<T> append(String s) {
				if (message.length() > 0)
					return new Message<T>(value, message + "," + s);
				else
					return new Message<T>(value, message + s);
			}
		}

		public static class Builder<T> {

			private Logger logger;
			private String loggerName;
			private final String onCompleteMessage = "onCompleted";
			private String subscribedMessage = "onSubscribe";
			private String unsubscribedMessage = "onUnsubscribe";
			private final boolean logOnNext = true;
			private final boolean logOnError = true;
			private String onErrorPrefix = "";
			private String onErrorSuffix = "";
			private String onNextPrefix = "";
			private String onNextSuffix = "";
			private Level onNextLevel = Level.INFO;
			private Level onErrorLevel = Level.ERROR;
			private Level onCompletedLevel = Level.INFO;
			private Level subscribedLevel = Level.DEBUG;
			private Level unsubscribedLevel = Level.DEBUG;
			private CountFormat countFormat;
			private boolean logObject = false;
			private Func1<T, ?> valueFunction = Functions.<T> identity();
			private boolean logStackTrace = false;
			private final PublishSubject<T> subject = PublishSubject
					.<T> create();
			private Observable<Message<T>> observable = subject.materialize()
					.map(new Func1<Notification<T>, Message<T>>() {

						@Override
						public Message<T> call(Notification<T> n) {
							return new Message<T>(n, "");
						}
					});

			private final Action1<Message<T>> log = new Action1<Message<T>>() {

				@Override
				public void call(Message<T> m) {
					if (m.value().isOnCompleted() && onCompleteMessage != null) {
						StringBuilder s = new StringBuilder();
						s.append(onCompleteMessage);
						if (s.length() > 0)
							s.append(",");
						s.append(m.message());
						Logging.log(getLogger(), s.toString(),
								onCompletedLevel, null);
					} else if (m.value().isOnError() && logOnError) {
						StringBuilder s = new StringBuilder();
						s.append(onErrorPrefix);
						s.append(m.value().getThrowable().getMessage());
						s.append(onErrorSuffix);
						if (s.length() > 0)
							s.append(",");
						s.append(m.message());
						Logging.log(getLogger(), s.toString(), onErrorLevel, m
								.value().getThrowable());
					} else if (m.value().isOnNext() && logOnNext) {
						StringBuilder s = new StringBuilder();
						s.append(onNextPrefix);
						if (logObject)
							s.append(String.valueOf(valueFunction.call(m
									.value().getValue())));
						s.append(onNextSuffix);
						if (s.length() > 0)
							s.append(",");
						s.append(m.message());
						Logging.log(getLogger(), s.toString(), onNextLevel,
								null);
					}

				}
			};

			public Logger getLogger() {
				if (logger != null)
					return logger;
				else if (loggerName != null)
					return LoggerFactory.getLogger(loggerName);
				else {
					return DEFAULT_LOGGER;
				}
			}

			private Builder() {
			}

			public Builder<T> logger(Logger logger) {
				this.logger = logger;
				return this;
			}

			public Builder<T> name(String loggerName) {
				this.loggerName = loggerName;
				return this;
			}

			public Builder<T> source() {
				StackTraceElement[] elements = Thread.currentThread()
						.getStackTrace();
				String callingClassName = elements[elements.length - 1]
						.getClassName();
				return name(callingClassName);
			}

			public Builder<T> logger(Class<?> cls) {
				return name(cls.getName());
			}

			public Builder<T> onComplete(final String onCompleteMessage) {
				this.observable = observable
						.map(new Func1<Message<T>, Message<T>>() {

							@Override
							public Message<T> call(Message<T> m) {
								if (m.value().isOnCompleted())
									return m.append(onCompleteMessage);
								else
									return m;
							}
						});
				return this;
			}

			public Builder<T> subscribed(String subscribedMessage) {
				this.subscribedMessage = subscribedMessage;
				return this;
			}

			public Builder<T> unsubscribed(String unsubscribedMessage) {
				this.unsubscribedMessage = unsubscribedMessage;
				return this;
			}

			public Builder<T> onNext(final boolean logOnNext) {
				this.observable = observable
						.filter(new Func1<Message<T>, Boolean>() {

							@Override
							public Boolean call(Message<T> m) {
								return m.value().isOnNext() == logOnNext;
							}
						});
				return this;
			}

			public Builder<T> onError(final boolean logOnError) {
				this.observable = observable
						.filter(new Func1<Message<T>, Boolean>() {

							@Override
							public Boolean call(Message<T> m) {
								return m.value().isOnError() == logOnError;
							}
						});
				return this;
			}

			public Builder<T> onErrorPrefix(String onErrorPrefix) {
				this.onErrorPrefix = onErrorPrefix;
				return this;
			}

			public Builder<T> onErrorSuffix(String onErrorSuffix) {
				this.onErrorSuffix = onErrorSuffix;
				return this;
			}

			public Builder<T> onNextPrefix(String onNextPrefix) {
				this.onNextPrefix = onNextPrefix;
				return this;
			}

			public Builder<T> onNextSuffix(String onNextSuffix) {
				this.onNextSuffix = onNextSuffix;
				return this;
			}

			public Builder<T> onNext(Level onNextLevel) {
				this.onNextLevel = onNextLevel;
				return this;
			}

			public Builder<T> onError(Level onErrorLevel) {
				this.onErrorLevel = onErrorLevel;
				return this;
			}

			public Builder<T> onCompleted(Level onCompletedLevel) {
				this.onCompletedLevel = onCompletedLevel;
				return this;
			}

			public Builder<T> subscribed(Level subscribedLevel) {
				this.subscribedLevel = subscribedLevel;
				return this;
			}

			public Builder<T> prefix(String prefix) {
				onNextPrefix = prefix;
				onErrorPrefix = prefix;
				return this;
			}

			public Builder<T> unsubscribed(Level unsubscribedLevel) {
				this.unsubscribedLevel = unsubscribedLevel;
				return this;
			}

			public Builder<T> count() {
				observable = observable
						.map(new Func1<Message<T>, Message<T>>() {
							AtomicLong count = new AtomicLong(0);

							@Override
							public Message<T> call(Message<T> m) {
								return m.append("count="
										+ +count.incrementAndGet());
							}
						});
				return this;
			}

			public Builder<T> every(final int every) {
				if (every > 1)
					observable = observable
							.filter(new Func1<Message<T>, Boolean>() {
								AtomicLong count = new AtomicLong(0);

								@Override
								public Boolean call(Message<T> t) {
									return count.incrementAndGet() % every == 0;
								}
							});
				return this;
			}

			public Builder<T> format(CountFormat countFormat) {
				this.countFormat = countFormat;
				return this;
			}

			public Builder<T> value(boolean logValue) {
				this.logObject = logValue;
				return this;
			}

			public Builder<T> value() {
				return value(true);
			}

			public Builder<T> value(Func1<T, ?> function) {
				this.valueFunction = function;
				return this;
			}

			public Builder<T> exclude() {
				return value(false);
			}

			public Builder<T> stackTrace(boolean logStackTrace) {
				this.logStackTrace = logStackTrace;
				return this;
			}

			public Builder<T> when(final Func1<T, Boolean> when) {
				observable = observable
						.filter(new Func1<Message<T>, Boolean>() {
							@Override
							public Boolean call(Message<T> t) {
								return when.call(t.value().getValue());
							}
						});
				return this;
			}

			public Builder<T> start(final long start) {
				observable = observable
						.filter(new Func1<Message<T>, Boolean>() {
							AtomicLong count = new AtomicLong(0);

							@Override
							public Boolean call(Message<T> t) {
								return start <= count.get();
							}
						});
				return this;
			}

			public Builder<T> finish(final Long finish) {
				observable = observable
						.filter(new Func1<Message<T>, Boolean>() {
							AtomicLong count = new AtomicLong(0);

							@Override
							public Boolean call(Message<T> t) {
								return finish >= count.get();
							}
						});
				return this;
			}

			public Builder<T> sample(long period, TimeUnit timeUnit) {
				this.observable = observable.sample(period, timeUnit);
				return this;
			}

			public Builder<T> to(
					Func1<Observable<Message<T>>, Observable<Message<T>>> f) {
				this.observable = f.call(observable);
				return this;
			}

			public OperatorLogging<T> log() {
				return new OperatorLogging<T>(new Parameters<T>(logger,
						loggerName, onCompleteMessage, subscribedMessage,
						unsubscribedMessage, logOnNext, logOnError,
						onErrorPrefix, onErrorSuffix, onNextPrefix,
						onNextSuffix, onNextLevel, onErrorLevel,
						onCompletedLevel, subscribedLevel, unsubscribedLevel,
						countFormat, logObject, valueFunction, logStackTrace,
						subject, observable.doOnNext(log)));
			}

		}

	}

	public static <T> Parameters.Builder<T> logger() {
		return Parameters.<T> builder().source();
	}

	public static <T> Parameters.Builder<T> logger(String name) {
		return Parameters.<T> builder().name(name);
	}

	public static <T> Parameters.Builder<T> logger(Logger logger) {
		return Parameters.<T> builder().logger(logger);
	}

	public static <T> Parameters.Builder<T> logger(Class<?> cls) {
		return Parameters.<T> builder().logger(cls);
	}

	public static void log(Logger logger, String msg, Level level, Throwable t) {

		if (t == null) {
			if (level == Level.INFO)
				logger.info(msg);
			else if (level == Level.DEBUG)
				logger.debug(msg);
			else if (level == Level.WARN)
				logger.warn(msg);
			else if (level == Level.TRACE)
				logger.trace(msg);
			else if (level == Level.ERROR)
				logger.error(msg);
		} else {
			if (level == Level.INFO)
				logger.info(msg, t);
			else if (level == Level.DEBUG)
				logger.debug(msg, t);
			else if (level == Level.WARN)
				logger.warn(msg, t);
			else if (level == Level.TRACE)
				logger.trace(msg, t);
			else if (level == Level.ERROR)
				logger.error(msg, t);
		}
	}
}