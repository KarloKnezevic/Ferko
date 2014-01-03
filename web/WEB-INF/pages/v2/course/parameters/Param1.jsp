<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.miSched.visible">
<h2>Specifikacija parametara za izradu rasporeda međuispita</h2>
<s:if test="data.miSched.modifiable">
  <s:if test="data.miSched.modifiableUntil==null">Parametar je moguće podešavati (rok nije definiran).</s:if><s:else>Ovaj parametar moguće je podešavati do <s:property value="data.formatDateTime(data.miSched.modifiableUntil)"/>.</s:else><br><br>
  <b>Kratki opis:</b> podatci koji se unesu poslužit će za izradu rasporeda međuispita i završnih ispita.<br><br>

  <s:form action="CourseParameters1" method="POST" theme="simple">
	<h3>Ispiti</h3>
	<s:if test="data.terms.size()!=0">
	<table>
	<tr><th>Naziv</th><th>Trajanje (minuta)</th></tr>
	<s:iterator value="data.terms" status="stat">
	<s:if test="#stat.even">
	<tr style="background-color: #EEEEEE;"><td><s:property value="caption"/></td><td><s:textfield theme="simple" name="data.terms[%{#stat.index}].duration" value="%{duration}" /> min<s:hidden theme="simple" name="data.terms[%{#stat.index}].assessmentTagID" value="%{assessmentTagID}"/><s:hidden theme="simple" name="data.terms[%{#stat.index}].caption" value="%{caption}"/></td></tr>
	</s:if><s:else>
	<tr><td><s:property value="caption"/></td><td><s:textfield theme="simple" name="data.terms[%{#stat.index}].duration" value="%{duration}" /> min<s:hidden theme="simple" name="data.terms[%{#stat.index}].assessmentTagID" value="%{assessmentTagID}"/><s:hidden theme="simple" name="data.terms[%{#stat.index}].caption" value="%{caption}"/></td></tr>
	</s:else>
	</s:iterator>
	</table>
	</s:if><s:else>
	<div>Nema definiranih ispita.</div>
	</s:else>

	<h3>Dvorane</h3>
	<s:if test="data.rooms.size()!=0">
	<table>
	<tr><th>Naziv</th><th>Kapacitet (studenata)</th><th>Broj asistenata</th></tr>
	<s:iterator value="data.rooms" status="stat">
	<s:if test="#stat.even">
	<tr style="background-color: #EEEEEE;"><td><s:property value="roomName"/></td><td><s:textfield theme="simple" name="data.rooms[%{#stat.index}].students" value="%{students}" /></td><td><s:textfield theme="simple" name="data.rooms[%{#stat.index}].assistants" value="%{assistants}" /><s:hidden theme="simple" name="data.rooms[%{#stat.index}].roomName" value="%{roomName}"/><s:hidden theme="simple" name="data.rooms[%{#stat.index}].roomId" value="%{roomId}"/></td></tr>
	</s:if><s:else>
	<tr><td><s:property value="roomName"/></td><td><s:textfield theme="simple" name="data.rooms[%{#stat.index}].students" value="%{students}" /></td><td><s:textfield theme="simple" name="data.rooms[%{#stat.index}].assistants" value="%{assistants}" /><s:hidden theme="simple" name="data.rooms[%{#stat.index}].roomName" value="%{roomName}"/><s:hidden theme="simple" name="data.rooms[%{#stat.index}].roomId" value="%{roomId}"/></td></tr>
	</s:else>
	</s:iterator>
	</table>
	</s:if><s:else>
	<div>Nema definiranih dvorana.</div>
	</s:else>
	<s:hidden theme="simple" name="courseInstanceID" value="%{data.courseInstance.id}"/>
	<s:submit theme="simple" value="%{getText('forms.general.update')}" method="update" />
  </s:form>

</s:if><s:else>

<h3>Ispiti</h3>
<s:if test="data.terms.size()!=0">
<ol>
<s:iterator value="data.terms">
<li><s:property value="caption"/>: <s:property value="duration"/> min</li>
</s:iterator>
</ol>
</s:if><s:else>
<div>Nema definiranih ispita.</div>
</s:else>

<h3>Dvorane</h3>
<s:if test="data.rooms.size()!=0">
<ol>
<s:iterator value="data.rooms">
<li><s:property value="roomName"/>: <s:property value="students"/> studenata, <s:property value="assistants"/> asistenata</li>
</s:iterator>
</ol>
</s:if><s:else>
<div>Nema definiranih dvorana.</div>
</s:else>

</s:else>
</s:if>
<s:else>
<div>Ovaj parametar nije vidljiv.</div>
</s:else>

<br><br><br>