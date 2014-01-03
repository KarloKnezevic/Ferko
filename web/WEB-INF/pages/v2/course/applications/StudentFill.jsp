<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.definition.name"/></h2>

<s:form action="ApplicationStudentSubmit" theme="simple" method="post">

<s:if test="data.bean.elements==null">
	<p>Navedite razlog prijave</p>
	<s:textarea name="bean.reason" cols="50" rows="10" />
</s:if><s:else>

<s:iterator value="data.bean.elements" status="itstat">
<s:if test="kind==3">
	<p><s:property value="text"/></p>
</s:if><s:elseif test="kind==2">
	<p><s:property value="text"/><br><s:textarea name="data.bean.map['mel%{#itstat.index}']" cols="50" rows="10"/></p>
</s:elseif><s:elseif test="kind==1">
	<p><s:property value="text"/><br>
	<s:iterator value="enabledOptions">
	  <s:if test="!other">
	    <input type="radio" name="data.bean.map['sel<s:property value="%{#itstat.index}"/>']" value="<s:property value="%{key}"/>"<s:if test="key==data.bean.map[renderingData].key"> checked="checked"</s:if>><s:property value="value"/><br>
	  </s:if><s:else>
	  <input type="radio" name="data.bean.map['sel<s:property value="%{#itstat.index}"/>']" value="<s:property value="%{key}"/>"<s:if test="key==data.bean.map[renderingData].key"> checked="checked"</s:if>><s:property value="value"/><br>
	  <input type="text" name="data.bean.map['sel<s:property value="%{#itstat.index}"/>.1']" value="<s:property value="%{data.bean.map[renderingData].text}"/>">
	  </s:else>
	</s:iterator>
	</p>
</s:elseif><s:elseif test="kind==4">
	<p><s:property value="text"/><br><s:textarea name="data.bean.map['tel%{#itstat.index}']" cols="12" rows="5"/></p>
</s:elseif>
</s:iterator>
	<s:hidden name="bean.state" />
	
</s:else>
	<s:hidden name="bean.id" />
	<s:hidden name="data.applicationID" />
	<s:hidden name="data.courseInstanceID" />
	<s:submit method="saveApplication" />
</s:form>
