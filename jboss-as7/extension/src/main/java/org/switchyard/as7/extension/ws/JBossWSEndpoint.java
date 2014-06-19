/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.switchyard.as7.extension.ws;

import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;

import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.as.webservices.publish.EndpointPublisherImpl;
import org.jboss.as.webservices.util.WSServices;
import org.jboss.logging.Logger;
import org.jboss.metadata.parser.jbossweb.JBossWebMetaDataParser;
import org.jboss.metadata.parser.util.NoopXMLResolver;
import org.jboss.metadata.property.PropertyReplacers;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceContainer;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesFactory;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.spi.metadata.webservices.WebservicesMetaData;
import org.jboss.wsf.spi.publish.Context;
import org.jboss.wsf.spi.publish.EndpointPublisher;
import org.jboss.wsf.spi.publish.EndpointPublisherFactory;
import org.switchyard.ServiceDomain;
import org.switchyard.common.type.Classes;
import org.switchyard.component.common.Endpoint;
import org.switchyard.component.soap.InboundHandler;
import org.switchyard.component.soap.WebServicePublishException;
import org.switchyard.component.soap.config.model.EndpointConfigModel;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.endpoint.BaseWebService;
import org.switchyard.deploy.internal.Deployment;

/**
 * Wrapper for JBossWS endpoints.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class JBossWSEndpoint implements Endpoint {

    private static final Logger LOGGER = Logger.getLogger("org.switchyard");

    private static final String HOST = "default-host";
    private static final EndpointPublisherFactory FACTORY;

    private EndpointPublisher _publisher;
    private Context _context;

    /**
     * Construct a JBossWS endpoint on default host.
     * @throws Exception If a publisher could not be created
     */
    public JBossWSEndpoint() throws Exception {
        _publisher = FACTORY.newEndpointPublisher(HOST);
    }

    /**
     * Construct a JBossWS endpoint on specified host.
     * @param host The host on which the pubhlisher should created
     * @throws Exception if a publisher could not be created
     */
    public JBossWSEndpoint(String host) throws Exception {
        _publisher = FACTORY.newEndpointPublisher(host);
    }

    /**
     * {@inheritDoc}
     */
    public void publish(ServiceDomain domain, String contextRoot, Map<String, String> urlPatternToClassNameMap, WebservicesMetaData wsMetadata, SOAPBindingModel bindingModel, InboundHandler handler) throws Exception {
        ClassLoader deploymentClassLoader = (ClassLoader)domain.getProperty(Deployment.CLASSLOADER_PROPERTY);
        EndpointConfigModel epcModel = bindingModel.getEndpointConfig();
        JBossWebservicesMetaData jbwsMetadata = null;
        if (epcModel != null) {
            String configName = epcModel.getConfigName();
            String configFile = epcModel.getConfigFile();
            if (configFile != null) {
                URL jbwsURL = Classes.getResource(configFile, deploymentClassLoader, getClass().getClassLoader());
                try {
                    JBossWebservicesFactory factory = new JBossWebservicesFactory(jbwsURL);
                    jbwsMetadata = factory.load(jbwsURL);
                } catch (WebServiceException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.error("Unable to load jboss-webservices metadata", e);
                    }
                    jbwsMetadata = new JBossWebservicesMetaData("/", configName, configFile, jbwsURL, null, null, null);
                }
            }
        }
        ClassLoader tccl = Classes.getTCCL();
        if (_publisher instanceof EndpointPublisherImpl) {
            JBossWebMetaData jbwMetadata = getJBossWebMetaData(deploymentClassLoader);
            if (jbwMetadata != null) {
                // cast hack
                EndpointPublisherImpl pubImpl = (EndpointPublisherImpl)_publisher;
                ServiceTarget baseTarget = currentServiceContainer().getService(WSServices.CONFIG_SERVICE).getServiceContainer();
                _context = pubImpl.publish(baseTarget, contextRoot, tccl, urlPatternToClassNameMap, jbwMetadata, wsMetadata, jbwsMetadata);
            }
        }
        if (_context == null) {
            // exposed by the public API
            _context = _publisher.publish(contextRoot, tccl, urlPatternToClassNameMap, wsMetadata, jbwsMetadata);
        }
        for (org.jboss.wsf.spi.deployment.Endpoint ep : _context.getEndpoints()) {
            BaseWebService wsProvider = (BaseWebService)ep.getInstanceProvider().getInstance(BaseWebService.class.getName()).getValue();
            wsProvider.setInvocationClassLoader(tccl);
            // Hook the handler
            wsProvider.setConsumer(handler);
            // Hook the interceptors
            Interceptors.addInterceptors(ep, bindingModel, tccl);
        }
    }

    private JBossWebMetaData getJBossWebMetaData(ClassLoader deploymentClassLoader) {
        JBossWebMetaData jbwMetadata = null;
        InputStream is = null;
        try {
            URL jbwURL = Classes.getResource("WEB-INF/jboss-web.xml", deploymentClassLoader, getClass().getClassLoader());
            if (jbwURL == null) {
                jbwURL = Classes.getResource("META-INF/jboss-web.xml", deploymentClassLoader, getClass().getClassLoader());
            }
            if (jbwURL != null) {
                is = jbwURL.openStream();
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                inputFactory.setXMLResolver(NoopXMLResolver.create());
                XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(is);
                jbwMetadata = JBossWebMetaDataParser.parse(xmlReader, PropertyReplacers.noop());
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error("Unable to load jboss-web metadata", e);
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable t) {
                    // keep checkstyle happy ("at least one statement")
                    t.getMessage();
                }
            }
        }
        return jbwMetadata;
    }

    private ServiceContainer currentServiceContainer() {
        if(System.getSecurityManager() == null) {
            return CurrentServiceContainer.getServiceContainer();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ServiceContainer>() {
            @Override
            public ServiceContainer run() {
                return CurrentServiceContainer.getServiceContainer();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void start() {
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (_context != null && _publisher != null) {
            try {
                //undeploy endpoints
                _publisher.destroy(_context);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    static {
        try {
            ClassLoader loader = ClassLoaderProvider.getDefaultProvider().getWebServiceSubsystemClassLoader();
            FACTORY = ServiceLoader.load(EndpointPublisherFactory.class, loader).iterator().next();
        } catch (Exception e) {
            throw new WebServicePublishException(e);
        }
    }
}
