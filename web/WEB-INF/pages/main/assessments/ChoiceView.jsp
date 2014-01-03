<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:elseif test="data.assessment.assessmentConfiguration==null">
  <div>Konfiguracija je null! Ovo se nije smjelo dogoditi!</div>
</s:elseif>
<s:else>
	<h2><s:property value="%{getText('Assessments.assessmentData')}"/></h2>
	<ul>
		<li><s:property value="%{getText('Info.groupAtAssessment').trim()}"/>: <s:if test="bean.data.group!=null"><s:property value="bean.data.group"/></s:if><s:else>-</s:else></li>
		<s:if test="!(bean.data.usingDetailedTaskScores)">
			<li><s:property value="%{getText('forms.scoreCorrect').trim()}"/>: <s:property value="bean.data.scoreCorrect"/></li>
			<li><s:property value="%{getText('forms.scoreIncorrect').trim()}"/>: <s:property value="bean.data.scoreIncorrect"/></li>
			<li><s:property value="%{getText('forms.scoreUnanswered').trim()}"/>: <s:property value="bean.data.scoreUnanswered"/></li>
		</s:if>
	</ul>
	
	<!-- Ili promjeniti napomenu ili je ukloniti... 
	<s:if test="bean.data.usingDetailedTaskScores">
		<s:property value="%{getText('forms.choiceConfScoreExplanation')}"/>
	</s:if>
	 -->
	<table>
		<thead>
		<tr>
			<th><s:property value="%{getText('Info.taskLabel')}"/></th>
			<th><s:property value="%{getText('Info.answer')}"/></th>
			<th><s:property value="%{getText('Info.correctAnswer')}"/></th>
			<th><s:property value="%{getText('Info.answerStatus')}"/></th>
			<s:if test="bean.data.usingDetailedTaskScores">
				<th><s:property value="%{getText('Assessments.scoreCorrectShort')}"/></th>
				<th><s:property value="%{getText('Assessments.scoreIncorrectShort')}"/></th>
				<th><s:property value="%{getText('Assessments.scoreUnansweredShort')}"/></th>
			</s:if>
		</tr>
		</thead>
		<tbody>
	      <s:iterator value="bean.data.answers" status="stat">
	      	<tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
		        <td><s:property value="%{bean.data.problemsLabels[#stat.index]}" /></td>
		        <td><s:property value="%{bean.data.answers[#stat.index]}" /></td>
		        <td><s:property value="%{bean.data.correctAnswers[#stat.index]}" /></td>
		        <td><s:property value="%{bean.data.answersStatus[#stat.index]}" /></td>
		        <s:if test="bean.data.usingDetailedTaskScores">
	        		<td><s:property value="%{bean.data.detailedScoresCorrect[#stat.index]}"/></td>
					<td><s:property value="%{bean.data.detailedScoresIncorrect[#stat.index]}"/></td>
					<td><s:property value="%{bean.data.detailedScoresUnanswered[#stat.index]}"/></td>
	        	</s:if>
	        </tr>
	      </s:iterator>
      </tbody>
	</table>
	
	<s:if test="data.currentUser.id.equals(data.score.user.id)">
		<h2><s:property value="%{getText('appeal.assessmentAppeal')}"/></h2>
		<s:if test="data.userAppeals!=null && data.userAppeals.size() != 0">
			<s:property value="%{getText('appeal.existingAppeals')}"/>:
			<ul>
				<s:iterator value="data.userAppeals">
					<li>
						<s:property value="%{getText('appeal.' + type)}"/> (<s:date name="creationDate" format="dd.MM.yyyy" />) - <s:property value="status"/>
					</li>
				</s:iterator>
			</ul>
		</s:if>
		
		<ul>
			<li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNotProcessed.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeBadScan.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeWrongSolution.jsp"></s:include></li>
		</ul>
	</s:if>
</s:else>
