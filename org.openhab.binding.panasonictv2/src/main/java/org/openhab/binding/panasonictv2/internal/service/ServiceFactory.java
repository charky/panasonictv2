/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.internal.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.panasonictv2.internal.service.api.PanasonicTV2Service;

/**
 * The {@link ServiceFactory} is helper class for creating Samsung TV related
 * services.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ServiceFactory {

    @SuppressWarnings("serial")
    private static final Map<String, Class<?>> serviceMap = Collections
            .unmodifiableMap(new HashMap<String, Class<?>>() {
                {
                    put(MediaRendererService.SERVICE_NAME, MediaRendererService.class);
                    put(RemoteControllerService.SERVICE_NAME, RemoteControllerService.class);
                }
            });

    /**
     * Create Samsung TV service.
     *
     * @param type
     * @param upnpIOService
     * @param udn
     * @param pollingInterval
     * @param host
     * @param port
     * @return
     */
    public static PanasonicTV2Service createService(String type, UpnpIOService upnpIOService, String udn,
            int pollingInterval, String host, int port) {

        PanasonicTV2Service service = null;

        switch (type) {
            case MediaRendererService.SERVICE_NAME:
                service = new MediaRendererService(upnpIOService, udn, pollingInterval);
                break;
            case RemoteControllerService.SERVICE_NAME:
                service = new RemoteControllerService(upnpIOService, udn);
                break;
        }

        return service;
    }

    /**
     * Procedure to query amount of supported services.
     *
     * @return Amount of supported services
     */
    public static int getServiceCount() {
        return serviceMap.size();
    }

    /**
     * Procedure to get service class by service name.
     *
     * @param serviceName Name of the service
     * @return Class of the service
     */
    public static Class<?> getClassByServiceName(String serviceName) {
        return serviceMap.get(serviceName);
    }
}
