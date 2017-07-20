package org.openhab.binding.panasonictv2.internal.protocol;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RemoteController} is responsible for sending key codes to the
 * Panasonic TV.
 *
 *
 *
 * @author Charky - Initial contribution
 *
 */

public class PanasonicTV2Communication {

    // Logging
    private final Logger logger = LoggerFactory.getLogger(PanasonicTV2Communication.class);
    // URL
    private URL url;
    //
    private MessageFactory messageFactory;
    // The SOAP connection
    private SOAPConnection soapConnection;

    public PanasonicTV2Communication(String host) {
        try {
            messageFactory = MessageFactory.newInstance();

            HttpClient client = new DefaultHttpClient();

            this.url = new URL(String.format(PanasonicTV2BindingConstants.SOAP_URL, host));
        } catch (final SOAPException e) {
            logger.debug("Error creating SoapMessage", e);
        } catch (MalformedURLException e) {
            logger.debug("Error creating SOAP URL", e);
        }
    }

    public void sendKey(KeyCode keyCode) {
        try {
            String response = internalSendKey(keyCode);
            logger.debug("SOAP Response: " + response);
        } catch (SOAPException soape) {
            logger.error("Error creating SoapActionMessage", soape);
        }
    }

    /**
     * This is the first SOAP message used in the login process and is used to retrieve
     * the cookie, challenge and public key used for authentication.
     *
     * @throws SOAPException
     */
    private String internalSendKey(KeyCode keyCode) throws SOAPException {
        SOAPMessage requestAction = messageFactory.createMessage();
        SOAPBody soapBody = requestAction.getSOAPBody();
        // <u:X_SendKey xmlns:u="urn:panasonic-com:service:p00NetworkControl:1">
        QName bodyName = new QName(PanasonicTV2BindingConstants.UPNP_XMLNS, "X_SendKey", "u");
        SOAPBodyElement bodyElement = soapBody.addBodyElement(bodyName);

        QName keyEventName = new QName("X_KeyEvent");
        SOAPElement keyCodeElement = bodyElement.addChildElement(keyEventName);
        keyCodeElement.addTextNode(keyCode.getValue());

        final MimeHeaders headers = requestAction.getMimeHeaders();
        headers.addHeader("Content-Type", "text/xml; charset=\"utf-8\"");
        headers.addHeader("SOAPAction", PanasonicTV2BindingConstants.SOAP_SENDKEY);

        requestAction.saveChanges();

        try {
            ByteArrayOutputStream out2 = new ByteArrayOutputStream();
            requestAction.writeTo(out2);
            logger.debug("SOAP Request: " + new String(out2.toByteArray()));

            SOAPMessage soapResponse = soapConnection.call(requestAction, url);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            return out.toString();
        } catch (Exception e) {
            logger.error("Error performing the SOAP Call: ", e);
        }
        return "";
    }
}