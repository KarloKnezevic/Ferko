<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <div>Definiranje nove zastavice:</div>
</s:if>
<s:else>
  <div>Uređivanje zastavice:</div>
</s:else>

<s:form action="AdminAssessmentFlagEdit">
<s:textfield name="bean.name" label="%{getText('forms.name')}"></s:textfield>
<s:textfield name="bean.shortName" label="%{getText('forms.shortName')}"></s:textfield>
<s:select list="data.visibilities" listKey="name" listValue="value" name="bean.visibility" label="%{getText('forms.assesmentVisibility')}" />
<s:textfield name="bean.sortIndex" label="%{getText('forms.sortIndex')}" />
<s:select list="data.tags" listKey="id" listValue="name" name="bean.assesmentFlagTagID" label="%{getText('forms.assesmentFlagTag')}"></s:select>
<s:textfield name="bean.programType" label="%{getText('forms.programType')}"></s:textfield>
<s:textarea name="bean.program" label="%{getText('forms.program')}" cols="80" rows="20"></s:textarea>
<s:hidden name="bean.id"></s:hidden>
<s:hidden name="bean.courseInstanceID"></s:hidden>
<s:hidden name="bean.programVersion"></s:hidden>
<s:submit method="saveFlag" />
</s:form>

<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000008</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
</div>
