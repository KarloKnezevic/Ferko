<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <s:if test="data.assessment.assessmentConfiguration.problemsNum == 0">
  	<div><s:property value="%{getText('forms.numberOfProblemsIsZero')}" /></div>
  </s:if>
  <s:else>
  	<h1><s:text name="forms.resultsSubmit" /></h1>
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
    <s:form action="AdminUploadChoiceConf" theme="ferko">
      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
      <s:textarea name="data.text" rows="30" cols="80" label="%{getText('forms.data')}"></s:textarea>
      <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="data.appendOrReplace" required="true" label="%{getText('forms.action')}"></s:select>
	  <s:select list="data.formats" listKey="name" listValue="value" name="data.dataFormat" required="true" label="%{getText('forms.filetype')}"></s:select>
      <s:submit method="upload" value="%{getText('forms.general.update')}" />
    </s:form>
    <div><strong><s:property value="%{getText('forms.uploadFromFile')}" /></strong>
      <s:form action="AdminUploadChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden> 
        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
        <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="data.appendOrReplace" required="true" label="%{getText('forms.action')}"></s:select>
	  <s:select list="data.formats" listKey="name" listValue="value" name="data.dataFormat" required="true" label="%{getText('forms.filetype')}"></s:select>
      <s:submit method="uploadFile" value="%{getText('forms.general.update')}" />
      </s:form>
    </div>
  </s:else>
