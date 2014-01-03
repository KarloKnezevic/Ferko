<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data==null || data.assessment.assessmentConfiguration==null">
      <div class="tabpage">
        <div class="tabtitle">Pogreška</div>
        <div class="tabbody">
		  <div>Nisam dobio niti data objekt, ili je konfiguracija null.</div>
        </div>
      </div>
</s:if>
<s:else>
	<!-- Renderiranje kartice "Bodovi" -->
	<s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
      <div class="tabpage">
        <div class="tabtitle">Rezultati studenata</div>
        <div class="tabbody">
		    <div>
		    	<a href="<s:url action="ConfChoiceScoreEdit" method="edit"><s:param name="bean.assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.scoreEditing"/></a>
		    	 | 
		   		<a href="<s:url action="AdminUploadChoiceConf"><s:param name="assessmentID"><s:property value="%{data.assessment.id}"/></s:param></s:url>"><s:text name="Navigation.inputScores"/></a>
		    	 | 
		    	<a href="<s:url action="CalculateConfChoiceResults"><s:param name="courseInstanceID"><s:property value="%{data.courseInstance.id}"/></s:param><s:param name="assessmentID"><s:property value="%{data.assessment.id}"/></s:param></s:url>"><s:text name="Navigation.calculateRawScoreSum"/></a>
		  	</div>
	
        </div>
      </div>
	</s:if>

      
	<!-- Renderiranje kartice "Parametri provjere" -->
      <div class="tabpage">
        <div class="tabtitle">Parametri provjere</div>
        <div class="tabbody">
		  <div>
		  	<a href="<s:url action="AdminSetDetailedChoiceConf"><s:param name="assessmentID"><s:property value="%{data.assessment.id}"/></s:param></s:url>"><s:text name="Navigation.assessments.detailedSettings"/></a>
		  </div>
		  <s:include value="/WEB-INF/pages/main/assessments/ChoiceConfTodo.jsp"></s:include>
        </div>
      </div>
      
</s:else>
