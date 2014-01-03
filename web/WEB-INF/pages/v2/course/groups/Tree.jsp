<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2>Grupe <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000018</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>

  <s:if test="data.accessibleGroupsTree.empty">
	<p class="emptyMsg">Nema podataka.</p>
  </s:if>
  <s:else>
  <script src="js/jquery.contextMenu.js" type="text/javascript"></script>
  <link href="css/jquery.contextMenu.css" rel="stylesheet" type="text/css" />

  <style>
	#JQTreeview
	{
	  border: 1px solid #11B700;
	  padding: 5px;
	}
	
	.expandNode
	{
	  cursor: pointer;
	  width: 16px;
	  height: 16px;
	  background-color: #000;
	  float: left;
	  margin-right: 8px;
	  margin-top: 4px;
	}
	
	.expand
	{
	  background: url(img/expandIcon.png) no-repeat;
	}
	
	.collapse
	{
	  background: url(img/collapseIcon.png) no-repeat;
	}
	
	.NodeContents
	{
	  border-left: 6px solid #D2FFCC;
	  margin-left: 4px;
	  display: none;
	  padding-left: 15px;
	}
	
	span.ItemTitle
	{
	  font-size: 10px;
	  width: 100px;
	  text-decoration: underline;
	}
	
	div.NodeItem div.ItemTxt
	{
	  font-size: 14px;
	  padding: 2px 5px;
	  margin-bottom: 2px;
	  text-decoration: none;
	}
</style>

<p>Naputak: akcije nad grupama dostupne su kroz iskočni izbornik.</p>
<div id="JQTreeview"></div>

