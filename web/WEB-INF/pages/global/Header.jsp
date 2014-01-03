<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="header" class="container">
	<div class="logo"><a href="<s:url action="Main" namespace="/" />">FER</a></div>

	<div id="infoBox" style="text-align:right;">
	<s:if test="currentUser != null">
		<span><s:property value="currentUser.firstName" /> <s:property value="currentUser.lastName" /></span> 
		  <a href="<s:url action="User" method="find" ><s:param name="bean.id"><s:property value="currentUser.userID"/></s:param></s:url>"><s:text name="Navigation.profile"/></a> |
		<a href="<s:url action="Logout" namespace="/"/>"><s:text name="Navigation.logout" /></a>
	</s:if>
	<s:else>
		<span><s:text name="Main.anonimous" /></span>
		<a href="<s:url action="Login" namespace="/" />"><s:text name="Navigation.login" /></a>
	</s:else>
		<div id="searchBox">
			<form action=".">
			<p><input type="text" name="search" /></p>
			</form>
		</div>
	</div>

	<ul class="mainNav">
		<li><a href="<s:url action="Main" namespace="/" />" ><s:text name="Navigation.home" /></a></li>
		<li><a href="#" ><s:text name="Navigation.courses" /></a></li>
		<li><a href="<s:url action="ForumIndex" namespace="/" />">Forum</a></li>
	</ul>
</div>
