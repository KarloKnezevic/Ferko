<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content withsidecontent poll-results">
	<div class="sidecontent">
	<s:if test='data.administrationPermissions.contains("canDeletePoll")
				|| data.administrationPermissions.contains("canEditPoll")
				|| data.administrationPermissions.contains("canProlongPoll")'>
		<h2>Administracija</h2>
		<ul class="sidenav">
		<s:if test='data.administrationPermissions.contains("canDeletePoll")'>
			<li>
				<a href="<s:url action="Delete" method="deletePoll"><s:param name="id"><s:property value="data.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>" class="action-delete">Izbriši anketu</a>
			</li>
		</s:if>
		<s:if test='data.administrationPermissions.contains("canEditPoll")'>
			<li>
				<a href="<s:url action="CourseEdit" method="editPoll"><s:param name="id"><s:property value="data.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>" class="action-edit">Uredi anketu</a>
			</li>
		</s:if>
		<s:if test='data.administrationPermissions.contains("canProlongPoll")'>
			<li>
				<a href="<s:url action="CourseProlong" method="prolong"><s:param name="id"><s:property value="data.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>" class="action-prolongpoll">Produži anketu</a>
			</li>
		</s:if>
		</ul>
	</s:if>
		<h2>Ostalo</h2>
		<s:if test='data.administrationPermissions.contains("canViewSingleResults")'>
		<ul class="sidenav">
			<li>
				<a href="<s:url action="CSVResults"><s:param name="id"><s:property value="data.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>" class="action-csv">Svi odgovori (.csv)</a>
			</li>
			<li>
				<a href="<s:url action="SinglePollResults"><s:param name="apid"><s:property value="data.answeredPollId"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>" class="action-singlepoll">Pojedinačni odgovori</a>
			</li>
		</ul>
		</s:if>
		<h2>Prikaži rezultate grupa</h2>
		<s:form action="CourseResults" theme="ferko" cssClass="sidepaneform cmxform" method="get">
			<li><fieldset>
			<legend>Izaberi grupe za prikaz</legend>
			<s:iterator value="data.groups">		
			<label>
			<input type="checkbox" name="group" value="<s:property value="id" />" id="Results_group"
			<s:if test='selected.contains(id)'>checked="checked"</s:if> />
			<s:property value="name" />
			</label>
			</s:iterator>
			</fieldset>
			</li>
			<s:hidden name="id" value="%{data.id}"/>
			<s:hidden name="courseInstanceID" value="%{courseInstanceID}"/>
			<s:submit />
		</s:form>
	</div>

	<div class="maincontent">
	<div class="inner-padding">
		<h2>Rezultati ankete: <em><s:property value="data.poll.title" /></em></h2>
		<p>Anketa je ispunjena <s:property value="data.answeredPollsCounter" /> puta.</p>
		<ul class="questions">
		<s:iterator value="data.questions">
			<li class="question">
			<span class="questionText"><s:property value="questionText" /></span>
			<s:if test="type == 'TEXT'">
				<s:if test="textAnswers.isEmpty()">
					<div>Nema odgovora!</div>
				</s:if>
				<s:else>
				<ul class="textAnswers">
					<s:iterator value="textAnswers">
						<li>
							<s:property value="answer"/>
							<a href="<s:url action="SinglePollResults"><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param><s:param name="apid"><s:property value="answeredPollId"/></s:param></s:url>">?</a>
						</li>
					</s:iterator>
				</ul>
				</s:else>
			</s:if>
			<s:if test="type == 'OPTION'">
				<ul class="optionAnswers">
					<s:iterator value="optionAnswers">
						<li><s:property value="text" />: <strong><s:property value="counter" /></strong></li>
					</s:iterator>
				</ul>
			</s:if>
			<s:if test="type == 'RATING'">
				<ul class="ratingAnswers">
					<li>Prosjek: <strong><s:property value="avgRating" /></strong></li>
				</ul>
			</s:if>
			</li>
		</s:iterator>
		</ul>

	</div>
	</div>
</div>
