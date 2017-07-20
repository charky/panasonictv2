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

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.panasonictv2.config.PanasonicTV2Configuration;
import org.openhab.binding.panasonictv2.internal.protocol.KeyCode;
import org.openhab.binding.panasonictv2.internal.protocol.PanasonicTV2Communication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PanasonicTV2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PanasonicTV2Handler.class);

    /** Global configuration for Samsung TV Thing */
    private PanasonicTV2Configuration configuration;
    private PanasonicTV2Communication ptvCommunication;

    public PanasonicTV2Handler(Thing thing) {
        super(thing);

        logger.debug("Create a Panasonic TV Handler for thing '{}'", getThing().getUID());

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);
        if (channelUID.getId().equals(CHANNEL_POWER)) {
            logger.debug("-> Sending keyCode:" + KeyCode.NRC_MUTE);
            ptvCommunication.sendKey(KeyCode.NRC_MUTE);
            logger.debug("<- Sending keyCode completed.");
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);

        configuration = getConfigAs(PanasonicTV2Configuration.class);

        logger.debug("Initializing Panasonic TV handler for uid '{}'", getThing().getUID());

        ptvCommunication = new PanasonicTV2Communication(configuration.hostName);
    }

}
