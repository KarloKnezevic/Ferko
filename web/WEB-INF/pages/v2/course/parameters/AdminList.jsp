<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<% boolean anyDisplayed = false; %>
<s:if test="data.miSched.visible">
<% anyDisplayed = true; %>
<h2>Specifikacija parametara za izradu rasporeda međuispita</h2>
<s:if test="data.miSched.modifiable">
  <s:if test="data.miSched.modifiableUntil==null">Parametar je moguće podešavati (rok nije definiran).</s:if><s:else>Ovaj parametar moguće je podešavati do <s:property value="data.formatDateTime(data.miSched.modifiableUntil)"/>.</s:else><br><br>
  <b>Kratki opis:</b> podatci koji se unesu poslužit će za izradu rasporeda međuispita i završnih ispita.<br><br>
</s:if><s:else>
Parametar više nije moguće podešavati.<br><br>
</s:else>
<a href="<s:url action="CourseParameters1"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>
</s:if>

<h2>Postavke web stranica kolegija</h2>
<% anyDisplayed = true; %>
<p><b>Kratki opis:</b> Ovdje možete uređivati postavke koje kontroliraju prikaz stranica kolegija.</p>
<a href="<s:url action="CourseParameters2"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>

<% if(!anyDisplayed) { %>
<div>Nema parametara koje biste mogli definirati.</div>
<% } %>

