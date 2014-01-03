<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:text name="Navigation.gradingPolicy"></s:text></h2>

<s:if test="data.editable">

<s:form action="ShowGradingPolicy" theme="ferko" method="post">
<s:select list="data.bean.gradesVisibilities" listKey="name" listValue="value" name="data.bean.gradesVisibility" label="%{getText('forms.grades.visibility')}" />
<s:checkbox name="data.bean.gradesValid" label="%{getText('forms.grades.valid')}" disabled="true" />
<s:checkbox name="data.bean.gradesLocked" label="%{getText('forms.grades.locked')}" />
<s:textfield name="data.bean.termDate" label="%{getText('forms.grades.termDate')}" />
<s:select list="data.bean.policyImplementations" listKey="name" listValue="value" name="data.bean.policyImplementation" label="%{getText('forms.grades.policyImpl')}"  />
<s:if test="data.bean.policyImplementation!=null">
<s:include value="/WEB-INF/pages/v2/course/grades/policy/Settings%{data.bean.policyImplementation}.jsp"></s:include>
</s:if>
<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
<s:submit method="update" value="%{getText('forms.update')}" />
</s:form>

<h2>Podjela ocjena</h2>
<p>Koristeći link u nastavku možete pokrenuti podjelu ocjena. Ukoliko je vidljivost ocjena nije postavljena na "Ocjene su vidljive", studenti neće vidjeti ocjene. Ukoliko je vidljivost ocjena postavljena na "Ocjene su vidljive", studenti će vidjeti ocjene i automatski će biti obaviješteni.</p>

<a style="padding-left: 10px; padding-right: 10px; padding-top: 5px; padding-bottom: 5px; background-color: #FFCCCC; border: 1px solid #FF0000;" href="<s:url action="ShowGradingPolicy" method="runGrading"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.runGrading"/></a>
<br><br>
</s:if><s:else>
<ul>
<li><s:text name="forms.grades.visibility" />: <s:text name="forms.grades.visibility.%{data.bean.gradesVisibility}" /></li>
<li><s:text name="forms.grades.valid" />: <s:text name="forms.grades.valid_%{data.bean.gradesValid}" /></li>
<li><s:text name="forms.grades.locked" />: <s:text name="forms.grades.locked.%{data.bean.gradesLocked}" /></li>
<li><s:text name="forms.grades.termDate" />: <s:property value="data.bean.termDate" /></li>
<li><s:text name="forms.grades.policyImpl" />: <s:text name="forms.grades.policyImpl.%{data.bean.policyImplementation}" /></li>
</ul>
</s:else>

<s:set name="gp" value="data.courseInstance.gradingPolicy" />

<s:if test="#gp.gradesValid">
<a style="padding-left: 10px; padding-right: 10px; padding-top: 5px; padding-bottom: 5px; background-color: #FFCCCC; border: 1px solid #FF0000;" href="<s:url action="ShowGradingPolicy" method="showGrades"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.viewGrades"/></a>
<br><br>
<s:if test="data.bean.graders!=null && !data.bean.graders.isEmpty()">
<s:form action="ShowGradingPolicy" theme="ferko" method="post">
<s:iterator status="grr" value="data.bean.graders">
<s:select list="data.bean.graderUsers" listKey="name" listValue="value" name="data.bean.graders[%{#grr.index}].userID" value="%{userID}" label="%{group}" />
<s:hidden name="data.bean.graders[%{#grr.index}].groupID" value="%{groupID}" />
</s:iterator>
<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
<s:submit method="exportISVUGrades" value="%{getText('forms.exportISVUGradesXML')}" />
</s:form>
</s:if>
<br><br>
</s:if>

<s:if test="#gp.gradesValid && #gp.gradingStat!=null">

<s:set name="gs" value="data.courseInstance.gradingPolicy.gradingStat" />
<h2>Informacije o podijeljenim ocjenama</h2>

<h3>Postotci - na kolegiju</h3>
Broj studenata koji su položili kolegij: <s:property value="#gs.passed"/> (<s:property value="data.df(#gs.passed/(#gs.passed+#gs.failed+0.0)*100,1)"/>%)<br>
Broj studenata koji nisu položili kolegij: <s:property value="#gs.failed"/> (<s:property value="data.df(#gs.failed/(#gs.passed+#gs.failed+0.0)*100,1)"/>%)<br><br>

<h3>Pragovi</h3>
Prag za ocjenu 5: <s:property value="data.dfScore(#gs.gradeTresholds[3])"/><br>
Prag za ocjenu 4: <s:property value="data.dfScore(#gs.gradeTresholds[2])"/><br>
Prag za ocjenu 3: <s:property value="data.dfScore(#gs.gradeTresholds[1])"/><br>
Prag za ocjenu 2: <s:property value="data.dfScore(#gs.gradeTresholds[0])"/><br><br>

<h3>Postotci - od studenata koji su prošli</h3>
Broj studenata s ocjenom 5: <s:property value="#gs.gradeCounts[4]"/> (<s:property value="data.df(#gs.gradeCounts[4]/(#gs.gradeCounts[1]+#gs.gradeCounts[2]+#gs.gradeCounts[3]+#gs.gradeCounts[4]+0.0)*100,1)"/>%)<br>
Broj studenata s ocjenom 4: <s:property value="#gs.gradeCounts[3]"/> (<s:property value="data.df(#gs.gradeCounts[3]/(#gs.gradeCounts[1]+#gs.gradeCounts[2]+#gs.gradeCounts[3]+#gs.gradeCounts[4]+0.0)*100,1)"/>%)<br>
Broj studenata s ocjenom 3: <s:property value="#gs.gradeCounts[2]"/> (<s:property value="data.df(#gs.gradeCounts[2]/(#gs.gradeCounts[1]+#gs.gradeCounts[2]+#gs.gradeCounts[3]+#gs.gradeCounts[4]+0.0)*100,1)"/>%)<br>
Broj studenata s ocjenom 2: <s:property value="#gs.gradeCounts[1]"/> (<s:property value="data.df(#gs.gradeCounts[1]/(#gs.gradeCounts[1]+#gs.gradeCounts[2]+#gs.gradeCounts[3]+#gs.gradeCounts[4]+0.0)*100,1)"/>%)<br><br>

</s:if>

