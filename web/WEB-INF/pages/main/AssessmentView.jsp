<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">
Deprecated!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data != null">
	<s:include value="/WEB-INF/pages/main/assessments/AssessmentViewBody.jsp"></s:include>
</s:if>

</div>
