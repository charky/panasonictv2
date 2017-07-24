/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.UpnpDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.openhab.binding.panasonictv2.config.PanasonicTV2Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTV2DiscoveryParticipant} is responsible for processing the
 * results of searched UPnP devices
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2DiscoveryParticipant implements UpnpDiscoveryParticipant {

    private Logger logger = LoggerFactory.getLogger(PanasonicTV2DiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(PanasonicTV2BindingConstants.THING_TYPE_PANASONICTV);
    }

    @Override
    public DiscoveryResult createResult(RemoteDevice device) {
        ThingUID uid = getThingUID(device);
        if (uid != null) {
            Map<String, Object> properties = new HashMap<>(3);
            String label = "Panasonic TV";
            try {
                label = device.getDetails().getFriendlyName();
            } catch (Exception e) {
                // ignore and use the default label
            }
            properties.put(PanasonicTV2Configuration.HOST_NAME, device.getIdentity().getDescriptorURL().getHost());

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties).withLabel(label)
                    .build();

            logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",
                    device.getDetails().getModelDetails().getModelName(),
                    device.getIdentity().getUdn().getIdentifierString());
            return result;
        } else {
            return null;
        }
    }

    @Override
    public ThingUID getThingUID(RemoteDevice device) {
        if (device == null) {
            return null;
        }

        String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
        String modelName = device.getDetails().getModelDetails().getModelName();
        String friedlyName = device.getDetails().getFriendlyName();

        if (manufacturer == null || modelName == null) {
            return null;
        }

        // UDN shouldn't contain '-' characters.
        String udn = device.getIdentity().getUdn().getIdentifierString().replace("-", "_");

        if (manufacturer.toUpperCase().contains(PanasonicTV2BindingConstants.UPNP_MANUFACTURER.toUpperCase())
                && device.getType().getType().equals("MediaRenderer")) {
            // && device.getType().getType().equals(PanasonicTV2BindingConstants.UPNP_TYPE)) {

            logger.debug("Discovered a Panasonic TV '{}' model '{}' thing with UDN '{}'", friedlyName, modelName, udn);

            return new ThingUID(PanasonicTV2BindingConstants.THING_TYPE_PANASONICTV, udn);
        } else {
            return null;
        }
    }
}
