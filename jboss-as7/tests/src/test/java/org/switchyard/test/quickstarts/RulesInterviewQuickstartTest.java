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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.test.ArquillianUtil;
import org.switchyard.test.mixins.HTTPMixIn;

/**
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
@RunWith(Arquillian.class)
public class RulesInterviewQuickstartTest {

    @Deployment(testable = false)
    public static JavaArchive createDeployment() {
        return ArquillianUtil.createJarQSDeployment("switchyard-quickstart-rules-interview");
    }

    @Test
    public void testDeployment() {
        /* TODO enable this once SWITCHYARD-600/JBWS-3424 is resolved
        
        HTTPMixIn httpMixIn = new HTTPMixIn();
        httpMixIn.initialize();
        Assert.assertNull(httpMixIn.postString("http://localhost:18001/quickstart-rules-interview/Interview", SOAP_REQUEST));
        
        */
    }

    private static final String SOAP_REQUEST =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + "    <soap:Body>\n"
          + "        <interview:applicant xmlns:interview=\"urn:switchyard-quickstart:rules-interview:0.1.0\">\n"
          + "            <name>David</name>\n"
          + "            <age>39</age>\n"
          + "        </interview:applicant>\n"
          + "    </soap:Body>\n"
          + "</soap:Envelope>";
}
