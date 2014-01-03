<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div>Nastavkom ove akcije uklonit će se stari raspored. Jeste li sigurni da to želite napraviti?</div>
<s:form action="AssessmentSchedule" method="post" theme="ferko">
  <s:hidden name="assessmentID" value="%{assessmentID}" />
  <s:hidden name="data.doit" value="true" />
  <s:submit method="synchronizeStudents">Siguran sam, nastavi!</s:submit>
</s:form>
