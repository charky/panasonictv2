/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.panasonictv2.internal.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The {@link PanasonicTVUtils} provides some utilities for internal use.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class PanasonicTVUtils {

    /**
     * Build {@link String} type {@link HashMap} from variable number of
     * {@link String}s.
     *
     * @param data
     *            Variable number of {@link String} parameters which will be
     *            added to hash map.
     */
    public static HashMap<String, String> buildHashMap(String... data) {
        HashMap<String, String> result = new HashMap<String, String>();

        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("Odd number of arguments");
        }
        String key = null;
        Integer step = -1;

        for (String value : data) {
            step++;
            switch (step % 2) {
                case 0:
                    if (value == null) {
                        throw new IllegalArgumentException("Null key value");
                    }
                    key = value;
                    continue;
                case 1:
                    result.put(key, value);
                    break;
            }
        }

        return result;
    }

    /**
     * Build {@link Document} from {@link String} which contains XML content.
     *
     * @param xml
     *            {@link String} which contains XML content.
     * @return {@link Document} or null if convert has failed.
     */
    public static Document loadXMLFromString(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            // Silently ignore exception and return null.
        }

        return null;
    }

    public static Node getNode(Node root, String nodePath) {
        String[] nodePathArr = nodePath.split("/");
        return getNode(root, nodePathArr, 0);
    }

    private static Node getNode(Node parent, String[] nodePath, int offset) {
        if (parent == null) {
            return null;
        }
        if (offset < nodePath.length - 1) {
            return getNode(((Element) parent).getElementsByTagName(nodePath[offset]).item(0), nodePath, offset + 1);
        } else {
            return ((Element) parent).getElementsByTagName(nodePath[offset]).item(0);
        }
    }

}
