<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="nonCourseCategories.size() != 0">
	<s:form action="ForumIndex!add" theme="ferko">
		<li><s:text name="Forum.nonCourseCategoriesSubscriptions" /></li>
		
		<s:radio name="categoryId" list="nonCourseCategories"	listKey="id" listValue="name" />
		
		<s:submit value="%{getText('Forum.subscribe')}" />
	</s:form>
</s:if>

<s:form action="ForumIndex!save" theme="ferko">
	<s:if test="subscriptions.size() == 0">
		<s:text name="Forum.noSubscriptions" />
	</s:if>
	<s:else>	
		<li><s:text name="Forum.hasSubscriptions" /></li>
	
		<s:checkboxlist name="canceledSubscriptions" list="subscriptions"
			listKey="category.id" listValue="category.displayName" />
		
		<s:submit value="%{getText('Forum.cancelSubscriptions')}" />
	</s:else>
</s:form>
