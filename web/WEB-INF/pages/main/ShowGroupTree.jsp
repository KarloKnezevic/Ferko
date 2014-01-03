<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="jcms" uri="/jcms-custom-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2>Grupe</h2>

  <s:if test="data.accessibleGroupsTree.empty">
	<p class="emptyMsg">Nema podataka.</p>
  </s:if>
  <s:else>

	  <ul type="disc">
	  <jcms:monoHierarchyIterator status="stat" value="data.accessibleGroupsTree.children" childGetter="children">
	    <s:if test="kind==0">
	     <li>Grupa: <s:property value="value.element.name" />
	      <s:if test="value.data!=null && value.data.membersListRetrievable">
            <a href="<s:url action="ShowGroupUsers"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="value.element.relativePath"/></s:param></s:url>"><s:text name="Navigation.listGroupUsers"/></a>
		    <a href="<s:url action="ExportGroupMembershipTree"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="value.element.relativePath"/></s:param></s:url>"><s:text name="Navigation.exportGroupMembershipTree"/></a>
		    <a href="<s:url action="ExportGroupMembershipTree"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="value.element.relativePath"/></s:param><s:param name="format">mm</s:param></s:url>"><s:text name="Navigation.exportGroupMembershipTreeMM"/></a>
	      </s:if>
	      <s:if test="value.data!=null && value.data.eventsRetrievable">
            <a href="<s:url action="ListGroupEvents"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="value.element.relativePath"/></s:param></s:url>"><s:text name="Navigation.listGroupEvents"/></a>
          </s:if>
	      <s:if test="!value.leaf"><ul type="circle"></s:if>
	    </s:if>
	    <s:elseif test="kind==1">
	     <s:if test="!value.leaf"></ul></s:if>
	     </li>
	    </s:elseif>
	  </jcms:monoHierarchyIterator>
	  </ul>

</s:else>

<h2>Privatne grupe</h2>

<h3>Dodavanje nove privatne glavne grupe</h3>
<s:form action="ShowGroupTree" theme="ferko">
	<s:textfield name="name" label="%{getText('forms.groupName')}" ></s:textfield>
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="relativePath" />
	<s:submit method="createNewPrivateMainGroup"></s:submit>
</s:form>

<h3>Dodavanje podgrupa privatnoj glavnoj grupi</h3>
<s:form action="ShowGroupTree" theme="ferko">
	<s:select list="data.privateGroups" listKey="id" listValue="name" name="mainPrivateGroupID" label="%{getText('forms.mainPrivateGroupName')}" required="true"></s:select>
	<s:textarea name="name" label="%{getText('forms.groupNames')}" rows="4" cols="30"></s:textarea>
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="relativePath" />
	<s:submit method="addNewPrivateSubgroup"></s:submit>
</s:form>
