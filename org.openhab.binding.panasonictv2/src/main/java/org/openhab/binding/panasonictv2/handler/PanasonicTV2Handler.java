/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.handler;

import static org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants.CHANNEL_POWER;

import java.util.Collection;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.registry.Registry;
import org.jupnp.registry.RegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTV2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2Handler extends BaseThingHandler implements DiscoveryListener, RegistryListener {

    private final Logger logger = LoggerFactory.getLogger(PanasonicTV2Handler.class);

    private UpnpIOService upnpIOService;
    private DiscoveryServiceRegistry discoveryServiceRegistry;
    private UpnpService upnpService;

    private ThingUID upnpThingUID = null;

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

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        if (channelUID.getId().equals(CHANNEL_POWER)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    // Removes all results belonging to one of the given types that are older than the given timestamp.
    @Override
    public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
            Collection<ThingTypeUID> thingTypeUIDs) {
        return null;
    }

    // Invoked synchronously when a DiscoveryResult has been created by the according DiscoveryService.
    @Override
    public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
    }

    // Invoked synchronously when an already existing Thing has been marked to be deleted by the according
    // DiscoveryService.
    @Override
    public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
    }

    // Called after the registry has been cleared on shutdown.
    @Override
    public void afterShutdown() {
    }

    // Called after registry maintenance stops but before the registry is cleared.
    @Override
    public void beforeShutdown(Registry registry) {
    }

    // Called after you add your own device to the Registry.
    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    }

    // Called after you remove your own device from the Registry.
    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    }

    // Called when complete metadata of a newly discovered device is available.
    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
    }

    // Called when service metadata couldn't be initialized.
    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
    }

    // Called as soon as possible after a device has been discovered.
    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    }

    // Called when a previously discovered device disappears.
    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
    }

    // Called when a discovered device's expiration timestamp is updated.
    @Override
    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
    }

}
