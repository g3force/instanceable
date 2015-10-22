/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;


/**
 * Panel for creating custom instances
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class InstanceablePanel extends JPanel
{
	/**  */
	private static final long							serialVersionUID	= -6272636064374504265L;
	private static final Logger						log					= Logger.getLogger(InstanceablePanel.class
																								.getName());
	private final JComboBox<IInstanceableEnum>	cbbInstances;
	private final JPanel									inputPanel;
	private final List<JComponent>					inputFields			= new ArrayList<>();
	private JButton										btnCreate			= null;
	private final List<IInstanceableObserver>		observers			= new CopyOnWriteArrayList<IInstanceableObserver>();
																						
	private final Properties							prop;
																
																
	/**
	 * @param instanceableEnums
	 */
	public InstanceablePanel(final IInstanceableEnum[] instanceableEnums)
	{
		this(instanceableEnums, new Properties());
	}
	
	
	/**
	 * @param instanceableEnums
	 * @param prop
	 */
	public InstanceablePanel(final IInstanceableEnum[] instanceableEnums, final Properties prop)
	{
		this.prop = prop;
		cbbInstances = new JComboBox<IInstanceableEnum>(instanceableEnums);
		CbbInstancesActionListener cbbInstAl = new CbbInstancesActionListener();
		cbbInstances.addActionListener(cbbInstAl);
		inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(0, 2));
		
		cbbInstAl.actionPerformed(null);
		
		setLayout(new BorderLayout());
		add(cbbInstances, BorderLayout.NORTH);
		add(inputPanel, BorderLayout.CENTER);
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @param show
	 */
	public void setShowCreate(final boolean show)
	{
		if (show && (btnCreate == null))
		{
			btnCreate = new JButton("Create");
			add(btnCreate, BorderLayout.SOUTH);
			btnCreate.addActionListener(new CreateInstanceActionListener());
		} else if (!show && (btnCreate != null))
		{
			btnCreate.removeActionListener(btnCreate.getActionListeners()[0]);
			remove(btnCreate);
			btnCreate = null;
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IInstanceableObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewInstance(final Object instance)
	{
		synchronized (observers)
		{
			for (IInstanceableObserver observer : observers)
			{
				observer.onNewInstance(instance);
			}
		}
	}
	
	
	/**
	 * @param item
	 */
	public final void setSelectedItem(final Enum<?> item)
	{
		cbbInstances.setSelectedItem(item);
	}
	
	
	/**
	 * @return
	 */
	public final IInstanceableEnum getSelectedItem()
	{
		return (IInstanceableEnum) cbbInstances.getSelectedItem();
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		cbbInstances.setEnabled(enabled);
		inputPanel.setEnabled(enabled);
		if (btnCreate != null)
		{
			btnCreate.setEnabled(enabled);
		}
	}
	
	
	private String getModelKey(final IInstanceableEnum instance, final InstanceableParameter param)
	{
		return instance.getClass().getCanonicalName() + "." + instance.name() + "." + param.getDescription();
	}
	
	
	private String loadParamValue(final IInstanceableEnum instance, final InstanceableParameter param)
	{
		return prop.getProperty(getModelKey(instance, param), param.getDefaultValue());
	}
	
	
	private void saveParamValue(final IInstanceableEnum instance, final InstanceableParameter param, final String value)
	{
		prop.setProperty(getModelKey(instance, param), value);
	}
	
	
	private class CbbInstancesActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			IInstanceableEnum instance = (IInstanceableEnum) cbbInstances.getSelectedItem();
			inputPanel.removeAll();
			inputFields.clear();
			for (InstanceableParameter param : instance.getInstanceableClass().getParams())
			{
				inputPanel.add(new JLabel(param.getDescription()));
				String value = loadParamValue(instance, param);
				JComponent comp;
				if (param.getImpl().isEnum())
				{
					JComboBox<?> cb = new JComboBox<>(param.getImpl().getEnumConstants());
					comp = cb;
					for (int i = 0; i < cb.getItemCount(); i++)
					{
						if (cb.getItemAt(i).toString().equals(value))
						{
							cb.setSelectedIndex(i);
							break;
						}
					}
				} else if (param.getImpl().equals(Boolean.class) || param.getImpl().equals(Boolean.TYPE))
				{
					Boolean bVal = Boolean.valueOf(value);
					JCheckBox cb = new JCheckBox("", bVal);
					comp = cb;
				} else
				{
					int size = value.length() + 2;
					comp = new JTextField(value, size);
				}
				inputPanel.add(comp);
				inputFields.add(comp);
			}
			updateUI();
		}
	}
	
	private class CreateInstanceActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			createInstance();
		}
	}
	
	
	/**
	 * 
	 */
	public void createInstance()
	{
		IInstanceableEnum instanceName = (IInstanceableEnum) cbbInstances.getSelectedItem();
		int i = 0;
		List<Object> params = new ArrayList<Object>(instanceName.getInstanceableClass().getParams().size());
		for (InstanceableParameter param : instanceName.getInstanceableClass().getParams())
		{
			JComponent comp = inputFields.get(i);
			if (comp.getClass().equals(JTextField.class))
			{
				JTextField textField = (JTextField) comp;
				try
				{
					Object value = param.parseString(textField.getText());
					saveParamValue(instanceName, param, textField.getText());
					params.add(value);
				} catch (NumberFormatException err)
				{
					log.error("Could not parse parameter: " + textField.getText(), err);
					return;
				}
			} else if (comp.getClass().equals(JComboBox.class))
			{
				JComboBox<?> cb = (JComboBox<?>) comp;
				params.add(cb.getSelectedItem());
				saveParamValue(instanceName, param, cb.getSelectedItem().toString());
			} else if (comp.getClass().equals(JCheckBox.class))
			{
				JCheckBox cb = (JCheckBox) comp;
				params.add(cb.isSelected());
				saveParamValue(instanceName, param, String.valueOf(cb.isSelected()));
			}
			i++;
		}
		Object instance;
		try
		{
			instance = instanceName.getInstanceableClass().newInstance(params.toArray());
			notifyNewInstance(instance);
		} catch (NotCreateableException err)
		{
			log.error("Could not create instance: " + instanceName, err);
		}
	}
}
