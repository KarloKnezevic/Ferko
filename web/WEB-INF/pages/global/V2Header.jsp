<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="header" class="container">
	<div class="logo"><a href="<s:url action="Main" namespace="/" />">FER</a></div>

	<div id="infoBox" style="text-align:right;">
	<s:if test="currentUser != null">
		<span><s:property value="currentUser.firstName" /> <s:property value="currentUser.lastName" /></span> 
		  <a href="<s:url action="User" method="find" ><s:param name="bean.id"><s:property value="currentUser.userID"/></s:param></s:url>"><s:text name="Navigation.profile"/></a> |
		<a href="<s:url action="Logout"/>"><s:text name="Navigation.logout" /></a>
	</s:if>
	<s:else>
		<span><s:text name="Main.anonimous" /></span>
		<a href="<s:url action="Login"/>"><s:text name="Navigation.login" /></a>
	</s:else>
		<div id="searchBox">
			<form action=".">
			<p><input type="text" name="search" /></p>
			</form>
		</div>
	</div>

	<ul class="mainNav">
    <s:iterator value="navigation.getNavigationBar('m0')">
      <s:iterator value="items">
        <s:if test="kind.equals('action')">
<li>
<a href="<s:url action="%{actionName}" method="%{actionMethod}"  escapeAmp="false"><s:iterator value="parameters"><s:param name="%{name}"><s:property value="value"/></s:param></s:iterator></s:url>"><s:text name="%{titleKey}"/></a>
</li>
        </s:if>
      </s:iterator>
    </s:iterator>
	</ul>
</div>
<tiles:insertAttribute name="header2"/>
