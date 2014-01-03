<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data != null && data.courseInstance != null">

  <div style="margin-top: 10px;">Definirane su sljedeće provjere znanja:</div>

  <div style="padding-left: 15px; margin-top: 10px;">
<s:if test="data.assessments == null || data.assessments.isEmpty()">
  <div>Nema definiranih provjera znanja.</div>
</s:if>
<s:else>
  <table>
    <thead>
    <tr>
      <th>Kratki naziv</th>
      <th>Naziv provjere</th>
      <th>Oznaka provjere</th>
      <th>Vrsta provjere</th>
      <th>Roditelj</th>
      <th>Ulančano dijete</th>
      <th>Preduvjet</th>
      <th>Akcija</th>
    </tr>
    </thead>
    <tbody>
    <s:iterator value="data.assessments" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="shortName"/></td>
      <td><s:property value="name"/></td>
      <td><s:if test="assessmentTag!=null"><s:property value="assessmentTag.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:property value="class.name"/></td>
      <td><s:if test="parent!=null"><s:property value="parent.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:if test="chainedChild!=null"><s:property value="chainedChild.shortName"/></s:if><s:else> - </s:else></td>
      <td><s:if test="assessmentFlag!=null"><s:property value="assessmentFlag.shortName"/></s:if><s:else> - </s:else></td>
      <td>
		<s:if test="data.canManageAssessments">
	        <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>
		</s:if>
	    <a href="<s:url action="AssessmentStat"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.astatistics"/></a>
      </td>
    </tr>
    </s:iterator>
    </tbody>
  </table>
</s:else>

<s:if test="data.canManageAssessments">
	<div style="margin-top: 5px; margin-bottom:10px;"><a href="<s:url action="AdminAssessmentEdit" method="newAssessment"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.newAssessment"/></a></div>
</s:if>
</div>

  <div style="margin-top: 20px;">Definirane su sljedeće zastavice:</div>

  <div style="padding-left: 15px; margin-top: 10px;">
<s:if test="data.assessmentFlags == null || data.assessmentFlags.isEmpty()">
  <div>Nema definiranih zastavica.</div>
</s:if>
<s:else>
  <table>
    <thead>
    <tr>
      <th>Kratki naziv</th>
      <th>Naziv zastavice</th>
      <th>Oznaka zastavice</th>
      <th>Akcija</th>
    </tr>
    </thead>
    <tbody>
    <s:iterator value="data.assessmentFlags" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="shortName"/></td>
      <td><s:property value="name"/></td>
      <td><s:if test="assesmentFlagTag!=null"><s:property value="assesmentFlagTag.shortName"/></s:if><s:else> - </s:else></td>
      <td>
		<s:if test="data.canManageAssessments">
        	<a href="<s:url action="AdminAssessmentFlagEdit" method="editFlag"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.edit"/></a>
        	<a href="<s:url action="AdminAssessmentFlagImport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.import"/></a>
            <a href="<s:url action="AssessmentFlagExport"><s:param name="assessmentFlagID" value="id"/><s:param name="format">csv</s:param></s:url>"><s:text name="Navigation.exportCSV"/></a>
            <a href="<s:url action="AssessmentFlagExport"><s:param name="assessmentFlagID" value="id"/><s:param name="format">xls</s:param></s:url>"><s:text name="Navigation.exportXLS"/></a>
		</s:if><s:else>
			-
		</s:else>
      </td>
    </tr>
    </s:iterator>
    </tbody>
  </table>
</s:else>

<s:if test="data.canManageAssessments">
	<div style="margin-top: 5px; margin-bottom:10px;"><a href="<s:url action="AdminAssessmentFlagEdit" method="newFlag"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.newFlag"/></a></div>
</s:if>
</div>

<div style="margin-top: 5px; margin-bottom: 5px; border: 1px dotted blue; padding: 5px;">
<s:form action="AdminAssessmentSummaryView" theme="simple" method="get">
	<s:text name="forms.group" /> <s:select list="data.groupsToDisplay" listKey="id" listValue="name" name="selectedGroup" label="%{getText('forms.group')}" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:submit value="%{getText('Navigation.adminScoreSummaryView')}" />
</s:form>
</div>

<s:if test="data.canManageAssessments">
<div>
  <a href="<s:url action="AdminAssessmentRecalc"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.syncScore"/></a>
</div>
</s:if>

</s:if>
