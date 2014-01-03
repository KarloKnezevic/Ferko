<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2>Izrada BarCode naljepnica za studente</h2>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div>Ukoliko želite generirati naljepnice samo za određene studente, upišite njihove JMBAG-ove u za to predviđeno polje. Ukoliko želite naljepnice za sve studente, polje s JMBAG-ovima ostavite prazno.</div>

<s:form action="BarcodeStickers" theme="ferko">
	<s:select list="bean.pageSizes" listKey="name" listValue="name" name="bean.pageSize" label="%{getText('forms.pageSize')}" />
	<s:textfield name="bean.marginTop" label="%{getText('forms.marginTop')}" />
	<s:textfield name="bean.marginBottom" label="%{getText('forms.marginBottom')}" />
	<s:textfield name="bean.marginLeft" label="%{getText('forms.marginLeft')}" />
	<s:textfield name="bean.marginRight" label="%{getText('forms.marginRight')}" />
	<s:checkbox name="bean.landscape" label="%{getText('forms.landscape')}" />
	<s:checkbox name="bean.altSort" label="%{getText('forms.alternativeSort')}" />
	<s:checkbox name="bean.showGroups" label="%{getText('forms.showLectureGroups')}" />
	<s:textfield name="bean.columnsCount" label="%{getText('forms.columnsCount')}" />
	<s:textfield name="bean.rowsCount" label="%{getText('forms.rowsCount')}" />
	<s:textfield name="bean.numberOfBarcodesPerStudent" label="%{getText('forms.numberOfBarcodesPerStudent')}" />
	<s:textarea name="bean.jmbags" label="%{getText('forms.jmbags')}" cols="20" rows="20" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"/>
	<s:submit method="create" />
</s:form>

<p><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000007</s:param></s:url>"><s:text name="Navigation.help"/></a></p>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
</div>
