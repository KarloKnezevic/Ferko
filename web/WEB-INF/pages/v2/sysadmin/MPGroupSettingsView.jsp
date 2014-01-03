<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<h1>Administracija</h1>

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<s:form action="MPGroupSettingsView" id="MPGroupSettingsView2" method="get" theme="ferko">
		<s:select name="semesterID"  label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle" />
		<s:hidden name="parentRelativePath" />
		<s:submit method="view" />
	</s:form>

	<h2>Pregled burzi grupa po kolegijima</h2>

	<form id="filter-form" action="."><p><label for="filter">Filter: </label><input type="text" id="filter" /></p></form>
	<hr class="hidden"/>
	<script type="text/javascript" src="/ferko/js/jquery.uitablefilter.js"></script>
	<script type="text/javascript">
		$(function() { 
		  var theTable = $('table#mpListTable')
		  $('#filter').keyup(function(){
			$.uiTableFilter( theTable, this.value );
		  });

		  $('#filter-form').submit(function(){
			return false;
		  }).focus(); //Give focus to input field
		});  
	</script>
	<table class="bigTable" id="mpListTable">
		<thead>
			<tr>
				<th class="tableFilterString">Kolegij</th>
				<th>Details</th>
				<th>Open</th>
				<th>Open from</th>
				<th>Open until</th>
				<th>FC</th>
				<th>SC</th>
				<th>TBuf</th>
			</tr>
		</thead>
		
		<tbody>
		<s:iterator value="data.courses">
		<tr id="mp<s:property value="isvuCode"/>">
		<s:if test="marketPlace.absent">
			<td class="mpClosed firstCol"><s:property value="courseName"/> (<s:property value="isvuCode"/>)</td>
			<td><a class="mpDetails" href="#mpd<s:property value="isvuCode"/>">Details</a></td>
			<td><span class="boolfalse">false</span></td>
			<td><span class="valuena">n/a</span></td>
			<td><span class="valuena">n/a</span></td>
			<td><span class="valuena">n/a</span></td>
			<td><span class="valuena">n/a</span></td>
			<td><span class="valuena">n/a</span></td>
		</s:if>
		<s:else>
			<td class="mpOpen firstCol"><s:property value="courseName"/> (<s:property value="isvuCode"/>)</td>
			<td><a class="mpDetails" href="#mpd<s:property value="isvuCode"/>">Details</a></td>
			<td><span class="bool<s:property value="marketPlace.open" />"><s:property value="marketPlace.open" /></span></td>
			<td>
			<s:if test="marketPlace.openFrom">
				<span title="<s:property value="marketPlace.openFrom" />">
					<s:date name="marketPlace.openFrom" format="%{getText('locale.date')}"/>
				</span>
			</s:if>
			<s:else>
				<span class="valuena">n/a</span>
			</s:else>
			</td>
			<td>
			<s:if test="marketPlace.openUntil">
				<span title="<s:property value="marketPlace.openUntil" />">
				<s:date name="marketPlace.openUntil" format="%{getText('locale.date')}"/>
				</span>
			</s:if>
			<s:else>
				<span class="valuena">n/a</span>
			</s:else>
			</td>
			<td>
				<span class="bool<s:property value="marketPlace.formulaConstraints!=null && marketPlace.formulaConstraints.length()!=0" />">
				<s:property value="marketPlace.formulaConstraints!=null && marketPlace.formulaConstraints.length()!=0" />
				</span>
			</td>
			<td>
				<span class="bool<s:property value="marketPlace.securityConstraints!=null && marketPlace.securityConstraints.length()!=0" />">
				<s:property value="marketPlace.securityConstraints!=null && marketPlace.securityConstraints.length()!=0" />
				</span>
			</td>
			<td><s:property value="marketPlace.timeBuffer"/></td>
		</s:else>
		</tr>
		</s:iterator>
		</tbody>
	</table>

	<table border="0" cols="2" cellspacing="0">
	<tr>
	<td>
	<s:form action="MPGroupSettingsView" method="post" theme="ferko">
		<s:hidden name="semesterID" />
		<s:hidden name="parentRelativePath" />
		<s:select list="data.courses" listKey="marketPlace.id" listValue="fullCourseName" name="selectedMarketPlaces" size="20" multiple="true" label="%{getText('forms.marketPlaces')}"></s:select>
		<s:submit method="openMPs" value="Open"></s:submit>
		<s:submit method="closeMPs" value="Close"></s:submit>
	</s:form>
	</td>
	<td>
	<s:form action="MPGroupSettingsView" id="MPGroupSettingsView3" method="post" theme="ferko">
		<s:hidden name="semesterID" />
		<s:hidden name="parentRelativePath" />
		<s:textarea name="data.ids" label="%{getText('forms.marketPlaces')}" rows="5" cols="10"/>
		<s:submit method="openMPsByCourses" value="Open"></s:submit>
		<s:submit method="closeMPsByCourses" value="Close"></s:submit>
	</s:form>
	</td>
	</tr>
	</table>
	
	<h2>Detaljniji pregled po kolegijima</h2>

	<table class="bigTable">
		<thead>
		<tr><th>Grupa</th><th>Kapacitet</th><th>Ulaz</th><th>Izlaz</th><th>SecTag</th></tr>
		</thead>
		<tbody>
	<s:iterator value="data.courses">
		<s:iterator value="groups" status="stat">
			<s:if test="#stat.first">
			<tr id="mpd<s:property value="isvuCode"/>">
				<td colspan="5" class="firstCol courseTitle">
					<a href="#mp<s:property value="isvuCode"/>" class="" title="Go back">
					<s:property value="courseName"/> (<s:property value="isvuCode"/>)
					</a>
				</td>
			</tr>
			</s:if>
			<s:if test="capacity==-1 && enteringAllowed">
			<tr>
				<td class="mpClosed"><s:property value="name"/></td>
				<td><s:property value="capacity"/> (!)</td>
				<td><span class="bool<s:property value="enteringAllowed" />"><s:property value="enteringAllowed" /></span></td>
				<td><span class="bool<s:property value="leavingAllowed" />"><s:property value="leavingAllowed" /></span></td>
				<td>
					<s:if test="marketPlace.mpSecurityTag==null || marketPlace.mpSecurityTag.length()==0">
					<span class="valuena">n/a</span>
					</s:if>
					<s:else>
					<span><s:property value="marketPlace.mpSecurityTag" /></span>
					</s:else>
				</td>
			</tr>
			</s:if>
			<s:else>
			<tr>
				<td class="mpOpen"><s:property value="name"/></td>
				<td><s:property value="capacity"/></td>
				<td><span class="bool<s:property value="enteringAllowed" />"><s:property value="enteringAllowed" /></span></td>
				<td><span class="bool<s:property value="leavingAllowed" />"><s:property value="leavingAllowed" /></span></td>
				<td>
					<s:if test="marketPlace.mpSecurityTag==null || marketPlace.mpSecurityTag.length()==0">
					<span class="valuena">n/a</span>
					</s:if>
					<s:else>
					<span><s:property value="marketPlace.mpSecurityTag" /></span>
					</s:else>
				</td>
			</tr>
			</s:else>
		</s:iterator>
	</s:iterator>
		</tbody>
	</table>


</div>
