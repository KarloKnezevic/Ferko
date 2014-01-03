<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">
<h1>Ferko</h1>
<div class="mainContentWrapper">
<div class="mainContent">
	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<s:if test="data.porukaAdmina != null">
		<ul class="msgList"> 
		<li><s:property value="data.porukaAdmina"/></li>
		</ul>
	</s:if>
<div style="margin-top: 20px; text-align: center; background-color: #FF8080; border: 1px dotted blue; padding: 5px; height: 20px; width: 100px;"><a target="_blank" href="Ideje.jsp" style="font-weight: bold;">Poziv</a></div>
<h2>&nbsp;</h2>
<p>Ferko je sustav za upravljanje kolegijima usmjeren studentima i djelatnicima Fakulteta kako bi im olakšao svakodnevne radnje vezane za Fakultet, poboljšao im međusobnu komunikaciju i unaprijedio iskustvo učenja, te bio pristupačan svima. Ferko je još uvijek u razvoju pa se strpite dok ne zasja u punom sjaju. Srdačan pozdrav!</p>
<p><em>P.S.</em> Vaš kalendar s fakultetskim obavezama je dostupan u <a href="<s:url action="ICalUser" namespace="ical"><s:param name="key"><s:property value="data.userKey"/></s:param></s:url>">iCal formatu</a>.</p><p><em>P.S.</em> Aktualnosti vezane uz predmete dostupne su u <a href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">rss_2.0</s:param><s:param name="which">activities</s:param></s:url>">rss</a> ili <a href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">atom_1.0</s:param><s:param name="which">activities</s:param></s:url>">atom</a> formatu.</p>
<link rel="alternate" type="application/rss+xml" title="RSS" href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">rss_2.0</s:param><s:param name="which">activities</s:param></s:url>" />

  <!--  prema administraciji kolegija -->
  <s:if test="data.renderCourseAdministration">
	<s:form action="ShowCourse" theme="ferko" method="get">
		<s:select list="data.allCourseInstances" listKey="id" listValue="course.name" label="Odabir kolegija" name="courseInstanceID" required="true" onchange="document.getElementById('ShowCourse').submit();" />
		<s:submit />
	</s:form>
  </s:if>

  <!-- Dio za odabir semestra kojeg korisnik želi gledati. Po defaultu je odabran trenutni semestar. -->
  <s:if test="data.allSemesters!=null">
    <s:form action="Main" theme="ferko" method="get">
		<s:select list="data.allSemesters" listKey="id" listValue="fullTitle" name="currentYearSemesterID" required="true" label="Odabir semestra" onchange="document.getElementById('Main').submit();" />
        <s:submit />
    </s:form>
  </s:if>

<s:if test="!data.pollsForUser.isEmpty()">
	<p>Postoje ankete za vas:</p>
	<dl>
		<s:iterator id="poll" value="data.pollsForUser">
			<dt><a href="<s:url action="AnswerPoll"><s:param name="pollId"><s:property value="id"/></s:param></s:url>" title="Ispuni anketu" /><s:property value="title"/></a></dt>
			<dd><s:property value="description"/></dd>
		</s:iterator>
	</dl>
</s:if>
<s:if test="!data.pollsForOwner.isEmpty()">
	<p>Pregled rezultata anketa:</p>
	<dl>
		<s:iterator id="poll" value="data.pollsForOwner">
			<dt><a href="<s:url action="ViewPoll"><s:param name="pollId"><s:property value="id"/></s:param></s:url>" title="Ispuni anketu" /><s:property value="title"/></a></dt>
			<dd><s:property value="description"/></dd>
		</s:iterator>
	</dl>
</s:if>

<!--  ToDo lista -->
	<h2>ToDo lista</h2>
	<a href="<s:url action="ToDo" method="execute"></s:url>"><s:text name="Detaljni prikaz"/></a>&nbsp;&nbsp;&nbsp;
	<a href="<s:url action="ToDo" method="newTask"></s:url>"><s:text name = "Novi zadatak" /></a>&nbsp;&nbsp;&nbsp;
	<s:if test="data.renderSystemAdministration">
		<a href="<s:url action="ToDo" method="newTask"><s:param name="data.allGroups">true</s:param></s:url>"><s:text name = "Novi zadatak (SVE grupe u Ferku!)" /></a>
	</s:if>
	<s:if test="data.ownToDoList.size>0">
		<ul>
			<s:iterator value="data.ownToDoList">
			  <li><span><s:property value="title"/>, do <s:property value="deadlineString"/></span></li>
			</s:iterator>
		</ul>	
	</s:if>
	<s:else>
		<br><i>Nema zadataka :-)</i>
	</s:else>

<!--  ToDo lista -->
	<h2>Aktivnosti</h2>
	<s:if test="data.activityBeans!=null && !data.activityBeans.isEmpty">
	<ul>
	<s:iterator value="data.activityBeans">
	  <li><span class="activity_viewed_<s:property value="viewed"/>">[<s:property value="data.formatDateTime(date)"/>] <s:property value="message"/></span><a href="<s:url action="goa"><s:param name="aid"><s:property value="id"/></s:param></s:url>" style="padding-right: 3px;"><img src="img/icons/link_go.png" border="0"></a></li>
	</s:iterator>
	</ul>
	</s:if><s:else>Nema.</s:else>
    <br><a href="<s:url action="ShowActivities"></s:url>"><s:text name="Navigation.moreActivities" /></a>
</div>
</div>
<div class="sideContentWrapper">

