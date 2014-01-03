<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<h1 class="pageh">Dobrodošli!</h1>

<div class="content">
	<div class="sidecontent">
		<s:if test="data.courseInstanceWithGroups != null && !data.courseInstanceWithGroups.isEmpty()">
		<h2>Moji predmeti</h2>
		<ul class="sidenav">
			<s:iterator value="data.courseInstanceWithGroups">
			<li>
				<a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="courseInstance.id"/></s:param></s:url>">
					<s:property value="courseInstance.course.name"/>
				</a>
				<ul class="subsidenav">
					<li><a href="#">Burza grupa</a></li>
					<li><a href="#">Seminar</a></li>
					<li><a href="#">Repozitorij</a></li>
				</ul>
			</li>
			</s:iterator>
		</ul>
		</s:if>
			<h2>*</h2>
			<ul class="sidenav">
				<a href="/ferko/poll/Index.action" class="action-delete">Ankete</a>
			</ul>

		<s:if test="data.renderSystemAdministration">
			<h2>Administracija</h2>
			<ul class="sidenav">
				<li>
				<a href="#" class="showhidelink">Sinkronizacija</a>
				<ul class="subsidenav2 sublistcontent">
					<li><a href="<s:url action="SynchronizeCourseLectureSchedule" />"><s:text name="Navigation.syncCourseLectureSchedule"/></a></li>
					<li><a href="<s:url action="SynchronizeLabSchedule" />"><s:text name="Navigation.syncLabSchedule"/></a></li>
					<li><a href="<s:url action="SynchronizeCourseStudents" />"><s:text name="Navigation.syncCourseStudents"/></a></li>
					<li><a href="<s:url action="SynchronizeRooms" />"><s:text name="Navigation.syncRooms"/></a></li>
					<li><a href="<s:url action="SynchronizeCourseIsvu" />"><s:text name="Navigation.syncCourseISVU"/></a></li>
				</ul>
				</li>
				
				<li>
				<a href="#" class="showhidelink">Ostalo</a>
				<ul class="subsidenav2 sublistcontent"> 
				<li><a href="<s:url action="EditRepository" method="input" />"><s:text name="Navigation.editRepository"/></a></li>
				<li>
					<a href="<s:url action="AdminAssessmentsImport" method="show"/>"><s:text name="Navigation.adminAssessmentImport"/></a>
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
					<a href="<s:url action="SeminarRootEdit" method="newSeminarRoot"/>"><s:text name="Navigation.createNewSeminarRoot"/></a> 
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
				</li>
			</ul>
		</s:if>
	</div>

	<div class="maincontent">
		<div class="innerpadding">
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
		<p>Ovdje ide tjedni pregled događaja.</p>
		</div>
	</div>

		<script type="text/javascript">
			 $(document).ready(function() {
				$('.sublistcontent').css('display','none');
				$('a.showhidelink').mouseup(function() {
					var x = $(this).parent().children('.sublistcontent');
					if(x.css('display')==='none') {
						x.css('display','block');
					} else {
						x.css('display','none');
					}
				})
			 });
		</script>
</div>

