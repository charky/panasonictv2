package org.openhab.binding.panasonictv2.internal.protocol;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

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
            soapConnection = SOAPConnectionFactory.newInstance().createConnection();

            this.url = new URL(String.format(PanasonicTV2BindingConstants.SOAP_URL, host));
        } catch (final SOAPException e) {
            logger.debug("Error creating SoapMessage", e);
        } catch (MalformedURLException e) {
            logger.debug("Error creating SOAP URL", e);
        }
    }

    public void sendKey(KeyCode keyCode) {
        try {
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

            /*
             * ByteArrayOutputStream out2 = new ByteArrayOutputStream();
             * requestAction.writeTo(out2);
             * logger.debug("SOAP Request: " + new String(out2.toByteArray()));
             */

            SOAPMessage soapResponse = soapConnection.call(requestAction, url);
            SOAPBody responseBody = soapResponse.getSOAPBody();
            if (responseBody.hasFault()) {
                logger.debug("Soap Response: " + responseBody.getFault().getFaultString());
            }

        } catch (SOAPException soape) {
            logger.error("Error creating SoapActionMessage", soape);

        } catch (Exception e) {
            logger.error("Error performing the SOAP Call: ", e);
        } finally {

        }

    }
}