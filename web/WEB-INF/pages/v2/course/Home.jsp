<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h1><s:property value="data.courseInstance.course.name"/></h1>

<div class="mainContentWrapper">
<div class="mainContent text">
    <s:if test="data.lecturers!=null && data.lecturers.size()>0">
	<h2>Moj nastavnik</h2>
	<ul>
	<s:iterator value="data.lecturers"><li>Grupa <b><s:property value="group.name"/></b>:
	<s:if test="lecturers.size()==0">grupa nema pridruženog nastavnika</s:if><s:else>
	<s:iterator value="lecturers" status="stat"><s:property value="lastName"/>, <s:property value="firstName"/><s:if test="userDescriptor.email!=null && userDescriptor.email.length()!=0"> (<a href="mailto:<s:property value="userDescriptor.email"/>" target="_blank">email</a>)</s:if><s:else> (<span style="text-decoration: line-through;" title="email nije podešen">email</span>)</s:else><s:if test="!#stat.last">; </s:if></s:iterator>
	</s:else>
	</li></s:iterator>
	</ul>
    </s:if>
	<h2>O kolegiju</h2>
    <s:if test="data.courseInstance.isvuData!=null"><p><s:property value="data.courseInstance.isvuData.cdata.opis"/></p></s:if><s:else><p class="emptyMsg">Podaci nisu dostupni.</p></s:else>
	<h2>Literatura</h2>
    <s:if test="data.courseInstance.isvuData!=null && data.courseInstance.isvuData.cdata.liter.size != 0">
	<ul>
	<s:iterator value="data.courseInstance.isvuData.cdata.liter"><li><s:property/></li></s:iterator>
	</ul>
	</s:if><s:else>
		<p class="emptyMsg">Podaci nisu dostupni.</p>
	</s:else>
</div>
</div>

<div class="sideContentWrapper">
    <div class="sideContent">
    <h2>Osnovne informacije</h2>
	<div class="courseShortInfo">
        <s:if test="data.courseInstance.isvuData!=null">
			<p>ECTS bodovi: <strong><s:property value="data.courseInstance.isvuData.cdata.opterecenja[4]"/></strong></p>
			<p>Sati izravne nastave tjedno: <strong><s:property value="data.courseInstance.isvuData.cdata.opterecenja[0]"/></strong></p>
			<p>Sati laboratorijskih vježbi: <strong><s:property value="data.courseInstance.isvuData.cdata.opterecenja[2]"/></strong></p>
			<p>Nositelji predmeta</p>
	        <s:if test="data.courseInstance.isvuData.cdata.nositelji.size != 0">
			<ul>
				<s:iterator value="data.courseInstance.isvuData.cdata.nositelji">
				<li><s:property/></li>
				</s:iterator>
			</ul>
			</s:if><s:else><p>Podaci nisu dostupni.</p></s:else>
		</s:if><s:else>
			<p>ECTS bodovi: <strong>?</strong></p>
			<p>Sati izravne nastave tjedno: <strong>?</strong></p>
			<p>Sati laboratorijskih vježbi: <strong>?</strong></p>
			<p>Nositelji predmeta</p>
			<p>Podaci nisu dostupni.</p>
		</s:else>
	</div>

	<s:if test='data.administrationPermissions.contains("canViewStudentAssessments")'>
	<p class="strongLink">
		<a href="<s:url action="AssessmentSummaryView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
	</p>
	</s:if>
	<s:if test='data.administrationPermissions.contains("canViewCourseComponents")'>
	<p class="strongLink">
		<a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
	</p>
	</s:if>
	<s:if test='data.administrationPermissions.contains("canViewCourseMarketPlace")'>
    <p class="strongLink">
		<a href="<s:url action="MPGroupsList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.marketPlaces"/></a>
	</p>
	</s:if>
	<s:if test='data.administrationPermissions.contains("canViewStudentApplications")'>
	<p class="strongLink">
		<a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationUser"/></a>
	</p>
	</s:if>
	<s:if test="data.newIssues">
		<p class="strongLink2">
			<a href="<s:url action="Issues"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.issueTrackingSystem"/></a>
		</p>
	</s:if>
	<s:else>
		<p class="strongLink">
			<a href="<s:url action="Issues"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.issueTrackingSystem"/></a>
		</p>
	</s:else>
	<s:if test='data.administrationPermissions.contains("canViewCourseWiki")'>
	<p class="strongLink">
		<a href="<s:url action="CourseWiki"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseWiki"/></a>
	</p>
	</s:if>
	
    <hr class="hidden"/>
	<s:if test="data.renderCourseAdministration">
	<h2>Administracija</h2>
		<ul class="listLink">
			<s:if test='data.administrationPermissions.contains("canViewCourseAssessments")'>
				<li> 
				<a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessmentsAdministration"/></a>
				</li>
			</s:if>
				<li>
				<a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
				</li>
			<s:if test='data.administrationPermissions.contains("canViewCourseApplications")'>
				<li>
				<a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationAdmin"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseGroupTree")'>
				<li>
				<a href="<s:url action="ShowGroupTree"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.groupsTree"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canCreatePoll")'>
				<li> 
				<a href="<s:url action="CourseCreate" namespace="poll"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.createPoll"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canCreatePoll")'>
				<li> 
				<a href="<s:url action="CourseIndex" namespace="poll"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.pollOverview"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewGradingPolicy")'>
				<li>
				<a href="<s:url action="ShowGradingPolicy"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.gradingPolicy"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCoursePermissions")'>
				<li>
				<a href="<s:url action="EditCoursePermissions"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.editCoursePermissions"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseTeachers")'>
				<li>
				<a href="<s:url action="GroupOwnership"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath">0</s:param></s:url>"><s:text name="Navigation.setLectureOwnerships"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseLectureGroups")'>
				<li>
				<a href="<s:url action="ShowGroupUsers"><s:param name="data.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="data.relativePath">0</s:param></s:url>"><s:text name="Navigation.lectureGroups"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseBarCode")'>
				<li>
				<a href="<s:url action="BarcodeStickers"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.barcodeStickers"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseAppeals")'>
				<li>
				<a href="<s:url action="AdminListAppeals"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.listCourseAppeals"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canBrowseQuestions")'>
				<li>
				<a href="<s:url action="QuestionBrowser"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.questionBrowser"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canViewCourseScheduleAnalyzer")'>
				<li>
				<a href="<s:url action="StudentScheduleAnalyzer" method="inputSemesterAndUsers"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.StudentScheduleAnalyserUsers"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canManageCourseParameters")'>
				<li>
				<a href="<s:url action="CourseParametersList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.manageCourseParameters"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canUseExternalGoToLabosiSSO")'>
				<li>
				<a href="<s:url action="ExternalGoToLabosiSSO"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.external.spec.labosi"/></a>
				</li>
			</s:if>
			<s:if test='data.administrationPermissions.contains("canUsePlanningService")'>
				<li>
				<a href="<s:url action="Planning"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.scheduling"/></a>
				</li>
			</s:if>
		</ul>
	</s:if>
    </div>
</div>
       <hr class="hidden"/>
