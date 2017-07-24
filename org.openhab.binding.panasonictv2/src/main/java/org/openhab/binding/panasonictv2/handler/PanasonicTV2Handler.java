/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.openhab.binding.panasonictv2.config.PanasonicTV2Configuration;
import org.openhab.binding.panasonictv2.internal.service.ServiceFactory;
import org.openhab.binding.panasonictv2.internal.service.api.PanasonicTV2Service;
import org.openhab.binding.panasonictv2.internal.service.api.ValueReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTV2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2Handler extends BaseThingHandler
        implements DiscoveryListener, RegistryListener, ValueReceiver {

    // Logging
    private final Logger logger = LoggerFactory.getLogger(PanasonicTV2Handler.class);

    /** Global configuration for Panasonic TV Thing */
    private PanasonicTV2Configuration configuration;
    private ThingUID upnpThingUID = null;

    /** Polling job for searching UPnP devices on startup */
    private ScheduledFuture<?> upnpPollingJob;

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;

    /** Panasonic TV services */
    private List<PanasonicTV2Service> services;

    private boolean powerOn = false;

    public PanasonicTV2Handler(Thing thing, UpnpIOService upnpIOService,
            DiscoveryServiceRegistry discoveryServiceRegistry, UpnpService upnpService) {
        super(thing);

        logger.debug("Create a Panasonic TV Handler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.upnpIOService = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

        if (discoveryServiceRegistry != null) {
            this.discoveryServiceRegistry = discoveryServiceRegistry;
        }

        if (upnpService != null) {
            this.upnpService = upnpService;
        } else {
            logger.debug("upnpService not set.");
        }

        services = new ArrayList<>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        logger.debug("Received channel: {}, command: {}", channelUID, command);

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            // Delegate command to correct service

            String channel = channelUID.getId();

            for (PanasonicTV2Service service : services) {
                if (service != null) {
                    List<String> supportedCommands = service.getSupportedChannelNames();
                    for (String s : supportedCommands) {
                        if (channel.equals(s)) {
                            service.handleCommand(channel, command);
                            return;
                        }
                    }
                }
            }

            logger.warn("Channel '{}' not supported", channelUID);
        } else {
            logger.debug("Panasonic TV '{}' is OFFLINE", getThing().getUID());
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        updateState(new ChannelUID(getThing().getUID(), PanasonicTV2BindingConstants.CHANNEL_POWER),
                getPowerState() ? OnOffType.ON : OnOffType.OFF);

        for (PanasonicTV2Service service : services) {
            if (service != null) {
                service.clearCache();
            }
        }
    }

    private synchronized void updatePowerState(boolean state) {
        powerOn = state;
    }

    private synchronized boolean getPowerState() {
        return powerOn;
    }

    /*
     * One Panasonic TV contains several UPnP devices. Panasonic TV is discovered by
     * Media Renderer UPnP device. This polling job tries to find another UPnP
     * devices related to same Panasonic TV and create handler for those.
     */
    private Runnable scanUPnPDevicesRunnable = new Runnable() {

        @Override
        public void run() {
            logger.debug("scanUPnPDevicesRunnable Runnable->run");
            checkAndCreateServices();
        }
    };

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);

        configuration = getConfigAs(PanasonicTV2Configuration.class);

        logger.debug("Initializing Panasonic TV handler for uid '{}'", getThing().getUID());

        if (configuration.hostName == null || configuration.hostName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname not set!");
            return;
        }

        if (discoveryServiceRegistry != null) {
            discoveryServiceRegistry.addDiscoveryListener(this);
        }

    }

    @Override
    public void dispose() {
        if (discoveryServiceRegistry != null) {
            discoveryServiceRegistry.removeDiscoveryListener(this);
        }
        shutdown();
    }

    private void shutdown() {
        if (upnpPollingJob != null && !upnpPollingJob.isCancelled()) {
            upnpPollingJob.cancel(true);
            upnpPollingJob = null;
        }

        if (upnpService != null) {
            upnpService.getRegistry().removeListener(this);
        }

        stopServices();
    }

    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
        logger.debug("thingDiscovered: {}", result);

        if (configuration.hostName.equals(result.getProperties().get(PanasonicTV2Configuration.HOST_NAME))) {
            /*
             * SamsungTV discovery services creates thing UID from UPnP UDN.
             * When thing is generated manually, thing UID may not match UPnP UDN, so store it for later use (e.g.
             * thingRemoved).
             */
            upnpThingUID = result.getThingUID();
            logger.debug("thingDiscovered, thingUID={}, discoveredUID={}", this.getThing().getUID(), upnpThingUID);
            upnpPollingJob = scheduler.schedule(scanUPnPDevicesRunnable, 0, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        logger.debug("thingRemoved: {}", thingUID);

        if (thingUID.equals(upnpThingUID)) {
            shutdown();
            putOffline();
        }
    }

    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        logger.debug("remoteDeviceAdded: device={}", device);
        createService(device);
    }

    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        logger.debug("remoteDeviceRemoved: device={}", device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    @Override
    public void beforeShutdown(Registry registry) {
    }

    @Override
    public void afterShutdown() {
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

    public void putOnline() {
        if (this.thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            updatePowerState(true);
            updateState(new ChannelUID(getThing().getUID(), PanasonicTV2BindingConstants.CHANNEL_POWER), OnOffType.ON);
        }
    }

    public synchronized void putOffline() {
        if (this.thing.getStatus() != ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE);
            updateState(new ChannelUID(getThing().getUID(), PanasonicTV2BindingConstants.CHANNEL_POWER), OnOffType.OFF);
            updatePowerState(false);
        }
    }

    @Override
    public synchronized void valueReceived(String variable, State value) {
        logger.debug("Received value '{}':'{}' for thing '{}'",
                new Object[] { variable, value, this.getThing().getUID() });

        updateState(new ChannelUID(getThing().getUID(), variable), value);

        if (!getPowerState()) {
            updatePowerState(true);
            updateState(new ChannelUID(getThing().getUID(), PanasonicTV2BindingConstants.CHANNEL_POWER), OnOffType.ON);
        }
    }

    private void checkAndCreateServices() {
        logger.debug("Check and create missing UPnP services");
        Iterator<?> itr = upnpService.getRegistry().getDevices().iterator();

        while (itr.hasNext()) {
            RemoteDevice device = (RemoteDevice) itr.next();
            createService(device);
        }

        if (upnpService != null) {
            upnpService.getRegistry().addListener(this);
        }
    }

    private synchronized void createService(RemoteDevice device) {
        if (configuration != null) {
            if (configuration.hostName.equals(device.getIdentity().getDescriptorURL().getHost())) {
                String modelName = device.getDetails().getModelDetails().getModelName();
                String udn = device.getIdentity().getUdn().getIdentifierString();
                String type = device.getType().getType();

                logger.debug(" modelName={}, udn={}, type={}", modelName, udn, type);

                PanasonicTV2Service service = findServiceInstance(type);
                if (service == null) {
                    PanasonicTV2Service newService = ServiceFactory.createService(type, upnpIOService, udn,
                            configuration.refreshInterval, configuration.hostName, configuration.port);

                    if (newService != null) {
                        startService(newService);
                        services.add(newService);
                    }
                } else {
                    logger.debug("Device rediscovered, clear caches");
                    service.clearCache();
                }
                putOnline();
            } else {
                logger.debug("Ignore device={}", device);
            }
        } else {
            logger.debug("Thing not yet initialized");
        }
    }

    private PanasonicTV2Service findServiceInstance(String serviceName) {
        Class<?> cl = ServiceFactory.getClassByServiceName(serviceName);

        if (cl != null) {
            for (PanasonicTV2Service service : services) {
                if (service != null) {
                    if (service.getClass() == cl) {
                        return service;
                    }
                }
            }
        }
        return null;
    }

    private void startService(PanasonicTV2Service service) {
        if (service != null) {
            service.addEventListener(this);
            service.start();
        }
    }

    private void stopService(PanasonicTV2Service service) {
        if (service != null) {
            service.stop();
            service.removeEventListener(this);
            service = null;
        }
    }

    private void stopServices() {
        logger.debug("Shutdown all UPnP services");
        for (PanasonicTV2Service service : services) {
            stopService(service);
        }
        services.clear();
    }

}
