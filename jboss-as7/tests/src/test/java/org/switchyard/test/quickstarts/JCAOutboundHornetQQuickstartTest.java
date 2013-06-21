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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.hornetq.HornetQMixIn;
import org.switchyard.test.ArquillianUtil;
import org.switchyard.test.quickstarts.util.ResourceDeployer;

@RunWith(Arquillian.class)
public class JCAOutboundHornetQQuickstartTest {

    private static final String ORDER_QUEUE = "OrderQueue";
    private static final String SHIPPING_QUEUE = "ShippingQueue";
    private static final String FILLING_STOCK_QUEUE = "FillingStockQueue";
    private static final String DLQ = "DLQ";
    
    private static final String namespace = "urn:switchyard-quickstart:jca-outbound-hornetq:0.1.0";
    private static final String application = new QName("switchyard-quickstart-jca-outbound-hornetq").toString();
    private static final String orderService = new QName(namespace, "OrderService").toString();
    private static final String orderBinding = "_OrderService_jca_1";
    private static final String shippingService = new QName(namespace, "ShippingReference").toString();
    private static final String shippingBinding = "_ShippingReference_jca_1";
    private static final String fillingService = new QName(namespace, "FillingStockReference").toString();
    private static final String fillingBinding = "_FillingStockReference_jca_1";

    @ArquillianResource
    private ManagementClient _client;

    @Deployment(testable = false)
    public static JavaArchive createDeployment() throws IOException {
        ResourceDeployer.addQueue(ORDER_QUEUE);
        ResourceDeployer.addQueue(SHIPPING_QUEUE);
        ResourceDeployer.addQueue(FILLING_STOCK_QUEUE);
        ResourceDeployer.addQueue(DLQ);
        ResourceDeployer.addPropertiesUser();
        return ArquillianUtil.createJarQSDeployment("switchyard-quickstart-jca-outbound-hornetq");
    }

    @Before
    public void clearQueues() throws Exception {
        HornetQMixIn hqMixIn = new HornetQMixIn(false).setUser(ResourceDeployer.USER).setPassword(
                ResourceDeployer.PASSWD);
        hqMixIn.initialize();

        try {
            Session session = hqMixIn.getJMSSession();
            clearQueue(session.createConsumer(HornetQMixIn.getJMSQueue(ORDER_QUEUE)));
            clearQueue(session.createConsumer(HornetQMixIn.getJMSQueue(SHIPPING_QUEUE)));
            clearQueue(session.createConsumer(HornetQMixIn.getJMSQueue(FILLING_STOCK_QUEUE)));
            clearQueue(session.createConsumer(HornetQMixIn.getJMSQueue(DLQ)));
        } finally {
            hqMixIn.uninitialize();
            ResourceDeployer.removeQueue(ORDER_QUEUE);
            ResourceDeployer.removeQueue(SHIPPING_QUEUE);
            ResourceDeployer.removeQueue(FILLING_STOCK_QUEUE);
            ResourceDeployer.removeQueue(DLQ);
        }
    }

    private void clearQueue(MessageConsumer consumer) throws Exception {
        while (consumer.receiveNoWait() != null) {
            // nothing
        }
    }

    @Test
    public void testDeployment() throws Exception {
        HornetQMixIn hqMixIn = new HornetQMixIn(false).setUser(ResourceDeployer.USER).setPassword(
                ResourceDeployer.PASSWD);
        hqMixIn.initialize();
        String[] orders = {"BREAD", "PIZZA", "JAM", "POTATO", "MILK", "JAM"};

        try {
            Session session = hqMixIn.getJMSSession();
            MessageProducer producer = session.createProducer(HornetQMixIn.getJMSQueue(ORDER_QUEUE));
            for (String order : orders) {
                BytesMessage message = session.createBytesMessage();
                message.writeBytes(order.getBytes());
                producer.send(message);
            }
            session = hqMixIn.createJMSSession();

            MessageConsumer consumer = session.createConsumer(HornetQMixIn.getJMSQueue(SHIPPING_QUEUE));
            List<String> expectedShippingOrders = new ArrayList<String>(Arrays.asList("BREAD", "JAM", "MILK", "JAM"));
            Message msg = null;
            while ((msg = consumer.receive(1000)) != null) {
                Assert.assertTrue(expectedShippingOrders.remove(hqMixIn.readStringFromJMSMessage(msg)));
            }
            Assert.assertEquals(0, expectedShippingOrders.size());

            consumer = session.createConsumer(HornetQMixIn.getJMSQueue(FILLING_STOCK_QUEUE));
            List<String> expectedFillingStockOrders = new ArrayList<String>(Arrays.asList("PIZZA", "POTATO"));
            while ((msg = consumer.receive(1000)) != null) {
                Assert.assertTrue(expectedFillingStockOrders.remove(hqMixIn.readStringFromJMSMessage(msg)));
            }
            Assert.assertEquals(0, expectedFillingStockOrders.size());
        } finally {
            hqMixIn.uninitialize();
            ResourceDeployer.removeQueue(ORDER_QUEUE);
            ResourceDeployer.removeQueue(SHIPPING_QUEUE);
            ResourceDeployer.removeQueue(FILLING_STOCK_QUEUE);
            ResourceDeployer.removeQueue(DLQ);
        }
    }

