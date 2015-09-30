package org.rdm.util;

import java.lang.reflect.Field;
import java.net.InetAddress;

// inspired by http://stackoverflow.com/a/10309323/47078
public abstract class ReflectionObject
{
    private static final String TAG = ReflectionObject.class.getSimpleName();

    protected Object mObject;
    protected Class mClass;

    public ReflectionObject(Object object, String className) throws ClassNotFoundException
    {
        mClass = Class.forName(className);

        if (!mClass.isInstance(object))
        {
            throw new IllegalArgumentException("object must be of type " + className);
        }
        mObject = object;
    }

    public ReflectionObject(String className, Object... args) throws ClassNotFoundException
    {
        this(newInstance(className, args), className);
    }

    public ReflectionObject(String className, Class[] types,
                            Object... args) throws ClassNotFoundException
    {
        this(newInstance(className, types, args), className);
    }

    @Override
    public String toString()
    {
        return invokeStringMethod("toString");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ReflectionObject))
        {
            return false;
        }

        ReflectionObject that = (ReflectionObject) o;

        if (mObject != null ? !mObject.equals(that.mObject) : that.mObject != null)
        {
            return false;
        }
        return !(mClass != null ? !mClass.equals(that.mClass) : that.mClass != null);

    }

    @Override
    public int hashCode()
    {
        int result = mObject != null ? mObject.hashCode() : 0;
        result = 31 * result + (mClass != null ? mClass.hashCode() : 0);
        return result;
    }

    public Object getObject()
    {
        return mObject;
    }

    public static Object newInstance(String className, Object... args)
    {
        Class[] types = ReflectionUtils.getParameterTypes(args);
        return newInstance(className, types, args);
    }

    public static Object newInstance(String className, Class[] types, Object... args)
    {
        try
        {
            return ReflectionUtils.instantiateClass(Class.forName(className), args, types);
        }
        catch (Exception e)
        {
            System.out.println(TAG + "\n" + StackTraceUtils.getStackTrace(e));
        }
        return null;
    }

    public String invokeStringMethod(String methodName, Object... args)
    {
        return (String) invokeObjectMethod(methodName, args);
    }

    public boolean invokeBooleanMethod(String methodName, Object... args)
    {
        return (Boolean) invokeObjectMethod(methodName, args);
    }

    public int invokeIntMethod(String methodName, Object... args)
    {
        return (Integer) invokeObjectMethod(methodName, args);
    }

    public long invokeLongMethod(String methodName, Object... args)
    {
        return (Long) invokeObjectMethod(methodName, args);
    }


    public InetAddress invokeInetAddressMethod(String methodName, Object... args)
    {
        return (InetAddress) invokeObjectMethod(methodName, args);
    }

    public void invokeVoidMethod(String methodName, Object... args)
    {
        invokeObjectMethod(methodName, args);
    }

    public Object invokeObjectMethod(String methodName, Object... args)
    {
        return ReflectionUtils.invokeMethodWithoutThrowing(mObject, methodName, args);
    }

    public static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getField(obj.getClass(), obj, name);
    }

    public static Object getField(Class klass, Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return klass.getField(name).get(obj);
    }

    public static Object getStaticField(Class klass, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        return getField(klass, null, name);
    }


    public static Object getFieldWithoutThrowing(Object obj, String name)
    {
        return getFieldWithoutThrowing(obj.getClass(), obj, name);
    }

    public static Object getFieldWithoutThrowing(Class klass, Object obj, String name)
    {
        try
        {
            return getField(klass, obj, name);
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    public static Object getStaticFieldWithoutThrowing(Class klass, String name)
    {
        return getFieldWithoutThrowing(klass, null, name);
    }

    public static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
                   IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    public static String getEnumField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        Enum e = (Enum) f.get(obj);
        return e.name();
    }

    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static int getIntField(Object obj,
                                  String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        return (Integer) f.get(obj);
    }
}
