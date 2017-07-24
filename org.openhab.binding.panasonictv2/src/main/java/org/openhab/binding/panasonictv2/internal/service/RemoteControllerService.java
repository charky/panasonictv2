/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.internal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.openhab.binding.panasonictv2.internal.protocol.KeyCode;
import org.openhab.binding.panasonictv2.internal.service.api.PanasonicTV2Service;
import org.openhab.binding.panasonictv2.internal.service.api.ValueReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteControllerService} is responsible for handling remote
 * controller commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RemoteControllerService implements UpnpIOParticipant, PanasonicTV2Service {

    public static final String SERVICE_NAME = "p00RemoteController";
    private final List<String> supportedCommands = Arrays.asList(PanasonicTV2BindingConstants.CHANNEL_KEY_CODE,
            PanasonicTV2BindingConstants.CHANNEL_POWER, PanasonicTV2BindingConstants.CHANNEL_CHANNEL);

    private Logger logger = LoggerFactory.getLogger(RemoteControllerService.class);

    private UpnpIOService service;

    private String udn;

    public RemoteControllerService(UpnpIOService upnpIOService, String udn) {
        logger.debug("Create a Panasonic TV MediaRenderer service");

        if (upnpIOService != null) {
            service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }
        this.udn = udn;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<String> getSupportedChannelNames() {
        return supportedCommands;
    }

    @Override
    public void addEventListener(ValueReceiver listener) {
        // This service does not send any value updates
    }

    @Override
    public void removeEventListener(ValueReceiver listener) {
    }

    @Override
    public void start() {
        // nothing to start
    }

    @Override
    public void stop() {
    }

    @Override
    public void clearCache() {
    }

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        KeyCode key = null;

        switch (channel) {
            case PanasonicTV2BindingConstants.CHANNEL_KEY_CODE:
                if (command instanceof StringType) {

                    try {
                        key = KeyCode.valueOf(command.toString().toUpperCase());
                    } catch (Exception e) {

                        try {
                            key = KeyCode.valueOf("NRC_D" + command.toString().toUpperCase());
                        } catch (Exception e2) {
                            // do nothing, error message is logged later
                        }
                    }

                    if (key != null) {
                        sendKeyCode(key);
                    } else {
                        logger.warn("Command '{}' not supported for channel '{}'", command, channel);
                    }
                }
                break;

            case PanasonicTV2BindingConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        sendKeyCode(KeyCode.NRC_POWER);
                    } else {
                        sendKeyCode(KeyCode.NRC_POWER);
                    }
                }
                break;

            case PanasonicTV2BindingConstants.CHANNEL_CHANNEL:
                if (command instanceof DecimalType) {
                    int val = ((DecimalType) command).intValue();
                    int num4 = val / 1000 % 10;
                    int num3 = val / 100 % 10;
                    int num2 = val / 10 % 10;
                    int num1 = val % 10;

                    List<KeyCode> commands = new ArrayList<KeyCode>();

                    if (num4 > 0) {
                        commands.add(KeyCode.valueOf("NRC_D" + num4));
                    }
                    if (num4 > 0 || num3 > 0) {
                        commands.add(KeyCode.valueOf("NRC_D" + num3));
                    }
                    if (num4 > 0 || num3 > 0 || num2 > 0) {
                        commands.add(KeyCode.valueOf("NRC_D" + num2));
                    }
                    commands.add(KeyCode.valueOf("NRC_D" + num1));
                    commands.add(KeyCode.NRC_ENTER);
                    sendKeys(commands);
                }
                break;
        }
    }

    @Override
    public String getUDN() {
        return udn;
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        logger.trace("ValueUpdate '{}' for {} received.", value, variable);
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("onStatusChanged");
    }

    /**
     * Sends a command to Panasonic TV device.
     *
     * @param key Button code to send
     */
    private void sendKeyCode(final KeyCode key) {
        updateResourceState("p00NetworkControl", "X_SendKey",
                PanasonicTVUtils.buildHashMap("X_KeyEvent", key.toString()));
    }

    protected Map<String, String> updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {

        Map<String, String> result = service.invokeAction(this, serviceId, actionId, inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), serviceId);
        }

        return result;
    }

    /**
     * Sends a sequence of command to Panasonic TV device.
     *
     * @param keys List of button codes to send
     */
    private void sendKeys(List<KeyCode> keys) {
        logger.debug("Try to send sequnce of commands: {}", keys);

        for (int i = 0; i < keys.size(); i++) {
            KeyCode key = keys.get(i);
            sendKeyCode(key);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
        }

        logger.debug("Command(s) successfully sent");
    }

}
