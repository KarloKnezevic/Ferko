<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.fullList">
  <p><a href="<s:url action="ApplicationAdminTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationTableShort"/></a></p>
</s:if><s:else>
  <p><a href="<s:url action="ApplicationAdminTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="data.fullList">true</s:param></s:url>"><s:text name="Navigation.applicationTableFull"/></a></p>
</s:else>
<p>
 <a href="<s:url action="ApplicationExportTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">csv</s:param></s:url>"><s:text name="Export to CSV"/></a>
 | <a href="<s:url action="ApplicationExportTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">xls</s:param></s:url>"><s:text name="Export to XLS"/></a>
</p>

<s:if test="data.definitions == null || data.definitions.isEmpty()">
  <p class="emptyMsg">Nema definiranih prijava.</p>
</s:if>
<s:else>
  <table class="bigTable">
    <thead>
    <tr>
      <th>Broj</th>
      <th style="text-align: left;">Student</th>
      <s:iterator value="data.definitions">
             <th><s:property value="shortName"/></th>
      </s:iterator>
    </tr>
    </thead>
    <tbody>
    <s:iterator id="userobj" value="data.users" status="stat">
    	<tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
		<td><s:property value="%{#stat.count}"/>.</td>
 		<td style="text-align: left;"><a href="<s:url action="ApplicationAdminAprove" method = "viewStudent">
 		    <s:param name="data.studentID">
 		    <s:property value="#userobj.id"/></s:param>
 		    <s:param name="data.courseInstanceID">
 		    <s:property value="data.courseInstance.id"/></s:param>
 		    </s:url>">
 		    <s:property value="%{#userobj.lastName}"/>, <s:property value="%{#userobj.firstName}"/> (<s:property value="%{#userobj.jmbag}"/>),
 		    <s:property value="%{#userobj.username}"/></a>
 		</td>
 		<s:iterator id="defobj" value="data.definitions">
 			<s:if test="data.applications.get(#userobj.id).get(#defobj.id) == null">
 				<td>-</td>
			</s:if>
			<s:else>
			 	<td><s:property value="data.applications.get(#userobj.id).get(#defobj.id).status"/></td>
			</s:else>
		</s:iterator>
		</tr>		
	</s:iterator>
    </tbody>
</table>
</s:else>
<s:if test="data.fullList">
  <p><a href="<s:url action="ApplicationAdminTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationTableShort"/></a></p>
</s:if><s:else>
  <p><a href="<s:url action="ApplicationAdminTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="data.fullList">true</s:param></s:url>"><s:text name="Navigation.applicationTableFull"/></a></p>
</s:else>
<p>
 <a href="<s:url action="ApplicationExportTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">csv</s:param></s:url>"><s:text name="Export to CSV"/></a>
 | <a href="<s:url action="ApplicationExportTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">xls</s:param></s:url>"><s:text name="Export to XLS"/></a>
</p>
