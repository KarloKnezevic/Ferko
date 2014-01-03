<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div style="float: left; width: 38%; padding-right: 10px; border-right: 1px dotted black;">

<style>
 .tablica1 { cell-spacing: 0; }
 .tablica1 tr td { border-bottom: 1px solid #0000FF; }
 .tablica1 tr:first-child td { border-top: 1px solid #0000FF; }
 .tablica1 tr td:first-child { font-variant: small-caps; font-weight: bold; }
</style>

  <table class="tablica1" border="1">
    <tr><td>Naziv provjere</td><td><s:property value="data.assessment.name"/></td></tr>
    <tr><td>Kratki naziv</td><td><s:property value="data.assessment.shortName"/></td></tr>
    <tr><td>Oznaka provjere</td><td><s:if test="data.assessment.assessmentTag!=null"><s:property value="data.assessment.assessmentTag.name"/></s:if><s:else> </s:else></td></tr>
    <tr><td>Vrsta provjere</td><td><s:if test="data.assessment.assessmentConfiguration!=null"><s:text name="%{data.assessment.assessmentConfiguration.class.name}"/></s:if><s:else><s:text name="AssessmentTypeNotDefined" /></s:else></td></tr>
    <tr><td>Maksimalni broj bodova</td><td><s:property value="data.assessment.maxScore"/></td></tr>
    <tr><td>Početak pisanja provjere</td><td <s:if test="data.assessment.event!=null && data.assessment.event.hidden"> style="color: red;" title="<s:text name="forms.hiddenEvent"/>"</s:if>><s:if test="data.assessment.event!=null"><s:property value="data.assessment.event.startAsText"/></s:if><s:else>-</s:else></tr>
    <tr><td>Trajanje (minuta)</td><td <s:if test="data.assessment.event!=null && data.assessment.event.hidden"> style="color: red;" title="<s:text name="forms.hiddenEvent"/>"</s:if>><s:if test="data.assessment.event!=null"><s:property value="data.assessment.event.duration"/></s:if><s:else>-</s:else></td></tr>
    <tr><td>Preduvjet</td><td><s:if test="data.assessment.assessmentFlag!=null"><s:property value="data.assessment.assessmentFlag.name"/></s:if><s:else> </s:else></td></tr>
    <tr><td>Roditelj</td><td><s:if test="data.assessment.parent!=null"><s:property value="data.assessment.parent.name"/></s:if><s:else> </s:else></td></tr>
    <tr><td>Ulančani roditelj</td><td><s:if test="data.assessment.chainedParent!=null"><s:property value="data.assessment.chainedParent.name"/></s:if><s:else> </s:else></td></tr>
  </table>
</div>
<div style="float: left; width: 58%; padding-left: 10px;">
  <div class="tabview" id="tabview1">
    <div class="tabtitles">
    </div>
    <div class="tabpages">

      <div class="tabpage">
        <div class="tabtitle">Osnovni izbornik</div>
        <div class="tabbody">

        <div>
			<a href="<s:url action="AdminAssessmentEdit" method="editAssessment"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.id"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.edit"/></a>
	        | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
	        | <a href="<s:url action="AssessmentScoreExport"><s:param name="assessmentID" value="assessmentID"/><s:param name="format">csv</s:param></s:url>"><s:text name="Navigation.exportCSV"/></a>
	        | <a href="<s:url action="AssessmentScoreExport"><s:param name="assessmentID" value="assessmentID"/><s:param name="format">xls</s:param></s:url>"><s:text name="Navigation.exportXLS"/></a>
        </div>
        <s:if test="data.assessment.assessmentConfiguration == null">
		    <div style="margin-top: 5px; font-weight: bold;">Vrsta provjere još nije definirana. U nastavku možete odrediti vrstu provjere.</div>
		    <s:form action="AdminAssessmentConfSelect">
		      <s:select list="data.confSelectors" listKey="id" listValue="name" name="confSelectorID" required="true"></s:select>
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:submit method="doIt"></s:submit>
		    </s:form>
        </s:if>
	    <s:if test="data.assessment.assessmentConfiguration != null && data.confSelectors.size() != 0">
	      <div style="margin-top: 5px; font-weight: bold;">Vrstu provjere možete i promijeniti, ako je to potrebno. Nova vrsta provjere neka bude:</div>
	      <s:form action="AdminAssessmentConfSelect" theme="ferko">
	        <s:select list="data.confSelectors" listKey="id" listValue="name" name="confSelectorID" required="true" label="%{getText('Assessments.kind')}"></s:select>
	        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
	        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	        <s:submit method="askConfirm"  value="%{getText('forms.general.update')}"/>
	      </s:form>
	    </s:if>
        </div>
      </div>

      <div class="tabpage">
        <div class="tabtitle">Datoteke</div>
        <div class="tabbody">

<s:if test="data.files!=null && data.files.size() != 0">
  <div>Sljedeće datoteke su uploadane na ispit:
    <ul>
      <s:iterator value="data.files">
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
      </s:iterator>
    </ul>
  </div>
</s:if>

  <div style="margin-top: 5px; font-weight: bold;">Upload datoteka na ispit</div>
  <s:form action="AssessmentFilesUpload" method="post" enctype="multipart/form-data" theme="ferko">
    <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
    <s:file name="archive" label="%{getText('forms.file')}"></s:file>
    <s:submit value="%{getText('forms.general.upload')}"/>
  </s:form>
  <div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000006</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

        </div>
      </div>

  <s:if test="data.assessment.assessmentConfiguration != null">
    <s:if test="data.assessmentConfigurationKey == null || data.assessmentConfigurationKey.length() == 0">
      <div class="tabpage">
        <div class="tabtitle">Pogreška</div>
        <div class="tabbody">
      Pogreška u utvrđivanju vrste provjere.
        </div>
      </div>
    </s:if>
    <s:elseif test="data.assessmentConfigurationKey.equals('PRELOAD')">
      <s:include value="/WEB-INF/pages/main/assessments/PreloadConf.jsp"></s:include>
    </s:elseif>
    <s:elseif test="data.assessmentConfigurationKey.equals('PROBLEMS')">
      <s:include value="/WEB-INF/pages/main/assessments/ProblemsConf.jsp"></s:include>
    </s:elseif>
    <s:elseif test="data.assessmentConfigurationKey.equals('CHOICE')">
      <s:include value="/WEB-INF/pages/main/assessments/ChoiceConf.jsp"></s:include>
    </s:elseif>
    <s:elseif test="data.assessmentConfigurationKey.equals('EXTERNAL')">
      <s:include value="/WEB-INF/pages/main/assessments/ExternalConf.jsp"></s:include>
    </s:elseif>
    <s:elseif test="data.assessmentConfigurationKey.equals('ACRANGE')">
      <!-- Ovdje ne radimo nista -->
    </s:elseif>
    <s:elseif test="data.assessmentConfigurationKey.equals('ACENUM')">
      <!-- Ovdje ne radimo nista -->
    </s:elseif>
    <s:else>
      <s:include value="/WEB-INF/pages/main/assessments/ViewDefConf.jsp"></s:include>
    </s:else>
  </s:if>

<!-- END OF TABVIEW COMPONENT -->

    </div>
  </div>
 <script type="text/javascript">
   tabbed_view_init("tabview1");
 </script>
</div>
