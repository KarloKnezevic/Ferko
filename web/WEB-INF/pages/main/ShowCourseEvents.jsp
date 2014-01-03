<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.events != null && !data.events.isEmpty()">
	<h2>Događaji</h2>
	<p id="eventsControl"><em>Popis svih događaja</em></p>
	<ol class="eventsList">
		<s:iterator value="data.events">
		<li class="vevent">
      		<span class="summary"><s:property value="title"/></span> 
      		<span class="dtstart" title="2001-01-15T14:00:00+06:00"><s:date name="start" format="%{getText('locale.datetime')}"/></span> 
			<span class="dtend"  title="2001-01-15T14:00:00+06:00"><s:property value="duration"/> min</span> 
			<span class="location"><s:property value="room.name"/></span> 
	    </li>
		</s:iterator>
	</ol>
</s:if>

       <hr class="hidden"/>
