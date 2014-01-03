<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
  
<h1><s:property value="displayName" /></h1>

<s:form action="Category!save" theme="ferko">
	<s:if test="course == null">
		<s:textfield label="%{getText('Forum.name')}" name="name" />
		<s:fielderror><s:param>name</s:param></s:fielderror>
	</s:if>
	<s:select label="%{getText('Forum.status')}" name="status" list="%{#{
			'open' : getText('Forum.categoryOpened'),
			'closed' : getText('Forum.categoryClosed'),
			'hidden' : getText('Forum.categoryHidden')}}" />
	
	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />

	<s:if test="id == null"><s:submit value="%{getText('Forum.newCategory')}" /></s:if>
	<s:else><s:submit value="%{getText('Forum.editCategory')}" /></s:else>
</s:form>
