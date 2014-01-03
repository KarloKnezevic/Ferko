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

	<h2>Podatci o dvoranama za ispite</h2>
	<p>Svaki redak treba sadržavati po jedan zapis formata ISVU_sifra TAB Venue#Room#Capacity TAB Venue#Room#Capacity TAB ...</p>
	<s:form action="AdminAssessmentsReserveRooms" method="post" theme="ferko">
		<s:select list="data.allAssessmentTags" listKey="shortName" listValue="name" name="data.assessmentTag" value="data.assessmentTag" label="%{getText('forms.assessmentTag')}" />
		<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="%{getText('forms.Semester')}" />
		<s:textarea name="text" rows="20" cols="80" label="Raspored" />
		<s:submit method="upload" />
	</s:form>

	<p><i>Napomena:</i> ova akcija je aditivna, u smislu da ako je kolegij već stvorio raspored dvorana,
	   na tom se kolegiju raspored neće učitavati, kako ne bi došlo do gaženja podataka.</p>
	   
</div>
