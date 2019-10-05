/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * An {@link InstanceableClass} can be used to create an object from a class and a set of parameters.
 */
public class InstanceableClass
{
	private final Class<?> impl;
	private final List<InstanceableParameter> params;


	public InstanceableClass(final Class<?> impl, final InstanceableParameter... params)
	{
		this.impl = impl;
		this.params = Arrays.asList(params);
	}


	/**
	 * Create a new instance with the specified arguments
	 *
	 * @param args
	 * @return
	 * @throws NotCreateableException
	 */
	public Object newInstance(final Object... args) throws NotCreateableException
	{
		Object result = null;
		try
		{
			Constructor<?> con = getConstructor();
			result = con.newInstance(args);
		} catch (NoSuchMethodException err)
		{
			throw new NotCreateableException("Wrong constructor types.", err);
		} catch (final Exception err)
		{
			throw new NotCreateableException("Can not create instance", err);
		}
		return result;
	}


	/**
	 * Create a new instance with default parameters (as defined in enum)
	 *
	 * @return
	 * @throws NotCreateableException
	 */
	public Object newDefaultInstance() throws NotCreateableException
	{
		if (getParams().isEmpty())
		{
			return newInstance();
		}
		List<Object> objParams = new ArrayList<>(getParams().size());
		for (InstanceableParameter param : getParams())
		{
			Object objParam = param.parseString(param.getDefaultValue());
			objParams.add(objParam);
		}
		return newInstance(objParams.toArray());
	}


	/**
	 * Add parameter
	 *
	 * @param param
	 */
	public void addParam(final InstanceableParameter param)
	{
		params.add(param);
	}


	/**
	 * Returns the first public constructor of the Play.
	 */
	public Constructor<?> getConstructor() throws NoSuchMethodException
	{
		Class<?>[] paramTypes = new Class<?>[params.size()];
		for (int i = 0; i < params.size(); i++)
		{
			paramTypes[i] = params.get(i).getImpl();
		}
		return impl.getConstructor(paramTypes);
	}


	/**
	 * @return the params
	 */
	public final List<InstanceableParameter> getParams()
	{
		return params;
	}


	public static class NotCreateableException extends Exception
	{
		private static final long serialVersionUID = 89775383135278930L;


		public NotCreateableException(final String message, final Throwable cause)
		{
			super(message, cause);
		}
	}


	/**
	 * @return the impl
	 */
	public final Class<?> getImpl()
	{
		return impl;
	}
}
