<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <h2>Definiranje nove prijave <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000025</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
</s:if>
<s:else>
  <h2>Uređivanje prijave <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000025</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
</s:else>

<s:form action="ApplicationAdminEdit" method="post" theme="ferko">
	<s:textfield name="bean.name" label="%{getText('forms.name')}" size="50"/>
	<s:textfield name="bean.shortName" label="%{getText('forms.shortName')}" size="5"/>
	<s:textfield name="bean.openFrom" label="Otvorena od" />
	<li class="fieldComment">(yyyy-MM-dd HH:mm:ss)</li>
	<s:textfield name="bean.openUntil" label="Otvorena do" />
	<li class="fieldComment">(yyyy-MM-dd HH:mm:ss)</li>
	<s:textarea name="bean.program" label="%{getText('forms.programApplDef')}" cols="100" rows="15"></s:textarea>
	<li class="fieldComment">Jednostavna skripta koja definira detalje prijave. Programska definicija prijave je opcionalna - za detalje pogledati pomoć!</li>
	<s:hidden name="bean.id" />
	<s:hidden name="bean.courseInstanceID" />
	<s:submit method="saveDefinition" value="%{getText('forms.general.update')}"/>
</s:form>
