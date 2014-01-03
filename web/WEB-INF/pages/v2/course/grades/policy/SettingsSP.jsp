<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.editable">

<s:select list="data.rules['aOffering']" listKey="name" listValue="value" name="data.rules['as']" label="%{getText('forms.grades.scoreAssessment')}" />
<s:textfield name="data.rules['p2']" label="%{getText('forms.grades.percentageGrade2')}" />
<s:textfield name="data.rules['p3']" label="%{getText('forms.grades.percentageGrade3')}" />
<s:textfield name="data.rules['p4']" label="%{getText('forms.grades.percentageGrade4')}" />
<s:textfield name="data.rules['p5']" label="%{getText('forms.grades.percentageGrade5')}" />

</s:if><s:else>

<li><s:text name="forms.grades.scoreAssessment" />: <s:property value="data.rules['as']"/> </li>
<li><s:text name="forms.grades.percentageGrade2" />: <s:property value="data.rules['p2']"/> </li>
<li><s:text name="forms.grades.percentageGrade3" />: <s:property value="data.rules['p3']"/> </li>
<li><s:text name="forms.grades.percentageGrade4" />: <s:property value="data.rules['p4']"/> </li>
<li><s:text name="forms.grades.percentageGrade5" />: <s:property value="data.rules['p5']"/> </li>

</s:else>
