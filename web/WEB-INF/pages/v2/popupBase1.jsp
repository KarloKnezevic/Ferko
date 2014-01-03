<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<tiles:useAttribute name="title"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
    	<title>
			<s:property value="getText(#attr.title)"/>
			<s:if test="title!=null">
				 - <s:property value="title"/>
			</s:if>
			 - Ferko
		</title>
		<link rel="shortcut icon" href="img/favicon.ico" />
		<s:if test="description!=null">
			<meta name="description" content="<s:property value="description"/>" />
		</s:if>
		<link rel="stylesheet" href="/ferko/css/style.v2.css" type="text/css" media="screen, projection" />
		<script type="text/javascript" src="/ferko/js/jquery-1.2.6.min.js"></script>
		<tiles:insertAttribute name="customhead"/>
	</head>
	<body>
		<div id="wrapper">
		<tiles:insertAttribute name="header"/>
		<tiles:insertAttribute name="body"/>
		<div class="push">&nbsp;</div>
		</div>
		<tiles:insertAttribute name="footer"/>

    <script type="text/javascript">
       var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
       document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
    </script>
    <script type="text/javascript">
       var pageTracker = _gat._getTracker("UA-5503250-1");
       pageTracker._trackPageview();
    </script>

	</body>	
</html>
