<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.editable">

<s:select list="data.rules['aOffering']" listKey="name" listValue="value" name="data.rules['as']" label="%{getText('forms.grades.scoreAssessment')}" />
<s:textfield name="data.rules['s3']" label="%{getText('forms.grades.tresholdGrade3')}" />
<s:textfield name="data.rules['s4']" label="%{getText('forms.grades.tresholdGrade4')}" />
<s:textfield name="data.rules['s5']" label="%{getText('forms.grades.tresholdGrade5')}" />

</s:if><s:else>

<li><s:text name="forms.grades.scoreAssessment" />: <s:property value="data.rules['as']"/> </li>
<li><s:text name="forms.grades.tresholdGrade3" />: <s:property value="data.rules['s3']"/> </li>
<li><s:text name="forms.grades.tresholdGrade4" />: <s:property value="data.rules['s4']"/> </li>
<li><s:text name="forms.grades.tresholdGrade5" />: <s:property value="data.rules['s5']"/> </li>

</s:else>
