<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div style="margin-top: 5px; margin-bottom: 5px; border: 1px dotted blue; padding: 5px;">
<div style="display: inline-block;">
<s:form action="AdminAssessmentSummaryView" theme="simple" method="get">
	<s:submit value="%{getText('Navigation.adminScoreSummaryView')}" />
	<s:text name="forms.group" /> <s:select list="data.groupsToDisplay" listKey="id" listValue="name" name="selectedGroup" label="%{getText('forms.group')}" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
</s:form>
</div><s:if test="data.canManageAssessments"><div style="display: inline-block; padding-left: 10px; padding-right: 10px;">|</div><div style="display: inline-block;">
  <a href="<s:url action="AdminAssessmentRecalc"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.syncScore"/></a><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000011</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>
</div><div style="display: inline-block; padding-left: 10px; padding-right: 10px;">|</div><div style="display: inline-block;"><a href="<s:url action="StudentScoreBrowserSelection"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">Podešavanje prikaza studentima</a></div></s:if>
</div>


<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <div style="padding-left: 15px; margin-top: 10px;">
  <table>
    <thead>
    <tr>
      <th><s:text name="Assessments.shortName"/></th>
      <th><s:text name="Assessments.name"/></th>
      <th><s:text name="Assessments.assesmentTag"/></th>
      <th><s:text name="Assessments.kind"/></th>
      <th><s:text name="Assessments.parent"/></th>
      <th><s:text name="Assessments.chainedChild"/></th>
      <th><s:text name="Assessments.assesmentFlag"/></th>
      <th><s:text name="forms.actions"/></th>
    </tr>
    </thead>
    <tbody>
<s:if test="data.assessments == null || data.assessments.isEmpty()">
    <tr class="oddrow">
      <td colspan="8"><s:text name="Assessments.noDefinedAssessments"/></td>
    </tr>
</s:if>
<s:else>
    <s:iterator value="data.assessments" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="shortName"/></td>
      <td><s:property value="name"/></td>
      <td><s:if test="assessmentTag!=null"><s:property value="assessmentTag.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:if test="assessmentConfiguration!=null"><s:text name="%{assessmentConfiguration.class.name}"/></s:if><s:else><s:text name="AssessmentTypeNotDefined" /></s:else></td>
      <td><s:if test="parent!=null"><s:property value="parent.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:if test="chainedChild!=null"><s:property value="chainedChild.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:if test="assessmentFlag!=null"><s:property value="assessmentFlag.shortName"/></s:if><s:else> - </s:else></td>
      <td>
		<s:if test="data.canManageAssessments">
	        <a href="<s:url action="AdminAssessmentView"><s:param name="assessmentID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>
		</s:if>
	    <a href="<s:url action="AssessmentStat"><s:param name="assessmentID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.astatistics"/></a>
      </td>
    </tr>
    </s:iterator>
</s:else>
<s:if test="data.canManageAssessments">
  <s:if test="data.assessments == null || data.assessments.isEmpty() || (data.assessments.size() % 2) == 1">
    <tr class="evenrow">
      <td colspan="7">&nbsp;</td>
      <td><a href="<s:url action="AdminAssessmentEdit" method="newAssessment"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Assessments.add"/></a></td>
    </tr>
  </s:if>
  <s:else>
    <tr class="oddrow">
      <td colspan="7">&nbsp;</td>
      <td><a href="<s:url action="AdminAssessmentEdit" method="newAssessment"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Assessments.add"/></a></td>
    </tr>
  </s:else>
</s:if>

    </tbody>
  </table>

</div>

  <div style="padding-left: 15px; margin-top: 10px;">
  <table>
    <thead>
    <tr>
      <th><s:text name="AssessmentFlags.shortName"/></th>
      <th><s:text name="AssessmentFlags.name"/></th>
      <th><s:text name="AssessmentFlags.assesmentTag"/></th>
      <th><s:text name="forms.actions"/></th>
    </tr>
    </thead>
    <tbody>
<s:if test="data.assessmentFlags == null || data.assessmentFlags.isEmpty()">
    <tr class="oddrow">
      <td colspan="4"><s:text name="AssessmentFlags.noDefinedFlags"/></td>
    </tr>
</s:if>
<s:else>
    <s:iterator value="data.assessmentFlags" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="shortName"/></td>
      <td><s:property value="name"/></td>
      <td><s:if test="assesmentFlagTag!=null"><s:property value="assesmentFlagTag.shortName"/></s:if><s:else> - </s:else></td>
      <td>
		<s:if test="data.canManageAssessments">
        	<a href="<s:url action="AdminAssessmentFlagEdit" method="editFlag"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.edit"/></a> | 
        	<a href="<s:url action="AdminAssessmentFlagData"><s:param name="data.assessmentFlagID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.dataViewEdit"/></a> | 
        	<a href="<s:url action="AdminAssessmentFlagData" method="resetNL"><s:param name="data.assessmentFlagID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.resetManualFlagValues"/></a> | 
        	<a href="<s:url action="AdminAssessmentFlagImport"><s:param name="id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.import"/></a> | 
            <a href="<s:url action="AssessmentFlagExport"><s:param name="assessmentFlagID" value="id"/><s:param name="format">csv</s:param></s:url>"><s:text name="Navigation.exportCSV"/></a> | 
            <a href="<s:url action="AssessmentFlagExport"><s:param name="assessmentFlagID" value="id"/><s:param name="format">xls</s:param></s:url>"><s:text name="Navigation.exportXLS"/></a>
		</s:if><s:else>
			-
		</s:else>
      </td>
    </tr>
    </s:iterator>
</s:else>

<s:if test="data.canManageAssessments">
  <s:if test="data.assessmentFlags == null || data.assessmentFlags.isEmpty() || (data.assessmentFlags.size() % 2) == 1">
    <tr class="evenrow">
      <td colspan="3">&nbsp;</td>
      <td><a href="<s:url action="AdminAssessmentFlagEdit" method="newFlag"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="AssessmentFlags.add"/></a></td>
    </tr>
  </s:if>
  <s:else>
    <tr class="oddrow">
      <td colspan="3">&nbsp;</td>
      <td><a href="<s:url action="AdminAssessmentFlagEdit" method="newFlag"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="AssessmentFlags.add"/></a></td>
    </tr>
  </s:else>
</s:if>

    </tbody>
  </table>

</div>
