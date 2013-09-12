package org.switchyard.as7.extension;

import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Cause;
import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.switchyard.SwitchYardException;

/**
 * <p/>
 * This file is using the subset 38400-38799 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface ExtensionMessages {
    /**
     * The default messages.
     */
    ExtensionMessages MESSAGES = Messages.getBundle(ExtensionMessages.class);

    /**
     * alreadyDeclared method definition.
     * @param localName local name
     * @param location location
     * @return XMLStreamException
     */
    @Message(id = 38400, value = "%s already declared %s")
    XMLStreamException alreadyDeclared(String localName, String location);

    /**
     * errorStartingGateway method definition.
     * @return String
     */
    @Message(id = 38401, value = "Error starting gateway: ")
    String errorStartingGateway();
    
    /**
     * unknownGateway method definition.
     * @return String
     */
    @Message(id = 38402, value = "Unknown gateway.")
    String unknownGateway();

    /**
     * unableToCreateTempDirectory method definition.
     * @param path path to temp directory
     * @return RuntimeException
     */
    @Message(id = 38403, value = "Unable to create temp directory %s")
    RuntimeException unableToCreateTempDirectory(String path);
    
    /**
     * contextAlreadyExists
     * @param contextName context name
     * @return RuntimeException
     */
    @Message(id = 38404, value = "Context %s already exists!")
    RuntimeException contextAlreadyExists(String contextName);

    /**
     * couldNotInstantiateInterceptor method definition.
     * @param interceptorClassName interceptor class name
     * @param t cause 
     * @return SwitchYardException
     */
    @Message(id = 38405, value = "Could not instantiate interceptor class: %s")
    SwitchYardException couldNotInstantiateInterceptor(String interceptorClassName, @Cause Throwable t);

}
