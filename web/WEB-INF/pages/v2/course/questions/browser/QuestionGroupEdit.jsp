<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <h2><s:text name="QuestionGroup.definingNew"></s:text></h2>
</s:if>
<s:else>
  <h2><s:text name="QuestionGroup.editing"></s:text></h2>
</s:else>

<s:form action="AddQuestionGroup" theme="ferko">
	<s:textfield name="data.bean.name" label="%{getText('QuestionGroup.name')}" />
	<s:hidden name="data.bean.id" />
	<s:hidden name="data.courseInstanceID" value="%{data.courseInstance.id}" />
	<s:submit method="save" />
</s:form>

<p><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000007</s:param></s:url>"><s:text name="Navigation.help"/></a></p>
