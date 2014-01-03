<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <h2><s:text name="Assessments.stat.on"/> <s:property value="data.assessment.name"/></h2>

    <table>
      <tr>
        <td><s:text name="Assessments.stat.students" /></td><td><s:property value="%{data.statBase.count}"/></td>
      </tr>
      <tr>
        <td><s:text name="Assessments.stat.average" /></td><td><s:property value="%{data.statBase.average}"/></td>
      </tr>
      <tr>
        <td><s:text name="Assessments.stat.median" /></td><td><s:property value="%{data.statBase.median}"/></td>
      </tr>
    </table>


      <div style="text-align: center;">
      	<img id="hslika" src="<s:url action="StudentAssessmentStat" method="scoreHistogram"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="kind">E</s:param></s:url>">
      </div>
