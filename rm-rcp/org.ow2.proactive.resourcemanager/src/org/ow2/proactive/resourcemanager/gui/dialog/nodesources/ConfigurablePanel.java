/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.dialog.nodesources;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.ow2.proactive.resourcemanager.Activator;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.resourcemanager.gui.dialog.CreateCredentialDialog;
import org.ow2.proactive.resourcemanager.gui.dialog.CreateSourceDialog;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;
import org.ow2.proactive.resourcemanager.nodesource.common.ConfigurableField;
import org.ow2.proactive.resourcemanager.nodesource.common.PluginDescriptor;


public class ConfigurablePanel extends Group {

    class Property extends Composite {

        private Label nameLabel;
        private Text text;
        private Label descriptionLabel;

        private String name;
        private Object value;

        public Property(Composite parent, ConfigurableField configurableField) {
            super(parent, SWT.LEFT);

            name = configurableField.getName();
            Configurable configurable = configurableField.getMeta();
            String description = configurable.description();

            setLayout(new FormLayout());

            nameLabel = new Label(this, SWT.LEFT);
            nameLabel.setText(PluginDescriptor.beautifyName(name));

            int passwdMask = configurableField.getMeta().password() ? SWT.PASSWORD : 0;
            text = new Text(this, SWT.LEFT | SWT.BORDER | passwdMask);
            text.setText(configurableField.getValue());

            FormData fd = new FormData();
            fd.top = new FormAttachment(1, 5);
            fd.left = new FormAttachment(1, 5);
            fd.width = 140;
            nameLabel.setLayoutData(fd);

            fd = new FormData();
            fd.left = new FormAttachment(nameLabel, 5);
            fd.width = 200;
            text.setLayoutData(fd);

            if (configurableField.getMeta().fileBrowser() || configurableField.getMeta().credential()) {
                Button chooseButton = new Button(this, SWT.NONE);
                chooseButton.setText("Choose file");
                chooseButton.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        FileDialog fileDialog = new FileDialog(ConfigurablePanel.this.parent.getShell(),
                            SWT.OPEN);
                        String fileName = fileDialog.open();
                        if (fileName != null)
                            text.setText(fileName);
                    }
                });

                FormData chooseFormData = new FormData();
                chooseFormData.left = new FormAttachment(text, 5);
                chooseButton.setLayoutData(chooseFormData);

