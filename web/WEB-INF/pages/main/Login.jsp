<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="body" class="container">
<script type="text/javascript">
$(document).ready(function() {
     $("#Login_username").focus();
});
</script>

<h1><s:text name="Navigation.login"/></h1>
	<s:actionerror theme="ferko" />
<s:form action="Login" theme="ferko">
	<s:textfield name="username" label="%{getText('username')}"/>
	<s:fielderror><s:param>username</s:param></s:fielderror>

	<s:password name="password" label="%{getText('password')}"/>
	<s:fielderror><s:param>password</s:param></s:fielderror>

	<s:hidden name="attempt" value="yes" />
	<s:submit value="%{getText('Navigation.login')}"/>
</s:form></div>

<div style="margin-left: 40px; margin-top: 30px;">Problemi s prijavom? Druga pitanja? Možda Vam <a href="<s:url action="LoginProblems" />">ovo</a> može pomoći.</div>
