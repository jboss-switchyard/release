/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.switchyard.test.quickstarts;

import java.io.IOException;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.test.ArquillianUtil;
import org.switchyard.test.quickstarts.util.ResourceDeployer;
import org.switchyard.test.mixins.HTTPMixIn;

/**
* A testcase for hornetq-reference-binding quickstart
* @author <a href="mailto:tm.igarashi@gmail.com">Tomohisa Igarashi</a>
*/
@RunWith(Arquillian.class)
public class HornetqReferenceBindingQuickstartTest {

    @Deployment(testable = false)
    public static JavaArchive createDeployment() throws IOException {
        ResourceDeployer.addQueue("GreetingServiceQueue");
        return ArquillianUtil.createJarQSDeployment("switchyard-quickstarts-hornetq-reference-binding");
    }
    
    @Test
    public void testDeployment() throws Exception {
        /* TODO enable when SWITCHYARD-600/JBWS-3424 is resolved

        HTTPMixIn httpMixIn = new HTTPMixIn();
        
        httpMixIn.initialize();
        try {
            httpMixIn.postString("http://localhost:18001/quickstart-hornetq-reference-binding/GreetingService", SOAP_REQUEST);
            ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(NettyConnectorFactory.class.getName()));
            ClientConsumer consumer = locator.createSessionFactory().createSession().createConsumer("jms.queue.GreetingServiceQueue");
            ClientMessage message = consumer.receive(3000);
            Assert.assertNotNull(message);
            byte[] bytea = new byte[message.getBodySize()];
            message.getBodyBuffer().readBytes(bytea);
            Assert.assertEquals("Hello there Tomo :-) ", new String(bytea));
        } finally {
            httpMixIn.uninitialize();
            ResourceDeployer.removeQueue("GreetingServiceQueue");
        }
        
        */
    }

    private static final String SOAP_REQUEST = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <soap:Body>\n" +
            "        <greeting:greet xmlns:greeting=\"urn:switchyard-quickstart:hornetq-reference-binding:1.0\">\n" +
            "            <name>Tomo</name>\n" +
            "        </greeting:greet>\n" +
            "    </soap:Body>\n" +
            "</soap:Envelope>";
}
