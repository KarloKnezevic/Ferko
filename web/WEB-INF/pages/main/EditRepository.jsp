<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <title><s:text name="EditRepository.title"/></title>
    <link href="<s:url value="/css/default.css"/>" rel="stylesheet" type="text/css"/>
</head>

<body>

<div class="content" style="border: 1px solid black;">

<div class="title1A"><s:text name="EditRepository.title" /></div>
<s:actionerror />
<s:form action="EditRepository">
  <s:iterator value="repository">
    <s:textfield name="repositoryMap['%{name}'].value" value="%{value}" label="%{name}"/>
  </s:iterator>
  <s:if test="!repositoryMap.isEmpty()">
    <s:submit value="%{getText('forms.update')}"/>
  </s:if>
  <s:textfield key="newName" label="%{getText('forms.newName')}"/>
  <s:textfield key="newValue" label="%{getText('forms.newValue')}"/>
  <s:submit method="addNew" value="%{getText('forms.add')}"/>
</s:form>

</div>

<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000002</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

<a href="<s:url action="Main" />"><s:text name="Navigation.main"/></a>

</body>

</html>
