<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h1>Odabir načina prikaza rezultata studentima</h1>

<s:if test="data.kind.equals('AUTO')">
 Odabran je pretpostavljeni način rada.<br><br>U ovom načinu sustav sam analizira međuodnose između definiranih provjera i zastavica, te rezultate
 prikazuje studentima kroz hijerarhijski prikaz. Ako želite sami definirati način na koji studenti vide svoje rezultate, najprije
 podesite željeni način prikaza (slijedite <a href="<s:url action="StudentScoreBrowserSettings"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">ovaj link</a>), 
 a potom taj prikaz aktivirajte (aktivaciju radite klikom na <a href="<s:url action="StudentScoreBrowserSelection" method="update"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="kind">TREE1</s:param></s:url>">ovaj link</a>).
</s:if>
<s:elseif test="data.kind.equals('TREE1')">
 Odabran je način prikaza rezultata prema predlošku koji ste definirali.<br><br>Uređivanje predloška možete raditi <a href="<s:url action="StudentScoreBrowserSettings"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">ovdje</a>.<br/>
 Ako se želite vratiti na pretpostavljeni način rada u kojem sustav sam analizira odnose između provjera i zastavica, slijedite <a href="<s:url action="StudentScoreBrowserSelection" method="update"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="kind">AUTO</s:param></s:url>">ovaj link</a>.
</s:elseif>
<s:else>
 Odabran je nepodržan način prikaza. Za reset na pretpostavljeni način slijedite <a href="<s:url action="StudentScoreBrowserSelection" method="update"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="kind">AUTO</s:param></s:url>">ovaj link</a>.
</s:else>
