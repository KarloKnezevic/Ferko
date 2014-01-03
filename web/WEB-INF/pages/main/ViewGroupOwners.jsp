<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
<s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)
</a>
<ul>
  <li>
    <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
    <s:property value="data.courseComponent.descriptor.name"/>
    </a>
    <ul>
      <li>
      <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>">
      <s:property value="data.courseComponentItem.position"/>. <s:property value="data.courseComponent.descriptor.positionalName"/> - <s:property value="data.courseComponentItem.name"/>
      </a>
      </li>
    </ul>
  </li>
</ul>

<!--
// Umetnuo cupic - POCETAK
-->
<div id="editor">
Molim pričekajte dok se podatci dohvaćaju...
</div>

<div id="editorDialog">
</div>

<script language="javascript">
  var currentData = undefined;
  var removeURL = "<s:url action="CCIManager" method="removeGroupOwnerJSON"><s:param name="id" value="data.courseComponentItem.id"/><s:param name="groupID" value="%{''}"/></s:url>";
  var addURL = "<s:url action="CCIManager" method="addGroupOwnerJSON"><s:param name="id" value="data.courseComponentItem.id"/><s:param name="goBean.groupID" value="%{'GIDGID'}"/><s:param name="goBean.userID" value="%{'UIDUID'}"/></s:url>";
  removeURL = removeURL.replace(/amp;/g, "");
  addURL = addURL.replace(/amp;/g, "");

  // Funkcija posebno pisana zbog glupog Explorera koji ne podrzava array.indexOf() koja uredno radi u FF i Operi.
  function arrayIndexOf(arr, elem) {
	  for(var i = 0; i < arr.length; i++) {
		  if(arr[i]==elem) return i;
      }
      return -1;
  }
  
  function brisi(i,ownerID) {
	  var siguran = confirm("Jeste li sigurni?");
	  if(!siguran) return;
	  $.getJSON(removeURL+ownerID, function(data) {
		  if(data.status=="ERR") {
			  alert("Dogodila se pogreška.");
			  return;
		  }
		  $("#li_"+i+"_"+ownerID).remove();
		  for(var j=0; j<currentData.glist[i].owners.length; j++) {
			  if(currentData.glist[i].owners[j].userID==ownerID) {
				  currentData.glist[i].owners.splice(j,1);
			  }
		  }
	  });
  }
  function dodajKorGrupa(i, gid, uid) {
	  var url = addURL.replace("UIDUID",uid);
	  url = url.replace("GIDGID",gid);
	  var dd = $("#editorDialog");
	  dd.dialog("close");
	  var g = currentData.glist[i];
	  $.getJSON(url, function(data) {
		  if(data.status=="ERR") {
			  alert("Dogodila se pogreška.");
			  return;
		  }
		  var ow = data.owner;
		  currentData.glist[i].owners.push(ow);
		  var str = "<li id='li_"+i+"_"+ow.userID+"'>"+ow.lastName+", "+ow.firstName+" ("+ow.jmbag+")"+" <span class='ui-state-default ui-corner-all ui-icon ui-icon-minusthick' style='display: inline-block; cursor: pointer;' onclick='brisi("+i+","+ow.userID+")'></span>"+"</li>";
		  $("#ul_"+i).append(str);
	  });
  }
  function dodaj(i) {
	  var dd = $("#editorDialog");
	  dd.empty();
	  var imam = [];
	  var g = currentData.glist[i];
	  $("#ul_"+i+" li").each(function(i,domElem) {
		  var owid = domElem.id.substring(domElem.id.lastIndexOf("_")+1);
		  for(var j=0; j<g.owners.length; j++) {
			  if(g.owners[j].userID==owid) {
				  imam.push(g.owners[j].jmbag);
				  break;
			  }
		  }
	  });
	  dd.append("<div>Odaberite korisnika kojeg želite dodati!</div>");
	  var ista = false;
	  for(var j=0; j<currentData.users.length; j++) {
		var u = currentData.users[j];
		if(arrayIndexOf(imam, u.jmbag)!=-1) continue;
		var t = "<div style='background-color: #CCDDCC; border: 1px solid black; padding-left: 5px; padding-right: 5px; cursor: pointer;' onclick='dodajKorGrupa("+i+","+g.groupID+","+u.userID+")'>"+u.lastName+", "+u.firstName+" ("+u.jmbag+")"+"</div>";
		dd.append(t);
		ista = true;
	  }
	  if(!ista) {
		dd.append("<div>Nema više korisnika!</div>");
	  }
	  dd.dialog("option", "title", g.groupName);
	  dd.dialog("open");
  }
  
  $(document).ready(function() {
	  $("#editorDialog").dialog({autoOpen: false});
	  $.getJSON("<s:url action="CCIManager" method="editGroupOwnersJSON"><s:param name="id" value="data.courseComponentItem.id"/></s:url>", function(data) {
		  currentData = data;
		  var ed = $("div#editor");
		  ed.empty();
		  for(var i=0; i<data.glist.length; i++) {
			var g = data.glist[i];
			ed.append("<div>"+g.groupName+" <span class='ui-state-default ui-corner-all ui-icon ui-icon-plusthick' style='display: inline-block; cursor: pointer;' onclick='dodaj("+i+")'>"+"dodaj"+"</span></div>");
			var str = "<ul id='ul_"+i+"'>";
		  	for(var j=0; j<g.owners.length; j++) {
				var ow = g.owners[j];
				str += "<li id='li_"+i+"_"+ow.userID+"'>"+ow.lastName+", "+ow.firstName+" ("+ow.jmbag+")"+" <span class='ui-state-default ui-corner-all ui-icon ui-icon-minusthick' style='display: inline-block; cursor: pointer;' onclick='brisi("+i+","+ow.userID+")'></span>"+"</li>";
			}			
			str += "</ul>";
			ed.append("<div>"+str+"</div>");
		  }
	  });
  });
</script>
<!--
// Umetnuo cupic - KRAJ
-->
<!--
<h1 align="center">Dodjela asistenata</h1>
Popis termina: <br>
<s:if test="data.groupList != null && data.groupList.size()>0">
<table>
<s:iterator value="data.groupList">
  <tr>
  <td>
  <s:property value="name"/>
  <ul>
    <s:if test="ownerList != null && ownerList.size()>0">
    <s:iterator value="ownerList">
    <li>
      <s:property value="lastName"/> <s:property value="firstName"/> (<s:property value="jmbag"/>)
      [<a href="<s:url action="CCIManager" method="removeGroupOwner"><s:param name="id" value="data.courseComponentItem.id"/><s:param name="groupID" value="userID"/></s:url>"><s:text name="Navigation.erase"/></a>]
    </li>
    </s:iterator>
    </s:if>
    <s:else>
    <li>Nema asistenata</li>
    </s:else>
  </ul>
  </td>
  <td>
  <s:form action="CCIManager" theme="simple">
  	<s:select list="data.userList" name="goBean.userID" listKey="id" listValue="lastName+' '+firstName+' ('+jmbag+')'"></s:select>
  	<s:hidden name="goBean.groupID" value="%{id}"></s:hidden>
  	<s:hidden name="id" value="%{data.courseComponentItem.id}"></s:hidden>
  	<s:submit method="addGroupOwner"></s:submit>
  </s:form>
  </td>
  </tr>
</s:iterator>
</table>
</s:if>
<s:else>
Nema termina
</s:else>
-->
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>
