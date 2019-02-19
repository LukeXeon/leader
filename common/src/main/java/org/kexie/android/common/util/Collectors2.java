package org.kexie.android.common.util;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.databinding.ObservableArrayList;
import java8.util.function.Function;
import java8.util.stream.Collector;
import java8.util.stream.Collectors;

public final class Collectors2
{
    private Collectors2()
    {
        throw new AssertionError();
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>>
    toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                    Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper,
                valueMapper,
                (t1, t2) -> t2,
                LinkedHashMap::new);
    }

    public static <T, U> Collector<T, ?, Map<String, U>>
    toIdentityHashMap(Function<? super T, String> keyMapper,
                      Function<? super T, ? extends U> valueMapper)
    {
        return Collectors.toMap(keyMapper,
                valueMapper,
                (t1, t2) -> t2,
                IdentityHashMap::new);
    }

    public static <T>
    Collector<T, ?, List<T>> toObservableList()
    {
        return Collectors.toCollection(ObservableArrayList::new);
    }
}
