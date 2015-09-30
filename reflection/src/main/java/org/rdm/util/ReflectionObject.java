package org.rdm.util;

import java.lang.reflect.Field;
import java.net.InetAddress;

// inspired by http://stackoverflow.com/a/10309323/47078
@SuppressWarnings("unused")
public abstract class ReflectionObject
{
    // Override this in base classes:
    public String className() { return null; }

    /*************************************************************************/

    private static final String TAG = ReflectionObject.class.getSimpleName();

    protected final Object mObject;
    protected final Class mClass;

    // Pass in an existing object...
    public ReflectionObject(Object object)
    {
        mClass = getClassType();
        //noinspection ConstantConditions
        if (!mClass.isInstance(object))
        {
            throw new IllegalArgumentException("object must be of type " + mClass.getCanonicalName());
        }
        mObject = object;
    }

    // Create a new object with the default constructor
    public ReflectionObject()
    {
        mClass = getClassType();
        mObject = newInstance(mClass);
    }

    // Create a new object using a constructor matching the types of args
    public ReflectionObject(Object... args)
    {
        mClass = getClassType();
        mObject = newInstance(mClass, args);
    }

    // Create a new object using a constructor matching types
    public ReflectionObject(Class[] types, Object... args)
    {
        mClass = getClassType();
        mObject = newInstance(mClass, types, args);
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

        //noinspection SimplifiableIfStatement
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

    private Class getClassType()
    {
        try
        {
            return Class.forName(this.className());
        }
        catch (ClassNotFoundException ignored)
        {
        }
        return null;
    }

    public static Object newInstance(Class class_, Object... args)
    {
        Class[] types = ReflectionUtils.getParameterTypes(args);
        return newInstance(class_, types, args);
    }

    public static Object newInstance(Class class_, Class[] types, Object... args)
    {
        try
        {
            return ReflectionUtils.instantiateClass(class_, args, types);
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
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassCastException
    {
        Field f = obj.getClass().getField(name);
        //noinspection unchecked
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }

    public static int getIntField(Object obj,
                                  String name) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field f = obj.getClass().getField(name);
        return (Integer) f.get(obj);
    }
}
