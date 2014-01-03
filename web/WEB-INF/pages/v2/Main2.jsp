<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript" src="js/ferko-calendar.js"></script>

<style>
<!--
div.subPanel { padding-left: 5px; padding-right: 5px; margin-bottom: 15px; background-color: #FFFFFF; margin-left: 5px; margin-right: 5px; margin-top: 5px;}
div.mainLeftPanel {float: left; width: 220px; background-color: #EEEEEE;}
div.mainRightPanel {float: right; width: 220px; background-color: #EEEEEE;}
div.mainCentralPanel {margin-left: 230px; margin-right: 230px;}
-->
</style>
<s:if test="data.porukaAdmina != null && !data.porukaAdmina.isEmpty()">
<ul class="subNav" style="background-color: #FF8888; font-weight: bold;">
<li><s:property value="data.porukaAdmina"/></li>
</ul>
</s:if>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div>
  <!--  LIJEVI PANEL: pocetak -->
  <div class="mainLeftPanel">
<!--  Aktivnosti: pocetak -->
<div class="subPanel">
	<h2>Aktivnosti</h2>
	<s:if test="data.activityBeans!=null && !data.activityBeans.isEmpty">
	<ul>
	<s:iterator value="data.activityBeans">
	  <li><span class="activity_viewed_<s:property value="viewed"/>">[<s:property value="data.formatDateTime(date)"/>] <s:property value="message"/></span><a href="<s:url action="goa"><s:param name="aid"><s:property value="id"/></s:param></s:url>" style="padding-right: 5px;"><img src="img/icons/link_go.png" border="0"></a></li>
	</s:iterator>
	</ul>
	</s:if><s:else>Nema.</s:else>
    <br><a href="<s:url action="ShowActivities"></s:url>"><s:text name="Navigation.moreActivities" /></a>
</div>
<!--  Aktivnosti: kraj -->
<!--  ToDo lista: pocetak -->
<div class="subPanel">
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
</div>
<!--  ToDo lista: kraj -->
<!--  Ankete za Vas: pocetak -->
<div class="subPanel">
	<h2>Ankete</h2>
	Potražite neispunjene ankete <a href="<s:url action="Index" namespace="poll"/>">ovdje</a>.
</div>
<!--  Ankete za Vas: kraj -->
<!--  Infobox: pocetak -->
<div class="subPanel">
	<h2>Info</h2>
    <p><em>P.S.</em> Vaš kalendar s fakultetskim obavezama je dostupan u <a href="<s:url action="ICalUser" namespace="ical"><s:param name="key"><s:property value="data.userKey"/></s:param></s:url>">iCal formatu</a>.<a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000028</s:param></s:url>" onclick="blur();"><img style="float: none; display: inline; clear: none; margin: 0; padding: 0;" src="img/icons/help.png"></a></p>
    <p><em>P.S.</em> Aktualnosti vezane uz predmete dostupne su u <a href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">rss_2.0</s:param><s:param name="which">activities</s:param></s:url>">rss</a> ili <a href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">atom_1.0</s:param><s:param name="which">activities</s:param></s:url>">atom</a> formatu.</p>
    <link rel="alternate" type="application/rss+xml" title="RSS" href="<s:url action="Feed"><s:param name="key"><s:property value="data.userKey"/></s:param><s:param name="format">rss_2.0</s:param><s:param name="which">activities</s:param></s:url>" />
</div>
<!--  Infobox: kraj -->
  </div>
  <!--  LIJEVI PANEL: kraj -->
  <!--  DESNI PANEL: pocetak -->
  <div class="mainRightPanel">
<!--  Lista studentskih predmeta: pocetak -->
<div class="subPanel">
  <h2>Predmeti</h2>
  <s:if test="data.courseInstanceWithGroups != null && !data.courseInstanceWithGroups.isEmpty()">
		<s:iterator value="data.courseInstanceWithGroups">
		 <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="courseInstance.id"/></s:param></s:url>"><img title="<s:property value="courseInstance.course.name"/>, grupa <s:property value="group.name"/>" alt="<s:property value="courseInstance.course.name"/>" src="<s:url action="CourseInstanceImage"><s:param name="data.courseInstanceID"><s:property value="courseInstance.id"/></s:param></s:url>" border="0"></a><br>
		</s:iterator>
  </s:if>
    <!-- Dio za odabir semestra kojeg korisnik želi gledati. Po defaultu je odabran trenutni semestar. -->
  <s:if test="data.allSemesters!=null">
    <div style="text-align: center; padding-bottom: 5px;"><s:form action="Main" theme="simple" method="get">
		<s:select list="data.allSemesters" listKey="id" listValue="fullTitle" name="currentYearSemesterID" required="true" label="Odabir semestra" onchange="document.getElementById('Main').submit();" />
    </s:form></div>
  </s:if>
    <!--  prema administraciji kolegija -->
  <s:if test="data.renderCourseAdministration">
	<div style="text-align: center; padding-bottom: 5px;"><s:form action="ShowCourse" theme="simple" method="get">
		<s:select list="data.allCourseInstances" listKey="id" listValue="course.name" label="Odabir kolegija" name="courseInstanceID" required="true" onchange="document.getElementById('ShowCourse').submit();" cssStyle="width: 200px;"/>
		<s:submit value="%{getText('forms.select')}"/>
	</s:form></div>
  </s:if>
</div>
<!--  Lista studentskih predmeta: kraj -->
<!--  Vaše ankete: pocetak -->
<s:if test="!data.pollsForOwner.isEmpty()">
<div class="subPanel">
	<h2>Rezultati anketa</h2>
	<dl>
		<s:iterator id="poll" value="data.pollsForOwner">
			<dt><a href="<s:url action="ViewPoll"><s:param name="pollId"><s:property value="id"/></s:param></s:url>" title="Ispuni anketu" /><s:property value="title"/></a></dt>
			<dd><s:property value="description"/></dd>
		</s:iterator>
	</dl>
</div>
</s:if>
<!--  Vaše ankete: kraj -->
<!--  Lista admin akcija: pocetak -->
  <s:if test="data.renderSystemAdministration">
<div class="subPanel">
  <h2>Administracija</h2>
    <div>
	<ul class="listLink"> 
	<li>
		<a href="<s:url action="EditRepository" method="input" />"><s:text name="Navigation.editRepository"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeRooms" />"><s:text name="Navigation.syncRooms"/></a>
	</li>
	<li>
		<a href="<s:url action="YearSemesterEdit" method="listYS"/>"><s:text name="Navigation.listYearSemester"/></a> 
	</li>
	<li>
		<a href="<s:url action="YearSemesterEdit" method="newYS"/>"><s:text name="Navigation.createNewYearSemester"/></a> 
	</li>
	<li>
		<a href="<s:url action="OpenCourseInstances" />"><s:text name="Navigation.openCourseInstances"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeCourseIsvu" />"><s:text name="Navigation.syncCourseISVU"/></a>
	</li>
	<li>
		<a href="<s:url action="SynchronizeCourseStudents" />"><s:text name="Navigation.syncCourseStudents"/></a>
	</li>
	<li>
		<a href="<s:url action="AdminAssessmentsImport" method="show"/>"><s:text name="Navigation.adminAssessmentImport"/></a>
	</li>
	<li>
		<a href="<s:url action="AdminAssessmentsReserveRooms" method="input"/>"><s:text name="Navigation.assessmentsRoomsReserve"/></a>
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
		<a href="<s:url action="CourseParameters1" method="exportRoomParameters"/>"><s:text name="Navigation.cp1ExportRoomParameters"/></a> 
	</li>
	<li>
		<a href="<s:url action="CourseParameters1" method="exportExamDurations"/>"><s:text name="Navigation.cp1ExportExamDurations"/></a> 
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
		<a href="<s:url action="UpdateForum" />"><s:text name="Navigation.updateForum" /></a>
	</li>
	<li>
		<a href="<s:url action="IssuesInitialization" />"><s:text name="ITS.initializeSystem" /></a>
	</li>
	<li>
		<a href="<s:url action="ListPollTag" namespace="poll" />"><s:text name="PollTag.overview" /></a>
	</li>
	</ul>
    </div>
  </div>
  </s:if>
<!--  Lista admin akcija: kraj -->
  </div>
  <!--  DESNI PANEL: kraj -->
  <!-- CENTRALNI SADRŽAJ: pocetak -->
  <div class="mainCentralPanel">
  <!--  Kalendar: pocetak -->
<div style="position: relative; left: 0px; top: 0px; border: 1px dashed black; height: 545px;">
<div id="ferkoCalendar" style="width: 100%;">
<div id="ferkoCalendarNavig" style="position: absolute; left: 0px; top: 0px; height: 20px; width: 100%">
</div>
<div id="ferkoCalendarDays" style="position: absolute; left: 0px; top: 20px; height: 25px; width: 100%">
</div>
<div id="ferkoCalendarBaseWrapper" style="position: relative; left: 0px; top: 45px; height: 500px; overflow: auto; width: 100%">
<div id="ferkoCalendarBase" style="position: absolute; left: 0px; top: 0px; height: 1100px; width: 100%">
Kalendar s Vašim obavezama se učitava. Kalendar neće raditi ukoliko nemate omogućen JavaScript.
</div>
</div>
</div>
</div>
  <!--  Kalendar: kraj -->
  </div>
  <!-- CENTRALNI SADRŽAJ: kraj -->
</div>

<script type="text/javascript">
<!--

  function initFC() {
    var jsonurls = [];
    jsonurls.push("<s:url action="FerkoCalendarJSONFetcher" escapeAmp="false"><s:param name="cachePrevention"><s:property value="data.currentUser.id"/>___RTS__</s:param><s:param name="command">fetchCurrentWeek</s:param></s:url>");
    jsonurls.push("<s:url action="FerkoCalendarJSONFetcher" escapeAmp="false"><s:param name="cachePrevention"><s:property value="data.currentUser.id"/>___RTS__</s:param><s:param name="command">fetchWeek</s:param><s:param name="sDateFrom">__SDF__</s:param><s:param name="sDateTo">__SDT__</s:param></s:url>");
    jsonurls.push("<s:url action="FerkoCalendarJSONFetcher" escapeAmp="false"><s:param name="cachePrevention"><s:property value="data.currentUser.id"/>___RTS__</s:param><s:param name="command">fetchNextWeek</s:param><s:param name="sDateFrom">__SDF__</s:param><s:param name="sDateTo">__SDT__</s:param></s:url>");
    jsonurls.push("<s:url action="FerkoCalendarJSONFetcher" escapeAmp="false"><s:param name="cachePrevention"><s:property value="data.currentUser.id"/>___RTS__</s:param><s:param name="command">fetchPreviousWeek</s:param><s:param name="sDateFrom">__SDF__</s:param><s:param name="sDateTo">__SDT__</s:param></s:url>");
    jsonurls.push("<s:url action="go"><s:param name="eid">__EID__</s:param></s:url>");
    initFerkoCalendar(jsonurls);
  }
  
  $(document).ready(new function() { setTimeout("initFC();",1); });
//-->
</script>
