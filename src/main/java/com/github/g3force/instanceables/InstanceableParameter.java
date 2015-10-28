/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import java.util.Arrays;
import java.util.List;

import com.github.g3force.s2vconverter.String2ValueConverter;


/**
 * Parameter of a {@link InstanceableClass}.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InstanceableParameter
{
	private static String2ValueConverter	s2vConv	= String2ValueConverter.getDefault();
																	
	private final Class<?>						impl;
	private final String							description;
	private final String							defaultValue;
	private final List<Class<?>>				genericsImpls;
														
														
	/**
	 * @param impl
	 * @param description
	 * @param defaultValue
	 * @param genericsImpls
	 */
	public InstanceableParameter(final Class<?> impl, final String description, final String defaultValue,
			final Class<?>... genericsImpls)
	{
		this.impl = impl;
		this.description = description;
		this.defaultValue = defaultValue;
		this.genericsImpls = Arrays.asList(genericsImpls);
	}
	
	
	/**
	 * Parse given String to value
	 * 
	 * @param value
	 * @return
	 */
	public Object parseString(final String value)
	{
		if (genericsImpls.isEmpty())
		{
			return s2vConv.parseString(impl, value);
		}
		return s2vConv.parseString(impl, genericsImpls, value);
	}
	
	
	/**
	 * @return the impl
	 */
	public final Class<?> getImpl()
	{
		return impl;
	}
	
	
	/**
	 * @return the description
	 */
	public final String getDescription()
	{
		return description;
	}
	
	
	/**
	 * @return the defaultValue
	 */
	public final String getDefaultValue()
	{
		return defaultValue;
	}
	
	
	/**
	 * @return the genericsImpls
	 */
	protected List<Class<?>> getGenericsImpls()
	{
		return genericsImpls;
	}
}
