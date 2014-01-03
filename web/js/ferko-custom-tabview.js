/*
 * ferko-custom-tabview.js
 *
 * JavaScript code for simple tabview components. Requires jQuery.
 * 
 */
   function tabbed_view_show(ident,index,slow) {
      var $tabs = $("#"+ident+" .tabpages .tabpage .tabbody");
      for(i=0; i<$tabs.length; i++) {
	      if(i==index) {
		      if(slow==1) $tabs.eq(i).fadeIn(); else $tabs.eq(i).show();
		  } else {
		      $tabs.eq(i).hide();
		  }
	  }
	  
      var $titles = $("#"+ident+" .tabtitles a");
      for(i=0; i<$tabs.length; i++) {
	      if(i==index) {
		      if(!($titles.eq(i).hasClass("activeTabTitle"))) {
			      $titles.eq(i).addClass("activeTabTitle");
			  }
		  } else {
		      $titles.eq(i).removeClass("activeTabTitle");
		  }
	  }
   }

   function tabbed_view_init(ident) {
      $("#"+ident+" .tabpages .tabpage .tabtitle").hide();
      var $titles = $("#"+ident+" .tabpages .tabpage .tabtitle");
      var naslovi = "";
      for(i=0; i<$titles.length; i++) {
	      naslovi += "<a href='#' onclick='tabbed_view_show(\""+ident+"\","+i+",1); this.blur(); return false;'>"+$titles.eq(i).text()+"</a> ";
	  }
      $("#"+ident+" .tabtitles").append(naslovi);
      if($titles.length>0) {
        tabbed_view_show(ident, 0, 0);
	  }
   }
