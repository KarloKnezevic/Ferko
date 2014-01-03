<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="footer" class="container">
<p> <a href="<s:url action="About" />">O Ferku</a> | <a href="<s:url action="Tutorials" />">Upute za korištenje Ferka</a> | 
    <a href="http://www.fer.hr/">Fakultet elektrotehnike i računarstva</a>
	<br/>
	<s:text name="Menu.lang.name"/>: 
	<s:url id="en" action="Main" namespace="/"><s:param name="request_locale">en</s:param></s:url><s:a href="%{en}"><s:text name="Menu.lang.en"/></s:a>, 
	<s:url id="hr" action="Main" namespace="/"><s:param name="request_locale">hr</s:param></s:url><s:a href="%{hr}"><s:text name="Menu.lang.hr"/></s:a>
</p>
</div>
