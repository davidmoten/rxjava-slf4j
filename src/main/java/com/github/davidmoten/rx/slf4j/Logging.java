package com.github.davidmoten.rx.slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Notification;
import rx.Observable;
import rx.Observable.Operator;
import rx.functions.Action1;
import rx.functions.Func1;

public class Logging {

	private static final Logger DEFAULT_LOGGER = LoggerFactory
			.getILoggerFactory().getLogger(Logging.class.getName());

	public enum Level {
		INFO, WARN, DEBUG, TRACE, ERROR;
	}

	public static class Parameters<T> {

		private final Logger logger;
		private final String subscribedMessage;
		private final String unsubscribedMessage;
		private final Level subscribedLevel;
		private final Level unsubscribedLevel;
		private final List<Func1<Observable<Message<T>>, Observable<Message<T>>>> transformations;

		private Parameters(
				Logger logger,
				String subscribedMessage,
				String unsubscribedMessage,
				Level subscribedLevel,
				Level unsubscribedLevel,
				List<Func1<Observable<Message<T>>, Observable<Message<T>>>> transformations) {
			this.logger = logger;
			this.subscribedMessage = subscribedMessage;
			this.unsubscribedMessage = unsubscribedMessage;
			this.subscribedLevel = subscribedLevel;
			this.unsubscribedLevel = unsubscribedLevel;
			this.transformations = transformations;
		}

		public Logger getLogger() {
			return logger;
		}

		public Level getSubscribedLevel() {
			return subscribedLevel;
		}

		public String getSubscribedMessage() {
			return subscribedMessage;
		}

		public String getUnsubscribedMessage() {
			return unsubscribedMessage;
		}

		public Level getUnsubscribedLevel() {
			return unsubscribedLevel;
		}

		public List<Func1<Observable<Message<T>>, Observable<Message<T>>>> getTransformations() {
			return transformations;
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
					return new Message<T>(value, message + ", " + s);
				else
					return new Message<T>(value, message + s);
			}
		}

		public static class Builder<T> {

			private Logger logger;
			private String loggerName;
			private String onCompleteMessage = "onCompleted";
			private String subscribedMessage = "onSubscribe";
			private String unsubscribedMessage = "onUnsubscribe";
			private final boolean logOnNext = true;
			private final boolean logOnError = true;
			private String onErrorFormat = "";
			private String onNextFormat = "";
			private Level onNextLevel = Level.INFO;
			private Level onErrorLevel = Level.ERROR;
			private Level onCompletedLevel = Level.INFO;
			private Level subscribedLevel = Level.DEBUG;
			private Level unsubscribedLevel = Level.DEBUG;
			private Func1<? super T, ?> valueFunction = new Func1<T, T>() {
				@Override
				public T call(T t) {
					return t;
				}
			};
			private boolean logStackTrace = false;
			private boolean logMemory = false;
			// private final PublishSubject<T> subject = PublishSubject
			// .<T> create();
			// private Observable<Message<T>> observable = subject.materialize()
			// .map(new Func1<Notification<T>, Message<T>>() {
			//
			// @Override
			// public Message<T> call(Notification<T> n) {
			// return new Message<T>(n, "");
			// }
			// });

			private final List<Func1<Observable<Message<T>>, Observable<Message<T>>>> transformations = new ArrayList<Func1<Observable<Message<T>>, Observable<Message<T>>>>();

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

			/**
			 * Sets the SLF4J {@link Logger} to be used to do the logging.
			 * 
			 * @param logger
			 * @return
			 */
			public Builder<T> logger(Logger logger) {
				this.logger = logger;
				return this;
			}

			/**
			 * Sets the name of the {@link Logger} to used to do the logging. Is
			 * used as in {@link LoggerFactory#getLogger(String)}.
			 * 
			 * @param loggerName
			 * @return
			 */
			public Builder<T> name(String loggerName) {
				this.loggerName = loggerName;
				return this;
			}

			/**
			 * Sets the cls to be used to create a {@link Logger} to do the
			 * logging. It is used as in {@link LoggerFactory#getLogger(Class)}.
			 * 
			 * @param cls
			 * @return
			 */
			public Builder<T> logger(Class<?> cls) {
				return name(cls.getName());
			}

