package kexie.android.common.util;

import android.databinding.ViewDataBinding;
import android.text.TextUtils;
import android.util.LruCache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class DataBindingCompat
{
    private static final LruCache<String, Method> TABLE = new LruCache<>(32);

    private static String toFrsitUpCaseName(String variableName)
    {
        return String.valueOf(
                Character.toUpperCase(variableName.charAt(0))
                        + variableName.substring(1));
    }

    public static void setVariable(ViewDataBinding binding,
                                   String variableName,
                                   Object data)
    {
        if (TextUtils.isEmpty(variableName))
        {
            throw new IllegalArgumentException();
        }
        variableName = "set" + toFrsitUpCaseName(variableName);
        try
        {
            Method setter = TABLE.get(variableName);
            if (setter == null)
            {
                for (Method method : binding.getClass().getMethods())
                {
                    Class<?>[] parameters = method.getParameterTypes();
                    if (method.getName().equals(variableName)
                            && parameters.length == 1
                            && parameters[0].isInstance(data)
                            && void.class.equals(method.getReturnType()))
                    {
                        setter = method;
                        TABLE.put(variableName, setter);
                    }
                }
                if (setter == null)
                {
                    throw new RuntimeException(variableName + " Setter not found");
                }
            }
            setter.invoke(binding, data);
        } catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getVariable(ViewDataBinding binding,
                                    String variableName)
    {
        variableName = "get" + toFrsitUpCaseName(variableName);
        if (TextUtils.isEmpty(variableName))
        {
            throw new IllegalArgumentException();
        }
        try
        {
            Method setter = TABLE.get(variableName);
            if (setter == null)
            {
                for (Method method : binding.getClass().getMethods())
                {
                    if (method.getName().equals(variableName)
                            && method.getParameterTypes().length == 0
                            && !void.class.equals(method.getReturnType()))
                    {
                        setter = method;
                        TABLE.put(variableName, setter);
                    }
                }
                if (setter == null)
                {
                    throw new RuntimeException(variableName + " Getter not found");
                }
            }
            return (T) setter.invoke(binding);
        } catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
    }
}
