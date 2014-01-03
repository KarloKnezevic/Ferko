<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h2><s:text name="Navigation.marketPlaces" /></h2>
	<s:if test="data.student">
	<p>Izaberite jednu od burzi gdje ćete moći promijeniti svoju grupu.</p>
	</s:if>
	
	<s:if test="data.mpRoots.size == 0">
	<p class="emptyMsg">Nema burzi grupa.</p>
	</s:if>
	<s:else>
    <div> 
	<ul>
	<s:iterator value="data.mpRoots">
		<li>
			<s:if test="active">
				<s:if test="data.student">
				<a href="<s:url action="MPView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="parentID"><s:property value="id"/></s:param></s:url>"><s:property value="name"/></a>
				</s:if><s:else>
				<s:property value="name"/>
				</s:else>
			</s:if>
			<s:else>
				<s:property value="name"/> (<em>burza je trenutno zatvorena</em>)
			</s:else>
			<s:if test="canManage">
				<a style="margin-left: 5px;" href="<s:url action="MPGroupsAdmin"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="parentID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.manage"/></a>
			</s:if>
		</li>
	</s:iterator>
	</ul>
    </div>
	</s:else>


