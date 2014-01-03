<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

    <h1>Analiza rasporeda studenata</h1>
    <div>Na ovom mjestu možete raditi analizu rasporeda studenata. Kao rezultat ćete dobiti dvije datoteke: 
         popis slobodnih termina za svakog studenta i popis zauzetih termina za svakoga studenta.</div>
    <div>Datum se unosi po formatu yyyy-MM-dd (primjerice, 2009-03-27).</div>

	<s:form action="StudentScheduleAnalyzer" method="post" theme="ferko">
		<s:select name="semesterID"  value="data.yearSemester" label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle"></s:select>
		<s:textfield name="dateFrom" label="%{getText('forms.dateFrom')}"></s:textfield>
		<s:textfield name="dateTo" label="%{getText('forms.dateTo')}"></s:textfield>
		<s:submit method="viewForSemester"></s:submit>
	</s:form>

</div>
