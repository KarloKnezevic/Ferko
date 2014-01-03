<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content withsidecontent poll-results">
	<div class="sidecontent">
		<ul class="ap-neighbours">
			<s:if test="data.prevAnsweredPoll != null">
			<li class="ap-prev">
				<a href="<s:url action="SinglePollResults"><s:param name="apid"><s:property value="data.prevAnsweredPoll.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>">Prethodna</a>
			</li>
			</s:if>
			<s:else>
			<li class="ap-prev">Prethodna</li>
			</s:else>

			<s:if test="data.nextAnsweredPoll != null">
			<li class="ap-next">
				<a href="<s:url action="SinglePollResults"><s:param name="apid"><s:property value="data.nextAnsweredPoll.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>">Sljedeća</a>
			</li>
			</s:if>
			<s:else>
			<li class="ap-next">Sljedeća</li>
			</s:else>
		</ul>
		<h2>Opcije</h2>
		<ul class="sidenav">
			<li>
				<a href="<s:url action="CourseResults"><s:param name="id"><s:property value="data.poll.id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstanceID"/></s:param></s:url>">Sumarni pregled ankete</a>
			</li>
		</ul>
	</div>

	<div class="maincontent">
	<div class="inner-padding">
		<h2>Pojedinačni rezultati ankete: <em><s:property value="data.poll.title" /></em></h2>
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
