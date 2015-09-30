/***************************************************************************************************
*
*	@(#)ReflectionUtils.java
*
***************************************************************************************************/
package org.rdm.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
*	Provides utility functions that deal with class reflection attributes.
*/
public class ReflectionUtils
{
	/**
	*
	*/
	public static class MethodMatcher
	{
		/**************************************************************************************************/
		public MethodMatcher(Class class_i)
		{
			myClass = class_i;

			obtainAvailableMethods();
		}


		/**************************************************************************************************/
		public Method	findBestMethodMatch(String methodName_i, Class[] methodValueTypes_i) throws NoSuchMethodException
		{
			// look for our list of methods
			List methodList = (List)myMethodToParameterListsMap.get(methodName_i);
			if (methodList == null)
			{
				throw new NoSuchMethodException(myClass.getName() + " has no method " + MethodMatcher.getMethodInvocationKey(methodName_i, methodValueTypes_i));
			}

			return (Method)findMemberInList(methodName_i,methodList,methodValueTypes_i);
		}


		private final Class		myClass;
		private final Map		myMethodToParameterListsMap = new HashMap();	// method name (String) ==> List()
		private final Map		myMethodToParameterTypesMap = new HashMap();	// Method ==> Class[]



		/**************************************************************************************************/
		private static Class[]	getParameterTypesFrom(Object[] __values_i)
		{
			if (__values_i == null)
			{
				return new Class[0];
			}

			Class[] parameterTypes = new Class[__values_i.length];

			for (int i = 0; i < __values_i.length; ++i)
			{
				parameterTypes[i] = (__values_i[i] != null)
				                  ? __values_i[i].getClass()
								  : Void.TYPE;
//				// TODO: better handling of primitive types
//				// TODO: option to return super types? e.g. object is detected as Inet4Address, but the method wants InetAddress
//				try
//				{
//					// replacement for primitive types, e.g. int.class instead of Integer.class
//					parameterTypes[i] = (Class) parameterTypes[i].getField("TYPE").get(null);
//				}
//				catch (Exception ignored)
//				{
//				}
			}

			return parameterTypes;
		}


		/**************************************************************************************************/
		private static String	getMethodInvocationKey(String methodName_i, Object[] __methodParameterValues_i)
		{
			return MethodMatcher.getMethodInvocationKey(
					methodName_i,
					MethodMatcher.getParameterTypesFrom(__methodParameterValues_i)
			);
		}



		/**************************************************************************************************/
		private static String	getMethodInvocationKey(String methodName_i, Class[] methodValueTypes_i)
		{
			return methodName_i + ArrayUtils.toString(methodValueTypes_i, "(", ",", ")");
		}



		/**************************************************************************************************/
		public static Method getAccessibleMethodFrom(Class class_i, String methodName_i, Class[] methodParameterTypes_i)
		{
			Method overriddenMethod = null;


			// Look for overridden method in the superclass.
			Class superclass = class_i.getSuperclass();
			if (
				(superclass != null)							&&
				ReflectionUtils.classIsAccessible(superclass)
			   )
			{
				try
				{
					overriddenMethod = superclass.getMethod(methodName_i, methodParameterTypes_i);
					if (overriddenMethod != null)
					{
						return overriddenMethod;
					}
				}
				catch (NoSuchMethodException exception_i)
				{
				}
			}

			// If here, then class_i represents Object, or an interface, or
			// the superclass did not have an override.  Check
			// implemented interfaces.
			Class[] interfaces = class_i.getInterfaces();
			for (int i = 0; i < interfaces.length; ++i)
			{
				if (classIsAccessible(interfaces[i]))
				{
					try
					{
						overriddenMethod = interfaces[i].getMethod(methodName_i, methodParameterTypes_i);
						if (overriddenMethod != null)
						{
							return overriddenMethod;
						}
					}
					catch (NoSuchMethodException exception_i)
					{
					}
				}
			}

			// Try superclass's superclass and implemented interfaces.
			if (superclass != null)
			{
				overriddenMethod = getAccessibleMethodFrom(superclass, methodName_i, methodParameterTypes_i);
				if (overriddenMethod != null)
				{
					return overriddenMethod;
				}
			}

			// Try implemented interfaces' extended interfaces...
			for (int i = 0; i < interfaces.length; ++i)
			{
				overriddenMethod = getAccessibleMethodFrom(interfaces[i], methodName_i, methodParameterTypes_i);
				if (overriddenMethod != null)
				{
					return overriddenMethod;
				}
			}

			return null;
		}


