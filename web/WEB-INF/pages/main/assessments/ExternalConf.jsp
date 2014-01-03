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
        <div class="tabtitle">Sinkronizacija</div>
        <div class="tabbody">
			<!--  
		  <div>
		    Odabrana konfiguracija: <s:property value="data.assessment.assessmentConfiguration.class.name"/>
		  </div>
			-->
		   <a href="<s:url action="FetchConfExternalResults"><s:param name="courseInstanceID"><s:property value="%{data.courseInstance.id}"/></s:param><s:param name="assessmentID"><s:property value="%{data.assessment.id}"/></s:param></s:url>"><s:text name="Navigation.fetchData"/></a>
        </div>
      </div>
</s:else>
