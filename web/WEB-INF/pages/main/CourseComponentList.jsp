<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.componentSet!=null && data.componentSet.size()>0">
<ul>
	<s:iterator value="data.componentSet">
      <li>
        <s:property value="descriptor.name"/>
        <s:if test="data.isAdmin()">
        <a href="<s:url action="CCIManager" method="newItem"><s:param name="courseComponentID" value="id"/></s:url>"><s:text name="Navigation.addComponentItem"/></a>
        </s:if>
        <ul>
          <s:iterator value="items">
            <li>
			<a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="id"/></s:url>">
				<s:property value="position"/>. 
				<s:property value="descriptor.positionalName"/> - 
				<s:property value="name"/>
			</a>&nbsp;
			<s:if test="data.isAdmin()">
			<a href="<s:url action="CCIManager" method="editItem"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.editComponentItem"/></a>
			</s:if>
            </li>
          </s:iterator>
        </ul>
      </li>
    </s:iterator>
</ul>
</s:if>
<s:else>
<p class="emptyMsg">Trenutno nema definiranih komponenti</p>
</s:else>

<s:if test="data.isAdmin()">
	<s:if test="data.descriptorList!=null && data.descriptorList.size()>0">
	  <s:form action="CCManager" theme="ferko">
	    <s:select list="data.descriptorList" name="componentShortName" listKey="shortName" listValue="name" label="Kreiraj novu komponentu"/>
	    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"/>
	    <s:submit method="addComponent"/>
	  </s:form>
	</s:if>
</s:if>
