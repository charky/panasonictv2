package org.openhab.binding.panasonictv2.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
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

    private final Logger logger = LoggerFactory.getLogger(PanasonicTV2Communication.class);

    // The address of the receiver.
    private final String host;

    // SOAP Objects
    private URL url;
    private SOAPMessage requestAction;

    public PanasonicTV2Communication(String host) {
        this.host = host;

        try {
            final MessageFactory messageFactory = MessageFactory.newInstance();
            requestAction = messageFactory.createMessage();
            this.url = new URL(String.format(PanasonicTV2BindingConstants.SOAP_URL, this.host));
        } catch (final SOAPException e) {
            logger.debug("Error creating SoapMessage", e);
        } catch (MalformedURLException e) {
            logger.debug("Error creating SOAP URL", e);
        }
    }

    public void sendKey(KeyCode keyCode) {
        try {
            internalSendKey(keyCode);
            postAndGetResponse(requestAction);
        } catch (SOAPException soape) {
            logger.error("Error creating SoapActionMessage", soape);
        } catch (IOException ioe) {
            logger.error("Error creating SoapActionMessage", ioe);
        }

    }

    /**
     * This is the first SOAP message used in the login process and is used to retrieve
     * the cookie, challenge and public key used for authentication.
     *
     * @throws SOAPException
     */
    private void internalSendKey(KeyCode keyCode) throws SOAPException {
        requestAction.getSOAPHeader().detachNode();
        final SOAPBody soapBody = requestAction.getSOAPBody();
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
    }

    private String postAndGetResponse(SOAPMessage message) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=\"utf-8\"");
            connection.setRequestProperty("SOAPAction", PanasonicTV2BindingConstants.SOAP_SENDKEY);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            message.writeTo(connection.getOutputStream());

            // Read response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            logger.error("Could not handle http post: ", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }
}