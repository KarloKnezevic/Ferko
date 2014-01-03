<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:property value="data.assessmentFlag.name"/> <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000036</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <div>
   <s:iterator value="data.letters" status="stat">
     <s:if test="#stat.first == false"> | </s:if>
     <s:if test="[0].toString()==data.letter">
       <s:property/>
     </s:if>
     <s:else>
       <a href="<s:url action="AdminAssessmentFlagData" method="pickLetter"><s:param name="data.assessmentFlagID"><s:property value="data.assessmentFlag.id"/></s:param><s:param name="data.letter"><s:property/></s:param></s:url>"><s:property/></a>
     </s:else>
   </s:iterator>
  </div>

  <s:form action="AdminAssessmentFlagData" method="post" theme="simple">
    <table>
    <tr>
      <th>Student</th>
      <th>Fiksiraj vrijednost</th>
      <th>Vrijednost zastavice</th>
      <th>Trenutna efektivna vrijednost</th>
      <th>Bodove unio</th>
    </tr>
    <s:iterator value="data.flagValues" status="stat">
    <s:if test="#stat.even"><tr style="background-color: #EEEEEE;"></s:if><s:else><tr></s:else>
      <td>
        <s:hidden name="data.flagValues[%{#stat.index}].studentId" value="%{studentId}"/>
        <s:hidden name="data.flagValues[%{#stat.index}].id" value="%{id}"/>
        <s:hidden name="data.flagValues[%{#stat.index}].originalManuallySet" value="%{originalManuallySet}"/>
        <s:hidden name="data.flagValues[%{#stat.index}].originalManualValue" value="%{originalManualValue}"/>
        <s:hidden name="data.flagValues[%{#stat.index}].version" value="%{version}"/>
        <s:property value="studentLastName"/>, <s:property value="studentFirstName"/> (<s:property value="studentJMBAG"/>)
      </td>
      <td>
        <s:checkbox name="data.flagValues[%{#stat.index}].manuallySet" value="%{manuallySet}" />
      </td>
      <td>
        <s:checkbox name="data.flagValues[%{#stat.index}].manualValue" value="%{manualValue}" />
      </td>
      <td>
        <s:if test="value">1</s:if><s:else>0</s:else>
      </td>
      <td>
        <s:if test="assignerJMBAG==null">-</s:if><s:else><s:property value="assignerLastName"/>, <s:property value="assignerFirstName"/></s:else>
      </td>
    </tr>
    </s:iterator>
    </table>
    <s:hidden name="data.letter" value="%{data.letter}" />
    <s:hidden name="assessmentFlagID" value="%{data.assessmentFlagID}" />
    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
    <s:submit method="save" value="%{getText('forms.update')}"></s:submit>
  </s:form>

