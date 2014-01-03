<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data != null">
	<h2><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)<br />
	<s:property value="%{getText('appeal.assessmentAppeal')}"/>: <s:property value="data.assessment.name"/> (<s:property value="data.assessment.shortName"/>)</h2>
	<s:if test="data.userAppeals!=null && data.userAppeals.size() != 0">
		<s:property value="%{getText('appeal.existingAppeals')}"/>:
		<ul>
			<s:iterator value="data.userAppeals">
				<li>
					<s:property value="%{getText('appeal.' + type)}"/> (<s:date name="creationDate" format="dd.MM.yyyy" />) - <s:property value="status"/>
				</li>
			</s:iterator>
		</ul>
	</s:if>
	<s:include value="/WEB-INF/pages/main/assessments/AssessmentViewBodyContent.jsp"></s:include>

    <s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNotProcessed.jsp"></s:include>
    <s:include value="/WEB-INF/pages/main/assessments/appeals/TypeBadScan.jsp"></s:include>
    <s:include value="/WEB-INF/pages/main/assessments/appeals/TypeWrongSolution.jsp"></s:include>
</s:if>
</div>
