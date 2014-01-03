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
		  <div>
		   <a href="<s:url action="ConfPreloadScoreEdit" method="edit"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.scoreEditing"/></a>
		  </div>
		  <div style="margin-top: 5px; font-weight: bold;">Upload bodova</div>
		  <div>
		    <s:form action="AdminUploadPreloadConf" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea name="text" rows="5" cols="30" label="%{getText('forms.data')}"></s:textarea>
		      <s:select list="#{'APPEND':getText('forms.append'), 'REPLACE':getText('forms.replace')}" name="appendOrReplace" required="true" label="Akcija"></s:select>
		      <s:submit method="upload"></s:submit>
		    </s:form>
		<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000005</s:param></s:url>"><s:text name="Navigation.help"/></a></div>
		  </div>

        </div>
      </div>
</s:else>
