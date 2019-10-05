/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

/**
 * Implement this in an enum that provides {@link InstanceableClass}s
 */
public interface IInstanceableEnum
{
	InstanceableClass getInstanceableClass();


	String name();
}