<!--  administracija sustava -->
  <s:if test="data.renderSystemAdministration">
    <div>
	<ul class="listLink"> 
	<li>
		<a href="<s:url action="EditRepository" method="input" />"><s:text name="Navigation.editRepository"/></a>
	</li>
	<li>
		<a href="<s:url action="AdminAssessmentsImport" method="show"/>"><s:text name="Navigation.adminAssessmentImport"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeCourseIsvu" />"><s:text name="Navigation.syncCourseISVU"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeCourseStudents" />"><s:text name="Navigation.syncCourseStudents"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeRooms" />"><s:text name="Navigation.syncRooms"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeCourseLectureSchedule" />"><s:text name="Navigation.syncCourseLectureSchedule"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeLabSchedule" />"><s:text name="Navigation.syncLabSchedule"/></a>
	</li>
	<li>
		<a href="<s:url action="UpdateCourseInstanceRoles" />"><s:text name="Navigation.updateCourseInstanceRoles"/></a>
	</li>
	<li>
		<a href="<s:url action="User" method="fillSearch"/>"><s:text name="Navigation.findUser"/></a>
	</li>
	<li>
	<a href="<s:url action="User" method="fillNew"/>"><s:text name="Navigation.addNewUser"/></a>
	</li>
	<li>
		<a href="<s:url action="UserImport" method="input"/>"><s:text name="Navigation.importOfUsers"/></a>
	</li>
	<li>
		<a href="<s:url action="JMBAGUsernameImport" method="input"/>"> <s:text name="Navigation.importJMBAGUsernames"/></a>
	</li>
	<li>
		<a href="<s:url action="GroupCoarseStat" method="input"/>"><s:text name="Navigation.coarseGroupStat"/></a>
	</li>
	<li>
		<a href="<s:url action="MPGroupSettingsView" method="input"/>"><s:text name="Navigation.globalMPOverview"/></a>
	</li>
	<li>
		<a href="<s:url action="ImportCourseMPConstraints" method="input"/>"><s:text name="Navigation.importCourseMPConstraints"/></a>
	</li>
	<li>
		<a href="<s:url action="GroupMembershipExport" method="input"/>"><s:text name="Navigation.groupMembershipExport"/></a>
	</li>
	<li>
		<a href="<s:url action="StudentScheduleAnalyzer" method="inputSemester"/>"><s:text name="Navigation.StudentScheduleAnalyserSem"/></a>
	</li>
	<li>
		<a href="<s:url action="StudentScheduleAnalyzer" method="inputSemesterAndUsers"/>"><s:text name="Navigation.StudentScheduleAnalyserUsers"/></a>
	</li>
	<li>
		<a href="<s:url action="SeminarRootEdit" method="listSeminarRoots"/>"><s:text name="Navigation.listSeminarRoots"/></a> 
	</li>
	<li>
		<a href="<s:url action="SeminarRootEdit" method="newSeminarRoot"/>"><s:text name="Navigation.createNewSeminarRoot"/></a> 
	</li>
	<li>
		<a href="<s:url action="YearSemesterEdit" method="listYS"/>"><s:text name="Navigation.listYearSemester"/></a> 
	</li>
	<li>
		<a href="<s:url action="YearSemesterEdit" method="newYS"/>"><s:text name="Navigation.createNewYearSemester"/></a> 
	</li>
	<li>
		<a href="<s:url action="UpdateForum" />"><s:text name="Navigation.updateForum" /></a>
	</li>
	<li>
		<a href="<s:url action="IssuesInitialization" />"><s:text name="ITS.initializeSystem" /></a>
	</li>
	</ul>
    </div>
  </s:if>

<s:if test="data.courseInstanceWithGroups != null && !data.courseInstanceWithGroups.isEmpty()">
	<h2>Predmeti</h2>
	<ul class="courseList">
		<s:iterator value="data.courseInstanceWithGroups">
		<li>
		<span class="courseName">
		<a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="courseInstance.id"/></s:param></s:url>">
			<s:property value="courseInstance.course.name"/></a> 
		</span>
		<span class="courseGroup"> grupa <s:property value="group.name"/></span>
		</li>
		</s:iterator>
	</ul>
</s:if>


	<h2>Događaji</h2>
	<div style="margin-bottom: 5px; font-size: 0.7em;">
		<s:if test="calendarType!=6"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">all</s:param></s:url>"><s:text name="Navigation.calAll"/></a></s:if><s:else><s:text name="Navigation.calAll"/></s:else> |
		<s:if test="calendarType!=1"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">semester</s:param></s:url>"><s:text name="Navigation.calSemester"/></a></s:if><s:else><s:text name="Navigation.calSemester"/></s:else> |
		<s:if test="calendarType!=2"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">month</s:param></s:url>"><s:text name="Navigation.calMonth"/></a></s:if><s:else><s:text name="Navigation.calMonth"/></s:else> |
		<s:if test="calendarType!=3"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">week</s:param></s:url>"><s:text name="Navigation.calWeek"/></a></s:if><s:else><s:text name="Navigation.calWeek"/></s:else> |
		<s:if test="calendarType!=5"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">next7</s:param></s:url>"><s:text name="Navigation.next7"/></a></s:if><s:else><s:text name="Navigation.next7"/></s:else> |
		<s:if test="calendarType!=4"><a href="<s:url action="SetCalendarType"><s:param name="calendarType">day</s:param></s:url>"><s:text name="Navigation.calDay"/></a></s:if><s:else><s:text name="Navigation.calDay"/></s:else>
    </div>
<s:if test="data.events != null && !data.events.isEmpty()">
	<p id="eventsControl"><em>Popis svih događaja</em></p>
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
	Nema događaja
</s:else>
</div>
</div>
