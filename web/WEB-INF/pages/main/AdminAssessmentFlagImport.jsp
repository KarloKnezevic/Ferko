<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>
<s:else>
  <div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>

<s:if test="data.bean.id==null || data.bean.length()==0">
  <div>Definiranje nove zastavice:</div>
</s:if>
<s:else>
  <div>Uređivanje zastavice:</div>
</s:else>

<s:form action="AdminAssessmentFlagImport">
<s:textarea name="text" label="%{getText('forms.data')}" rows="40" cols="80"></s:textarea>
<s:hidden name="courseInstanceID"></s:hidden>
<s:hidden name="id"></s:hidden>
<input type="hidden" name="doit" value="true" />
<s:submit></s:submit>
</s:form>

</s:else>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
</div>

</div>
