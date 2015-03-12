<?xml version="1.0" encoding="UTF-8"?>
<!--
 - Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 - 
 - Licensed under the Apache License, Version 2.0 (the "License");
 - you may not use this file except in compliance with the License.
 - You may obtain a copy of the License at
 - http://www.apache.org/licenses/LICENSE-2.0
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:xdt="http://www.w3.org/2005/xpath-datatypes"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:as="urn:jboss:domain:1.6"
    xmlns:inf="urn:jboss:domain:infinispan:1.5"
    xmlns:sy="urn:jboss:domain:switchyard:1.0"
    xmlns:log="urn:jboss:domain:logging:1.4"
    exclude-result-prefixes="xs xsl xsi fn xdt as inf sy log">

<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

<xsl:template match="@*|node()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>


<xsl:template match="//*[local-name()='subsystem' and contains(namespace-uri(),'urn:jboss:domain:switchyard')]/*[local-name()='modules']">
    <xsl:choose>
      <xsl:when test="not(sy:module[@identifier='org.switchyard.component.bpm']) and not(sy:module[@identifier='org.switchyard.component.rules'])">
         <xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
                <module identifier="org.switchyard.component.bpm" implClass="org.switchyard.component.bpm.deploy.BPMComponent"/>
		<module identifier="org.switchyard.component.rules" implClass="org.switchyard.component.rules.deploy.RulesComponent"/>
        </xsl:copy> 
      </xsl:when>
       <xsl:when test="not(sy:module[@identifier='org.switchyard.component.bpm'])">
         <xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
                <module identifier="org.switchyard.component.bpm" implClass="org.switchyard.component.bpm.deploy.BPMComponent"/>
        </xsl:copy> 
      </xsl:when>
      <xsl:when test="not(sy:module[@identifier='org.switchyard.component.rules'])">
         <xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
                <module identifier="org.switchyard.component.rules" implClass="org.switchyard.component.rules.deploy.RulesComponent"/>
        </xsl:copy>      

      </xsl:when>
      <!-- Otherwise, we need to insert the jacc api dependency -->
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()|text()" />
        </xsl:copy>       
      </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