		/**************************************************************************************************/
		private void	obtainAvailableMethods()
		{
			Method[] methods = myClass.getMethods();
			for (int i = 0; i < methods.length; ++i)
			{
				Method method = methods[i];

				String methodName = method.getName();
				Class[] parameterTypes = method.getParameterTypes();

				List list = (List)myMethodToParameterListsMap.get(methodName);
				if (list == null)
				{
					list = new ArrayList();
					myMethodToParameterListsMap.put(methodName,list);
				}

				if (!ReflectionUtils.classIsAccessible(myClass))
				{
					method = getAccessibleMethodFrom(myClass,methodName,parameterTypes);
				}

				if (method != null)
				{
					list.add(method);
					myMethodToParameterTypesMap.put(method,parameterTypes);
				}
				
			}
		}


		/**************************************************************************************************/
		private Member	findMemberInList(String methodName_i, List memberList_i, Class[] methodValueTypes_i) throws NoSuchMethodException
		{
			List matchingMembers = new ArrayList();

			Iterator iterator = memberList_i.iterator();
			while (iterator.hasNext())
			{
				Member member = (Member)iterator.next();

				Class[] methodParameterTypes = (Class[])myMethodToParameterTypesMap.get(member);
				if (Arrays.equals(methodParameterTypes,methodValueTypes_i))
				{
					return member;
				}

				if (ReflectionUtils.valuesAreCompatibleWithTypes(methodValueTypes_i,methodParameterTypes))
				{
					matchingMembers.add(member);
				}
			}

			switch (matchingMembers.size())
			{
				case 0:
						throw new NoSuchMethodException(myClass.getName() + " has no method " + MethodMatcher.getMethodInvocationKey(methodName_i, methodValueTypes_i));

				case 1:
						return (Member)matchingMembers.get(0);

				default:
						return findMostSpecificMemberIn(matchingMembers);
			}

		}



		/**************************************************************************************************/
		private Member findMostSpecificMemberIn(List memberList) throws NoSuchMethodException
		{
			List mostSpecificMembers = new ArrayList();

			for (Iterator memberIt = memberList.iterator(); memberIt.hasNext();)
			{
				Member member = (Member) memberIt.next();

				if (mostSpecificMembers.isEmpty())
				{
					// First guy in is the most specific so far.
					mostSpecificMembers.add(member);
				}
				else
				{
					boolean moreSpecific = true;
					boolean lessSpecific = false;

					// Is member more specific than everyone in the most-specific set?
					for (Iterator specificIt = mostSpecificMembers.iterator(); specificIt.hasNext();)
					{
						Member moreSpecificMember = (Member) specificIt.next();

						if (! memberIsMoreSpecific(member, moreSpecificMember))
						{
							moreSpecific = false;
							lessSpecific = memberIsMoreSpecific(moreSpecificMember, member);
							break;
						}
					}

					if (moreSpecific)
					{
						// Member is the most specific now.
						mostSpecificMembers.clear();
						mostSpecificMembers.add(member);
					}
					else if (!lessSpecific)
					{
						// Add to ambiguity set if mutually unspecific.
						mostSpecificMembers.add(member);
					}
				}
			}

			if (mostSpecificMembers.size() > 1)
			{
				throw new NoSuchMethodException("Ambiguous request for member in " + myClass.getName() + " matching given args" ); 
			}

			return (Member)mostSpecificMembers.get(0);
		}



