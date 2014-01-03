<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.assessmentFlag.name"/></h2>

<table cols="2" rows="2" width="100%">
<tr><td colspan="2" align="justify">Jeste li sigurni da želite obrisati sve ručno unesene vrijednosti na ovoj zastavici? Ako nastavite, ovu akciju više ne možete poništiti.<br>Ako nastavite, nemojte zaboraviti da se efektivne vrijednosti zastavice neće odmah promijeniti -- promjena će biti vidljiva tek kada zatražite ažuriranje svih provjera i vrijednosti zastavica.</td></tr>
<tr><td align="right">
  <s:form action="AdminAssessmentList" method="post" theme="simple">
    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
    <s:submit value="%{getText('forms.cancel')}" align="right"></s:submit>
  </s:form>
</td><td align="left">
  <s:form action="AdminAssessmentFlagData" method="post" theme="simple">
    <s:hidden name="assessmentFlagID" value="%{data.assessmentFlag.id}" />
    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
    <s:hidden name="data.confirmed" value="true" />
    <s:submit method="reset" value="%{getText('forms.update')}" align="left"></s:submit>
  </s:form>
</td></tr>
</table>
