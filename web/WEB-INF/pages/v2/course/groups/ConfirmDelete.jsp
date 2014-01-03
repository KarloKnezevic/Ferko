<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
<h2><s:text name="Navigation.deleteGroup"></s:text>: <b><s:property value="data.group.name"/></b></h2>

<p>Pažnja</p>
<p>Zatražili ste brisanje grupe. Ukoliko nastavite, tražena grupa bit će obrisana, zajedno s eventualnim
korisnicima i podgrupama. Ovu akciju nije moguće poništiti. Jeste li sigurni da želite nastaviti s brisanjem?</p>
<s:form action="GroupDelete" theme="ferko" method="post">
	<s:hidden name="data.lid" />
	<s:hidden name="data.groupID" />
	<s:hidden name="data.confirmed" value="yes"/>
	<s:submit value="%{getText('forms.confirm.groupDelete')}"/>
</s:form>
