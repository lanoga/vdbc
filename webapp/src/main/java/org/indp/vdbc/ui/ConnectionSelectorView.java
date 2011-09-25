package org.indp.vdbc.ui;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import java.util.List;
import org.indp.vdbc.ConnectionListener;
import org.indp.vdbc.DatabaseSessionManager;
import org.indp.vdbc.SettingsManager;
import org.indp.vdbc.model.config.ConnectionProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pi
 */
public class ConnectionSelectorView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionSelectorView.class);
    private final DatabaseSessionManager databaseSessionManager;
    private final ConnectionListener connectionListener;

    public ConnectionSelectorView(DatabaseSessionManager databaseSessionManager, ConnectionListener connectionListener) {
        this.databaseSessionManager = databaseSessionManager;
        this.connectionListener = connectionListener;
    }

    @Override
    public void attach() {
        setSizeFull();
//        vl.setSpacing(true);
//        vl.setMargin(true);

        Panel panel = new Panel("DB Console");
        panel.setWidth(300, UNITS_PIXELS);
        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);

        FormLayout l = new FormLayout();
        panel.addComponent(l);
        l.setSizeFull();
        l.setSpacing(true);
//        l.setMargin(false);
        l.setMargin(false, false, true, false);

        final LabelField driver = new LabelField();
        final LabelField url = new LabelField();
        final TextField userName = new TextField();
        final TextField password = new TextField();
        password.setSecret(true);

        List<ConnectionProfile> profilesList = new SettingsManager().getConfiguration().getProfiles();
        final ComboBox profiles = new ComboBox(null, profilesList);
        profiles.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                ConnectionProfile cp = (ConnectionProfile) profiles.getValue();
                driver.setValue(cp.getDriver());
                url.setValue(cp.getUrl());
                url.setDescription(cp.getUrl());
                userName.setValue(cp.getUser());
            }
        });

        profiles.setImmediate(true);
        profiles.setNewItemsAllowed(false);
        profiles.setNullSelectionAllowed(false);

        if (!profilesList.isEmpty())
            profiles.select(profilesList.get(0));

        addToForm(l, "Profile:", profiles);
        addToForm(l, "Driver:", driver);
        addToForm(l, "URL:", url);
        addToForm(l, "Username:", userName);
        addToForm(l, "Password:", password);

        Button connectButton = new Button("Connect", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                ConnectionProfile connectionProfile = new ConnectionProfile(null,
                        driver.getValue().toString(), url.getValue().toString(),
                        userName.getValue().toString(), password.getValue().toString());
                try {
                    databaseSessionManager.connect(connectionProfile);
                    connectionListener.connectionEstablished(connectionProfile);
                } catch (Exception ex) {
                    getApplication().getMainWindow().showNotification("Failed to connect<br/>", ex.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                }
            }
        });

        Button testButton = new Button("Test", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    databaseSessionManager.test(driver.getValue().toString(), url.getValue().toString(), userName.getValue().toString(), password.getValue().toString());
                    getApplication().getMainWindow().showNotification("Test successful");
                } catch (Exception ex) {
                    LOG.warn("connection test failed", ex);
                    getApplication().getMainWindow().showNotification("Test failed<br/>", ex.getMessage(), Notification.TYPE_ERROR_MESSAGE);
                }
            }
        });

        Button settingsButton = new Button("Settings...", new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                getApplication().getMainWindow().showNotification("Not implemented yet.");
            }
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(100, UNITS_PERCENTAGE);
        panel.addComponent(hl);
        hl.addComponent(settingsButton);
        hl.addComponent(testButton);
        hl.addComponent(connectButton);
        hl.setComponentAlignment(testButton, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(connectButton, Alignment.MIDDLE_RIGHT);

        userName.focus();

        super.attach();
    }

    private void addToForm(FormLayout grid, String title, Component component) {
//        Label label = new Label(title);
        component.setWidth("100%");
//        component.setWidth(200, UNITS_PIXELS);
//        grid.addComponent(label);
        component.setCaption(title);
        grid.addComponent(component);
//        grid.setComponentAlignment(label, Alignment.MIDDLE_RIGHT);
    }
}
