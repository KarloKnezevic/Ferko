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
	
	
	<s:url id="json" action="ToDoUsersListJSON"></s:url>
	
	<h2>Novi ToDo zadatak</h2>
	<s:form action="ToDo" method="post" theme="ferko">
		
		<s:hidden name="data.newTemplate" value="%{data.newTemplate}"/>
		
		<p><p>
		<s:textfield size= "40" name="data.newTask.title" label="%{getText('todo.title')}" required="true" />
		<s:if test="data.newTask.groupTask">
			<s:hidden name="data.newTask.description" value="%{data.newTask.description}"/>
			<s:textarea rows="5" cols="40" name="data.newTask.groupTaskDescription" label="%{getText('todo.description')}" />
		</s:if>
		<s:else>
			<s:textarea rows="5" cols="40" name="data.newTask.description" label="%{getText('todo.description')}" />
		</s:else>
		
		<s:textfield size= "40" name="data.newTask.deadlineString"  label="%{getText('todo.deadline')}" required="true" /> 
		<s:select name = "data.newTask.priorityString" list="data.priorities" label="%{getText('todo.priorities')}" />
		<s:hidden name="data.newTask.id" value="%{data.newTask.id}" />
		<s:hidden name="data.newTask.version" value="%{data.newTask.version}" />
		
		<s:if test="data.newTemplate">
			<s:select name = "data.newTask.statusString" list="#{'TEMPLATE':'PRIVATE TEMPLATE', 'PUBLIC_TEMPLATE':'PUBLIC TEMPLATE'}" label="%{getText('todo.templateStatus')}" />
		</s:if>
		<s:else>
			<s:hidden name="data.newTask.statusString" value="%{data.newTask.statusString}" />
		</s:else>
		
		<br><br>
		<b><s:label name="%{getText('todo.subTasks')}" value="%{getText('todo.subTasks')}"/></b>
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
						<s:hidden name="data.subTasks[%{#stat.index}].statusString"  value="%{statusString}"/>
					</tr>
				</s:iterator>
			</table>
		</s:if>
		<s:else>
			<i><s:label name="%{getText('todo.noSubTasks')}" value="%{getText('todo.noSubTasks')}"/></i>
			
		</s:else>

		<s:submit method="addSubTask" type="button" label="Dodaj podzadatke" ></s:submit> 
		
		
		<!-- BEGIN: REALIZATORI -->
		<br><br>
		<b><s:label name="%{getText('todo.realizers')}" value="%{getText('todo.realizers')}" /></b>
		<s:if test="data.realizers.size>0">
			<table style="width:60%;font-size:0.8em"> 
				<tr>
					<th>Opis</th>
					<s:if test="data.newTask.id==null&&data.renderBothToDoLists"><th>Obrisati</th></s:if>
				</tr>
				<s:iterator value="data.realizers" status="stat">
					<tr>
						<td>
							<s:if test="data.newTask.groupTask">
								<s:label name="data.newTask.realizerGroupName" value="%{data.newTask.realizerGroupName}" />
							</s:if>
							<s:else>
							<s:label name="data.realizers[%{#stat.index}].description" value="%{description}" />
							</s:else>
							<s:hidden name="data.realizers[%{#stat.index}].description" value="%{description}" />
							<s:hidden name="data.realizers[%{#stat.index}].id" value="%{id}" />
							<s:hidden name="data.realizers[%{#stat.index}].userRealizer" value="%{userRealizer}" />
						</td>
						<td>
							<s:if test="data.newTask.id==null&&data.renderBothToDoLists">
								<!-- <s:checkbox name="data.realizers[%{#stat.index}].checked" value="%{data.realizers[%{#stat.index}].checked}" /> -->
								<input type="checkbox" value="%{data.realizers[%{#stat.index}].checked}" name="data.realizers[%{#stat.index}].checked" >
							</s:if>
						</td>
					</tr>
				</s:iterator>
			</table>
		</s:if>
		<s:else>
			<i><s:label name="%{getText('todo.noRealizers')}" value="%{getText('todo.noRealizers')}"  /></i>
		</s:else>
		<s:if test="data.renderBothToDoLists&&data.newTask.id==null && !data.newTemplate">
		
			<s:select label="%{getText('todo.availableGroups')}" name = "data.selectedGroupID" list="%{#{}}" >
				<s:iterator value="data.userGroups">
					<s:optgroup label="%{courseName}" list="groups" listKey="id" listValue="name" />
				</s:iterator>
			</s:select>
	
			<s:label value="%{getText('todo.individualRealizers')}" />
			<s:autocompleter theme="ajax" href="%{json}" name="user" autoComplete="true" loadOnTextChange="true" loadMinimumCount="1" forceValidOption="true"  />
			<s:submit method="addTaskRealizer" type="button" label="Dodaj/obriši korisnike" ></s:submit>
			
		</s:if>	
	
		<!-- END: REALIZATORI -->

	<p><p>
	
	<s:if test="data.newTemplate">
		<s:submit method="insertNewTask" type="button" label="Pohrani predložak!" ></s:submit> 
	</s:if>
	<s:else>
		<s:submit method="insertNewTask" type="button" label="Objavi zadatak!" ></s:submit> 
	</s:else>
			
	</s:form>
	
	

</div>

