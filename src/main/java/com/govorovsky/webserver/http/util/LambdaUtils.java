package com.govorovsky.webserver.http.util;

import java.lang.reflect.Array;
import java.util.function.BiConsumer;

/**
 * Created by Andrew Govorovsky on 08.09.14
 */
public final class LambdaUtils {

   public interface BiConsumerWithException<T , U> {
        public void accept(T t, U u) throws Exception;
    }

    public static <T, U> BiConsumer<T, U> wrap(BiConsumerWithException<T, U> consumer) {
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
