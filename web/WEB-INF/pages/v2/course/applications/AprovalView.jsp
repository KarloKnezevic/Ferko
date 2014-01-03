<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:property value="data.student.firstName "/>, <s:property value="data.student.lastName"/> (<s:property value="data.student.jmbag"/>)</h2>

<s:if test="data.beans.isEmpty()">
  <p class="emptyMsg">Student nema ispunjenih prijava.</p>
</s:if>
<s:else>
<s:form action="ApplicationAdminAprove" theme="ferko">
	<s:iterator value="data.beans" status="stat">
	<li style="border-left: 1px solid gray; border-bottom: 1px solid gray; margin-top: 10px;"><ul>
  		<li><span style="margin-left: -20px; font-weight: bold; font-size: 1.2em; background-color: #DDDDEE; border-bottom: 1px dashed black; border-top: 1px dashed black; padding-top: 3px; padding-bottom: 3px; padding-left: 5px; padding-right: 5px;"><s:property value="definition"/></span></li>
		<li>Prijava zaprimljena: <s:date name="date" format="%{getText('locale.datetime')}"/></li>

		
<s:if test="elements==null">
		<li>Navedeni razlog: <s:property value="reason"/></li>
</s:if><s:else>
<s:iterator value="elements" status="itstat">
<s:if test="kind==3">
	<li><s:property value="text"/></li>
</s:if><s:elseif test="kind==2">
	<li><b><s:property value="text"/></b><br><s:property value="map[renderingData]"/></li>
</s:elseif><s:elseif test="kind==1">
	<li><b><s:property value="text"/></b><br>
	<s:if test="getOption(map[renderingData].key).isOther()">
	<s:property value="getOption(map[renderingData].key).value"/>: <i><s:property value="map[renderingData].text"/></i>
	</s:if><s:else>
	<s:property value="getOption(map[renderingData].key).value"/>
	</s:else>
	</li>
</s:elseif><s:elseif test="kind==4">
	<li><b><s:property value="text"/></b><br><s:property value="map[renderingData]"/></li>
</s:elseif>

</s:iterator>
</s:else>
		<li><i>Na ovom mjestu možete odlučiti želite li prihvatiti ili odbiti prijavu. Ako prijavu odbijate bilo bi poželjno da studentu navedete obrazloženje.</i></li>
  		<s:textarea name="data.beans[%{#stat.index}].statusReason" label="%{getText('forms.reason')}" cols="50" rows="10" />
  		<s:radio name="data.beans[%{#stat.index}].status" list="data.statuses" />
		<s:hidden name="data.beans[%{#stat.index}].id" />
		<s:hidden name="data.beans[%{#stat.index}].definition" />
		<s:hidden name="data.beans[%{#stat.index}].date" />
		<s:hidden name="data.beans[%{#stat.index}].reason" />

</ul></li>

	</s:iterator>
    <s:if test="data.fromDefinitionID!=null">
		<s:hidden name="data.fromDefinitionID" />
    </s:if>
	<s:hidden name="data.courseInstanceID" />
	<s:hidden name="data.studentID" />
	<s:submit method="aprove" />
</s:form>
</s:else>