<script language="javascript">
<!-- begin script
//
//  Cookie Functions -- "Night of the Living Cookie" Version (25-Jul-96)
//
//  Written by:  Bill Dortch, hIdaho Design <bdortch@hidaho.com>
//  The following functions are released to the public domain.
//
//  This version takes a more aggressive approach to deleting
//  cookies.  Previous versions set the expiration date to one
//  millisecond prior to the current time; however, this method
//  did not work in Netscape 2.02 (though it does in earlier and
//  later versions), resulting in "zombie" cookies that would not
//  die.  DeleteCookie now sets the expiration date to the earliest
//  usable date (one second into 1970), and sets the cookie's value
//  to null for good measure.
//
//  Also, this version adds optional path and domain parameters to
//  the DeleteCookie function.  If you specify a path and/or domain
//  when creating (setting) a cookie**, you must specify the same
//  path/domain when deleting it, or deletion will not occur.
//
//  The FixCookieDate function must now be called explicitly to
//  correct for the 2.x Mac date bug.  This function should be
//  called *once* after a Date object is created and before it
//  is passed (as an expiration date) to SetCookie.  Because the
//  Mac date bug affects all dates, not just those passed to
//  SetCookie, you might want to make it a habit to call
//  FixCookieDate any time you create a new Date object:
//
//    var theDate = new Date();
//    FixCookieDate (theDate);
//
//  Calling FixCookieDate has no effect on platforms other than
//  the Mac, so there is no need to determine the user's platform
//  prior to calling it.
//
//  This version also incorporates several minor coding improvements.
//
//  **Note that it is possible to set multiple cookies with the same
//  name but different (nested) paths.  For example:
//
//    SetCookie ("color","red",null,"/outer");
//    SetCookie ("color","blue",null,"/outer/inner");
//
//  However, GetCookie cannot distinguish between these and will return
//  the first cookie that matches a given name.  It is therefore
//  recommended that you *not* use the same name for cookies with
//  different paths.  (Bear in mind that there is *always* a path
//  associated with a cookie; if you don't explicitly specify one,
//  the path of the setting document is used.)
//  
//  Revision History:
//
//    "Toss Your Cookies" Version (22-Mar-96)
//      - Added FixCookieDate() function to correct for Mac date bug
//
//    "Second Helping" Version (21-Jan-96)
//      - Added path, domain and secure parameters to SetCookie
//      - Replaced home-rolled encode/decode functions with Netscape's
//        new (then) escape and unescape functions
//
//    "Free Cookies" Version (December 95)
//
//
//  For information on the significance of cookie parameters, and
//  and on cookies in general, please refer to the official cookie
//  spec, at:
//
//      http://www.netscape.com/newsref/std/cookie_spec.html    
//
//******************************************************************
//
// "Internal" function to return the decoded value of a cookie
//
function getCookieVal (offset) {
  var endstr = document.cookie.indexOf (";", offset);
  if (endstr == -1)
    endstr = document.cookie.length;
  return unescape(document.cookie.substring(offset, endstr));
}
//
//  Function to correct for 2.x Mac date bug.  Call this function to
//  fix a date object prior to passing it to SetCookie.
//  IMPORTANT:  This function should only be called *once* for
//  any given date object!  See example at the end of this document.
//
function FixCookieDate (date) {
  var base = new Date(0);
  var skew = base.getTime(); // dawn of (Unix) time - should be 0
  if (skew > 0)  // Except on the Mac - ahead of its time
    date.setTime (date.getTime() - skew);
}
//
//  Function to return the value of the cookie specified by "name".
//    name - String object containing the cookie name.
//    returns - String object containing the cookie value, or null if
//      the cookie does not exist.
//
function GetCookie (name) {
  var arg = name + "=";
  var alen = arg.length;
  var clen = document.cookie.length;
  var i = 0;
  while (i < clen) {
    var j = i + alen;
    if (document.cookie.substring(i, j) == arg)
      return getCookieVal (j);
    i = document.cookie.indexOf(" ", i) + 1;
    if (i == 0) break; 
  }
  return null;
}
//
//  Function to create or update a cookie.
//    name - String object containing the cookie name.
//    value - String object containing the cookie value.  May contain
//      any valid string characters.
//    [expires] - Date object containing the expiration data of the cookie.  If
//      omitted or null, expires the cookie at the end of the current session.
//    [path] - String object indicating the path for which the cookie is valid.
//      If omitted or null, uses the path of the calling document.
//    [domain] - String object indicating the domain for which the cookie is
//      valid.  If omitted or null, uses the domain of the calling document.
//    [secure] - Boolean (true/false) value indicating whether cookie transmission
//      requires a secure channel (HTTPS).  
//
//  The first two parameters are required.  The others, if supplied, must
//  be passed in the order listed above.  To omit an unused optional field,
//  use null as a place holder.  For example, to call SetCookie using name,
//  value and path, you would code:
//
//      SetCookie ("myCookieName", "myCookieValue", null, "/");
//
//  Note that trailing omitted parameters do not require a placeholder.
//
//  To set a secure cookie for path "/myPath", that expires after the
//  current session, you might code:
//
//      SetCookie (myCookieVar, cookieValueVar, null, "/myPath", null, true);
//
function SetCookie (name,value,expires,path,domain,secure) {
  document.cookie = name + "=" + escape (value) +
    ((expires) ? "; expires=" + expires.toGMTString() : "") +
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") +
    ((secure) ? "; secure" : "");
}

