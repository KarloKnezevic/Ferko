<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:property value="data.parent.name"/></h2>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:form action="MPGroupsAdmin" method="post" theme="ferko">
	<s:checkbox name="bean.open" label="Burza je otvorena" />
	<s:textfield name="bean.openFrom" label="Otvoreno od" />
	<s:textfield name="bean.openUntil" label="Otvoreno do" />
	<s:textarea name="bean.formulaConstraints" rows="10" cols="80" label="Ograničenja na grupe" />
	<s:textfield name="bean.securityConstraints" label="Sigurnosna ograničenja" />
	<s:textfield name="bean.timeBuffer" label="Vremenski buffer" />
	<li>
		<table>
		<tr><th>Grupa</th><th>Kapacitet</th><th>Moguć ulazak</th><th>Moguć izlazak</th><th>Tag grupe</th></tr>
		<s:iterator value="bean.groups" status="stat">
		<tr>
			<td><s:property value="name"/>
			<s:hidden theme="simple" name="bean.groups[%{#stat.index}].id"/>
			<s:hidden theme="simple" name="bean.groups[%{#stat.index}].name"/>
			<s:hidden theme="simple" name="bean.groups[%{#stat.index}].compositeCourseID"/>
			<s:hidden theme="simple" name="bean.groups[%{#stat.index}].relativePath"/>
			<s:hidden theme="simple" name="bean.groups[%{#stat.index}].managedRoot"/>
			</td>
			<td><s:textfield theme="simple" name="bean.groups[%{#stat.index}].capacity" value="%{capacity}" /></td>
			<td><s:checkbox theme="simple" name="bean.groups[%{#stat.index}].enteringAllowed" value="%{enteringAllowed}" /></td>
			<td><s:checkbox theme="simple" name="bean.groups[%{#stat.index}].leavingAllowed" value="%{leavingAllowed}" /></td>
			<td><s:textfield theme="simple" name="bean.groups[%{#stat.index}].mpSecurityTag" value="%{mpSecurityTag}" /></td>
		</tr>
		</s:iterator>
		</table>	</li>
	<s:hidden name="bean.id" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="parentID" value="%{data.parent.id}" />
	<s:submit method="update" />
</s:form>

<s:if test="bean.groups!=null && !bean.groups.empty">

<script language="javascript"><!--
function toggleBuilder() {
	var el = document.getElementById("builder");
	var elTitle = document.getElementById("builderTitle");
	if ( el.style.display != 'none' ) {
		el.style.display = 'none';
		elTitle.style.borderBottomStyle = "solid";
		elTitle.style.marginBottom = "5px";
	}
	else {
		el.style.display = '';
		elTitle.style.borderBottomStyle = "dotted";
		elTitle.style.marginBottom = "0";
	}
}
//-->
</script>

