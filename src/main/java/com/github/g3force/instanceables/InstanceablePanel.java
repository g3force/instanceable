/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.instanceables;

import com.github.g3force.instanceables.InstanceableClass.NotCreateableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Panel for creating custom instances
 */
public class InstanceablePanel extends JPanel
{
    private static final long serialVersionUID = -6272636064374504265L;
    private static final Logger log = LogManager.getLogger(InstanceablePanel.class.getName());
    private static final String DEFAULT_SELECTION = "default";
    private final JComboBox<IInstanceableEnum> cbbInstances;
    private final JPanel inputPanel;
    private final List<JComponent> inputFields = new ArrayList<>();
    private JButton btnCreate = null;
    private final transient List<IInstanceableObserver> observers = new CopyOnWriteArrayList<>();

    private final Properties prop;


    public InstanceablePanel(final IInstanceableEnum[] instanceableEnums)
    {
        this(instanceableEnums, new Properties());
    }


    public InstanceablePanel(final IInstanceableEnum[] instanceableEnums, final Properties prop)
    {
        this.prop = prop;
        cbbInstances = new JComboBox<>(instanceableEnums);
        CbbInstancesActionListener cbbInstAl = new CbbInstancesActionListener();
        cbbInstances.addActionListener(cbbInstAl);
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(0, 2));

        setLayout(new BorderLayout());
        add(cbbInstances, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);

        if (instanceableEnums.length > 0)
        {
            loadDefaultValue(instanceableEnums[0]);
        }

        cbbInstAl.actionPerformed(null);
    }


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


    public void addObserver(final IInstanceableObserver observer)
    {
        synchronized (observers)
        {
            observers.add(observer);
        }
    }


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


    public final void setSelectedItem(final Enum<?> item)
    {
        cbbInstances.setSelectedItem(item);
    }


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


    private String getModelParameterKey(final IInstanceableEnum instance, final InstanceableParameter param)
    {
        return instance.getClass().getCanonicalName() + "." + instance.name() + "." + param.getDescription();
    }


    private String getModelDefaultSelectionKey(final IInstanceableEnum instance)
    {
        return instance.getClass().getCanonicalName() + "." + DEFAULT_SELECTION;
    }


    private String loadParamValue(final IInstanceableEnum instance, final InstanceableParameter param)
    {
        return prop.getProperty(getModelParameterKey(instance, param), param.getDefaultValue());
    }


    private void saveParamValue(final IInstanceableEnum instance, final InstanceableParameter param, final String value)
    {
        prop.setProperty(getModelParameterKey(instance, param), value);
    }


    private void loadDefaultValue(final IInstanceableEnum instance)
    {
        String value = prop.getProperty(getModelDefaultSelectionKey(instance));
        if (value != null)
        {
            try
            {
                IInstanceableEnum instanceableEnum = instance.parse(value);
                cbbInstances.setSelectedItem(instanceableEnum);
            } catch (IllegalArgumentException e)
            {
                log.debug("Could not parse enum value: {}", value);
            }
        }
    }


    private void saveDefaultValue(final IInstanceableEnum instance, final String value)
    {
        prop.setProperty(getModelDefaultSelectionKey(instance), value);
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
                    boolean bVal = Boolean.parseBoolean(value);
                    comp = new JCheckBox("", bVal);
                } else
                {
                    int size = value.length() + 2;
                    comp = new JTextField(value, size);
                }
                inputPanel.add(comp);
                inputFields.add(comp);
            }
            saveDefaultValue(instance, instance.name());
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


    public void createInstance()
    {
        IInstanceableEnum instanceName = (IInstanceableEnum) cbbInstances.getSelectedItem();
        int i = 0;
        List<Object> params = new ArrayList<>(instanceName.getInstanceableClass().getParams().size());
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
                    log.error("Could not parse parameter: {}", textField.getText(), err);
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
            log.error("Could not create instance: {}", instanceName, err);
        }
    }
}
