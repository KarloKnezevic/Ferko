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

<s:if test="data != null">
  <div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>

  <div>Podaci o provjeri znanja: <s:property value="data.assessment.name"/></div>

  <table border="1">
    <tr><th>Kratki naziv</th><td><s:property value="data.assessment.shortName"/></td></tr>
    <tr><th>Maksimalni broj bodova</th><td><s:property value="data.assessment.maxScore"/></td></tr>
    <tr><th>Preduvjet</th><td><s:if test="data.assessment.assessmentFlag!=null"><s:property value="data.assessment.assessmentFlag.name"/></s:if><s:else> </s:else></td></tr>
    <s:if test="data.canTake">
      <s:if test="data.flagValue != null && data.flagValue.error">
        <tr><th>Može pristupiti</th><td>Nepoznato - dogodila se je pogreška prilikom izračuna prava.</td></tr>
      </s:if>
      <s:else>
        <tr><th>Može pristupiti</th><td>Može.</td></tr>
      </s:else>
    </s:if>
    <s:else>
      <tr><th>Može pristupiti</th><td>Ne može.</td></tr>
    </s:else>
    <s:if test="data.score != null && data.score.error">
    <tr><th>Poruka</th><td>Dogodila se pogreška prilikom izračuna bodova.</td></tr>
    </s:if>
    <s:else>
	  <s:if test="data.score==null || !data.score.present">
	    <tr><th>Pristupili</th><td>NE</td></tr>
	  </s:if>
	  <s:else>
	    <tr><th>Pristupili</th><td>DA</td></tr>
	    <tr><th>Ostvareni bodovi prije obrade</th><td><s:property value="data.score.rawScore"/></td></tr>
	    <tr><th>Konačni ostvareni bodovi</th><td><s:property value="data.score.score"/></td></tr>
	    <tr><th>Provjera položena</th><td><s:property value="data.score.status"/></td></tr>
	    <tr><th>Rang</th><td><s:property value="data.score.rank"/></td></tr>
	    <tr><th>Bodove unio</th><td><s:if test="data.score.assigner!=null"><s:property value="data.score.assigner.lastName"/> <s:property value="data.score.assigner.firstName"/></s:if> </td></tr>
	  </s:else>
    </s:else>
  </table>

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

<s:if test="data.files!=null && data.files.size() != 0">
  <div>Sljedeće datoteke su uploadane na ispit:
    <ul>
      <s:iterator value="data.files">
      <li>
        <a href="<s:url action="AssessmentFileDownload"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="assessmentFileID"><s:property value="id"/></s:param></s:url>"><s:property value="descriptor"/></a>
        <s:if test="originalFileName==null">
          <s:property value="id"/> (<s:property value="mimeType"/>)
        </s:if>
        <s:else>
          <s:property value="originalFileName"/> (<s:property value="mimeType"/>)
        </s:else>
        <s:if test="description!=null">
          <br/><s:property value="description"/>
        </s:if>
      </li>
      </s:iterator>
    </ul>
  </div>
</s:if>
   	<s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNotProcessed.jsp"></s:include>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AssessmentSummaryView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessmentsOnCourse"/></a>
</div>

</s:if>

</div>
