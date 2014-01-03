<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>
	
	<s:if test="!data.archive">
		<h3><s:text name = "ITS.activeIssuesSubtitle" /></h3>
	</s:if>
	<s:else>
		<h3><s:text name = "ITS.resolvedIssuesSubtitle" /></h3>
	</s:else>	
	
	<s:hidden name="data.archive" value="%{data.archive}"/>
	
	<!-- koristi YUI skripta -->
	<s:hidden id="courseInstanceID" value="%{courseInstanceID}"/>
	<s:hidden id="archiveFlag" value="%{data.archive}"/>	


<!-- Dependencies -->
<script type="text/javascript" src="/ferko/js/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="/ferko/js/yui/element/element-beta-min.js"></script>
<script type="text/javascript" src="/ferko/js/yui/datasource/datasource-min.js"></script>
<script type="text/javascript" src="/ferko/js/yui/connection/connection-min.js"></script>
<script type="text/javascript" src="/ferko/js/yui/json/json-min.js"></script>
<!-- Main source -->
<script type="text/javascript" src="/ferko/js/yui/datatable/datatable-min.js"></script>
<script type="text/javascript" src="/ferko/js/issues/issuesDatatable.js"></script>

<!-- Style -->
<link rel="stylesheet" type="text/css" href="/ferko/js/yui/datatable/assets/skins/sam/datatable.css" />


<!-- Dodatak za bojanje redova -->
<style type="text/css">

	.yui-skin-sam .yui-dt table {font-size:13px;}
	.yui-skin-sam .yui-dt th a {text-decoration:underline;}
	
	.yui-skin-sam .yui-dt tr.mark,
	.yui-skin-sam .yui-dt tr.mark td.yui-dt-asc,
	.yui-skin-sam .yui-dt tr.mark td.yui-dt-desc,
	.yui-skin-sam .yui-dt tr.mark td.yui-dt-asc,
	.yui-skin-sam .yui-dt tr.mark td.yui-dt-desc {
	    background-color: #FF9966;
</style>

<div class="yui-skin-sam">

	<div id="json"></div> 

</div> 
