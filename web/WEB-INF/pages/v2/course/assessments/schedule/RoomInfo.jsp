<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.roomName"/></h2>
<div>Detalji: </div>
<table>
	<thead>
	<tr>
	  <th colspan="2">Asistenti</th>
	</tr>
	<tr>
	  <th>Prezime</th>
      <th>Ime</th>
	</tr>
	</thead>
	<tbody>
	<s:if test="data.assistantList != null && data.assistantList.size()>0">
	<s:iterator value="data.assistantList" status="stat">
    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
	  <td><s:property value="lastName"/></td>
	  <td><s:property value="firstName"/></td>
	</tr>
	</s:iterator>
	</s:if>
	<s:else>
	<tr>
	  <td colspan="2">Nema asistenata</td>
	</tr>
	</s:else>
	</tbody>
</table>
<table>
    <thead>
    <tr>
      <th colspan="4">Studenti</th>
    <tr>
      <th>RB.</th>
      <th>Jmbag</th>
      <th>Prezime</th>
      <th>Ime</th>
    </tr>
    </thead>
    <tbody>
    <s:if test="data.userList != null && data.userList.size()>0">
    <s:iterator value="data.userList" status="stat">
    <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="%{#stat.index+1}"/> </td>
      <td><s:property value="jmbag"/></td>
      <td><s:property value="lastName"/></td>
      <td><s:property value="firstName"/></td>
    </tr>
    </s:iterator>
    </s:if>
    <s:else>
	<tr>
	  <td colspan="4">Nema studenata</td>
	</tr>
	</s:else>
    </tbody>
</table>
