package com.github.g3force.instanceables;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InstanceableUtil
{
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param enumClazz
	 * @return
	 */
	public static Set<Class<?>> getAllClasses(final Class<? extends Enum<? extends IInstanceableEnum>> enumClazz)
	{
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		Enum<? extends IInstanceableEnum> values[] = enumClazz.getEnumConstants();
		IInstanceableEnum valInstancable[] = (IInstanceableEnum[]) values;
		for (IInstanceableEnum en : valInstancable)
		{
			Class<?> clazz = en.getInstanceableClass().getImpl();
			if (clazz == null)
			{
				continue;
			}
			classes.add(clazz);
			while ((clazz.getSuperclass() != null) && !clazz.getSuperclass().equals(Object.class))
			{
				clazz = clazz.getSuperclass();
				classes.add(clazz);
			}
		}
		return classes;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param classes1
	 * @param classes2
	 * @return
	 */
	public static Set<Class<?>> mergeClasses(final Set<Class<?>> classes1, final Set<Class<?>> classes2)
	{
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		classes.addAll(classes1);
		classes.addAll(classes2);
		return classes;
	}
}
