/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.internal;

import static org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants.THING_TYPE_PANASONICTV;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.panasonictv2.handler.PanasonicTV2Handler;

/**
 * The {@link PanasonicTV2HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Charky - Initial contribution
 */
public class PanasonicTV2HandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_PANASONICTV);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PANASONICTV)) {
            return new PanasonicTV2Handler(thing);
        }

        return null;
    }

}
