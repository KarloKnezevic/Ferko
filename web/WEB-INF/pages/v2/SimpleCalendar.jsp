<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">


	<h2>Dogadaji</h2>
	
	<p>Na ovim stranicama nalazi se Vaš osobni kalendar u jednostavnijem formatu no što je onaj koji se nalazi na početnoj stranici sustava.</p>
	
	<div style="margin-bottom: 5px; font-size: 0.7em;">
		<s:if test="calendarType!=6"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">all</s:param></s:url>"><s:text name="Navigation.calAll"/></a></s:if><s:else><s:text name="Navigation.calAll"/></s:else> |
		<s:if test="calendarType!=1"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">semester</s:param></s:url>"><s:text name="Navigation.calSemester"/></a></s:if><s:else><s:text name="Navigation.calSemester"/></s:else> |
		<s:if test="calendarType!=2"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">month</s:param></s:url>"><s:text name="Navigation.calMonth"/></a></s:if><s:else><s:text name="Navigation.calMonth"/></s:else> |
		<s:if test="calendarType!=3"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">week</s:param></s:url>"><s:text name="Navigation.calWeek"/></a></s:if><s:else><s:text name="Navigation.calWeek"/></s:else> |
		<s:if test="calendarType!=5"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">next7</s:param></s:url>"><s:text name="Navigation.next7"/></a></s:if><s:else><s:text name="Navigation.next7"/></s:else> |
		<s:if test="calendarType!=4"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">day</s:param></s:url>"><s:text name="Navigation.calDay"/></a></s:if><s:else><s:text name="Navigation.calDay"/></s:else>
    </div>
<s:if test="data.events != null && !data.events.isEmpty()">
	<p id="eventsControl"><em>Popis svih dogadaja</em></p>
	<ol class="eventsList">
		<s:iterator value="data.events">
		<li class="vevent">
			<s:if test="context!=null && context.length()>0">
      		<span class="summary_<s:property value="context.substring(0,context.indexOf(':'))"/>"><s:property value="title"/></span>
      		<span class="dtstart" title="2001-01-15T14:00:00+06:00"><a href="<s:url action="go"><s:param name="eid"><s:property value="id"/></s:param></s:url>" style="padding-right: 5px;"><img src="img/icons/link_go.png" border="0"></a><s:date name="start" format="%{getText('locale.datetime')}"/></span> 
			</s:if><s:else>
      		<span class="summary"><s:property value="title"/></span> 
      		<span class="dtstart" title="2001-01-15T14:00:00+06:00"><s:date name="start" format="%{getText('locale.datetime')}"/></span> 
			</s:else>
			<span class="dtend"  title="2001-01-15T14:00:00+06:00"><s:property value="duration"/> min</span> 
			<span class="location"><s:if test="room==null">?</s:if><s:else><s:property value="room.name"/></s:else></span> 
	    </li>
		</s:iterator>
	</ol>
</s:if><s:else>
	Nema dogadaja
</s:else>

</div>