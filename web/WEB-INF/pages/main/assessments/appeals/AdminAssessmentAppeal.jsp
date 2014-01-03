<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h1><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</h1>
<div><s:property value="data.assessment.name"/></div>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:else>
  <s:property value="%{bean.appeal.creatorUser.username}"/>
  <h2><s:text name="appeal.%{bean.appeal.type}"/></h2>
  Povratak: <a href="<s:url action="AdminListAppeals"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.listCourseAppeals"/></a>
  <table>
  	<tr>
	<s:iterator value="bean.appeal.propertiesKeys" status="stat">
    	<th><s:text name="appeal.%{[0].top}"/></th>   
	</s:iterator>
	</tr>
	<tr>
	<s:iterator value="bean.appeal.propertiesValues" status="stat">
    	<td><s:property /></td>   
	</s:iterator>
	</tr>
  </table>
  <div>Status: <s:text name="appeal.%{bean.appeal.status}"/></div>
  <div>
  	<s:text name="appeal.process"/>:
  	<ul>
  		<li><a href="<s:url action="AdminProcessAssessmentAppeal!approve"><s:param name="appealID"><s:property value="bean.appeal.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="assessment.id"/></s:param></s:url>"><s:text name="appeal.approve"></s:text></a></li>
  		<li><a href="<s:url action="AdminProcessAssessmentAppeal!lock"><s:param name="appealID"><s:property value="bean.appeal.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="assessment.id"/></s:param></s:url>"><s:text name="appeal.lock"></s:text></a></li>
  		<li><a href="<s:url action="AdminProcessAssessmentAppeal!unlock"><s:param name="appealID"><s:property value="bean.appeal.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="assessment.id"/></s:param></s:url>"><s:text name="appeal.unlock"></s:text></a></li>
  		<li><a href="<s:url action="AdminProcessAssessmentAppeal!reject"><s:param name="appealID"><s:property value="bean.appeal.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="assessment.id"/></s:param></s:url>"><s:text name="appeal.reject"></s:text></a></li>
  	</ul>
  </div>
  <div>
	<s:include value="/WEB-INF/pages/main/assessments/AssessmentViewBody.jsp"></s:include>
  </div>
</s:else>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
<s:if test="data.assessment != null">
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.backToDetails"/></a>
</s:if>
</div>

</div>
