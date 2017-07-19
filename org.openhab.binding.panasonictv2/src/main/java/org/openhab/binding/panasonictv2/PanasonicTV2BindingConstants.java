/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PanasonicTV2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2BindingConstants {

    private static final String BINDING_ID = "panasonictv2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PANASONICTV = new ThingTypeUID(BINDING_ID, "panasonictv");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";

    // Additional Finals
    public static final String UPNP_MANUFACTURER = "Panasonic";
    public static final String UPNP_XMLNS = "urn:panasonic-com:service:p00NetworkControl:1";
    public static final String UPNP_TYPE = "p00RemoteController";
    // SOAP actions
    public static final String SOAP_URL = "http://%s/nrc/control_0/";
    public static final String SOAP_SENDKEY = "\"urn:panasonic-com:service:p00NetworkControl:1#X_SendKey\"";

}
