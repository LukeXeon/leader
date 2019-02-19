package org.kexie.android.common.util;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java8.util.function.Function;
import java8.util.stream.Collector;

public final class Collectors
{
    private Collectors()
    {
        throw new AssertionError();
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>>
    toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                    Function<? super T, ? extends U> valueMapper)
    {
        return java8.util.stream.Collectors.toMap(keyMapper,
                valueMapper,
                (t1, t2) -> t2,
                LinkedHashMap::new);
    }

    public static <T, U> Collector<T, ?, Map<String, U>>
    toIdentityHashMap(Function<? super T, String> keyMapper,
                      Function<? super T, ? extends U> valueMapper)
    {
        return java8.util.stream.Collectors.toMap(keyMapper,
                valueMapper,
                (t1, t2) -> t2,
                IdentityHashMap::new);
    }
}