			/**
			 * The message to be logged on stream completion.
			 * 
			 * @param onCompleteMessage
			 * @return
			 */
			public Builder<T> onCompleted(final String onCompleteMessage) {
				this.onCompleteMessage = onCompleteMessage;
				return this;
			}

			/**
			 * The message to be logged when subscribed.
			 * 
			 * @param subscribedMessage
			 * @return
			 */
			public Builder<T> subscribed(String subscribedMessage) {
				this.subscribedMessage = subscribedMessage;
				return this;
			}

			/**
			 * The message to be logged when unsubscribed.
			 * 
			 * @param unsubscribedMessage
			 * @return
			 */
			public Builder<T> unsubscribed(String unsubscribedMessage) {
				this.unsubscribedMessage = unsubscribedMessage;
				return this;
			}

			/**
			 * If and only if <tt>logOnNext</tt> is true requests that
			 * <i>onNext</i> values are logged.
			 * 
			 * @param logOnNext
			 * @return
			 */
			public Builder<T> onNext(final boolean logOnNext) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.filter(new Func1<Message<T>, Boolean>() {

											@Override
											public Boolean call(Message<T> m) {
												return m.value().isOnNext() == logOnNext;
											}
										});

							}
						});
				return this;
			}

			/**
			 * If and only if <tt>logOnError</tt> is true requests that
			 * <i>onError</i> notifications are logged.
			 * 
			 * @param logOnError
			 * @return
			 */
			public Builder<T> onError(final boolean logOnError) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.filter(new Func1<Message<T>, Boolean>() {

											@Override
											public Boolean call(Message<T> m) {
												return m.value().isOnError() == logOnError;
											}
										});

							}
						});
				return this;
			}

			/**
			 * Requests that errors are to be prefixed with value of
			 * <tt>onErrorPrefix</tt>.
			 * 
			 * @param onErrorPrefix
			 * @return
			 */
			public Builder<T> onErrorPrefix(String onErrorPrefix) {
				this.onErrorFormat = onErrorPrefix + "%s";
				return this;
			}

			/**
			 * Requests that errors are to be formatted using
			 * <tt>onErrorFormat</tt> (as in
			 * {@link String#format(String, Object...)}.
			 * 
			 * @param onErrorFormat
			 * @return
			 */
			public Builder<T> onErrorFormat(String onErrorFormat) {
				this.onErrorFormat = onErrorFormat;
				return this;
			}

			public Builder<T> onNextPrefix(String onNextPrefix) {
				this.onNextFormat = onNextPrefix + "%s";
				return this;
			}

			public Builder<T> onNextFormat(String onNextFormat) {
				this.onNextFormat = onNextFormat;
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
				onNextPrefix(prefix);
				return onErrorPrefix(prefix);
			}

			public Builder<T> unsubscribed(Level unsubscribedLevel) {
				this.unsubscribedLevel = unsubscribedLevel;
				return this;
			}

			public Builder<T> showCount(final String label) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.map(new Func1<Message<T>, Message<T>>() {
											AtomicLong count = new AtomicLong(0);

											@Override
											public Message<T> call(Message<T> m) {
												long val;
												if (m.value().isOnNext())
													val = count
															.incrementAndGet();
												else
													val = count.get();

												return m.append(label + "="
														+ val);
											}
										});
							}
						});
				return this;
			}

			public Builder<T> showRateSince(final String label,
					final long sinceMs) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.map(new Func1<Message<T>, Message<T>>() {
											AtomicLong count = new AtomicLong(0);
											volatile long lastTime = 0;
											volatile long lastNum = 0;
											volatile double rate = 0;

											@Override
											public Message<T> call(Message<T> m) {
												long t = System
														.currentTimeMillis();
												long num;
												if (m.value().isOnNext()) {
													num = count
															.incrementAndGet();
												} else
													num = count.get();
												long diffMs = t - lastTime;
												if (diffMs >= sinceMs) {
													rate = ((num - lastNum) * 1000.0 / diffMs);
													lastTime = t;
													lastNum = num;
												}
												return m.append(label + "="
														+ rate);
											}
										});
							}
						});
				return this;
			}

			public Builder<T> showRateSinceStart(final String label) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.map(new Func1<Message<T>, Message<T>>() {
											AtomicLong count = new AtomicLong(0);
											volatile long startTime = 0;
											volatile double rate = 0;

											@Override
											public Message<T> call(Message<T> m) {
												long t = System
														.currentTimeMillis();
												if (startTime == 0)
													startTime = t;
												long num;
												if (m.value().isOnNext())
													num = count
															.incrementAndGet();
												else
													num = count.get();

												long diffMs = t - startTime;
												if (diffMs > 0) {
													rate = num * 1000.0
															/ diffMs;
												}
												return m.append(label + "="
														+ rate);
											}
										});
							}
						});
				return this;
			}

			public Builder<T> showCount() {
				return showCount("count");
			}

			public Builder<T> every(final int every) {
				if (every > 1) {
					transformations
							.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

								@Override
								public Observable<Message<T>> call(
										Observable<Message<T>> observable) {
									return observable
											.filter(new Func1<Message<T>, Boolean>() {
												AtomicLong count = new AtomicLong(
														0);

												@Override
												public Boolean call(Message<T> t) {
													if (t.value().isOnNext())
														return count
																.incrementAndGet()
																% every == 0;
													else
														return true;
												}
											});
								}
							});
				}
				return this;
			}

			public Builder<T> showValue(boolean logValue) {
				if (logValue)
					return showValue();
				else
					return excludeValue();
			}

			public Builder<T> showValue() {
				if (onNextFormat.length() == 0)
					onNextFormat = "%s";
				return this;
			}

			public Builder<T> value(Func1<? super T, ?> function) {
				this.valueFunction = function;
				return this;
			}

			public Builder<T> excludeValue() {
				onNextFormat = "";
				return this;
			}

			public Builder<T> showStackTrace() {
				this.logStackTrace = true;
				return this;
			}

			public Builder<T> when(final Func1<? super T, Boolean> when) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.filter(new Func1<Message<T>, Boolean>() {
											@Override
											public Boolean call(Message<T> t) {
												if (t.value().isOnNext())
													return when.call(t.value()
															.getValue());
												else
													return true;
											}
										});
							}
						});
				return this;
			}

			public Builder<T> start(final long start) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.filter(new Func1<Message<T>, Boolean>() {
											AtomicLong count = new AtomicLong(0);

											@Override
											public Boolean call(Message<T> t) {
												if (t.value().isOnNext())
													return start <= count
															.incrementAndGet();
												else
													return true;
											}
										});
							}
						});
				return this;
			}

			public Builder<T> finish(final long finish) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable
										.filter(new Func1<Message<T>, Boolean>() {
											AtomicLong count = new AtomicLong(0);

											@Override
											public Boolean call(Message<T> t) {
												if (t.value().isOnNext())
													return finish >= count
															.incrementAndGet();
												else
													return true;
											}
										});
							}
						});
				return this;
			}

			public Builder<T> to(
					final Func1<Observable<? super Message<T>>, Observable<Message<T>>> f) {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return f.call(observable);
							}
						});
				return this;
			}

			public Builder<T> showMemory() {
				logMemory = true;
				return this;
			}

			public OperatorLogging<T> log() {
				transformations
						.add(new Func1<Observable<Message<T>>, Observable<Message<T>>>() {

							@Override
							public Observable<Message<T>> call(
									Observable<Message<T>> observable) {
								return observable.doOnNext(log);
							}
						});
				return new OperatorLogging<T>(new Parameters<T>(getLogger(),
						subscribedMessage, unsubscribedMessage,
						subscribedLevel, unsubscribedLevel, transformations));
			}

			private Builder<T> source() {
				StackTraceElement[] elements = Thread.currentThread()
						.getStackTrace();
				String callingClassName = elements[4].getClassName();
				return name(callingClassName);
			}

			private final Action1<Message<T>> log = new Action1<Message<T>>() {

				@Override
				public void call(Message<T> m) {

					if (m.value().isOnCompleted() && onCompleteMessage != null) {
						StringBuilder s = new StringBuilder();
						addDelimited(s, onCompleteMessage);
						addDelimited(s, m.message());
						addMemory(s);
						Logging.log(getLogger(), s.toString(),
								onCompletedLevel, null);
					} else if (m.value().isOnError() && logOnError) {
						StringBuilder s = new StringBuilder();
						addDelimited(
								s,
								String.format(onErrorFormat, m.value()
										.getThrowable().getMessage()));
						addDelimited(s, m.message());
						addMemory(s);
						Logging.log(getLogger(), s.toString(), onErrorLevel, m
								.value().getThrowable());
					} else if (m.value().isOnNext() && logOnNext) {
						StringBuilder s = new StringBuilder();
						if (onNextFormat.length() > 0)
							s.append(String.format(onNextFormat, String
									.valueOf(valueFunction.call(m.value()
											.getValue()))));
						addDelimited(s, m.message());
						addMemory(s);
						addStackTrace(s);
						Logging.log(getLogger(), s.toString(), onNextLevel,
								null);
					}
				}

				private void addStackTrace(StringBuilder s) {
					if (logStackTrace) {
						for (StackTraceElement elem : Thread.currentThread()
								.getStackTrace()) {
							s.append("\n    ");
							s.append(elem);
						}
					}
				}

				private void addMemory(StringBuilder s) {
					if (logMemory)
						addDelimited(s, memoryUsage());
				}

			};
		}
	}

	private static void delimiter(StringBuilder s) {
		if (s.length() > 0)
			s.append(", ");
	}

	private static void addDelimited(StringBuilder b, String s) {
		if (s.length() > 0) {
			delimiter(b);
			b.append(s);
		}
	}

	private static String memoryUsage() {
		StringBuilder s = new StringBuilder();
		Runtime r = Runtime.getRuntime();
		long mem = r.totalMemory() - r.freeMemory();
		s.append("usedMem=");
		s.append(new DecimalFormat("0").format(mem / 1000000.0));
		s.append("MB, percentMax=");
		s.append(new DecimalFormat("0.0").format((double) mem / r.maxMemory()
				* 100));
		s.append(", max=");
		s.append(new DecimalFormat("0").format(r.maxMemory() / 1000000.0));
		s.append("MB");
		return s.toString();
	}

	/**
	 * Returns an {@link Operator} that logs every <i>onNext</i> value using a
	 * {@link Logger} based on the current class. Is equivalent to
	 * <tt>logger().showValue().log()</tt>.
	 * 
	 * @return operator that logs every <i>onNext</i> value.
	 */
	public static <T> OperatorLogging<T> log() {
		return Logging.<T> logger().showValue().log();
	}

	/**
	 * Returns an {@link Operator} that logs every <i>onNext</i> value using a
	 * {@link Logger} based on the current class. Is equivalent to
	 * <tt>logger().showValue().log()</tt>.
	 * 
	 * @return operator that logs every <i>onNext</i> value.
	 */
	public static <T> OperatorLogging<T> log(String onNextFormat) {
		return Logging.<T> logger().onNextFormat(onNextFormat).log();
	}

	/**
	 * Returns a builder for which the {@link Logger} is based on the current
	 * class (using {@link LoggerFactory#getLogger(Class)}.
	 * 
	 * @return builder
	 */
	public static <T> Parameters.Builder<T> logger() {
		return Parameters.<T> builder().source();
	}

	/**
	 * Returns a builder for which the {@link Logger} is based on the given name
	 * (using {@link LoggerFactory#getLogger(String)}.
	 * 
	 * @param name
	 *            logger name
	 * @return builder
	 */
	public static <T> Parameters.Builder<T> logger(String name) {
		return Parameters.<T> builder().name(name);
	}

	/**
	 * Returns a builder using the supplied {@link Logger}.
	 * 
	 * @param logger
	 * @return builder
	 */
	public static <T> Parameters.Builder<T> logger(Logger logger) {
		return Parameters.<T> builder().logger(logger);
	}

	/**
	 * Returns a builder for which the {@link Logger} is based on the given
	 * class (using {@link LoggerFactory#getLogger(Class)}.
	 * 
	 * @param cls
	 *            class to base the logger on
	 * @return builder
	 */
	public static <T> Parameters.Builder<T> logger(Class<?> cls) {
		return Parameters.<T> builder().logger(cls);
	}

	static void log(Logger logger, String msg, Level level, Throwable t) {

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
