<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.assessment.assessmentConfiguration.problemsNum == 0 || data.assessment.assessmentConfiguration.correctAnswers == null || data.assessment.assessmentConfiguration.correctAnswers.equals('') || (data.assessment.assessmentConfiguration.scoreCorrect == 0 && (data.assessment.assessmentConfiguration.detailTaskScores == null || data.assessment.assessmentConfiguration.detailTaskScores.equals('')))">
    <div style="margin-top: 5px; font-weight: bold;">Što još morate napraviti?</div>
	<div>
		<ul class="msgList">
		<s:if test="data.assessment.assessmentConfiguration.problemsNum == 0">
			<li>Podesite osnovne parametre provjere.</li> <!-- Lokaliziraj! -->
		</s:if>
		<s:else>
			<s:if test="data.assessment.assessmentConfiguration.correctAnswers == null || data.assessment.assessmentConfiguration.correctAnswers.equals('')">
				<li>Postavite točne odgovore.</li> <!-- Lokaliziraj! -->
			</s:if>
			<s:if test="data.assessment.assessmentConfiguration.scoreCorrect == 0 && (data.assessment.assessmentConfiguration.detailTaskScores == null || data.assessment.assessmentConfiguration.detailTaskScores.equals(''))">
				<li>Postavite bodovanje.</li> <!-- Lokaliziraj! -->
			</s:if>
		</s:else>
		</ul>
	</div>
</s:if>