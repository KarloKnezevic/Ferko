<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="jcms" uri="/jcms-custom-tags" %>

<s:if test="!navigation.getNavigationBar('m1').isEmpty()">
<ul class="subNav">

    <s:iterator value="navigation.getNavigationBar('m1')">
      <s:iterator value="items" id="navitem">
        <s:if test="kind.equals('action')">
<li>
<s:if test="'execute'.equals(actionMethod)"><s:url action="%{actionName}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:if>
<s:else><s:url action="%{actionName}" method="%{actionMethod}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:else>
<a href="<s:property value="#url1"/>"><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></a>
</li>
        </s:if><s:elseif test="kind.equals('text')">
<li><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></li>
        </s:elseif>
      </s:iterator>
    </s:iterator>

</ul>
</s:if>

<s:if test="!navigation.getNavigationBar('m2').isEmpty()">
<ul class="subsubNav">

    <s:iterator value="navigation.getNavigationBar('m2')">
      <s:iterator value="items" id="navitem">
        <s:if test="kind.equals('action')">
<li>
<s:if test="'execute'.equals(actionMethod)"><s:url action="%{actionName}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:if>
<s:else><s:url action="%{actionName}" method="%{actionMethod}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:else>
<a href="<s:property value="#url1"/>"><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></a>
</li>
        </s:if><s:elseif test="kind.equals('text')">
<li><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></li>
        </s:elseif>
      </s:iterator>
    </s:iterator>

</ul>
</s:if>