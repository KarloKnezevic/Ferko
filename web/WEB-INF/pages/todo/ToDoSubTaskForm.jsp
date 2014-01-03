<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<a href="<s:url action="ToDo" method="execute"></s:url>">
			<s:text name="Moje ToDo liste"/>
	</a>
	<p>

	<s:hidden name="data.renderBothToDoLists" />
	
	
	<h2>Podzadaci</h2>
	<s:form action="ToDo" method="post" theme="ferko"> 
		
		
		<s:hidden name="data.newTemplate" value="%{data.newTemplate}"/>
		<s:hidden name="data.subTaskID" value = "%{data.subTaskID}" />
	

		<p><p>
		<s:hidden name="data.newSubTask.id"  value="%{data.newSubTask.id}"/>
		<s:textfield size= "40" name="data.newSubTask.title" label="%{getText('todo.subTaskTitle')}" required="true" />
		<s:if test="groupTask">
			<s:textarea rows="5" cols="40" name="data.newSubTask.groupTaskDescription" label="%{getText('todo.subTaskDescription')}" />
		</s:if>
		<s:else>
			<s:textarea rows="5" cols="40" name="data.newSubTask.description" label="%{getText('todo.subTaskDescription')}" />
		</s:else>
		
		<s:textfield size= "40" name="data.newSubTask.deadlineString"  label="%{getText('todo.subTaskDeadline')}" required="true" /> 
		<s:select name = "data.newSubTask.priorityString" list="data.priorities" label="%{getText('todo.priorities')}" />
		<s:hidden name="data.newSubTask.statusString" value="%{data.newSubTask.statusString}" />
		
		<br><br>
		
		<s:if test="data.subTaskID==null || data.subTaskID.isEmpty()">
			<s:submit method="addSubTask" type="button" label="Dodaj podzadatak" ></s:submit> 
			<s:hidden name="data.newTask.title" value = "%{data.newTask.title}" />
			<s:hidden name="data.newTask.description" value="%{data.newTask.description}" />
			<s:hidden name="data.newTask.deadlineString" value="%{data.newTask.deadlineString}" /> 
			<s:hidden name = "data.newTask.priorityString" value="%{data.newTask.priorityString}" /> 
			<s:hidden name="data.newTask.statusString" value="%{data.newTask.statusString}" />
			<s:hidden name="data.newTask.id" />
			<s:hidden name="data.newTask.version" />
		
			<br><br><br><br>
			<b><s:label name="%{getText('todo.subTasks')}" value="%{getText('todo.subTasks')}"/></b>
			<i><s:label value="%{getText('todo.editAfterSave')}"/></i>
			<s:if test="data.subTasks.size>0">
				<table style="font-size:0.7em">
				<tr><th>Naziv</th><th>Opis</th><th>Rok</th><th>Prioritet</th></tr>
					<s:iterator value="data.subTasks" status="stat">
						<tr>
							<td>
								<s:label name="data.subTasks[%{#stat.index}].title" value="%{title}" />
								<s:hidden name="data.subTasks[%{#stat.index}].title" value="%{title}" />
							</td>
							<td>
								<s:label name="data.subTasks[%{#stat.index}].description" value="%{description}" />
								<s:hidden name="data.subTasks[%{#stat.index}].description" value="%{description}" />
							</td>
							<td>
								<s:label name="data.subTasks[%{#stat.index}].deadlineString" value="%{deadlineString}" />
								<s:hidden name="data.subTasks[%{#stat.index}].deadlineString" value="%{deadlineString}" />
							</td>
							<td>
								<s:label name="data.subTasks[%{#stat.index}].priorityString" value="%{priorityString}" />
								<s:hidden name="data.subTasks[%{#stat.index}].priorityString" value="%{priorityString}" />
							</td>
							<s:hidden name="data.subTasks[%{#stat.index}].id"  value="%{id}"/>
							<s:hidden name="data.subTasks[%{#stat.index}].version"  value="%{version}"/>
							<s:hidden name="data.subTasks[%{#stat.index}].statusString" value="%{statusString}" />
						</tr>
					</s:iterator>
				</table>
			</s:if>
			<s:else>
				<i><s:label name="%{getText('todo.noSubTasks')}" value="%{getText('todo.noSubTasks')}"/></i>
			</s:else>
			<s:submit method="newTask" type="button" label="Natrag na glavni zadatak" ></s:submit> 
			
		</s:if>
		<s:else>
				<s:submit method="insertNewTask" type="button" label="Pohrani podzadatak" ></s:submit> 
		</s:else>
		
		<!-- REALIZATORI -->
		<s:iterator value="data.realizers" status="stat">
			<tr>
				<td>
					<s:hidden name="data.realizers[%{#stat.index}].description" value="%{description}" />
					<s:hidden name="data.realizers[%{#stat.index}].id" value="%{id}" />
					<s:hidden name="data.realizers[%{#stat.index}].userRealizer" value="%{userRealizer}" />
				</td>
			</tr>
		</s:iterator>
		
	

	</s:form>
	
	

</div>

