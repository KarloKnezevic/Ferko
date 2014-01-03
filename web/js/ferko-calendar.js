  function ferkoGetWindowWidth() {
    var myWidth = 0;
    if( typeof( window.innerWidth ) == 'number' ) {
      //Non-IE
      myWidth = window.innerWidth;
    } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
      //IE 6+ in 'standards compliant mode'
      myWidth = document.documentElement.clientWidth;
    } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
      //IE 4 compatible
      myWidth = document.body.clientWidth;
    }
    return myWidth;
  }

  function ferkoGetWindowHeight() {
    var myHeight = 0;
    if( typeof( window.innerWidth ) == 'number' ) {
      //Non-IE
      myHeight = window.innerHeight;
    } else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
      //IE 6+ in 'standards compliant mode'
      myHeight = document.documentElement.clientHeight;
    } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
      //IE 4 compatible
      myHeight = document.body.clientHeight;
    }
    return myHeight;
  }

  function FerkoDay(day, title, date) {
	  this.day = day;
	  this.title = title;
	  this.date = date;
  }
  
  function FerkoEvent(day, shour, smin, ehour, emin, title, eventID, room, context) {
	  this.day = day;
	  this.shour = shour;
	  this.smin = smin;
	  this.ehour = ehour;
	  this.emin = emin;
	  this.title = title;
	  this.eventID = eventID;
	  this.room = room;
	  this.context = context;
  }

  // Funkcija posebno pisana zbog glupog Explorera koji ne podrzava array.indexOf() koja uredno radi u FF i Operi.
  function arrayIndexOf(arr, elem) {
	  for(var i = 0; i < arr.length; i++) {
		  if(arr[i]==elem) return i;
      }
      return -1;
  }
    
  function FerkoCalendar(left, top, width, height, jsonurls) {
	  this.winWidth = ferkoGetWindowWidth();
	  this.winHeight = ferkoGetWindowHeight();
	  this.top = top;
	  this.left = left;
	  this.width = width;
	  this.height = height;
	  this.dayHeight = 25;
	  this.hoursWidth = 50;
	  this.dogadaji = new Array();
	  //this.days = new Array();
	  //this.days[0] = new FerkoDay(0, "Ponedjeljak", "2009-07-13");
	  //this.days[1] = new FerkoDay(1, "Utorak", "2009-07-14");
	  //this.days[2] = new FerkoDay(2, "Srijeda", "2009-07-15");
	  //this.days[3] = new FerkoDay(3, "Cetvrtak", "2009-07-16");
	  //this.days[4] = new FerkoDay(4, "Petak", "2009-07-17");
	  //this.days[5] = new FerkoDay(5, "Subota", "2009-07-18");
	  //this.days[6] = new FerkoDay(6, "Nedjelja", "2009-07-19");
	  this.offsetTop = 0;
	  this.offsetBottom = 0;
	  this.offsetLeft = this.hoursWidth;
	  this.offsetRight = 0;
	  this.clientWidth = this.width - this.offsetLeft - this.offsetRight - 1;
	  this.clientHeight = this.height - this.offsetTop - this.offsetBottom - 1;
	  this.dayColumnWidth = this.clientWidth / 7 - 5;
	  this.jsonurls = jsonurls;
	  this.startDate = "";
	  this.endDate = "";
  }

  var ferkoKalendar = undefined;

  function renderFerkoCalendarEvents() {
    // Renderiram dane
    $("#ferkoCalendarDays").empty();
    var currentDate = new Date();
    var currentDateD = currentDate.getDate(); // 1 - 31
    var currentDateM = currentDate.getMonth()+1; // 1-12
    var currentDateY = currentDate.getFullYear(); // godina
    var currentDateS = ""+currentDateY+"-";  // Ovo ce biti aktualni datum u formatu yyyy-MM-dd
    var currentDateAbsTime = currentDate.getHours() * 60 + currentDate.getMinutes();
    if(currentDateM<10) currentDateS += "0";
    currentDateS += currentDateM;
    currentDateS += "-";
    if(currentDateD<10) currentDateS += "0";
    currentDateS += currentDateD;
    var currentDayIndex = -1;
    for(var i = 0; i < ferkoKalendar.days.length; i++) {
	    var d = ferkoKalendar.days[i];
	    var l = d.day * ferkoKalendar.clientWidth / 7 + ferkoKalendar.offsetLeft;
	    var w = ferkoKalendar.clientWidth / 7;
	    var t = 0;
	    var h = ferkoKalendar.dayHeight;
	    var dt = d.date.substr(5,5);
	    var bgcol = "ffffff";
	    if(d.date==currentDateS) {
		    currentDayIndex = d.day;
		    bgcol = "ffffaa";
	    }
        $("#ferkoCalendarDays").append("<div style='background-color: #"+bgcol+"; text-align: center; position: absolute; overflow: hidden; left: "+l+"px; top: "+t+"px; width: "+w+"px; height: "+h+"px; border: 1px dotted black;'>"+d.title+" "+dt+"</div>");
    }
    $("#ferkoCalendarBase").empty();
    // Renderiram sate
    for(var i = 0; i < 24; i++) {
	    var l = -2;
	    var w = ferkoKalendar.hoursWidth - 2;
	    var t = i * ferkoKalendar.clientHeight / 24;
	    var h = ferkoKalendar.clientHeight / 24;
	    var tekst = i<10 ? "0"+i : ""+i;
        $("#ferkoCalendarBase").append("<div style='text-align: center; position: absolute; overflow: hidden; left: "+l+"px; top: "+t+"px; width: "+w+"px; height: "+h+"px; border: 1px dotted black;'>"+tekst+":00</div>");
    }
    // Renderiram prazne celije rasporeda
    for(var i = 0; i < 24; i++) {
      for(var j = 0; j < 7; j++) {
	    var l = j * ferkoKalendar.clientWidth / 7 + ferkoKalendar.offsetLeft;
	    var w = ferkoKalendar.dayColumnWidth + 4;
	    var t = (i*60)*ferkoKalendar.clientHeight / (24*60);
	    var h = ((i+1)*60)*ferkoKalendar.clientHeight / (24*60) - t;
	    t += ferkoKalendar.offsetTop;
	    h += ferkoKalendar.offsetTop -1;
	    var bgcol = "ffffff";
	    if(j==currentDayIndex) {
		    bgcol = "ffffaa";
	    }
        $("#ferkoCalendarBase").append("<div style='background-color: #"+bgcol+"; position: absolute; overflow: hidden; left: "+l+"px; top: "+t+"px; width: "+w+"px; height: "+h+"px; border: 1px solid #CCCCCC;'>&nbsp;</div>");
      }
    }
    var najranijiDogadaj = 24*60; // pretpostavimo da nema ranijeg dogadaja od ponoci navecer!
    var najkasnijiDogadaj = 0; // pretpostavimo da nema kasnijeg dogadaja od ponoci ujutro!
    
    var poDanima = [];
    for(var i = 0; i < 7; i++) {
	    poDanima[i] = [];
    }
    for(var i = 0; i < ferkoKalendar.dogadaji.length; i++) {
	    var e = ferkoKalendar.dogadaji[i];
	    poDanima[e.day].push(e);
	    var pocetakMin = e.shour*60 + e.smin;
	    if(pocetakMin < najranijiDogadaj) najranijiDogadaj = pocetakMin;
	    var krajMin = e.ehour*60 + e.emin;
	    if(krajMin > najkasnijiDogadaj) najkasnijiDogadaj = krajMin;
    }
    // Za slucaj da nema dogadaja pa sam ostao na ponoci, pomakni na 8h ujutro
    if(najranijiDogadaj==24*60) {
    	najranijiDogadaj = 8*60;
    	najkasnijiDogadaj = 24*60;
    }
    // Racunam potrebna zauzeca
    for(var j = 0; j < poDanima.length; j++) {
	    var dan = poDanima[j];
	    var gustocaVremena = [];
	    for(var i = 0; i < dan.length; i++) {
		    var e = dan[i];
		    var v = e.shour*60 + e.smin;
		    if(arrayIndexOf(gustocaVremena,v)==-1) gustocaVremena.push(v);
		    v = e.ehour*60 + e.emin;
		    if(arrayIndexOf(gustocaVremena,v)==-1) gustocaVremena.push(v);
	    }
	    gustocaVremena.sort(function(a,b){return a-b;});
	    var gustoca = new Array(gustocaVremena.length);
	    var dogadaji = new Array(gustocaVremena.length);
	    for(var i = 0; i < gustocaVremena.length; i++) {
		    gustoca[i] = 0;
		    dogadaji[i] = [];
	    }
	    for(var i = 0; i < dan.length; i++) {
		    var e = dan[i];
		    var st = e.shour*60 + e.smin;
		    var en = e.ehour*60 + e.emin;
	    	for(var k = 0; k < gustocaVremena.length; k++) {
		    	if(gustocaVremena[k]>=st && gustocaVremena[k]<en) gustoca[k] = gustoca[k]+1;
		    	if(gustocaVremena[k]==st) dogadaji[k].push(e);
	    	}
	    }
	    var lineIndex = 0;
	    while(lineIndex<gustoca.length) {
		    var lineStart = lineIndex;
		    var max = gustoca[lineIndex];
		    while(true) {
			    lineIndex++;
			    if(lineIndex>=gustoca.length) break;
			    if(gustoca[lineIndex]==0) break;
			    if(gustoca[lineIndex]>max) max = gustoca[lineIndex];
			}
			// Priprema matrice ispunjenih stupaca
			var delta = lineIndex - lineStart;
			var ispunjeno = new Array(delta);
			for(var i = 0; i < delta; i++) {
				ispunjeno[i] = new Array(max);
				for(var k = 0; k < max; k++) {
					ispunjeno[i][k] = 0;
				}
			}
			// Za svaki pocetak vremena:
			for(var line = lineStart; line < lineIndex; line++) {
				// Za svaki dogadaj koji tada pocinje
				for(var dogIndex = 0; dogIndex < dogadaji[line].length; dogIndex++) {
					var e = dogadaji[line][dogIndex];
				    var en = e.ehour*60 + e.emin;
					// Nadi gdje ga mozes zapoceti:
					var stupac = 0;
					for(stupac=0; stupac<max; stupac++) {
						if(ispunjeno[line-lineStart][stupac]==0) break;
					}
					// Sada znam u koji ga stupac stavljam; pa stavi ga
					ispunjeno[line-lineStart][stupac]=1;
					for(var k=line+1; k<lineIndex; k++) {
						if(gustocaVremena[k]<en) {
							ispunjeno[k-lineStart][stupac]=1;
						} else {
							break;
						}
					}
					// nacrtaj dogadaj
				    var l = e.day * ferkoKalendar.clientWidth / 7 + ferkoKalendar.offsetLeft + stupac*ferkoKalendar.dayColumnWidth/max;
				    var w = ferkoKalendar.dayColumnWidth / max - 5;
				    if(w<2) w=5;
				    var t = (e.shour*60+e.smin)*ferkoKalendar.clientHeight / (24*60);
				    var h = (e.ehour*60+e.emin)*ferkoKalendar.clientHeight / (24*60) - t;
				    t += ferkoKalendar.offsetTop;
				    h += ferkoKalendar.offsetTop;
				    var hdr = e.shour<10 ? "0"+e.shour : ""+e.shour;
				    hdr += ":";
				    hdr += e.smin<10 ? "0"+e.smin : ""+e.smin;
				    hdr += " - ";
				    hdr += e.ehour<10 ? "0"+e.ehour : ""+e.ehour;
				    hdr += ":";
				    hdr += e.emin<10 ? "0"+e.emin : ""+e.emin;
				    var hdr0 = hdr;
				    var aktivniDogadaj = 0;
					if(j==currentDayIndex) {
						var cds = e.shour*60+e.smin;
						var cde = e.ehour*60+e.emin;
						if(cds<=currentDateAbsTime && cde>currentDateAbsTime) aktivniDogadaj = 1;
					}
					if(aktivniDogadaj==0) {
				    	hdr = "<div style='background-color: #3333DD; font-weight: bold; font-size: 0.8em; color: white; height: 15px; overflow: hidden;'>"+hdr+"</div>";
					} else {
				    	hdr = "<div style='background-color: #3333DD; font-weight: bold; font-size: 0.8em; color: white; height: 15px; overflow: hidden;'>"+hdr+"<span style='color: red;'> -- u tijeku</span></div>";
					}
				    var ttl = e.title+(e.room==""?"":" ("+e.room+")");
				    var bgcol = "#AAAAFF";
				    if(e.context && e.context != "") {
				    	var dividerPos = e.context.indexOf(":");
				    	if(dividerPos>=0) {
				    		var evType = e.context.substring(0,dividerPos);
				    		if(evType=="sem") bgcol = "#CCCCCC";
				    		else if(evType=="a") bgcol = "#BB3333";
				    		else if(evType=="l") bgcol = "#AAAAFF";
				    		else if(evType=="c_LAB") bgcol = "#338833";
				    		else if(evType=="c_SEM") bgcol = "#999900";
				    		else if(evType=="c_ZAD") bgcol = "#008888";
				    	}
				    }
				    var elem = $("<div class='f_ev' style='cursor: hand; cursor: pointer; background-color: "+bgcol+"; color: white; position: absolute; overflow: hidden; left: "+l+"px; top: "+t+"px; width: "+w+"px; height: "+h+"px; border: 1px solid #0000FF; font-size: 0.8em;' title='"+ttl+"\r\n"+hdr0+"'>"+hdr+"<div>"+ttl+"</div>"+"</div>");
			        $("#ferkoCalendarBase").append(elem);
				    elem.bind("click", {eventID: e.eventID}, function(ev) {
			        	var noClosureEvent = ev.data.eventID;
					    activateEventLink(noClosureEvent);
					});
				}
			}
		}
    }

    var maksPomakPix = 1100-500;
    var vidljiveMinute = Math.floor(500.0/1100.0*24*60); // Ovoliko se vremena vidi odjednom na ekranu
    var maksPomakMin = 24*60-vidljiveMinute;
    var pomak = 0;
    if(najranijiDogadaj>=8*60 && najkasnijiDogadaj<=(8*60+vidljiveMinute)) {
    	najranijiDogadaj = 8*60;
    } else if(najkasnijiDogadaj-najranijiDogadaj>=vidljiveMinute) {
    	 // nista ne diraj
    } else {
    	var luftaGore = Math.floor((vidljiveMinute - (najkasnijiDogadaj-najranijiDogadaj))/2);
    	najranijiDogadaj -= luftaGore;
    }
    if(najranijiDogadaj>maksPomakMin) najranijiDogadaj=maksPomakMin;
	if(najranijiDogadaj<0) najranijiDogadaj = 0;
    pomak = Math.floor(najranijiDogadaj*maksPomakPix/maksPomakMin);
    $("#ferkoCalendarBaseWrapper").scrollTop(pomak);
    // $("#ferkoCalendarBaseWrapper").scrollTop(500/(1200-500)*500);
  } 
  function initFerkoCalendar(jsonurls){
    var fc = $("#ferkoCalendar");
    var pos = fc.position();
    ferkoKalendar = new FerkoCalendar(pos.left, pos.top, fc.innerWidth()-20, 1100, jsonurls);
    loadFerkoCalendar(jsonurls);
  }
  
  function obradiPodatkeKalendara(data) {
	    if(data.status=="ERR") {
		    alert("Dogodila se je pogreška pri inicijalizaciji kalendara. Tekst: "+data.message);
		    return;
		}
		ferkoKalendar.startDate = data.startDate;
		ferkoKalendar.endDate = data.endDate;
		ferkoKalendar.days = [];
		for(var ii = 0; ii < data.dateDesc.length; ii++) {
			ferkoKalendar.days[ii] = new FerkoDay(data.dateDesc[ii].i, data.dateDesc[ii].t, data.dateDesc[ii].d);
	    }
	    ferkoKalendar.dogadaji = [];
		for(var ii = 0; ii < data.calEvents.length; ii++) {
			var stH = parseInt(data.calEvents[ii].s.substr(11,2), 10);
			var stM = parseInt(data.calEvents[ii].s.substr(14,2), 10);
			var endAbs = 60*stH + stM + data.calEvents[ii].d;
			var enH = Math.floor(endAbs / 60);
			var enM = endAbs % 60;
			ferkoKalendar.dogadaji[ii] = new FerkoEvent(
			  data.dateMap[data.calEvents[ii].s.substr(0,10)],
			  stH, 
			  stM, 
			  enH, 
			  enM, 
			  data.calEvents[ii].t,
			  data.calEvents[ii].i,
			  data.calEvents[ii].r,
			  data.calEvents[ii].c);
	    }
	    brisi();
	    $("#ferkoCalendarNavig").empty();
	    $("#ferkoCalendarNavig").append("<span>Tjedan: "+data.startDate+" - "+data.endDate+" </span>");
	    var l3 = $("<span style=\"color: blue; cursor: hand; cursor: pointer;\"><u>Prethodni tjedan</u> </span>");
	    l3.bind("click", function(e) {
		    loadFerkoCalendarByIndex(data.startDate,data.endDate,3);
		});
	    $("#ferkoCalendarNavig").append(l3);
	    var l1 = $("<span style=\"color: blue; cursor: hand; cursor: pointer;\"><u>Trenutni tjedan</u> </span>");
	    l1.bind("click", function(e) {
		    loadFerkoCalendar();
		});
	    $("#ferkoCalendarNavig").append(l1);
	    var l2 = $("<span style=\"color: blue; cursor: hand; cursor: pointer;\"><u>Sljedeći tjedan</u> </span>");
	    l2.bind("click", function(e) {
		    loadFerkoCalendarByIndex(data.startDate,data.endDate,2);
		});
	    $("#ferkoCalendarNavig").append(l2);
	    renderFerkoCalendarEvents();
  }
  
  function loadFerkoCalendar(){
    var fc = $("#ferkoCalendar");
    var pos = fc.position();
    $.getJSON(antiCacheURL(ferkoKalendar.jsonurls[0]), function(data) {
		obradiPodatkeKalendara(data);
	});
  }

  function antiCacheURL(url) {
	  var d = new Date();
	  var mili = d.getTime();
	  mili = Math.floor(mili / (1000*60*30));
	  return url.replace(/__RTS__/g,""+mili);
  }
  
  function loadFerkoCalendarByIndex(startDate,endDate,index) {
    var fc = $("#ferkoCalendar");
    var pos = fc.position();
    var url = antiCacheURL(ferkoKalendar.jsonurls[index]);
    url = url.replace(/__SDF__/g,startDate);
    url = url.replace(/__SDT__/g,endDate);
    $.getJSON(url, function(data) {
		obradiPodatkeKalendara(data);
	});
  }

  function brisi() {
        $("#ferkoCalendarBase").find("div.f_ev").remove();
  }    
  function vrati() {
        renderFerkoCalendarEvents();
  }    

  function activateEventLink(eventID) {
	  var url = ferkoKalendar.jsonurls[4];
      url = url.replace(/__EID__/g,eventID);
	  document.location = url;
  }