//  Function to delete a cookie. (Sets expiration date to start of epoch)
//    name -   String object containing the cookie name
//    path -   String object containing the path of the cookie to delete.  This MUST
//             be the same as the path used to create the cookie, or null/omitted if
//             no path was specified when creating the cookie.
//    domain - String object containing the domain of the cookie to delete.  This MUST
//             be the same as the domain used to create the cookie, or null/omitted if
//             no domain was specified when creating the cookie.
//
function DeleteCookie (name,path,domain) {
  if (GetCookie(name)) {
    document.cookie = name + "=" +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}
// -->
</script>
<script>
// Ovaj kolačić pamti koji je zadnji link bio pritisnut, tako da ga mogu automatski otvoriti...
var cookieTreeData = GetCookie("treeExpandedPath");
</script>
<script>
var data = <s:property value="data.treeAsJSON" escape="false"/>;
</script>
<script>
    var treeMenuAbsoluteCounter = 0;
    var treeMenuAbsoluteCounter2 = 0;
    var treeMenuActionRegistry = {};

    function pathMatches(path, begin) {
        if(!path || !begin) return false;
        var pl = path.length;
        var bl = begin.length;
        if(pl<bl) return false;
        if(pl==bl) return path==begin;
        return path.slice(0,bl+1)==(begin+"#");
    }
    
	var treeview = function(tData, container)
	{
	  var dataSource = tData;
	  
	  this.addPopup = function(actions, container, pth) {
        if(actions && actions.length>0) {
	        treeMenuAbsoluteCounter++;
	        var arr = actions;
	        var alist = "<ul id='treeMenu"+treeMenuAbsoluteCounter+"' class='contextMenu'>";
	        for(var ai = 0; ai < arr.length; ai++) {
		        var act = arr[ai];
		        act['currentPath'] = pth;
		        var oznakaAkcije = "stavka_"+treeMenuAbsoluteCounter+"_"+ai;
		        treeMenuActionRegistry[oznakaAkcije] = act;
		        if(act.pu) {
			        alist += "<li><a href='#"+oznakaAkcije+"'>"+dataSource.linkBuilder["l_"+act.pu].plabel+"</a></li>";
			    } else {
		        	alist += "<li><a href='#"+oznakaAkcije+"'>"+act.label+"</a></li>";
			    }
		    }
	        alist += "</ul>";
	        $("body").append($(alist));
	        container.contextMenu(
	          {menu: 'treeMenu'+treeMenuAbsoluteCounter},
	          function(action, el, pos) {
			      var ll = action.lastIndexOf("#");
				  if(ll>=0) {
				    action = action.substring(ll+1);
				  }
		          var act = treeMenuActionRegistry[action];
		          SetCookie("treeExpandedPath", act['currentPath']);
				  if(act.pu) {
			          // alert(action+", url="+dataSource.linkBuilder["l_"+act.pu].purl+act.pl+", pth="+act['currentPath']);
			          document.location = dataSource.linkBuilder["l_"+act.pu].purl+act.pl;
				  } else {
			          // alert(action+", url="+act.url+", pth="+act['currentPath']);
			          document.location = act.url;
				  }
		      }
	        );
	    }
	  }
		
	  this.build = function(nodeInfo, pth)
	  {
		treeMenuAbsoluteCounter2++;
	    var nodeID = nodeInfo['title'].replace(/\s/g, "_");
		var cpth = pth + "#" + nodeID;

        var shouldExpand = nodeInfo['e'] && nodeInfo['e']==1;
        if(!shouldExpand) shouldExpand = pathMatches(cookieTreeData, cpth);
        	
	    var stil = shouldExpand ? "collapse" : "expand";
	    var disp = shouldExpand ? "block" : "none";
		
	    var node = $('<div id="'+nodeID+'-Node" style="margin-top: 5px;"><div class="expandNode '+stil+'"></div><span id="melem_'+treeMenuAbsoluteCounter2+'">'+nodeInfo['title']+'</span></div>');
        this.addPopup(nodeInfo['a'], $("#melem_"+treeMenuAbsoluteCounter2, node), cpth);

	    //$('<div class="expandNode '+stil+'"></div>')
	    //  .prependTo(node);
	       
	    var contents = $('<div class="NodeContents" style="display: '+disp+';"></div>');
	   
	    var ctx = this;
		jQuery.each(nodeInfo['i'], function(item, obj) {
	      if(obj['o'] == 'L')
	      {
	        var x = $('<div class="NodeItem"><div class="ItemTxt">'+obj.title+'</div></div>');
	        x.appendTo(contents);
	        ctx.addPopup(obj['a'], $(".ItemTxt",x), cpth+"#"+obj.title.replace(/\s/g, "_"));
	      }
	      if(obj['o'] == 'T')
	      {
	        sNode = ctx.build(obj, cpth);
	        contents.append(sNode);
	      }
	    });
	   
	    node.append(contents);
	   
	    node.children('.expandNode').click(function() {
	      var contents = $(this).parent().children(".NodeContents");
	      contents.toggle();
	      if(contents.css('display') != "none")
	      {
	        $(this).attr("class", "expandNode collapse");
	      }
	      else
	      {
	        $(this).attr("class", "expandNode expand");
	      }
	     
	    });
	   
	    return node;
	  }

	  //var tst1 = new Date();
	  this.tree = this.build(tData, "");
	  //var tst2 = new Date();
	  var treeCon = container;
	 
	  treeCon.append(this.tree);
	  //var tst3 = new Date();
	  //alert("Trajanje 1: "+(tst2.valueOf()-tst1.valueOf())+", trajanje 2: "+(tst3.valueOf()-tst2.valueOf()));
	}
</script>
<script>
	$(document).ready(function() {
	  tView = new treeview(data, $('#JQTreeview'));
	});
</script>
    
  </s:else>
