<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<s:if test="data != null">
  <table>
    <tr><td><strong><s:property value="%{getText('Assessments.maxScore')}"/>:</strong></td><td><s:if test="data.assessment.maxScore != null"><s:property value="data.assessment.maxScore"/></s:if><s:else>-</s:else></td></tr>
    <s:if test="!data.canTake">
	    <tr>
	    	<td><strong><s:property value="%{getText('Assessments.canWrite')}"/>:</strong></td>
	    	<td><s:property value="%{getText('Assessments.no')}"/></td>
	    </tr>
    </s:if>
    <s:else>
	    <s:if test="data.canTake && data.flagValue != null && data.flagValue.error">
	    	<tr>
		    	<td><strong><s:property value="%{getText('Assessments.canWrite')}"/>:</strong></td>
		    	<td><s:property value="%{getText('Assessments.Assessments.canWriteError')}"/></td>
		    </tr>
	    </s:if>
	    <s:else>
		    <s:if test="data.score != null && data.score.error">
		    	<tr><td><strong><s:property value="%{getText('Assessments.message')}"/>:</strong></td><td><s:property value="%{getText('Assessments.calcError')}"/></td></tr>
		    </s:if>
		    <s:else>
		    	<s:if test="data.score==null || !data.score.present">
		    		<tr>
				    	<td><strong><s:property value="%{getText('Assessments.canWrite')}"/>:</strong></td>
				    	<td><s:property value="%{getText('Assessments.yes')}"/></td>
				    </tr>
				    <tr>
				    	<td><strong><s:property value="%{getText('Assessments.wasPresent')}"/>:</strong></td>
				    	<td><s:property value="%{getText('Assessments.no')}"/></td>
				    </tr>
		    	</s:if>
		    	<s:else>
				    <tr><td><strong><s:property value="%{getText('Assessments.score')}"/> <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000027</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>:</strong></td><td><s:property value="data.score.effectiveScore"/></td></tr>
				    <tr>
				    	<td><strong><s:property value="%{getText('Assessments.status')}"/>:</strong></td>
				    	<td>
				    		<s:if test="data.score.effectiveStatus.toString().equals(\"PASSED\")">
				    			<s:property value="%{getText('Assessments.statusPassed')}"/>
				    		</s:if>
				    		<s:else>
				    			<s:property value="%{getText('Assessments.statusFailed')}"/>
				    		</s:else>
				    	</td>
				    </tr>
				    <tr><td><strong><s:property value="%{getText('Assessments.rank')}"/>:</strong></td><td><s:property value="data.score.effectiveRank"/></td></tr>
				    <tr><td><strong><s:property value="%{getText('Assessments.nonProcessedScore')}"/> <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000027</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>:</strong></td><td><s:property value="data.score.rawScore"/></td></tr>
				    <tr>
				    	<td><strong><s:property value="%{getText('Assessments.thisAssessmentScore')}"/> <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000027</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>:</strong></td>
				    	<td><s:property value="data.score.score"/></td></tr>
				    <!-- <tr><th>Provjera položena</th><td><s:property value="data.score.status"/></td></tr> -->
				    <!-- <tr><th>Rang</th><td><s:property value="data.score.rank"/></td></tr> -->
				    <tr><td><strong><s:property value="%{getText('Assessments.assignerName')}"/>:</strong></td><td><s:if test="data.score.assigner!=null"><s:property value="data.score.assigner.lastName"/>, <s:property value="data.score.assigner.firstName"/></s:if></td></tr>
				    <tr>
				    	<td><strong>Statistike:</strong></td>
				    	<td><a href="<s:url action="StudentAssessmentStat"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="kind">E</s:param></s:url>">Link</a></td>
				    </tr>
		    	</s:else>
		    </s:else>
	    </s:else>
    </s:else>
  </table>
	
	<s:if test="data.score!=null && data.score.rawPresent">
		<s:if test="data.assessment.assessmentConfiguration != null">
		    <s:if test="data.assessmentConfigurationKey == null || data.assessmentConfigurationKey.length() == 0">
		      Pogreška u utvrđivanju vrste provjere.
		    </s:if>
		    <s:elseif test="data.assessmentConfigurationKey.equals('PRELOAD')">
		      <s:include value="/WEB-INF/pages/main/assessments/PreloadView.jsp"></s:include>
		    </s:elseif>
		    <s:elseif test="data.assessmentConfigurationKey.equals('PROBLEMS')">
		      <s:include value="/WEB-INF/pages/main/assessments/ProblemsView.jsp"></s:include>
		    </s:elseif>
		    <s:elseif test="data.assessmentConfigurationKey.equals('CHOICE')">
		      <s:include value="/WEB-INF/pages/main/assessments/ChoiceView.jsp"></s:include>
		    </s:elseif>
		    <s:else>
		      <s:include value="/WEB-INF/pages/main/assessments/DefView.jsp"></s:include>
		    </s:else>
		</s:if>
	
		<s:if test="data.files!=null && data.files.size() != 0">
			<h2><s:property value="%{getText('Assessments.imagesList')}"/>:</h2>
			<s:if test="data.score!=null && data.score.rawPresent">
		    <ul>
		    </s:if>
		      <s:iterator value="data.files">
		      	<s:if test="descriptor.matches('O[0-9]++')">
			      <s:if test="data.score!=null && data.score.rawPresent && data.currentUser.id.equals(data.score.user.id)">
			      <li>
			      	  <a href="<s:url action="AssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="userID"><s:property value="user.id"/></s:param><s:param name="assessmentScanId"><s:property value="id"/></s:param></s:url>"><s:property value="descriptor"/></a>
			          <s:if test="originalFileName!=null">
			            <s:property value="originalFileName"/>
			          </s:if>
			          (<s:property value="mimeType"/>)
			          <s:if test="description!=null">
			            <br/><s:property value="description"/>
			          </s:if>
			      </li>
			      </s:if>
			      <s:else>
			        <div><img src="<s:url action="AssessmentFileDownload"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="assessmentFileID"><s:property value="id"/></s:param></s:url>" /></div>
			      </s:else>
			  	</s:if>
		      </s:iterator>
		    <s:if test="data.score!=null && data.score.rawPresent">
		    </ul>
		    </s:if>
		  <s:if test="data.score!=null && data.score.rawPresent && data.currentUser.id.equals(data.score.user.id)">
		    <s:if test="assessmentScanId != null && !assessmentScanId.equals('')">
		  	  <img src="<s:url action="AssessmentFileDownload"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="assessmentFileID"><s:property value="assessmentScanId"/></s:param></s:url>" />
		    </s:if>
		  </s:if>
		</s:if>
	</s:if>

	<s:if test="data.files!=null && data.files.size() != 0">
	  <h2><s:property value="%{getText('Assessments.filesAttachedToAssessment')}"/>:</h2>
	    <ul>
	      <s:iterator value="data.files">
	      <s:if test="!descriptor.matches('O[0-9]++')">
	        <li>
	          <a href="<s:url action="AssessmentFileDownload"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="assessmentFileID"><s:property value="id"/></s:param></s:url>"><s:property value="descriptor"/></a>
			  <s:if test="originalFileName==null">
		        <s:property value="id"/> (<s:property value="mimeType"/>)
	          </s:if>
	          <s:else>
	            <s:property value="originalFileName"/> (<s:property value="mimeType"/>)
	          </s:else>
	          <s:if test="description!=null">
	            <br/><s:property value="description"/>
	          </s:if>
	        </li>
	      </s:if>
	      </s:iterator>
	    </ul>
	</s:if>
</s:if>
