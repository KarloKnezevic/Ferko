<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:elseif test="data.assessment.assessmentConfiguration==null">
  <div>Konfiguracija je null! Ovo se nije smjelo dogoditi!</div>
</s:elseif>
<s:else>
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
		</ul>
	</s:if>
</s:else>