                if (configurableField.getMeta().credential()) {
                    Button createCredentialButton = new Button(this, SWT.NONE);
                    createCredentialButton.setText("Create");
                    createCredentialButton.addListener(SWT.Selection, new Listener() {
                        public void handleEvent(Event event) {
                            CreateCredentialDialog dialog = new CreateCredentialDialog(
                                ConfigurablePanel.this.parent.getShell(), null);
                            value = dialog.getCredentials();
                            if (value != null)
                                text.setText("<generated>");
                        }
                    });

                    FormData createCredentialFormData = new FormData();
                    createCredentialFormData.left = new FormAttachment(chooseButton, 5);
                    createCredentialButton.setLayoutData(createCredentialFormData);
                }

            } else if (description != null && description.length() > 0) {
                descriptionLabel = new Label(this, SWT.LEFT);
                descriptionLabel.setText(description);

                fd = new FormData();
                fd.top = new FormAttachment(1, 5);
                fd.left = new FormAttachment(text, 5);
                descriptionLabel.setLayoutData(fd);
            }

            pack();
        }

        public String getPropertyName() {
            return name;
        }

        public Object getValue() {
            return value == null ? text.getText() : value;
        }

        public void setValue(String value) {
            this.value = null;
            text.setText(value);
        }
    }

    private Combo combo;
    private List<Property> properties = new LinkedList<Property>();
    private Button loadFromFile = null;
    private Button saveToFile = null;
    private Label description;
    private PluginDescriptor selectedDescriptor = null;
    private HashMap<String, PluginDescriptor> comboStates = new HashMap<String, PluginDescriptor>();
    private Composite parent;
    private int selectedComboIndex = -1;

    public ConfigurablePanel(final Composite parent, String labelText, final CreateSourceDialog dialog) {
        super(parent, SWT.NONE);

        this.parent = parent;
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        this.setLayout(layout);

        setText(labelText);
        Label typeLabel = new Label(this, SWT.NONE);
        typeLabel.setText("Type : ");

        combo = new Combo(this, SWT.READ_ONLY);
        description = new Label(this, SWT.NONE);

        FormData fd = new FormData();
        fd.top = new FormAttachment(1, 3);
        typeLabel.setLayoutData(fd);

        fd = new FormData();
        fd.left = new FormAttachment(typeLabel, 5);
        fd.width = 200;
        combo.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(typeLabel, 10);
        description.setLayoutData(fd);

        combo.add("");
        comboStates.put("", null);

        combo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                if (combo.getSelectionIndex() == selectedComboIndex) {
                    return;
                } else {
                    selectedComboIndex = combo.getSelectionIndex();
                }

                for (Property l : properties) {
                    l.dispose();
                }

                if (loadFromFile != null) {
                    loadFromFile.dispose();
                    loadFromFile = null;
                }
                if (saveToFile != null) {
                    saveToFile.dispose();
                    saveToFile = null;
                }
                properties.clear();

                generateGui(comboStates.get(combo.getText()));
                parent.pack();

                dialog.repack();
            }
        });
    }

    public void addComboValue(PluginDescriptor descriptor) {
        String pluginName = PluginDescriptor.beautifyName(descriptor.getPluginName());
        combo.add(pluginName);
        comboStates.put(pluginName, descriptor);
    }

    private void generateGui(PluginDescriptor descriptor) {
        selectedDescriptor = descriptor;
        if (descriptor == null) {
            description.setText("");
            return;
        }

        for (ConfigurableField configurableField : descriptor.getConfigurableFields()) {
            Property property = new Property(this, configurableField);
            Control lowest = properties.size() > 0 ? properties.get(properties.size() - 1) : this.description;
            FormData fd = new FormData();
            fd.top = new FormAttachment(lowest, 10);
            property.setLayoutData(fd);

            properties.add(property);
        }

        if (properties.size() > 0) {
            // adding 'load properties from file' button
            loadFromFile = new Button(this, SWT.NONE);
            loadFromFile.setText("Load from file");
            // adding 'save properties to file' button
            saveToFile = new Button(this, SWT.NONE);
            saveToFile.setText("Save to file");

            loadFromFile.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                    String fileName = fileDialog.open();
                    if (fileName != null) {
                        Properties loadedProperties = new Properties();
                        try {
                            loadedProperties.load(new FileInputStream(fileName));

                            for (Property prop : properties) {
                                String value = loadedProperties.getProperty(prop.getPropertyName());
                                if (value == null) {
                                    value = loadedProperties.getProperty(PluginDescriptor.beautifyName(prop
                                            .getPropertyName()));
                                }

                                if (value != null) {
                                    prop.setValue(value);
                                }
                            }

                        } catch (Exception e) {
                            manageException(e, fileName);
                        }

                    }
                }
            });
            saveToFile.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
                    String fileName = fileDialog.open();
                    if (fileName != null) {
                        Properties saveProperties = new Properties();
                        try {

                            FileOutputStream fos = new FileOutputStream(fileName);

                            for (Property prop : properties) {
                                String propName = prop.getPropertyName();
                                if (propName == null) {
                                    propName = PluginDescriptor.beautifyName(prop.getPropertyName());
                                }
                                String value = prop.getValue().toString();
                                if (propName != null) {
                                    saveProperties.setProperty(propName, value);

                                }
                            }
                            saveProperties.store(fos, null);
                            fos.close();
                        } catch (Exception e) {
                            manageException(e, fileName);
                        }
                    }
                }
            });

            FormData fd = new FormData();
            fd.top = new FormAttachment(properties.get(properties.size() - 1), 10);
            loadFromFile.setLayoutData(fd);

            fd = new FormData();
            System.getProperty("pa.scheduler.serverURL");
            fd.left = new FormAttachment(loadFromFile, 0);
            fd.top = new FormAttachment(properties.get(properties.size() - 1), 10);
            saveToFile.setLayoutData(fd);
        }

        if (descriptor.getPluginDescription() != null) {
            description.setText(descriptor.getPluginDescription());
            description.pack();
        }

    }

    @Override
    protected void checkSubclass() {
    }

    public Object[] getParameters() throws RMException {
        List<Object> params = new ArrayList<Object>();

        for (Property p : properties) {
            params.add(p.getValue());
        }

        if (selectedDescriptor == null) {
            throw new RMException("Incorrect plugin selection");
        }
        return selectedDescriptor.packParameters(params.toArray());
    }

    public PluginDescriptor getSelectedPlugin() {
        return selectedDescriptor;
    }

    private void manageException(Exception e, String fileName) {
        Activator.log(IStatus.ERROR, "Cannot read file " + fileName, e);
        MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot read file",
                "Cannot read file " + fileName + "\n\n" + e.getMessage());
    }

}