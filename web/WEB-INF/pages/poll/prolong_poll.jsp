<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content">
	<div class="maincontent">
	<div class="inner-padding">
	<h2>Produživanje ankete</h2>
	<s:form action="CourseProlong" method="prolong" theme="ferko" method="post">
		<li><label for="Create_endDate">Novi završetak ankete</label>
		<s:textfield name="endDate" cssClass="date" theme="simple" />
		<s:textfield name="endTime" theme="simple" value="12:00" />
		<s:fielderror><s:param>endDate</s:param></s:fielderror>
		</li>
		<s:hidden name="id" />
		<s:hidden name="courseInstanceID" />
		<s:submit name="submit" value="Promijeni" />
	</s:form>
	</div>
	</div>
</div>

<script type="text/javascript" src="/ferko/js/jquery-1.2.6.min.js"></script> 
<script type="text/javascript" src="/ferko/js/jquery-ui/jquery-ui.js"></script>
<script type="text/javascript"><!--
$(document).ready(function(){
	$.datepicker.setDefaults($.datepicker.regional['hr']);
	$('.date').datepicker({dateFormat: 'yy-mm-dd'});
});
--></script>
