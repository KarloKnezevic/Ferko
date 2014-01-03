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

	<h2>Podatci o kolegijima</h2>
	<p>Svaki redak treba sadržavati po jedan zapis formata ISVU_sifra TAB Naziv_kolegija</p>
	<s:form action="OpenCourseInstances" method="post" theme="ferko">
		<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="Semestar" />
		<s:textarea name="text" rows="20" cols="80" label="Kolegiji" />
		<s:submit method="upload" />
	</s:form>

	<p><i>Napomena:</i> ova akcija je aditivna, u smislu da ako kolegij već postoji, ne dira ga, a ako kolegij
	   ne postoji u sustavu, dodaje ga. Kolegiji koji postoje u sustavu ali nisu na ovoj listi također se ne
	   diraju (neće biti obrisani).</p>
	   
</div>
