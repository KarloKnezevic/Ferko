/**
*    Sa: http://plugins.jquery.com/project/json_autocomplete
*    Json key/value autocomplete for jQuery 
*    Provides a transparent way to have key/value autocomplete
*    Copyright (C) 2008 Ziadin Givan www.CodeAssembly.com  
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU Lesser General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU Lesser General Public License
*    along with this program.  If not, see http://www.gnu.org/licenses/
*    
*    Examples 
*	 $("input#example").autocomplete("autocomplete.php");//using default parameters
*	 $("input#example").autocomplete("autocomplete.php",{minChars:3,timeout:3000,validSelection:false,parameters:{'myparam':'myvalue'},before : function(input,text) {},after : function(input,text) {}});
*    minChars = Minimum characters the input must have for the ajax request to be made
*	 timeOut = Number of miliseconds passed after user entered text to make the ajax request   
*    validSelection = If set to true then will invalidate (set to empty) the value field if the text is not selected (or modified) from the list of items.
*    parameters = Custom parameters to be passed
*    after, before = a function that will be caled before/after the ajax request
*/
jQuery.fn.autocomplete = function(url, settings)
{
	return this.each( function()//do it for each matched element
	{
		//this is the original input
		var textInput = $(this);
		//create a new hidden input that will be used for holding the return value when posting the form, then swap names with the original input
		textInput.after('<input type=hidden name="' + textInput.attr("name") + '"/>').attr("name", textInput.attr("name") + "_text");
		var valueInput = $(this).next();
		//create the ul that will hold the text and values
		valueInput.after('<ul class="autocomplete"></ul>');
		var list = valueInput.next().css({top: textInput.offset().top + textInput.outerHeight(), left: textInput.offset().left, width: textInput.width()});
		var oldText = '';
		var typingTimeout;
		var size = 0;
		var selected = 0;

		settings = jQuery.extend(//provide default settings
		{
			minChars : 1,
			timeout: 1000,
			after : null,
			before : null,
			validSelection : true,
			parameters : {}
		} , settings);

		if(settings.extraParams) {
			jQuery.each(settings.extraParams, function(key, param) {
				settings.parameters[key] = typeof param == "function" ? param() : param;
			});
		}
		
		function getData(text)
		{
			window.clearInterval(typingTimeout);
			// alert("Pozvan s "+text);
			if (text != oldText && (settings.minChars != null && text.length >= settings.minChars))
			{
				clear();
				if (settings.before == "function") 
				{
					settings.before(textInput,text);
				}
				textInput.addClass('autocomplete-loading');
				settings.parameters.q = text;
				$.getJSON(url,settings.parameters,function(data)
				{
					var items = '';
					if (data)
					{
						size = data.length;
						for (i = 0; i < data.length; i++)//iterate over all options
						{
						  for ( key in data[i] )//get key => value
						  {	
								items += '<li value3="' + key + '">' + data[i][key].replace(new RegExp("(" + text + ")","i"),"<strong>$1</strong>") + '</li>';
						  }
						  list.html(items);
						  //on mouse hover over elements set selected class and on click set the selected value and close list
						  list.show().children().
						  hover(function() { $(this).addClass("selected").siblings().removeClass("selected");}, function() { $(this).removeClass("selected") } ).
						  click(function () { valueInput.val( $(this).attr('value3') );textInput.val( $(this).text() ); clear(); });
						}
						if (settings.after == "function") 
						{
							settings.after(textInput,text);
						}
					}
					textInput.removeClass('autocomplete-loading');
				});
				oldText = text;
			}
		}
		
		function clear()
		{
			list.hide();
			size = 0;
			selected = 0;
		}	
		
		textInput.keydown(function(e) 
		{
			window.clearInterval(typingTimeout);
			if(e.which == 27)//escape
			{
				clear();
			} else if (e.which == 46 || e.which == 8)//delete and backspace
			{
				clear();
				//invalidate previous selection
				if (settings.validSelection) valueInput.val('');
			}
			else if(e.which == 13)//enter 
			{ 
				if ( list.css("display") == "none")//if the list is not visible then make a new request, otherwise hide the list
				{ 
					getData(textInput.val());
				} else
				{
					clear();
				}
				e.preventDefault();
				return false;
			}
			else if(e.which == 40 || e.which == 9 || e.which == 38)//move up, down 
			{
			  e.preventDefault();
			  switch(e.which) 
			  {
				case 40: 
				case 9:
				  selected = selected >= size - 1 ? 0 : selected + 1; break;
				case 38:
				  selected = selected <= 0 ? size - 1 : selected - 1; break;
				default: break;
			  }
			  //set selected item and input values
			  textInput.val( list.children().removeClass('selected').eq(selected).addClass('selected').text() );	        
			  valueInput.val( list.children().eq(selected).attr('value3') );
			  return false;
			} else 
			{ 
				//invalidate previous selection
				if (settings.validSelection) valueInput.val('');
				typingTimeout = window.setTimeout(function() { getData(textInput.val()) },settings.timeout);
			}
		});
	});
};