    @Test
    public void testGatewayRestart() throws Exception {
        HornetQMixIn hqMixIn = new HornetQMixIn(false).setUser(ResourceDeployer.USER).setPassword(
                ResourceDeployer.PASSWD);
        hqMixIn.initialize();

        try {
            // producers
            Session session = hqMixIn.getJMSSession();
            Queue orderQueue = HornetQMixIn.getJMSQueue(ORDER_QUEUE);
            MessageProducer orderProducer = session.createProducer(HornetQMixIn.getJMSQueue(ORDER_QUEUE));
            QueueBrowser orderBrowser = session.createBrowser(orderQueue);

            // consumers
            MessageConsumer shippingConsumer = session.createConsumer(HornetQMixIn.getJMSQueue(SHIPPING_QUEUE));
            MessageConsumer fillingConsumer = session.createConsumer(HornetQMixIn.getJMSQueue(FILLING_STOCK_QUEUE));
            MessageConsumer dlqConsumer = session.createConsumer(HornetQMixIn.getJMSQueue(DLQ));

            BytesMessage message = session.createBytesMessage();
            message.writeBytes("BREAD".getBytes());
            orderProducer.send(message);

            Assert.assertTrue("BREAD".equals(hqMixIn.readStringFromJMSMessage(shippingConsumer.receive(1000))));
            Assert.assertTrue(shippingConsumer.receiveNoWait() == null);
            Assert.assertTrue(fillingConsumer.receiveNoWait() == null);

            message = session.createBytesMessage();
            message.writeBytes("PIZZA".getBytes());
            orderProducer.send(message);

            Assert.assertTrue("PIZZA".equals(hqMixIn.readStringFromJMSMessage(fillingConsumer.receive(1000))));
            Assert.assertTrue(shippingConsumer.receiveNoWait() == null);
            Assert.assertTrue(fillingConsumer.receiveNoWait() == null);

            // stop the gateways
            executeOperation(orderService, orderBinding, false);
            executeOperation(shippingService, shippingBinding, false);
            executeOperation(fillingService, fillingBinding, false);

            message = session.createBytesMessage();
            message.writeBytes("BREAD".getBytes());
            orderProducer.send(message);
            Thread.sleep(1000);
            Assert.assertTrue("BREAD".equals(hqMixIn.readStringFromJMSMessage((Message) orderBrowser.getEnumeration()
                    .nextElement())));

            // start the order service
            executeOperation(orderService, orderBinding, true);
            Thread.sleep(1000);
            Assert.assertFalse(orderBrowser.getEnumeration().hasMoreElements());
            Assert.assertTrue(shippingConsumer.receiveNoWait() == null);
            Assert.assertTrue(fillingConsumer.receiveNoWait() == null);
            Assert.assertTrue("BREAD".equals(hqMixIn.readStringFromJMSMessage(dlqConsumer.receiveNoWait())));
            Assert.assertTrue(dlqConsumer.receiveNoWait() == null);

            // restart shipping
            executeOperation(shippingService, shippingBinding, true);
            message = session.createBytesMessage();
            message.writeBytes("BREAD".getBytes());
            orderProducer.send(message);
            Assert.assertTrue("BREAD".equals(hqMixIn.readStringFromJMSMessage(shippingConsumer.receive(1000))));
            Assert.assertTrue(shippingConsumer.receiveNoWait() == null);
            Assert.assertTrue(fillingConsumer.receiveNoWait() == null);
            Assert.assertTrue(dlqConsumer.receiveNoWait() == null);
        } finally {
            hqMixIn.uninitialize();
            ResourceDeployer.removeQueue(ORDER_QUEUE);
            ResourceDeployer.removeQueue(SHIPPING_QUEUE);
            ResourceDeployer.removeQueue(FILLING_STOCK_QUEUE);
            ResourceDeployer.removeQueue(DLQ);
        }
    }

    private void executeOperation(final String serviceName, final String bindingName, final boolean start)
            throws Exception {
        final ModelNode operation = new ModelNode();
        operation.get(ModelDescriptionConstants.OP_ADDR).add("subsystem", "switchyard");
        operation.get("application-name").set(application);
        if (start) {
            operation.get(ModelDescriptionConstants.OP).set("start-gateway");
        } else {
            operation.get(ModelDescriptionConstants.OP).set("stop-gateway");
        }
        operation.get(ModelDescriptionConstants.NAME).set(bindingName);
        operation.get("service-name").set(serviceName);
        final ModelNode result = _client.getControllerClient().execute(operation);
        Assert.assertEquals("Failed to " + (start ? "start" : "stop") + " gateway: " + result.toString(),
                ModelDescriptionConstants.SUCCESS, result.get(ModelDescriptionConstants.OUTCOME).asString());
    }

}
