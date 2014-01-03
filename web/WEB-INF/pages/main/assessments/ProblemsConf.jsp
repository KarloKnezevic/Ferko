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
      <div class="tabpage">
        <div class="tabtitle">Bodovi</div>
        <div class="tabbody">
			<!--  
		  <div>
		    Odabrana konfiguracija: <s:property value="data.assessment.assessmentConfiguration.class.name"/>
		  </div>
			-->
		  <s:if test="data.assessment.assessmentConfiguration.numberOfProblems != 0">
		    <div>
		     <a href="<s:url action="ConfProblemsScoreEdit" method="edit"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.scoreEditing"/></a>
		     | <a href="<s:url action="CalculateConfProblemsResults"><s:param name="courseInstanceID"><s:property value="%{data.courseInstance.id}"/></s:param><s:param name="assessmentID"><s:property value="%{data.assessment.id}"/></s:param></s:url>"><s:text name="Navigation.calculateRawScoreSum"/></a>
		    </div>
		  </s:if>
		  <s:else>
		  	<ul class="msgList"> 
				<li>Morate definirati broj zadataka.</li>
			</ul>
		  </s:else>
			<div style="font-weight: bold;">Definiranje broja zadataka</div>
		    <s:form action="AdminSetProblemsConf" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textfield name="numberOfProblems" label="%{getText('forms.numberOfProblems')}" value="%{data.assessment.assessmentConfiguration.numberOfProblems}"></s:textfield>
		      <s:submit method="confNumberOfProblems"></s:submit>
		    </s:form>
		  <s:if test="data.assessment.assessmentConfiguration.numberOfProblems != 0">
			<div style="font-weight: bold;">Definiranje broja bodova po zadacima i grupama</div>
		    <s:form action="AdminUploadProblemsConf" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea name="text" value="%{data.assessment.assessmentConfiguration.scorePerProblem}" rows="30" cols="80" label="%{getText('forms.data')}"></s:textarea>
		      <s:submit method="uploadMax"></s:submit>
		    </s:form>
			<div style="font-weight: bold;">Upload rezultata studenata</div>
		    <s:form action="AdminUploadProblemsConf" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea name="text" rows="30" cols="80" label="%{getText('forms.data')}"></s:textarea>
		      <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="appendOrReplace" required="true" label="Akcija"></s:select>
		      <s:submit method="upload"></s:submit>
		    </s:form>
		  </s:if> 
		<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000010</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

        </div>
      </div>
</s:else>
