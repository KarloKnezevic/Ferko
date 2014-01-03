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
      <s:if test="data.group!=null">
        <ul><li><strong><s:property value="data.group.name"/></strong></li></ul>
      </s:if>
      </li>
    </ul>
  </li>
</ul>

<s:if test="data.isAdmin()">
<div>
 <s:iterator value="data.letters" status="stat">
   <s:if test="#stat.first == false"> | </s:if>
     <a href="<s:url action="CCIManager" method="viewItemScores"><s:param name="id" value="data.courseComponentItem.id"/><s:param name="editItemScoreBean.letter"><s:property/></s:param></s:url>"><s:property/></a>
 </s:iterator>
</div>
</s:if>

<s:form action="CCIManager" method="post" theme="simple">
  <table>
  <thead>
  <tr>
    <th>Br.</th>
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
  <s:elseif test="#stat1.isEven()">
    <tr bgcolor="#F0F0F0" >
  </s:elseif>
  <s:else>
  	<tr>
  </s:else>
    <s:hidden name="editItemScoreBean.scoreList[%{#stat1.index}].id" value="%{id}"/>
    <td align="right">
      <s:property value="#stat1.index+1"/>.
    </td>
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
  <s:if test="!data.isAdmin()">
    <s:hidden name="groupID" value="%{data.group.id}"/>
  	<s:submit method="saveGroupScores" value="%{getText('forms.update')}"></s:submit>
  </s:if>
  <s:else>
    <s:hidden name="editItemScoreBean.letter" value="%{editItemScoreBean.letter}" />
    <s:submit method="saveItemScores" value="%{getText('forms.update')}"></s:submit>
  </s:else>
</s:form>
<br>
<s:if test="!data.isAdmin()">
	<a href="<s:url action="CCIManager" method="viewUserScores"><s:param name="groupID" value="data.group.id"/><s:param name="id" value="data.courseComponentItem.id"/></s:url>">Studenti izvan termina</a>
</s:if>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>
