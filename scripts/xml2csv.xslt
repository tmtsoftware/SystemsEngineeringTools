<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:fo="http://www.w3.org/1999/XSL/Format" >
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">class, title, duration (ms), status
<xsl:for-each select="//cases/case">
  <xsl:variable name="status">
   <xsl:choose>
     <xsl:when test="skipped='true'">
        <xsl:value-of select="'Skipped'"/>
     </xsl:when>
     <xsl:when test="errorDetails">
        <xsl:value-of select="'FAILED'"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="'PASSED'"/>
     </xsl:otherwise>
   </xsl:choose>
</xsl:variable>
   <xsl:value-of select="concat(className, ',' , testName, ',' , duration,',', $status)"/>
<xsl:text>&#10;</xsl:text>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