		/**************************************************************************************************/
		private boolean memberIsMoreSpecific(Member first, Member second)
		{
			Class[] firstParamTypes  = (Class[])myMethodToParameterTypesMap.get(first);
			Class[] secondParamTypes = (Class[])myMethodToParameterTypesMap.get(second);

			return ReflectionUtils.valuesAreCompatibleWithTypes(firstParamTypes,secondParamTypes);
		}
	}





	/***************************************************************************************************
	*
	*	Obtain the {@link Class} for a given primitive type.
	*
	*		@param	primitiveName_i			The name of a primitive type.
	*
	*		@return	primitive type's {@link Class}.  A name_i parameter of <CODE>null</CODE>, an
	*				empty string, "null" and "void" will all yield {@link Void#TYPE}.
	*
	*		@throws	ClassNotFoundException if the <CODE>name_i</CODE> parameter is an unknown
	*				primitive.
	*
	***************************************************************************************************/
	public static Class	classForPrimative(String primitiveName_i) throws ClassNotFoundException
	{
		Class primitiveClass = (Class)ourPrimitiveTypeNameToClassMap.get(primitiveName_i);
		if (primitiveClass == null)
		{
			throw new ClassNotFoundException(primitiveName_i);
		}
		return primitiveClass;
	}



	/***************************************************************************************************
	*
	*	Obtain the {@link Class} for a given type.
	*
	*		@param	typeName_i				The name of a class or a primitive type.
	*
	*		@param	classLoader_i			The {@link ClassLoader} to use to aid in the search
	*										for the class type.
	*
	*		@return	primitive type's {@link Class}.  A name_i parameter of <CODE>null</CODE>, an
	*				empty string, "null" and "void" will all yield {@link Void#TYPE}.
	*
	*		@throws	ClassNotFoundException if the <CODE>name_i</CODE> parameter is an unknown
	*				primitive type and unknown class.
	*
	***************************************************************************************************/
	public static Class	classForTypeName(String typeName_i, ClassLoader classLoader_i) throws ClassNotFoundException
	{
		Class primitiveClass = (Class)ourPrimitiveTypeNameToClassMap.get(typeName_i);
		if (primitiveClass == null)
		{
			primitiveClass = Class.forName(typeName_i,false,classLoader_i);
		}
		return primitiveClass;
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Class	getPrimitiveEquivalentOf(Class class_i)
	{
		if (class_i.isPrimitive())
		{
			return class_i;
		}

		return (Class)ourObjectToPrimitiveMap.get(class_i);
	}



	/***************************************************************************************************
	*
	*	Determine if a primitive value's class is assignable to a primitive type's class.
	*
	***************************************************************************************************/
	public static boolean	primitiveIsAssignableFrom(Class parameterClass_i, Class valueClass_i)
	{
		if ((parameterClass_i == null) || (valueClass_i == null))
		{
			return false;
		}

		if (!parameterClass_i.isPrimitive() || !valueClass_i.isPrimitive())
		{
			return false;
		}

		if (parameterClass_i.equals(valueClass_i))
		{
			return true;
		}

		Set wideningSet = (Set)ourPrimitiveWideningsMap.get(valueClass_i);
		if (wideningSet == null)
		{
			return false;
		}

		return wideningSet.contains(parameterClass_i);
	}



	/***************************************************************************************************
	*
	*	Determine if a class is accessible via reflective mechanisms.
	*
	*		@param	class_i						The class to check for accessibility.
	*
	*		@return	the <CODE>true</CODE> if the class is accessible via reflection; otherwise,
	*				<CODE>false</CODE>.
	*
	***************************************************************************************************/
	public static boolean	classIsAccessible(Class class_i)
	{
		return Modifier.isPublic(class_i.getModifiers());
	}




	/***************************************************************************************************
	*
	*	Determine if a given object implements a particular interface or not.
	*
	*		@param	object_i			The object to inspect for implementation of an interface.
	*
	*		@param	interfaceClass_i	The class of the interface we are interested in.
	*
	*		@return	<CODE>true</CODE> if the instance of the given object implements the given
	*				interface class; <CODE>false</CODE> otherwise.
	*
	***************************************************************************************************/
	public static boolean	implementsInterface(Object object_i, Class interfaceClass_i)
	{
		return ReflectionUtils.implementsInterface(object_i.getClass(),interfaceClass_i);
	}



	/***************************************************************************************************
	*
	*	Determine if a given class implements a particular interface or not.
	*
	*		@param	class_i				The class to inspect for implementation of an interface.
	*
	*		@param	interfaceClass_i	The class of the interface we are interested in.
	*
	*		@return	<CODE>true</CODE> if the instance of the given object implements the given
	*				interface class; <CODE>false</CODE> otherwise.
	*
	***************************************************************************************************/
	public static boolean	implementsInterface(Class class_i, Class interfaceClass_i)
	{
		for (Class currentClass = class_i; currentClass != null; currentClass = currentClass.getSuperclass())
		{
			Class[] objectInterfaces = currentClass.getInterfaces();

			final int interfaceCount = objectInterfaces.length;
			for (int i = 0; i < interfaceCount; ++i)
			{
				Class interfaceClass = objectInterfaces[i];
				if (interfaceClass.equals(interfaceClass_i))
				{
					return true;
				}
			}
		}

		return false;
	}



	/***************************************************************************************************
	*
	*	Determine if a class implements a specific collection of interfaces or not.
	*
	*		@param	class_i						The class to check for interface implementation.
	*
	*		@param	desiredInterfaceClasses_i	The collection of interfaces that are needed.
	*
	*		@return	the <CODE>true</CODE> if all interfaces are implemented; otherwise,
	*				<CODE>false</CODE>.
	*
	***************************************************************************************************/
	public static boolean	implementsInterfaces(Class class_i, Class[] desiredInterfaceClasses_i)
	{
		Set neededInterfaces = new HashSet(desiredInterfaceClasses_i.length);
		ArrayUtils.populateCollection(neededInterfaces,desiredInterfaceClasses_i);

		for (
		     Class currentClass = class_i;
			 !neededInterfaces.isEmpty() && (currentClass != null);
			 currentClass = currentClass.getSuperclass()
			)
		{
			if (ReflectionUtils.implementsInterfaces(currentClass,neededInterfaces))
			{
				return true;
			}
		}

		return false;
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	instantiateClass(Class class_i, Object[] parameters_i) throws NoSuchMethodException,
	                                                                                      SecurityException,
	                                                                                      IllegalAccessException,
	                                                                                      InvocationTargetException,
																						  InstantiationException
	{
		// do we want to make this a more fuzzy match?...
		Class[] parameterTypes = ReflectionUtils.getParameterTypes(parameters_i);
//		System.out.println("*********** parameter types are " + ArrayUtils.toString(parameterTypes,","));
		return instantiateClass(class_i, parameters_i, parameterTypes);
	}


	/***************************************************************************************************
	 *
	 *
	 ***************************************************************************************************/
	public static Object	instantiateClass(Class class_i, Object[] parameters_i, Class[] parameterTypes_i) throws NoSuchMethodException,
																						  SecurityException,
																						  IllegalAccessException,
																						  InvocationTargetException,
																						  InstantiationException
	{
		Constructor constructor = class_i.getConstructor(parameterTypes_i);
		// System.out.println("*********** constructor " + constructor);

		Object object = constructor.newInstance(parameters_i);
		return object;
	}

	/***************************************************************************************************
	*
	*	Determine if a collection of classes are compatible with a collection of parameter types.
	*
	*		@param	actualValueTypes_i				The classes of the actual parameter values.
	*
	*		@param	methodParameterTypes_i			The classes of the signature's parameter types.
	*
	*		@return	the <CODE>true</CODE> if the <CODE>actualValueTypes_i</CODE> all properly
	*				match the <CODE>methodParameterTypes_i</CODE>; otherwise, <CODE>false</CODE>.
	*
	***************************************************************************************************/
	public static boolean	valuesAreCompatibleWithTypes(Class[] actualValueTypes_i, Class[] methodParameterTypes_i)
	{
		if (methodParameterTypes_i.length != actualValueTypes_i.length)
		{
			return false;
		}

		for (int i = 0; i < methodParameterTypes_i.length; ++i)
		{
			if ((actualValueTypes_i[i] == null) || actualValueTypes_i[i].equals(Void.TYPE))
			{
				if (methodParameterTypes_i[i].isPrimitive())
				{
					return false;
				}
				continue;
			}

			if (!methodParameterTypes_i[i].isAssignableFrom(actualValueTypes_i[i]))
			{
				Class methodParameterTypePrimitiveEquivalent = getPrimitiveEquivalentOf(methodParameterTypes_i[i]);
				Class actualValueTypePrimitiveEquivalent     = getPrimitiveEquivalentOf(actualValueTypes_i[i]);

				if (!primitiveIsAssignableFrom(methodParameterTypePrimitiveEquivalent,actualValueTypePrimitiveEquivalent))
				{
					return false;
				}
			}
		}

		return true;
	}




	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Method	geMethodThatBestMatches(Object object_i, String methodName_i, Class[] methodParameterTypes_i) throws NoSuchMethodException,
																												    InvocationTargetException,
																												    IllegalAccessException
	{
		return ReflectionUtils.geMethodThatBestMatches(object_i.getClass(),methodName_i,methodParameterTypes_i);
	}




	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Method	geMethodThatBestMatches(Class class_i, String methodName_i, Class[] methodParameterTypes_i) throws NoSuchMethodException,
																												    InvocationTargetException,
																												    IllegalAccessException
	{
		// determine if we have already found a matching method
		Method method;
		String key = class_i + ">" + methodName_i + ":" + ArrayUtils.toString(methodParameterTypes_i,",");
		synchronized (ReflectionUtils.class)
		{
			if (ourMethodCache == null)
			{
				ourMethodCache = new LRUCache(100);
			}

			method = (Method)ourMethodCache.get(key);
		}
		if (method == null)
		{
			// look for a matching method
			MethodMatcher methodMatcher = new MethodMatcher(class_i);
			method = methodMatcher.findBestMethodMatch(methodName_i,methodParameterTypes_i);
			if (method != null)
			{
				synchronized (ReflectionUtils.class)
				{
					ourMethodCache.put(key,method);
				}
			}
		}

		// return the method
		return method;
	}




	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeMethod(Object object_i, String methodName_i) throws NoSuchMethodException,
	                                                                                  InvocationTargetException,
																					  IllegalAccessException
	{
		return ReflectionUtils.invokeMethod(object_i,methodName_i,null);
	}



	/***************************************************************************************************
	 *
	 *
	 ***************************************************************************************************/
	public static Object	invokeMethodWithoutThrowing(Object object_i, String methodName_i)
	{
		return ReflectionUtils.invokeMethodWithoutThrowing(object_i,methodName_i,null);
	}
	



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeMethod(Object object_i, String methodName_i, Object methodParameter_i) throws NoSuchMethodException,
	                                                                                                            InvocationTargetException,
																												IllegalAccessException
	{
		Object[] methodParameters = { methodParameter_i };
		return ReflectionUtils.invokeMethod(object_i,methodName_i,methodParameters);
	}



	/***************************************************************************************************
	 *
	 *
	 ***************************************************************************************************/
	public static Object	invokeMethodWithoutThrowing(Object object_i, String methodName_i, Object methodParameter_i)
	{
		Object[] methodParameters = { methodParameter_i };
		return ReflectionUtils.invokeMethodWithoutThrowing(object_i,methodName_i,methodParameters);
	}
	
	


	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeMethod(Object object_i, String methodName_i, Object[] methodParameters_i) throws NoSuchMethodException,
	                                                                                                               InvocationTargetException,
																												   IllegalAccessException
	{
		return ReflectionUtils.invokeClassMethod(
		                                         object_i.getClass(),
												 object_i,
												 methodName_i,
												 methodParameters_i
												);
	}

	/***************************************************************************************************
	 *
	 *
	 ***************************************************************************************************/
	public static Object	invokeMethodWithoutThrowing(Object object_i, String methodName_i, Object[] methodParameters_i)
	{
		Object result = null;
		try
		{
			result = ReflectionUtils.invokeClassMethod(
													 object_i.getClass(),
													 object_i,
													 methodName_i,
													 methodParameters_i
													 );
		}
		catch (Throwable exception_i)
		{
//			exception_i.printStackTrace();
		}
		
		return result;
	}
	



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeStaticMethod(Class class_i, String methodName_i) throws NoSuchMethodException,
	                                                                                      InvocationTargetException,
																						  IllegalAccessException
	{
		return ReflectionUtils.invokeStaticMethod(class_i,methodName_i,new Object[0]);
	}




	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeStaticMethod(Class class_i, String methodName_i, Object... methodParameters_i) throws NoSuchMethodException,
	                                                                                                                   InvocationTargetException,
																													   IllegalAccessException
	{
		return ReflectionUtils.invokeClassMethod(
		                                         class_i,
												 null,
												 methodName_i,
												 methodParameters_i
												);
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeStaticMethodWithoutThrowing(Class class_i, String methodName_i)
	{
		try
		{
			return ReflectionUtils.invokeStaticMethod(class_i,methodName_i);
		}
		catch (Throwable exception_i)
		{
			return null;
		}
	}



	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Object	invokeStaticMethodWithoutThrowing(Class class_i, String methodName_i, Object[] methodParameters_i)
	{
		try
		{
			return ReflectionUtils.invokeStaticMethod(class_i,methodName_i,methodParameters_i);
		}
		catch (Throwable exception_i)
		{
			return null;
		}
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//	IMPLEMENTATION DETAILS -- NO EXTERNAL REFERENCE TO THE FOLLOWING INFORMATION SHOULD BE MADE.
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	*
	*/
	private static final Map	ourPrimitiveTypeNameToClassMap = new HashMap(17);
	{
		ourPrimitiveTypeNameToClassMap.put(null,Void.TYPE);
		ourPrimitiveTypeNameToClassMap.put("",Void.TYPE);
		ourPrimitiveTypeNameToClassMap.put("null",Void.TYPE);
		ourPrimitiveTypeNameToClassMap.put("void",Void.TYPE);
		ourPrimitiveTypeNameToClassMap.put("boolean",Boolean.TYPE);
		ourPrimitiveTypeNameToClassMap.put("byte",Byte.TYPE);
		ourPrimitiveTypeNameToClassMap.put("char",Character.TYPE);
		ourPrimitiveTypeNameToClassMap.put("short",Short.TYPE);
		ourPrimitiveTypeNameToClassMap.put("int",Integer.TYPE);
		ourPrimitiveTypeNameToClassMap.put("long",Long.TYPE);
		ourPrimitiveTypeNameToClassMap.put("float",Float.TYPE);
		ourPrimitiveTypeNameToClassMap.put("double",Double.TYPE);
	}



	/**
	*	A map of the primitive wrapper classes to their corresponding primitive type classes.
	*/
	private static final Map	ourObjectToPrimitiveMap = new HashMap(13);
	static
	{
		ourObjectToPrimitiveMap.put(Boolean.class,Boolean.TYPE);
		ourObjectToPrimitiveMap.put(Byte.class,Byte.TYPE);
		ourObjectToPrimitiveMap.put(Character.class,Character.TYPE);
		ourObjectToPrimitiveMap.put(Short.class,Short.TYPE);
		ourObjectToPrimitiveMap.put(Integer.class,Integer.TYPE);
		ourObjectToPrimitiveMap.put(Long.class,Long.TYPE);
		ourObjectToPrimitiveMap.put(Float.class,Float.TYPE);
		ourObjectToPrimitiveMap.put(Double.class,Double.TYPE);
	}


	/**
	*	A map of the primitive wrapper classes to their corresponding primitive type classes.
	*/
	private static final Map	ourPrimitiveWideningsMap = new HashMap(11);
	static
	{
		Set set = new HashSet(5);
		set.add(Short.TYPE);
		set.add(Integer.TYPE);
		set.add(Long.TYPE);
		set.add(Float.TYPE);
		set.add(Double.TYPE);
		ourPrimitiveWideningsMap.put(Byte.TYPE,set);

		set = new HashSet(4);
		set.add(Integer.TYPE);
		set.add(Long.TYPE);
		set.add(Float.TYPE);
		set.add(Double.TYPE);
		ourPrimitiveWideningsMap.put(Character.TYPE,set);
		ourPrimitiveWideningsMap.put(Short.TYPE,set);

		set = new HashSet(3);
		set.add(Long.TYPE);
		set.add(Float.TYPE);
		set.add(Double.TYPE);
		ourPrimitiveWideningsMap.put(Integer.TYPE,set);

		set = new HashSet(2);
		set.add(Float.TYPE);
		set.add(Double.TYPE);
		ourPrimitiveWideningsMap.put(Long.TYPE,set);

		set = new HashSet(1);
		set.add(Double.TYPE);
		ourPrimitiveWideningsMap.put(Float.TYPE,set);
	}


	private static LRUCache		ourMethodCache;



	/***************************************************************************************************/
	private ReflectionUtils()
	{
		/*** NOTHING TO DO HERE ***/
	}




	/***************************************************************************************************/
	private static boolean	implementsInterfaces(Class class_i, Collection neededInterfacesCollection_io)
	{
		final Class[] classInterfaces = class_i.getInterfaces();

		Iterator interfaceIterator = neededInterfacesCollection_io.iterator();
		while (interfaceIterator.hasNext())
		{
			final Class neededInterfaceClass = (Class)interfaceIterator.next();

			for (int i = 0; i < classInterfaces.length; ++i)
			{
				if (classInterfaces[i].equals(neededInterfaceClass))
				{
					interfaceIterator.remove();
					break;
				}
			}
		}

		return neededInterfacesCollection_io.isEmpty();
	}





	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	public static Class[] getParameterTypes(Object[] __parameters_i)
	{
		return MethodMatcher.getParameterTypesFrom(__parameters_i);
	}




	/***************************************************************************************************
	*
	*
	***************************************************************************************************/
	private static Object	invokeClassMethod(Class class_i, Object __object_i, String methodName_i, Object[] __methodParameters_i) throws NoSuchMethodException,
	                                                                                                                                     InvocationTargetException,
																																		 IllegalAccessException
	{
		//
		if (__methodParameters_i == null)
		{
			__methodParameters_i = new Object[0];
		}

		// create the class array for the method parameter types
		Class[] methodParameterTypes = ReflectionUtils.getParameterTypes(__methodParameters_i);

		// look for a matching method
		Method method = ReflectionUtils.geMethodThatBestMatches(class_i,methodName_i,methodParameterTypes);

		// invoke the method
		Object result = method.invoke(__object_i,__methodParameters_i);

		// return the result
		return result;
	}




	/****************************************************************************************************/
	public static void	main(String[] arguments_i)
	{
		try
		{
			boolean result = ReflectionUtils.implementsInterface(Class.forName(arguments_i[0]),Class.forName(arguments_i[1]));
			System.out.println(result ? "yes" : "no");
		}
		catch (ClassNotFoundException exception_i)
		{
			exception_i.printStackTrace();
		}
	}

}