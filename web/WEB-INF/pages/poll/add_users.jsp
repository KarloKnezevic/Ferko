<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<h1 class="pageh">Ankete: <em>dodijeli korisnike anketi</em></h1>
<div class="content">

	<div class="maincontent">
	<div class="inner-padding">
		<div id="user_search">
			<form id="user_search" action="#">
			<input type="text" id="search_term" />
			<button name="Search" type="button" id="user_search_button">Traži</button>
			</form>
			<ul class="user_search_list" id="user_search_results">
			</ul>
			<p><a href="#" id="add_all_users">Dodaj sve</a></p>
			<ul class="user_search_list" id="user_list">
			</ul>
			<p><a href="#" id="remove_all_users">Izbriši sve</a></p>
		</div>
		<div>
		<s:form action="AddUsers" theme="simple" method="post">
			<s:hidden name="users" id="users_form_list" />
			<s:hidden name="id" value="%{id}" />
			<s:submit name="submit" value="Dodaj korisnike" />
		</s:form>
		</div>
	</div>
	</div>
</div>


<script type="text/javascript" src="/ferko/js/jquery-1.2.6.min.js"></script>
<script type="text/javascript"><!--
	// TODO: napraviti od ovoga iskoristivu "skriptu"
	// Array Remove - By John Resig (MIT Licensed)
	Array.prototype.remove = function(from, to) {
		var rest = this.slice((to || from) + 1 || this.length);
		this.length = from < 0 ? this.length + from : from;
		return this.push.apply(this, rest);
	};
	// stavi "search gif" i obavijest ako nije ništa pronađeno
	var url = '/ferko/search/UserSearch.action?term=';
	users = [];
	var usersField = $('#users_form_list');
	function fillField() {
		usersField.val('');
		var str = '';
		var k;
		for(k=0;k<users.length-1;k++) str+=users[k].userId+'/'+users[k].groupId+',';
		str+=users[users.length-1].userId+'/'+users[users.length-1].groupId
		usersField.val(str);
	}
	function remove_user(user) {
		var k;
		for(k=0;k<users.length;k++) {
			if(users[k].userId==user.userId && users[k].groupId==user.groupId) users.remove(k);
		}
		$('#remu'+user.userId+'g'+user.groupId).parent().remove();
		fillField();
	}
	function contains_user(user) {
		var k;
		for(k=0;k<users.length;k++) {
			if(users[k].userId==user.userId && users[k].groupId==user.groupId) return true;
		}
		return false;
	}
	function createHTML(user, add) {
		var str = '<li>';
		str += '<span class="firstlastname">';
		str += user.firstName+' '+user.lastName+'</span><br/>';
		str += '<span class="group">'+user.courseName+', grupa <em>'+user.groupName+'</em></span>';
		if(add == true) {
			str += ' <a href="#" class="add_user" id="addu'+user.userId+'g'+user.groupId+'">Dodaj</a>';
		} else {
			str += ' <a href="#" class="remove_user" id="remu'+user.userId+'g'+user.groupId+'">Izbriši</a>';
		}
		str += '</li>';
		return str;
	}
	function add_user(user) {
		users.push(user);
		$('#user_list').append(createHTML(user,false));
		$('#addu'+user.userId+'g'+user.groupId).parent().remove();
		$('#remu'+user.userId+'g'+user.groupId).mouseup(function(){ remove_user(user); });
		fillField();
	}
	function add_all_users() {
		$('#user_search_results > li > a').trigger('mouseup');
	}
	function results(data) {
		$('#user_search_results > li').remove();
		var term = $('#search_term').val();
		$.each(data.data.users, function(i,user){
			if(contains_user(user)) return;
			$('#user_search_results').append(createHTML(user,true));
			$('#addu'+user.userId+'g'+user.groupId).mouseup(function(){ add_user(user); });
		});
	}
	$('#add_all_users').mouseup(function() { 
		$('#user_search_results > li > a').trigger('mouseup');
	});
	$('#remove_all_users').mouseup(function() {
		$('#user_list > li > a').trigger('mouseup');
	});
	$('#user_search_button').click(function() { $.getJSON(url+$('#search_term').val(), results); });
	$('#remove_all_users').mouseup(function() { remove_all_users(); });
	$('#user_search').submit(function() { $.getJSON(url+$('#search_term').val(), results); });
	
--></script>
