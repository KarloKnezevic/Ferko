<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h1 class="pageh">Rezultati ankete: <em><s:property value="data.title" /></em></h1>
<div class="content withsidecontent">
	<div class="sidecontent">
		<h2>Administracija</h2>
		<ul class="sidenav">
			<li>
				<a href="<s:url action="Delete" method="deletePoll"><s:param name="id"><s:property value="data.id"/></s:param></s:url>" class="action-delete">Izbriši anketu</a>
			</li>
		</ul>
		<h2>Prikaži rezultate grupa</h2>
		<s:form action="Results" theme="ferko" cssClass="sidepaneform cmxform" method="get">
			<s:checkboxlist list="data.groups" listKey="id" listValue="name" name="group" label="Izaberi grupe za prikaz" />
			<s:hidden name="id" value="%{data.id}"/>
			<s:submit />
		</s:form>
	</div>

	<div class="maincontent">
	<div class="inner-padding">

		<ul>
		<s:iterator value="data.questions">
			<li>
			<s:property value="questionText" />
			<s:if test="type == 'TEXT'">
				<s:if test="textAnswers.isEmpty()">
					<div>Nema odgovora!</div>
				</s:if>
				<s:else>
				<ul>
					<s:iterator value="textAnswers">
						<li>
							<s:property value="answer"/>
							<a href="<s:url action="SinglePollResults" method="viewSinglePollResults"><s:param name="apid"><s:property value="answeredPollId"/></s:param></s:url>">?</a>
						</li>
					</s:iterator>
				</ul>
				</s:else>
			</s:if>
			<s:if test="type == 'OPTION'">
				<ul>
					<s:iterator value="optionAnswers">
						<li><s:property value="text" /> - <strong><s:property value="counter" /></strong></li>
					</s:iterator>
				</ul>
			</s:if>
			<s:if test="type == 'RATING'">
				<ul>
					<li>Prosjek: <strong><s:property value="avgRating" /></strong></li>
				</ul>
			</s:if>
			</li>
		</s:iterator>
		</ul>

	</div>
	</div>
</div>
