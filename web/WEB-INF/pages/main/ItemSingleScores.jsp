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

<a href="<s:url action="CCIManager" method="viewGroupScores"><s:param name="groupID" value="data.group.id"/><s:param name="id" value="data.courseComponentItem.id"/></s:url>">Povratak</a>
<br>
<br>
<s:form action="CCIManager" method="post" theme="simple" name="CCIManagerForm">
  <s:textfield name="jmbag" label="%{getText('forms.jmbag')}" value=""/>
  <s:hidden name="id" value="%{data.courseComponentItem.id}"/>
  <s:hidden name="groupID" value="%{data.group.id}"/>
  <s:hidden name="data.userSelection" value="%{data.userSelection}"/>
  <s:submit method="viewUserScores" value="%{getText('forms.find')}"/>
  <s:submit value="Složena pretraga" onclick="jsonPretrazi(); return false;"/>
</s:form>

<!--
// Umetnuo cupic - POCETAK
-->

<div id="editorDialog">
</div>

<script language="javascript">
  var findURL = "<s:url action="UserFetcher" escapeAmp="false"><s:param name="data.courseInstanceID" value="data.courseInstance.id"/><s:param name="data.context" value="%{'ci'}"/><s:param name="data.criteria" value="%{''}"/></s:url>";
  findURL = findURL.replace(/amp;/g, "");

  function jsonPretrazi() {
	  var f = document.forms['CCIManagerForm'];

	  $.getJSON(findURL+f.jmbag.value, function(data) {
		  if(data.status=="ERR") {
			  alert("Dogodila se pogreška.");
			  return;
		  }
		  var node = $("<div></div>");
		  var any = false;
		  for(var i=0; i<data.users.length; i++) {
			  any = true;
			  var n = $("<div onclick='jsonPretrazi2(\""+data.users[i].j+"\")'>"+data.users[i].l+", "+data.users[i].f+" ("+data.users[i].j+")"+"</div>");
			  node.append(n);
		  }
		  if(any==false) {
			  var n = $("<div>Nema studenata koji zadovoljavaju zadane kriterije.</div>");
			  node.append(n);
		  }
		  var dd = $("#editorDialog");
		  dd.empty();
		  dd.append(node);
		  dd.dialog("option", "title", "Rezultat pretraživanja");
		  dd.dialog("open");
	  });
	  return false;
  }
	  
  function jsonPretrazi2(jmbag) {
	  var f = document.forms['CCIManagerForm'];
	  var autosubmit = null;
	  for(var i = 0; i < f.elements.length; i++) {
		  if(f.elements[i].name=="method:viewUserScores") {
			  autosubmit = f.elements[i]; 
			  break;
		  }
	  }
	  f.jmbag.value = jmbag;
	  if(autosubmit!=null) {
		  autosubmit.click();
	  }
	  return false;
  }

  $(document).ready(function() {
	  $("#editorDialog").dialog({autoOpen: false});
  });

</script>

<!--
// Umetnuo cupic - KRAJ
-->
<br>
<s:if test="data.isOk()">
<s:form action="CCIManager" method="post" theme="simple">
  <table>
  <thead>
  <tr>
    <th>Student</th>
    <s:if test="editItemScoreBean.scoreTypeList!=null && editItemScoreBean.scoreTypeList.size()>0">
      <s:iterator value="editItemScoreBean.scoreTypeList" status="st">
        <th><s:property value="name"/></th>
		<s:hidden name="editItemScoreBean.scoreTypeList[%{#st.index}].id" value="%{id}"/>
      </s:iterator>
    </s:if>
    <s:else>
      Nema komponenti ocjenjivanja
    </s:else>
    <th>Oznaka studenta</th>
    <th>Bodove unio</th>
  </tr>
  </thead>
  <tbody>
  <s:iterator value="editItemScoreBean.scoreList" status="stat1">
  <s:if test="error">
	<tr bgcolor="#FF7777" >
  </s:if>
  <s:else> 
	<tr> 
  </s:else>
    <s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].id" value="%{id}"/>
    <td>
      <s:property value="lastName"/> <s:property value="firstName"/> (<s:property value="jmbag"/>)
    </td>
    <s:if test="scores!=null && scores.size()>0">
      <s:iterator value="scores" status="stat2">
      	<s:if test="editItemScoreBean.scoreTypeList.get(#stat2.index).type.equals('boolean')">
          <td><s:checkbox name="editItemScoreBean.scoreList[%{#stat1.index}].scores[%{#stat2.index}]" value="%{toString()}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].versions[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].versions[#stat2.index]}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].oScores[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].oScores[#stat2.index]}"/></td>
        </s:if>
        <s:elseif test="editItemScoreBean.scoreTypeList.get(#stat2.index).type.equals('range')">
          <td><s:textfield name="editItemScoreBean.scoreList[%{#stat1.index}].scores[%{#stat2.index}]" value="%{toString()}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].versions[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].versions[#stat2.index]}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].oScores[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].oScores[#stat2.index]}"/></td>
        </s:elseif>
        <s:elseif test="editItemScoreBean.scoreTypeList.get(#stat2.index).type.equals('enum')">
          <td><s:textfield name="editItemScoreBean.scoreList[%{#stat1.index}].scores[%{#stat2.index}]" value="%{toString()}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].versions[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].versions[#stat2.index]}"/><s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].oScores[%{#stat2.index}]" value="%{editItemScoreBean.scoreList[#stat1.index].oScores[#stat2.index]}"/></td>
        </s:elseif>
      </s:iterator>
    </s:if>
    <td>
      <s:property value="tag"/>
    </td>
    <td>
      <s:property value="assignedBy"/>
    </td>
  </tr>
  </s:iterator>
  </tbody>
  </table>
  <s:hidden name="id" value="%{data.courseComponentItem.id}"></s:hidden>
  <s:hidden name="groupID" value="%{data.group.id}"/>
  <s:hidden name="data.userSelection" value="%{data.userSelection}"/>
  <s:submit method="saveUserScores" value="%{getText('forms.update')}"></s:submit>
</s:form>
</s:if>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>
