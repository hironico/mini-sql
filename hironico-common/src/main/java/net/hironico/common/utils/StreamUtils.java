package net.hironico.common.utils;

import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * JAVA 8+ happy developpers can use these super usefull streams utils to get their life easier.
 */
public class StreamUtils {

    /**
     * This collector assumes there is one and only one element in the stream and collect it.
     * If there is more than one element in the stream then illegal state exception is thrown.
     * If the stream is empty then returns null.
     * Use the returned collector as a parameter in the {@link java.util.stream.Stream#collect(Collector)}
     * Source : https://stackoverflow.com/questions/22694884/filter-java-stream-to-1-and-only-1-element/50514439
     * @param <T> the single element class in this stream
     * @return the instance of the single element in the stream.
     */
    public static <T> Collector<T, ?, T> singletonCollector() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() > 1) {
                        if (LangUtils.isAllNull(list)) {
                            return null;
                        }
                        throw new IllegalStateException();
                    }
                    return list.isEmpty() ? null : list.get(0);
                }
        );
    }

    /**
     * Unckec any checked exception that could be thrown in a stream call.
     * See https://stackoverflow.com/questions/19757300/java-8-lambda-streams-filter-by-method-with-exception
     * DO NOT USE WITHOUT ALERTING EVERY SINGLE PROGRAMMER IN THE POJECT OF THIS CODE !
     * @param callable
     * @param <T>
     * @return
     */
    public static <T> T uncheckCall(Callable<T> callable) {
        try {
            return callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class EnumerationSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private final Enumeration<T> enumeration;

        public EnumerationSpliterator(long est, int additionalCharacteristics, Enumeration<T> enumeration) {
            super(est, additionalCharacteristics);
            this.enumeration = enumeration;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (enumeration.hasMoreElements()) {
                action.accept(enumeration.nextElement());
                return true;
            }
            return false;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            while (enumeration.hasMoreElements())
                action.accept(enumeration.nextElement());
        }
    }

    /**
     * Convert an Enumeration into Stream using Spliterator
     * @param enumeration to convert into Stream
     * @return a Stream containing the enumeration's elements of type T
     * @param <T> the type of the enumeration elements
     */
    public static <T> Stream<T> stream(Enumeration<T> enumeration) {
        EnumerationSpliterator<T> spliterator = new EnumerationSpliterator<T>(Long.MAX_VALUE, Spliterator.ORDERED, enumeration);
        return StreamSupport.stream(spliterator, false);
    }
}
