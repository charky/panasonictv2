package org.openhab.binding.panasonictv2.internal.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.openhab.binding.panasonictv2.PanasonicTV2BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The {@link RemoteController} is responsible for sending key codes to the
 * Panasonic TV.
 *
 *
 *
 * @author Charky - Initial contribution
 *
 */

public class PanasonicTVCommunication {

    private final Logger logger = LoggerFactory.getLogger(PanasonicTVCommunication.class);

    // The address of the receiver.
    private final String host;

    // SOAP Objects
    private URI uri;
    private HttpClient httpClient;

    private DocumentBuilder parser;
    private SOAPMessage requestAction;

    public PanasonicTVCommunication(String host) {
        this.host = host;

        httpClient = new HttpClient();

        try {
            uri = new URI(String.format(PanasonicTV2BindingConstants.SOAP_URL, this.host));
            httpClient.start();

            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            final MessageFactory messageFactory = MessageFactory.newInstance();
            requestAction = messageFactory.createMessage();

        } catch (final SOAPException e) {
            logger.debug("PanasonicTVCommunication - Internal error", e);
        } catch (final URISyntaxException e) {
            logger.debug("PanasonicTVCommunication - Internal error", e);
        } catch (final ParserConfigurationException e) {
            logger.debug("PanasonicTVCommunication - Internal error", e);
        } catch (final Exception e) {
            // Thrown by httpClient.start()
            logger.debug("PanasonicTVCommunication - Internal error", e);
        }
    }

    /**
     * Stop communicating with the device
     */
    public void dispose() {
        try {
            httpClient.stop();
        } catch (final Exception e) {
            // Ignored
        }
    }

    /**
     * This is the first SOAP message used in the login process and is used to retrieve
     * the cookie, challenge and public key used for authentication.
     *
     * @throws SOAPException
     */
    private void sendKey(KeyCode keyCode) throws SOAPException {
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

    /**
     * Send the SOAP message using Jetty HTTP client.
     *
     * @param action - SOAP Action to send
     * @param timeout - Connection timeout in milliseconds
     * @return The result
     * @throws IOException
     * @throws SOAPException
     * @throws SAXException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    protected Document sendReceive(final SOAPMessage action, final int timeout) throws IOException, SOAPException,
            SAXException, InterruptedException, TimeoutException, ExecutionException {

        Document result;

        final Request request = httpClient.POST(uri);
        request.timeout(timeout, TimeUnit.MILLISECONDS);

        final Iterator<?> it = action.getMimeHeaders().getAllHeaders();
        while (it.hasNext()) {
            final MimeHeader header = (MimeHeader) it.next();
            request.header(header.getName(), header.getValue());
        }

        try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            action.writeTo(os);
            request.content(new BytesContentProvider(os.toByteArray()));
            final ContentResponse response = request.send();
            try (final ByteArrayInputStream is = new ByteArrayInputStream(response.getContent())) {
                result = parser.parse(is);
            }
        }

        return result;
    }

    /**
     * Output unexpected responses to the debug log.
     *
     * @param message
     * @param soapResponse
     */
    protected void logUnexpectedResult(final String message, final Document soapResponse) {

        // No point formatting for output if debug logging is not enabled
        if (logger.isDebugEnabled()) {
            try {
                final TransformerFactory transFactory = TransformerFactory.newInstance();
                final Transformer transformer = transFactory.newTransformer();
                final StringWriter buffer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(soapResponse), new StreamResult(buffer));
                logger.debug("{} : {}", message, buffer);
            } catch (final TransformerException e) {
                logger.debug("{}", message);
            }
        }
    }
}