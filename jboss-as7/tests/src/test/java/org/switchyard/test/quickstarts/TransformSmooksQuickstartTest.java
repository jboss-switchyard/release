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

import org.custommonkey.xmlunit.XMLAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.test.ArquillianUtil;
import org.switchyard.test.mixins.HTTPMixIn;

/**
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
@RunWith(Arquillian.class)
public class TransformSmooksQuickstartTest {

    @Deployment(testable = false)
    public static JavaArchive createDeployment() {
        return ArquillianUtil.createJarQSDeployment("switchyard-quickstart-transform-smooks");
    }

    @Test
    public void testDeployment() throws Exception {
        HTTPMixIn httpMixIn = new HTTPMixIn();
        httpMixIn.initialize();
        try {
            String response = httpMixIn.postString("http://localhost:18001/quickstart-transform-smooks/OrderService", SOAP_REQUEST);
            XMLAssert.assertXpathEvaluatesTo("PO-19838-XYZ", "//orderAck/orderId", response);
            XMLAssert.assertXpathEvaluatesTo("true", "//orderAck/accepted", response);
            XMLAssert.assertXpathEvaluatesTo("Order Accepted", "//orderAck/status", response);
        } finally {
            httpMixIn.uninitialize();
        }
    }

    private static final String SOAP_REQUEST =
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + "    <soapenv:Body>\n"
          + "        <orders:submitOrder xmlns:orders=\"urn:switchyard-quickstart:transform-smooks:1.0\">\n"
          + "            <order>\n"
          + "                <orderId>PO-19838-XYZ</orderId>\n"
          + "                <itemId>BUTTER</itemId>\n"
          + "                <quantity>200</quantity>\n"
          + "            </order>\n"
          + "        </orders:submitOrder>\n"
          + "    </soapenv:Body>\n"
          + "</soapenv:Envelope>";

}