<div id="builderTitle" style="border-top: 1px solid black; border-left: 1px solid black; border-right: 1px solid black; border-bottom: 1px solid black; margin-left: 5px; margin-right: 5px; margin-top: 5px; margin-bottom: 5px; padding: 5px; background-color: #EEEEEE;">
<a href="javascript:toggleBuilder();">Prikaži/sakrij izgradnju ograničenja</a>
</div>
<div style="display: none; border-top: 0px solid black; border-left: 1px solid black; border-right: 1px solid black; border-bottom: 1px solid black; margin-left: 5px; margin-right: 5px; margin-top: 0px; margin-bottom: 5px; padding: 5px; background-color: #EEEEEE;" id="builder">
<form name="builderForm">
<div style="font-weight: bold; font-size: 1.1em;">Pregledna tablica</div>
<table style="border: 1px dotted black;">
<tr><td>Name</td><td>Min</td><td>Max</td></tr>
<s:iterator value="bean.groups" status="stat">
<tr>
	<td><s:property value="name"/><input type="hidden" name="name_<s:property value="%{#stat.index}"/>" value="<s:property value="name"/>"></td>
	<td><input type="text" name="min_<s:property value="%{#stat.index}"/>" value="<s:property value="capacity"/>"></td>
	<td><input type="text" name="max_<s:property value="%{#stat.index}"/>" value="<s:property value="capacity"/>"></td>
</tr>
</s:iterator>
</table>

<script language="javascript"><!--
function popuni() {
  e = document.forms["builderForm"].elements;
  filter = document.forms["builderForm"].elements["filter"].value;
  filterRE = new RegExp(filter);
  numberRE = /^\d+$/;
  number2RE = /^-\d+$/;
  nameRE = /name_\d+/;
  n = "";
  for (var i=0; i<e.length; i++) {
	  name = e[i].name;
	  if(name.match(nameRE)) {
		  groupName = e[i].value;
		  if(groupName.match(filterRE)) {
			  n += "\r\n"+groupName;
			  myArr = name.split("_");
			  min = ""+e["min"].value;
			  if( min.match(numberRE) || min.match(number2RE) ) {
				  e["min_"+myArr[1]].value = min;
			  }
			  max = ""+e["max"].value;
			  if( max.match(numberRE) || max.match(number2RE) ) {
				  e["max_"+myArr[1]].value = max;
			  }
		  }
	  }
  }
  alert("Ažurirao sam:"+n);
}
function izgradi() {
  e = document.forms["builderForm"].elements;
  numberRE = /^\d+$/;
  nameRE = /name_\d+/;
  program = "";
  program = "";
  kat = ""+e["nazivKategorije"].value;
  var arr = new Array();
  for (var i=0; i<e.length; i++) {
	  name = e[i].name;
	  if(name.match(nameRE)) {
		  groupName = e[i].value;
		  myArr = name.split("_");
		  min = ""+e["min_"+myArr[1]].value;
		  if( min.match(numberRE) && min != "-1" ) {
			  p = "\""+groupName+"\"";
			  if(kat!="") {
				  p += "."+kat;
			  }
			  p += " >= "+min + "\n";
			  program += p;
			  arr.push(p);
		  }
		  max = ""+e["max_"+myArr[1]].value;
		  if( max.match(numberRE) && max != "-1" ) {
			  p = "\""+groupName+"\"";
			  if(kat!="") {
				  p += "."+kat;
			  }
			  p += " <= "+max + "\n";
			  program += p;
			  arr.push(p);
		  }
	  }
  }
  var n=new Date();
  var x=window.open('', 'Ogranicenja'+n.getTime(), "width=500,height=500,scrollbars=yes");
  x.document.write("<html>");
  x.document.write("<head><title>Prozor s ogranicenjima</title></head>");
  x.document.write("<body><pre>");
  for(var r = 0; r < arr.length; r++) {
	t = arr[r];
	t = t.replace("&", "&amp;");
	t = t.replace("\"", "&quot;");
	t = t.replace("<", "&lt;");
	t = t.replace(">", "&gt;");
    x.document.write(t);
  }
  x.document.write("</pre></body>");
  x.document.write("</html>");
  x.focus();
}
//-->
</script>

<div style="font-weight: bold; font-size: 1.1em;">Popunjavanje pregledne tablice</div>
<table style="border: 1px dotted black;">
<tr>
 <td>Filter</td>
 <td>Min</td>
 <td>Max</td>
 <td>Akcija</td>
</tr>
<tr>
 <td><input name="filter" value=""></td>
 <td><input type="text" name="min" value=""></td>
 <td><input type="text" name="max" value=""></td>
 <td><button type="button" onclick="popuni(); return false;">Primijeni</button></td>
</tr>
</table>
<div style="font-weight: bold; font-size: 1.1em;">Generiranje programa s ograničenjima</div>
<table style="border: 1px dotted black;">
<tr>
 <td>Kategorija</td>
 <td>Akcija</td>
</tr>
<tr>
 <td><input name="nazivKategorije" value=""></td>
 <td><button type="button" onclick="izgradi(); return false;">Izgradi</button></td>
</tr>
</table>

</form>
</s:if>

</div>
