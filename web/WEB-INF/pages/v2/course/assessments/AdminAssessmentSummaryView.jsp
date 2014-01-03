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
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data==null || data.table==null || data.activeIndex==null">
  <s:text name="Info.assessmentDataNotAvailable" />
</s:if>
<s:else>

<s:if test="data.selectedGroup!=null">
  <div><s:text name="forms.group" /> <s:property value="data.selectedGroup.name"/></div>
</s:if>

<script type="text/javascript">
  var filterData = null;
  var filterExpr = "";
  var filterIndex = 2;
  var cachedStudentClass = null;
  var gradeHeaderIndex = -1;
  var groupHeaderIndex = -1;
  
  function studentClass(lastName, firstName, jmbag) {
	  this.jmbag = jmbag;
	  this.lastName = lastName;
	  this.firstName = firstName;
  }
  
  function student() {
	  var x = filterData[filterIndex][0];
	  if(cachedStudentClass==null || cachedStudentClass.jmbag!=x.j) {
		  cachedStudentClass = new studentClass(x.l, x.f, x.j);
	  }
	  return cachedStudentClass;
  }
  function jmbagIn() {
	  var st = student();
	  for(var i = 0; i < arguments.length; i++) {
		  if(arguments[i]==st.jmbag) return true;
	  }
	  return false;
  }
  function lastNameIn() {
	  var st = student();
	  for(var i = 0; i < arguments.length; i++) {
		  if(arguments[i]==st.lastName) return true;
	  }
	  return false;
  }
  function firstNameIn() {
	  var st = student();
	  for(var i = 0; i < arguments.length; i++) {
		  if(arguments[i]==st.firstName) return true;
	  }
	  return false;
  }
  function present(name) {
	  return filterData[filterIndex][mapaProvjera["Name"+name]].ep;
  }
  function score(name) {
	  return filterData[filterIndex][mapaProvjera["Name"+name]].es;
  }
  function passed(name) {
	  return filterData[filterIndex][mapaProvjera["Name"+name]].y=="PASSED";
  }
  function rank(name) {
	  return filterData[filterIndex][mapaProvjera["Name"+name]].er;
  }
  function flag(name) {
	  return filterData[filterIndex][mapaProvjera["Name"+name]].v;
  }
  function grade() {
	  if(gradeHeaderIndex==-1) return 0;
	  return filterData[filterIndex][gradeHeaderIndex].g;
  }
  function group() {
	  if(groupHeaderIndex==-1) return "";
	  return filterData[filterIndex][groupHeaderIndex].n;
  }
  
  function primjeniFilter() {
	  napraviTablicu();
  }
</script>

<div><i>Info:</i> Klik na ime i prezime studenta prikazat će njegov/njezin pogled na bodove. Klik na bodove studenta prikazat će detalje.</div>

<div style="text-align: right;">
<form name="exprfil">Filter<a target='_jcms_help' href='<s:url action="Help"><s:param name="helpKey">000029</s:param></s:url>' onclick='blur();'><img src='img/icons/help.png'></a>: <input name="fil" value="" size="50"><button onclick="primjeniFilter(); return false;">Primjeni</button></form>
</div>

<div id="datatable"><div id="datatableH"><s:text name="Info.pleaseWaitData" /></div><div id="datatableT">&nbsp;</div></div>

<script type="text/javascript">
  var data = <s:property value="data.dataJSON" escape="false"/>;
  var headers = <s:property value="data.headersJSON" escape="false"/>;
  var dependencies = <s:property value="data.dependenciesJSON" escape="false"/>;
</script>

