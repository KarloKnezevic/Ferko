<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
  
<s:form action="CourseCategory!save" theme="ferko">
	<li><s:text name="Forum.nonexistentCategory" /></li>
	
	<s:if test="canCreateCategory">
		<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
		
		<s:submit value="%{getText('Forum.newCategory')}" />
	</s:if>
</s:form>
