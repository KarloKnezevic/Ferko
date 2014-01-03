<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<style>
.ST_E { background-color: #FFFF00; }
.ST_PASSED { background-color: #00FF00; }
.ST_FAILED { background-color: #FF0000; }
table.adata {margin-bottom:0;width:100%;}
table.adata p {margin:0 0 1.5em;}
table.adata th, table.adata td {padding:0px 0px 0px 0px;}
table.adata td {border-bottom: 1px dotted gray; }
.hlt td {background-color: #FFFF80; }
</style>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data==null || data.table==null || data.activeIndex==null">
  Podaci trenutno nisu dostupni. Pokušajte pokrenuti izračun svih bodova na kolegiju.
</s:if>
<s:else>

<s:if test="data.selectedGroup!=null">
  <div><s:text name="forms.group" /> <s:property value="data.selectedGroup.name"/></div>
</s:if>

<div id="datatable"><div id="datatableH">Molim pričekajte, podaci se dohvaćaju...</div><div id="datatableT">&nbsp;</div></div>

<script type="text/javascript">
  var data = <s:property value="data.dataJSON" escape="false"/>;
  var headers = <s:property value="data.headersJSON" escape="false"/>;
  var dependencies = <s:property value="data.dependenciesJSON" escape="false"/>;
</script>

<script type="text/javascript">

  function rowHighlight(obj, newClass) {
    obj.className = newClass;
  }

  function filterClicked(checkBox, uniqueID) {
	  var trebaPrikazati = checkBox.checked;
	  namesMap[uniqueID].selected = trebaPrikazati;
	  //napraviTablicu();
	  return true;
  }
  
  // Evo konstruktora objekta
  function RemapNameEntry(index, item, selected) {
	  this.index = index;
	  this.item = item;
	  this.selected = selected;
  }
  
  function remapNames() {
	  // Ovo dolje je globalna varijabla! Ko bi to reko? Ha? Ja svakako ne bi...
	  namesMap = new Array();
	  for(var i = 0; i < headers.length; i++) {
		  var headerItem = headers[i];
		  namesMap[headerItem.headerUniqueID] = new RemapNameEntry(i, headerItem, false);
	  }
  }

  function toggleParameters() {
	  $("#hierTree").toggle();
  }
  
  function hideParameters() {
	  $("#hierTree").hide();
  }
  
  function nacrtajHijerarhiju() {
	  var thtml = nacrtajHijerarhiju1(dependencies);
	  thtml += "<a href='#' onclick='napraviTablicu(); hideParameters(); return false;'>Osvježi prikaz</a><br>";
	  var thtml2 = "<div id='hierTree'>"+thtml+"</div>";
	  thtml2 = "<div><a href='#' onclick='toggleParameters(); return false;'>Parametri</a></div>"+thtml2;
	  $("#datatableH").html(thtml2);
  }

  
  function nacrtajHijerarhiju1(currentDeps) {
	  var h = "<ul>";
	  for(var i=0; i<currentDeps.length; i++) {
		  var item = currentDeps[i];
		  h += "<li>"+namesMap[item.uniqueID].item.shortName+" <input type='checkbox' "+(namesMap[item.uniqueID].selected?"checked":"")+" onclick='filterClicked(this, \""+item.uniqueID+"\"); return true;'>";
		  if(item.deps.length>0) {
			  h += nacrtajHijerarhiju1(item.deps);
		  }
		  h += "</li>";
	  }
	  h += "</ul>";
	  return h;
  }

  var struktura = new Array();
  var dubine = new Array();
  
  function izravnajStrukturu(currentDeps, level) {
	  for(var i=0; i<currentDeps.length; i++) {
		  var item = currentDeps[i];
		  var uid = item.uniqueID;
		  if(!dubine[uid]) {
			  dubine[uid] = level;
			  namesMap[uid].selected = namesMap[uid].item.headerType=="A" && level<=2;
		  } else {
			  if(dubine[uid] > level) {
				  dubine[uid] = level;
				  namesMap[uid].selected = namesMap[uid].item.headerType=="A" && level<=2;
			  }
		  }
		  var sadrzi = false;
		  for(var j=0; j<struktura.length; j++) {
			  if(struktura[j]==uid) { sadrzi=true; break; }
		  }
		  if(sadrzi) continue;
		  struktura[struktura.length] = uid;
		  if(item.deps.length>0) {
			  izravnajStrukturu(item.deps, level+1);
		  }
	  }
  }

  function StringBuilder2() {
	  this.l = 0;
	  this.buffer = []; 
  }

  StringBuilder2.prototype.dodaj = function dodaj(string) { 
   this.buffer[this.l]=string;
   this.l++;
  }; 

  StringBuilder2.prototype.toString = function toString() { 
   return this.buffer.join(""); 
  }; 

  function napraviTablicu(colSortIndex,sortOrder) {
	  var sb = new StringBuilder2();
	  sb.dodaj("<table class='adata' cellspacing='0'>\r\n<tr>");
	  for(var ci=0; ci<headers.length; ci++) {
		  var ht = headers[ci].headerType;
		  if(ht=="S") {
			  sb.dodaj("<th>Br</th><th>Prezime, ime <a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",0)'>N</a><a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",1)'>R</a></th>");
		  } else {
			  var nentry = namesMap[headers[ci].headerUniqueID];
			  if(nentry == null || !nentry.selected) continue;
			  if(ht=="A") {
				  sb.dodaj("<th>");
				  sb.dodaj(headers[ci].shortName);
				  sb.dodaj(" <a href='#' onclick='napraviTablicu(");
				  sb.dodaj(ci);
				  sb.dodaj(",0)'>N</a><a href='#' onclick='napraviTablicu(");
				  sb.dodaj(ci);
				  sb.dodaj(",1)'>R</a></th>");
			  } else if(ht=="F") {
				  sb.dodaj("<th>");
				  sb.dodaj(headers[ci].shortName);
				  sb.dodaj(" <a href='#' onclick='napraviTablicu(");
				  sb.dodaj(ci);
				  sb.dodaj(",0)'>N</a><a href='#' onclick='napraviTablicu(");
				  sb.dodaj(ci);
				  sb.dodaj(",1)'>R</a></th>");
			  }
		  }
	  }
	  if(!colSortIndex) colSortIndex=0;
	  if(!sortOrder) sortOrder=0;
	  var elements = new Array();
	  for(var si=0; si<data.length; si++) {
		  elements[si] = data[si];
	  }
	  if(colSortIndex==0) {
		if(sortOrder==1) elements.reverse();  
	  } else {
		  if(headers[colSortIndex].headerType=='A') {
			  if(sortOrder==1) {
				  elements.sort(function(a, b) { if(a[colSortIndex].effectiveScore<b[colSortIndex].effectiveScore) return -1; if(a[colSortIndex].effectiveScore>b[colSortIndex].effectiveScore) return 1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].effectiveScore<b[colSortIndex].effectiveScore) return 1; if(a[colSortIndex].effectiveScore>b[colSortIndex].effectiveScore) return -1; return 0;});
			  }
		  } else if(headers[colSortIndex].headerType=='F') {
			  if(sortOrder==1) {
				  elements.sort(function(a, b) { if(a[colSortIndex].value<b[colSortIndex].value) return -1; if(a[colSortIndex].value>b[colSortIndex].value) return 1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].value<b[colSortIndex].value) return 1; if(a[colSortIndex].value>b[colSortIndex].value) return -1; return 0;});
			  }
		  }
	  }
	  
	  sb.dodaj("</tr>");
	  for(var si=0; si<elements.length; si++) {
		  sb.dodaj("<tr>");
		  var student = elements[si];
		  for(var j=0; j<headers.length; j++) {
			  var ht = headers[j].headerType;
			  if(ht=="S") {
				  sb.dodaj("<td>");
				  sb.dodaj(si+1);
				  sb.dodaj("</td><td>");
				  sb.dodaj(student[j].lastName);
				  sb.dodaj(", ");
				  sb.dodaj(student[j].firstName);
				  sb.dodaj(" (");
				  sb.dodaj(student[j].jmbag);
				  sb.dodaj(")</td>");
			  } else {
				  var x = student[j];
				  var nentry = namesMap[headers[j].headerUniqueID];
				  if(nentry == null || !nentry.selected) continue;
				  if(ht=="A") {
					  sb.dodaj("<td>");
					  if(x.error) {
						  sb.dodaj('<div class="ST_ERROR">*</div>');
					  } else if(!x.effectivePresent) {
						  sb.dodaj("&nbsp;");
					  } else {
						  sb.dodaj('<span class="ST_');
						  sb.dodaj(x.effectiveStatus);
						  sb.dodaj('">');
						  sb.dodaj(x.effectiveScoreAsString);
						  sb.dodaj('</span>');
						  if(x.effectiveRank<30000) {
						    sb.dodaj(' (');
						    sb.dodaj(x.effectiveRank);
						    sb.dodaj(')');
						  }
					  }
					  sb.dodaj("</td>");
				  } else if(ht=="F") {
					  sb.dodaj("<td>");
					  if(x.error) {
						  sb.dodaj('<div class="ST_ERROR">*</div>');
					  } else if(x.value) {
						  sb.dodaj('<img src="/ferko/img/icons/famtick.png" border="0" />');
					  } else {
						  sb.dodaj('<img src="/ferko/img/icons/famcross.png" border="0" />');
					  }
					  sb.dodaj("</td>");
				  }
			  }
		  }
		  sb.dodaj("</tr>");
	  }
	  sb.dodaj("</table>");
	  $("#datatableT").html(sb.toString());
	  $("table.adata tr").bind("mouseenter", function() {rowHighlight(this,'hlt');}).bind("mouseleave", function() {rowHighlight(this,'');});
  }
  
  remapNames();
  izravnajStrukturu(dependencies,1);
  nacrtajHijerarhiju();
  hideParameters();
  napraviTablicu();
  
</script>

<div>
<s:if test="data.selectedGroup!=null">
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="selectedGroup"><s:property value="data.selectedGroup.id"/></s:param><s:param name="format">xls</s:param></s:url>"><s:text name="Navigation.assessmentSummaryExportXls"/></a>
  | <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="selectedGroup"><s:property value="data.selectedGroup.id"/></s:param><s:param name="format">csv</s:param></s:url>"><s:text name="Navigation.assessmentSummaryExportCsv"/></a>
</s:if>
<s:else>
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">xls</s:param></s:url>"><s:text name="Navigation.assessmentSummaryExportXls"/></a>
  | <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">csv</s:param></s:url>"><s:text name="Navigation.assessmentSummaryExportCsv"/></a>
</s:else>
</div>

</s:else>


<div  class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessmentsAdministration"/></a>
</div>