<script type="text/javascript">

  var mapaProvjera = new Object();
  for(var i = 1; i < headers.length; i++) {
	  if(headers[i].headerType=="G") gradeHeaderIndex=i;
	  if(headers[i].headerType=="L") groupHeaderIndex=i;
	  mapaProvjera["Name"+headers[i].shortName] = i;
  }
  
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
	  thtml2 = "<div><a href='#' onclick='toggleParameters(); return false;'>Parametri</a><a target='_jcms_help' href='<s:url action="Help"><s:param name="helpKey">000012</s:param></s:url>' onclick='blur();'><img src='img/icons/help.png'></a></div>"+thtml2;
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
	  try {
	      napraviTablicuInner(colSortIndex,sortOrder);
      } catch(err) {
          var txt="There was an error on this page.\n\n";
          txt+="Error description: " + err.description + "\n\n";
          txt+="Click OK to continue.\n\n";
          alert(txt);
  	      return false;
	  }
  }

  function popup1(uid) {
	  var win = window.open('<s:url action="AssessmentSummaryView"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>&data.studentID='+uid, 'ferkoPopup1', 'width=900,height=600,resizable=yes,scrollbars=yes,location=no,directories=no,status=no,menubar=no,copyhistory=no,left=0,top=0,titlebar=no,toolbar=no');
	  win.focus();
  }
  
  function popup2(uid, aid) {
	  var win = window.open('<s:url action="AssessmentView"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>&assessmentID='+aid+'&userID='+uid, 'ferkoPopup1', 'width=900,height=600,resizable=yes,scrollbars=yes,location=no,directories=no,status=no,menubar=no,copyhistory=no,left=0,top=0,titlebar=no,toolbar=no');
	  win.focus();
  }
  
  var tblGlobalPagesCount = 0;
  var tblGlobalCurrentPage = 0;
  var tblGlobalPageSize = 0;
  var tblGlobalLastPageSize = 0;

  function showTablePage(pg) {
	  if(pg==0) {
	    $("table tbody tr").show();
  	    tblGlobalCurrentPage = pg;
		return;
	  }
  	  tblGlobalCurrentPage = pg;
	  $("table.adata tbody tr").show();
	  $("table.adata tbody tr:lt("+((tblGlobalCurrentPage-1)*tblGlobalPageSize)+")").hide();
	  $("table.adata tbody tr:gt("+((tblGlobalCurrentPage)*tblGlobalPageSize-1)+")").hide();
	  //if(tblGlobalCurrentPage!=pg) {
	  //  $("table tbody tr").hide();
  	  //}
  	  //tblGlobalCurrentPage = pg;
  	  //var filter = "table tbody tr.tblPage"+tblGlobalCurrentPage;
	  //$(filter).show();
  }
    
  function napraviTablicuInner(colSortIndex,sortOrder) {
	  var sb = new StringBuilder2();
	  
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
				  elements.sort(function(a, b) { if(a[colSortIndex].es<b[colSortIndex].es) return -1; if(a[colSortIndex].es>b[colSortIndex].es) return 1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].es<b[colSortIndex].es) return 1; if(a[colSortIndex].es>b[colSortIndex].es) return -1; return 0;});
			  }
		  } else if(headers[colSortIndex].headerType=='F') {
			  if(sortOrder==1) {
				  elements.sort(function(a, b) { if(a[colSortIndex].v<b[colSortIndex].v) return -1; if(a[colSortIndex].v>b[colSortIndex].v) return 1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].v<b[colSortIndex].v) return 1; if(a[colSortIndex].v>b[colSortIndex].v) return -1; return 0;});
			  }
		  } else if(headers[colSortIndex].headerType=='G') {
			  if(sortOrder==1) {
				  elements.sort(function(a, b) { if(a[colSortIndex].g<b[colSortIndex].g) return -1; if(a[colSortIndex].g>b[colSortIndex].g) return 1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].g<b[colSortIndex].g) return 1; if(a[colSortIndex].g>b[colSortIndex].g) return -1; return 0;});
			  }
		  } else if(headers[colSortIndex].headerType=='L') {
			  if(sortOrder==1) {
				  elements.sort(function(a, b) { if(a[colSortIndex].n<b[colSortIndex].n) return 1; if(a[colSortIndex].n>b[colSortIndex].n) return -1; return 0;});
			  } else {
				  elements.sort(function(a, b) { if(a[colSortIndex].n<b[colSortIndex].n) return -1; if(a[colSortIndex].n>b[colSortIndex].n) return 1; return 0;});
			  }
		  }
	  }

	  var filtrirajUvjet = document.forms['exprfil'].fil.value;
	  var filtriraj = filtrirajUvjet!="";
	  filterData = elements;

	  var dozvoljeniRetci = new Array();
	  var brojDozvoljenihRedaka = 0;
	  for(var si=0; si<elements.length; si++) {
		  dozvoljeniRetci[si] = 1;
		  brojDozvoljenihRedaka++;
		  filterIndex = si;
		  var studentVar = elements[si];
		  if(filtriraj) {
			  if(!eval(filtrirajUvjet)) {
				  brojDozvoljenihRedaka--;
		          dozvoljeniRetci[si] = 0;
				  continue;
			  }
		  }
      }
      var rowsPerLogicalPage = 100;
      var remainder = brojDozvoljenihRedaka % rowsPerLogicalPage;
      var quotient = Math.round(( brojDozvoljenihRedaka - remainder ) / rowsPerLogicalPage);
      var brojStranica = remainder==0 ? quotient : quotient+1;
      var pageMeni = "";
      for(var pgcnt = 0; pgcnt < brojStranica; pgcnt++) {
	    var link1 = pgcnt==0 ? "" : " | ";
	    link1 = link1 + "<a href=\"javascript:showTablePage("+(pgcnt+1)+");\">"+(pgcnt+1)+"</a>";
	    pageMeni += link1;
	  }
	  pageMeni += " | <a href=\"javascript:showTablePage(0);\">sve</a><br>";
	  if(brojStranica>1) sb.dodaj(pageMeni);
	  
	  tblGlobalPagesCount = brojStranica;
  	  tblGlobalCurrentPage = 1;
  	  tblGlobalPageSize = rowsPerLogicalPage;
  	  tblGlobalLastPageSize = remainder==0 ? rowsPerLogicalPage : remainder;

	  sb.dodaj("<table class='adata' cellspacing='0'>\r\n<thead><tr>");
	  for(var ci=0; ci<headers.length; ci++) {
		  var ht = headers[ci].headerType;
		  if(ht=="S") {
			  sb.dodaj("<th>Br</th><th>Prezime, ime <a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",0)'>N</a><a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",1)'>R</a></th>");
		  } else if(ht=="G") {
			  var nentry = namesMap[headers[ci].headerUniqueID];
			  if(nentry == null) continue;
			  sb.dodaj("<th>");
			  sb.dodaj(headers[ci].shortName);
			  sb.dodaj(" <a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",0)'>N</a><a href='#' onclick='napraviTablicu(");
			  sb.dodaj(ci);
			  sb.dodaj(",1)'>R</a></th>");
		  } else if(ht=="L") {
			  var nentry = namesMap[headers[ci].headerUniqueID];
			  if(nentry == null) continue;
			  sb.dodaj("<th>");
			  sb.dodaj(headers[ci].shortName);
			  sb.dodaj(" <a href='#' onclick='napraviTablicu(");
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
			  } else if(ht=="G") {
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
	  sb.dodaj("</tr></thead>");
	  var stvarniIndex = 0;
	  var currPage = 1;
	  var currRowWithinPage = 0;
	  for(var si=0; si<elements.length; si++) {
		  filterIndex = si;
		  var studentVar = elements[si];
		  if(dozvoljeniRetci[si]==0) continue;
		  //if(filtriraj) {
		  //	  if(!eval(filtrirajUvjet)) continue;
		  //}
		  stvarniIndex++;
		  currRowWithinPage++;
		  if(currRowWithinPage>tblGlobalPageSize) {
			  currRowWithinPage = 1;
			  currPage++;
		  }
		  sb.dodaj("<tr class=\"tblPage"+currPage+"\""+(currPage!=tblGlobalCurrentPage ? " style=\"display: none;\"" : "")+">");
		  var studentoviPodatci;
		  for(var j=0; j<headers.length; j++) {
			  var ht = headers[j].headerType;
			  if(ht=="S") {
				  studentoviPodatci = studentVar[j];
				  break;
			  }
		  }
		  for(var j=0; j<headers.length; j++) {
			  var ht = headers[j].headerType;
			  if(ht=="S") {
				  sb.dodaj("<td>");
				  sb.dodaj(stvarniIndex);
				  sb.dodaj("</td><td onclick=\"popup1("+studentVar[j].id+")\">");
				  sb.dodaj(studentVar[j].l);
				  sb.dodaj(", ");
				  sb.dodaj(studentVar[j].f);
				  sb.dodaj(" (");
				  sb.dodaj(studentVar[j].j);
				  sb.dodaj(")</td>");
			  } else if(ht=="G") {
				  var x = studentVar[j];
				  var nentry = namesMap[headers[j].headerUniqueID];
				  if(nentry == null) continue;
				  sb.dodaj("<td>");
				  sb.dodaj(""+x.g);
				  sb.dodaj("</td>");
			  } else if(ht=="L") {
				  var x = studentVar[j];
				  var nentry = namesMap[headers[j].headerUniqueID];
				  if(nentry == null) continue;
				  sb.dodaj("<td>");
				  sb.dodaj(x.n);
				  sb.dodaj("</td>");
			  } else {
				  var x = studentVar[j];
				  var nentry = namesMap[headers[j].headerUniqueID];
				  if(nentry == null || !nentry.selected) continue;
				  if(ht=="A") {
					  sb.dodaj("<td onclick=\"popup2("+studentoviPodatci.id+","+headers[j].headerID+")\">");
					  if(x.e) {
						  sb.dodaj('<div class="ST_ERROR">*</div>');
					  } else if(!x.ep) {
						  sb.dodaj("&nbsp;");
					  } else {
						  sb.dodaj('<span class="ST_');
						  sb.dodaj(x.y);
						  sb.dodaj('">');
						  sb.dodaj(x.ea);
						  sb.dodaj('</span>');
						  if(x.er<30000) {
						    sb.dodaj(' (');
						    sb.dodaj(x.er);
						    sb.dodaj(')');
						  }
					  }
					  sb.dodaj("</td>");
				  } else if(ht=="F") {
					  sb.dodaj("<td>");
					  if(x.e) {
						  sb.dodaj('<div class="ST_ERROR">*</div>');
					  } else if(x.v) {
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
	  filterData = null;
	  sb.dodaj("</table>");
	  if(brojStranica>1) sb.dodaj(pageMeni);
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
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="selectedGroup"><s:property value="data.selectedGroup.id"/></s:param><s:param name="format">xls</s:param></s:url>"><img width="32" height="32" src="img/icons/ft-xls.png" title="<s:text name="Navigation.assessmentSummaryExportXls"/>" align="right"></a>
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="selectedGroup"><s:property value="data.selectedGroup.id"/></s:param><s:param name="format">csv</s:param></s:url>"><img width="32" height="32" src="img/icons/ft-csv.png" title="<s:text name="Navigation.assessmentSummaryExportCsv"/>" align="right"></a>
</s:if>
<s:else>
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">xls</s:param></s:url>"><img width="32" height="32" src="img/icons/ft-xls.png" title="<s:text name="Navigation.assessmentSummaryExportXls"/>" align="right"></a>
    <a href="<s:url action="AdminAssessmentSummaryExport"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="format">csv</s:param></s:url>"><img width="32" height="32" src="img/icons/ft-csv.png" title="<s:text name="Navigation.assessmentSummaryExportCsv"/>" align="right"></a>
</s:else>
</div>

</s:else>
