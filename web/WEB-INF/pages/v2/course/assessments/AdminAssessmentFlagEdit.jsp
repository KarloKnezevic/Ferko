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
  <h2><s:text name="AssessmentFlags.definingNew"></s:text></h2>
</s:if>
<s:else>
  <h2><s:text name="AssessmentFlags.editing"></s:text></h2>
</s:else>

<s:form action="AdminAssessmentFlagEdit" theme="ferko">
<s:textfield name="bean.name" label="%{getText('AssessmentFlags.name')}"></s:textfield>
<s:textfield name="bean.shortName" label="%{getText('AssessmentFlags.shortName')}"></s:textfield>
<s:select list="data.visibilities" listKey="name" listValue="value" name="bean.visibility" label="%{getText('AssessmentFlags.flagVisibility')}" />
<s:textfield name="bean.sortIndex" label="%{getText('AssessmentFlags.sortIndex')}" />
<s:select list="data.tags" listKey="id" listValue="name" name="bean.assesmentFlagTagID" label="%{getText('AssessmentFlags.flagTag')}"></s:select>
<s:textfield name="bean.programType" label="%{getText('AssessmentFlags.programType')}"></s:textfield>
<s:textarea name="bean.program" label="%{getText('AssessmentFlags.program')}" cols="80" rows="20"></s:textarea>
<s:hidden name="bean.id"></s:hidden>
<s:hidden name="bean.courseInstanceID"></s:hidden>
<s:hidden name="bean.programVersion"></s:hidden>
<s:submit method="saveFlag" />
</s:form>

<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000008</s:param></s:url>"><s:text name="Navigation.help"/></a></div>
