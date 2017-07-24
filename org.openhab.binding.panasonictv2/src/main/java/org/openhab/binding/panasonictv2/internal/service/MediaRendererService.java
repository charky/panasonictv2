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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.openhab.binding.panasonictv2.internal.service.api.PanasonicTV2Service;
import org.openhab.binding.panasonictv2.internal.service.api.ValueReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The {@link MediaRendererService} is responsible for handling MediaRenderer
 * commands.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class MediaRendererService implements UpnpIOParticipant, PanasonicTV2Service {

    public static final String SERVICE_NAME = "MediaRenderer";
    private final List<String> supportedCommands = Arrays.asList(PanasonicTV2BindingConstants.CHANNEL_VOLUME,
            PanasonicTV2BindingConstants.CHANNEL_MUTE, PanasonicTV2BindingConstants.CHANNEL_CHANNEL,
            PanasonicTV2BindingConstants.CHANNEL_CHANNEL_NAME, PanasonicTV2BindingConstants.CHANNEL_PROGRAM_TITLE);

    private Logger logger = LoggerFactory.getLogger(MediaRendererService.class);

    private UpnpIOService service;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollingJob;

    private String udn;
    private int pollingInterval;

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    private List<ValueReceiver> listeners = new ArrayList<ValueReceiver>();

    public MediaRendererService(UpnpIOService upnpIOService, String udn, int pollingInterval) {
        logger.debug("Create a Panasonic TV MediaRenderer service");

        if (upnpIOService != null) {
            service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        this.udn = udn;
        this.pollingInterval = pollingInterval;

        scheduler = Executors.newScheduledThreadPool(1);
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
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(ValueReceiver listener) {
        listeners.remove(listener);
    }

    @Override
    public void start() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            logger.debug("Start refresh task, interval={}", pollingInterval);
            pollingJob = scheduler.scheduleAtFixedRate(pollingRunnable, 0, pollingInterval, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void clearCache() {
        stateMap.clear();
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            if (isRegistered()) {

                try {
                    updateResourceState("RenderingControl", "GetVolume",
                            PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                    updateResourceState("RenderingControl", "GetMute",
                            PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
                    updateResourceState("AVTransport", "GetMediaInfo",
                            PanasonicTVUtils.buildHashMap("InstanceID", "0"));
                } catch (Exception e) {
                    logger.debug("Exception during poll : {}", e);
                }
            }
        }
    };

    @Override
    public void handleCommand(String channel, Command command) {
        logger.debug("Received channel: {}, command: {}", channel, command);

        switch (channel) {
            case PanasonicTV2BindingConstants.CHANNEL_VOLUME:
                setVolume(command);
                break;
            case PanasonicTV2BindingConstants.CHANNEL_MUTE:
                setMute(command);
                break;
            default:
                logger.warn("Panasonic TV doesn't support transmitting for channel '{}'", channel);
        }
    }

    private boolean isRegistered() {
        return service.isRegistered(this);
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

        String oldValue = stateMap.get(variable);
        if ((value == null && oldValue == null) || (value != null && value.equals(oldValue))) {
            logger.trace("Value '{}' for {} hasn't changed, ignoring update", value, variable);
            return;
        }

        stateMap.put(variable, value);

        for (ValueReceiver listener : listeners) {
            switch (variable) {
                case "CurrentVolume":
                    listener.valueReceived(PanasonicTV2BindingConstants.CHANNEL_VOLUME,
                            (value != null) ? new PercentType(value) : UnDefType.UNDEF);
                    break;

                case "CurrentMute":
                    State newState = UnDefType.UNDEF;
                    if (value != null) {
                        newState = value.equals("true") ? OnOffType.ON : OnOffType.OFF;
                    }
                    listener.valueReceived(PanasonicTV2BindingConstants.CHANNEL_MUTE, newState);
                    break;

                case "CurrentURIMetaData":
                    Document doc = PanasonicTVUtils.loadXMLFromString(value);
                    Element basicElement = (Element) doc.getFirstChild().getFirstChild();

                    Node valueNode;

                    // Channel Nr.
                    valueNode = basicElement.getElementsByTagName("upnp:channelNr").item(0);
                    if (valueNode != null) {
                        listener.valueReceived(PanasonicTV2BindingConstants.CHANNEL_CHANNEL,
                                (value != null) ? new DecimalType(valueNode.getTextContent()) : UnDefType.UNDEF);
                    }
                    // Channel Name
                    valueNode = basicElement.getElementsByTagName("upnp:channelName").item(0);
                    if (valueNode != null) {
                        listener.valueReceived(PanasonicTV2BindingConstants.CHANNEL_CHANNEL_NAME,
                                (value != null) ? new StringType(valueNode.getTextContent()) : UnDefType.UNDEF);
                    }
                    // Program Title
                    valueNode = basicElement.getElementsByTagName("dc:title").item(0);
                    if (valueNode != null) {
                        listener.valueReceived(PanasonicTV2BindingConstants.CHANNEL_PROGRAM_TITLE,
                                (value != null) ? new StringType(valueNode.getTextContent()) : UnDefType.UNDEF);
                    }
                    break;
            }
        }
    }

    protected Map<String, String> updateResourceState(String serviceId, String actionId, Map<String, String> inputs) {

        Map<String, String> result = service.invokeAction(this, serviceId, actionId, inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), serviceId);
        }

        return result;
    }

    private void setVolume(Command command) {
        int newValue;

        try {
            newValue = DataConverters.convertCommandToIntValue(command, 0, 100,
                    Integer.valueOf(stateMap.get("CurrentVolume")));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetVolume", PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel",
                "Master", "DesiredVolume", Integer.toString(newValue)));

        updateResourceState("RenderingControl", "GetVolume",
                PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));
    }

    private void setMute(Command command) {
        boolean newValue;

        try {
            newValue = DataConverters.convertCommandToBooleanValue(command);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Command '" + command + "' not supported");
        }

        updateResourceState("RenderingControl", "SetMute", PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel",
                "Master", "DesiredMute", Boolean.toString(newValue)));

        updateResourceState("RenderingControl", "GetMute",
                PanasonicTVUtils.buildHashMap("InstanceID", "0", "Channel", "Master"));

    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("onStatusChanged");
    }
}
