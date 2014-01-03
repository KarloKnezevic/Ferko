<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <h2>Definiranje nove provjere znanja</h2>
</s:if>
<s:else>
  <h2>Uređivanje provjere znanja</h2>
</s:else>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:form action="AdminAssessmentEdit" theme="ferko">
	<s:textfield name="bean.name" label="%{getText('forms.name')}" />
	<s:textfield name="bean.shortName" label="%{getText('forms.shortName')}" />
	<s:select list="data.visibilities" listKey="name" listValue="value" name="bean.visibility" label="%{getText('forms.assesmentVisibility')}" />
	<s:textfield name="bean.sortIndex" label="%{getText('forms.sortIndex')}" />
	<s:checkbox name="bean.locked" label="%{getText('forms.locked')}" />
	<s:select list="data.tags" listKey="id" listValue="name" name="bean.assesmentTagID" label="%{getText('forms.assesmentTag')}" />
	<s:select list="data.flags" listKey="id" listValue="name" name="bean.assesmentFlagID" label="%{getText('forms.assesmentFlag')}" />
	<s:textfield name="bean.maxScore" label="%{getText('forms.maxScore')}" />
	<s:select list="data.possibleParents" listKey="id" listValue="name" name="bean.parentID" label="%{getText('forms.parent')}" />
	<s:select list="data.possibleChainedParents" listKey="id" listValue="name" name="bean.chainedParentID" label="%{getText('forms.chainedParent')}" />
	<s:textfield name="bean.startsAt" label="%{getText('forms.startsAt')}" />
	<s:textfield name="bean.duration" label="%{getText('forms.duration')}" />
	<s:textfield name="bean.programType" label="%{getText('forms.programType')}" />
	<s:textarea name="bean.program" label="%{getText('forms.program')}" cols="80" rows="20" />
	<s:hidden name="bean.id" />
	<s:hidden name="bean.courseInstanceID" />
	<s:hidden name="bean.programVersion" />
	<s:submit method="saveAssessment" />
</s:form>

<p><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000007</s:param></s:url>"><s:text name="Navigation.help"/></a></p>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
<s:if test="data.assessment!=null && data.assessment.id!=null">
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.assessment"/></a>
</s:if>
</div>
