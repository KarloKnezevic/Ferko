<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<div class="content">
	<div class="maincontent">
		<div class="innerpadding">
			<s:if test="data.messageLogger.hasMessages()">
				<ul class="msgList">
					<s:iterator value="data.messageLogger.messages">
						<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
					</s:iterator>
				</ul>
			</s:if>
			<h2>Aktivnosti</h2>
			<s:if test="data.activityBeans!=null && !data.activityBeans.isEmpty">
			<ul>
			<s:iterator value="data.activityBeans">
			  <li><span class="activity_viewed_<s:property value="viewed"/>">[<s:property value="data.formatDateTime(date)"/>] <s:property value="message"/></span><a href="<s:url action="goa"><s:param name="aid"><s:property value="id"/></s:param></s:url>" style="padding-right: 5px;"><img src="img/icons/link_go.png" border="0"></a></li>
			</s:iterator>
			</ul>
			</s:if><s:else>Nema.</s:else>
		</div>
	</div>
</div>

</div>

