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
    <div>Na ovom mjestu možete raditi analizu rasporeda studenata. Kao rezultat ćete dobiti dvije datoteke 
         (ili tri ako odaberete opciju izrade mape zauzeća): popis slobodnih termina za svakog studenta i 
         popis zauzetih termina za svakoga studenta (te dodatno, sumarni popis zauzeća studenata ako je 
         tražena mapa zauzeća).</div>
    <div>Datum se unosi po formatu yyyy-MM-dd (primjerice, 2009-03-27). Ukoliko želite analizu rasporeda 
         svih studenata na kolegiju, tada polje za unos JMBAG-ova ostavite praznim. Ukoliko želite analizu
         za određene studente na kolegiju, u polje za unos JMBAG-ova unesite njihove JMBAG-ove (jedan JMBAG
         po retku; puni format s 10 znamenki).</div>

	<s:form action="StudentScheduleAnalyzer" method="post" theme="ferko">
		<s:if test="courseInstanceID==null || courseInstanceID.length()==0">
			<s:select name="semesterID"  value="data.yearSemester" label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle"></s:select>
		</s:if>
		<s:textfield name="dateFrom" label="%{getText('forms.dateFrom')}"></s:textfield>
		<s:textfield name="dateTo" label="%{getText('forms.dateTo')}"></s:textfield>
		<s:textarea rows="10" cols="20" name="jmbagsList" label="%{getText('forms.jmbags')}"></s:textarea>
		<s:checkbox name="createOccupancyMap" label="%{getText('forms.createOccupancyMap')}"></s:checkbox>
		<s:hidden name="courseInstanceID"></s:hidden>
		<s:submit method="viewForSemesterAndUsers" value="Dohvati ZIP arhivu"></s:submit>
		<s:submit method="showApplet" value="Pokreni interaktivni prikaz"></s:submit>
	</s:form>

</div>
