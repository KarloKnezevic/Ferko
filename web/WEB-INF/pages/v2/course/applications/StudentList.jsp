<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.definition.name"/></h2>
<p>
<s:if test="data.users.isEmpty()">
	<p class="emptyMsg">Student nema ispunjenih prijava.</p>
</s:if>
<s:else>
	<s:iterator id="userobj" value="data.users">
    	<a href="<s:url action="ApplicationAdminAprove" method = "viewStudent">
  		<s:param name="data.studentID">
 		<s:property value="#userobj.id"/></s:param>
 		<s:param name="data.courseInstanceID">
 		<s:property value="data.courseInstance.id"/></s:param>
  		<s:param name="data.fromDefinitionID">
   		<s:property value="definitionID"/></s:param>
 		</s:url>">
 		<s:text name="%{#userobj.jmbag}"/></a>, 
		<s:property value="%{#userobj.lastName}"/>, 
		<s:property value="%{#userobj.firstName}"/>, 
		<s:property value="%{#userobj.username}"/>  (Status: 
		<s:property value="data.applications.get(#userobj.id).status"/>)
 		<br>
 	</s:iterator>
</s:else>
</p>
<p>
 <a href="<s:url action="ApplicationExportList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="definitionID"><s:property value="data.definition.id"/></s:param><s:param name="format">csv</s:param></s:url>"><s:text name="Export to CSV"/></a>
|  <a href="<s:url action="ApplicationExportList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="definitionID"><s:property value="data.definition.id"/></s:param><s:param name="format">xls</s:param></s:url>"><s:text name="Export to XLS"/></a>
</p>
