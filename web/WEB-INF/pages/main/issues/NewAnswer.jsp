<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>
	
	<h3><s:text name = "ITS.newIssueAnswerSubtitle" /></h3>
	
	<a href="<s:url action="ViewIssue" method="execute"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param><s:param name="issueID"><s:property value="data.messageBean.ID"/></s:param></s:url>">
				<s:text name = "ITS.backToIssueLink" />
	</a> &nbsp;&nbsp;&nbsp;&nbsp;
	
	<br><br>
	
	<link rel="stylesheet" type="text/css" href="/ferko/css/issues.css" />
	
<div id="content" class="ticket">

  <div id="ticket">
	 <h2 class="summary"><s:property value="data.messageBean.messageName"/></h2>
	 <table class="properties">
		  <tr>
			   <th>Autor:</th>
			   		<td><s:property value="data.messageBean.ownerName"/></td>
			   <th>Status:</th>
			   		<td><s:property value="data.messageBean.messageStatus"/></td>
		  </tr>
		  <tr>
			    <th>Postavljeno:</th>
				    <td><s:property value="data.messageBean.creationDate"/></td>
			    <th>Zadnja izmjena:</th>
				    <td><s:property value="data.messageBean.lastModificationDate"/></td>
		  </tr>
		  <tr>
			    <th>Pitanje javno:</th>
			    	<td headers="h_component">
			    		<s:property value="data.messageBean.publicity"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			    	</td>
			    <th>Tema:</th>
					<td><s:property value="data.messageBean.topicName"/></td>
		  </tr>
	 </table>
	   <div class="description">
		   <p>
				<s:property value="data.messageBean.messageContent"/>
		   </p>
	   </div>
  	</div>
</div>

	
	
	<s:form action="ViewIssue" method="post" theme="ferko">
		<s:hidden name="courseInstanceID" value="%{courseInstanceID}"/>
		<s:hidden name="issueID" value="%{data.messageBean.ID}"/>
		<s:textarea rows="8" cols="50" name="data.answerBean.content" label="%{getText('ITS.answerContent')}" />
		<s:submit method="sendAnswer" type="button" label="%{getText('ITS.sendNewAnswerButton')}" ></s:submit>
	</s:form>


