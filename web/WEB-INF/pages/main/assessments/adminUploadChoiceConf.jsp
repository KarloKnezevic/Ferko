<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>
<h2><s:property value="data.assessment.name"/></h2>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:else>
  <s:if test="data.assessment.assessmentConfiguration.problemsNum == 0">
  	<div><s:property value="%{getText('forms.numberOfProblemsIsZero')}" /></div>
  </s:if>
  <s:else>
  	<h1><s:property value="%{getText('forms.resultsSubmit')}" /></h1>
  	<div>
	  	<div><strong>Format:</strong></div>
		<table border="1">
			<tr>
				<td>JMBAG</td>
				<td>Grupa</td>
				<td>Odgovor1</td>
				<td>Odgovor2</td>
				<td>Odgovor3</td>
				<td>...</td>
				<td>OdgovorN</td>
			</tr>
			<tr>
				<td>JMBAG</td>
				<td>Grupa</td>
				<td>Odgovor1</td>
				<td>Odgovor2</td>
				<td>Odgovor3</td>
				<td>...</td>
				<td>OdgovorN</td>
			</tr>
			<tr>
				<td>...</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
		<div><em>Elementi su međusobno odvojeni tabovima.<br />
		Grupa može biti BLANK (nije označena).<br />
		Odgovor može biti BLANK (neodgovoreno).</em></div>
	</div>
    <s:form action="AdminUploadChoiceConf">
      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
      <s:textarea name="text" rows="30" cols="80" label="%{getText('forms.data')}"></s:textarea>
      <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="appendOrReplace" required="true"></s:select>
	  <s:select list="formats" listKey="name" listValue="value" name="dataFormat" required="true" label="%{getText('forms.filetype')}"></s:select>
      <s:submit method="upload"></s:submit>
    </s:form>
    <div><strong><s:property value="%{getText('forms.uploadFromFile')}" /></strong>
      <s:form action="ChoiceConfInputFromFile" method="post" enctype="multipart/form-data">
        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden> 
        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
        <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="appendOrReplace" required="true"></s:select>
	  <s:select list="formats" listKey="name" listValue="value" name="dataFormat" required="true" label="%{getText('forms.filetype')}"></s:select>
        <s:submit></s:submit>
      </s:form>
    </div>
  </s:else>
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
