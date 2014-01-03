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
		<li><s:property value="%{getText('Info.groupAtAssessment')}"/>: <s:if test="bean.data.group!=null"><s:property value="bean.data.group"/></s:if><s:else>-</s:else></li>
	</ul>

	<table>
	    <thead>
	    <tr>
	      <th><s:property value="%{getText('Assessments.task')}"/></th>
	      <th><s:property value="%{getText('Assessments.score')}"/></th>
	      <th><s:property value="%{getText('Assessments.maxScore')}"/></th>
	    </tr>
	    </thead>
	    <tbody>
			<s:iterator value="bean.data.scores" status="problemNum">
				<tr class="<s:if test="#problemNum.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
			        <td><s:property value="%{#problemNum.index+1}" />.</td>
					<td><s:property value="%{bean.data.scores[#problemNum.index]}" /></td>
					<td>
						<s:if test="bean.data.maxScores!=null && bean.data.maxScores[#problemNum.index]!=null"><s:property value="%{bean.data.maxScores[#problemNum.index]}" /></s:if>
						<s:else>-</s:else>
					</td>
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
			<li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNotEvaluated.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNotProcessed.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeNewScore.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeBadEval.jsp"></s:include></li>
		    <li><s:include value="/WEB-INF/pages/main/assessments/appeals/TypeWrongSolution.jsp"></s:include></li>
	    </ul>
    </s:if>	
</s:else>
