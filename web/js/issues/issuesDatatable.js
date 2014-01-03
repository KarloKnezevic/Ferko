	
	
	YAHOO.util.Event.addListener(window, "load", function() {
	    YAHOO.example.XHR_JSON = function() {

			var course = document.getElementById('courseInstanceID').value;
			var archive = document.getElementById('archiveFlag').value;
			// Create a shortcut 
			var Dom = YAHOO.util.Dom;

			YAHOO.widget.DataTable.formatLink = function(elCell, oRecord, oColumn, oData) {
                var title = oData;
                var msgID = oRecord.getData("msgid");
                elCell.innerHTML = "<a href='/ferko/ViewIssue.action?courseInstanceID="+course+"&issueID=" + msgID + "'>" + title + "</a>";
            };

	        var myColumnDefs = [
	            {key:"title", label:"Naslov", sortable:true, formatter:YAHOO.widget.DataTable.formatLink},
	            {key:"topic", label:"Tema"},
	            {key:"owner", label:"Pitanje postavio"},
	            {key:"creationDate", label:"Postavljeno", sortable:true},
	            {key:"lastModificationDate", label:"Zadnja izmjena", sortable:true},
	            {key:"msgid", hidden:true},
	            {key:"status", label:"Status"},
	            {key:"public", label:"Javno pitanje"},
	            {key:"colorIndication", hidden:true}
	        ];

			var myRowFormatter = function(elTr, oRecord) {
    			if (oRecord.getData('colorIndication') == 'true') 
    			{
        			Dom.addClass(elTr, 'mark');
    			}
    			return true;
			}; 

	        var myDataSource = new YAHOO.util.DataSource("/ferko/IssueListJSON.action?courseInstanceID="+course+"&data.archive="+archive);
	        myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
	        myDataSource.connXhrMode = "queueRequests";
	        myDataSource.responseSchema = {
	            resultsList: "ResultSet.Result",
	            fields: ["title","topic","msgid","owner","creationDate", "lastModificationDate", "status", "public", "colorIndication"]
	        };
	
	        var myDataTable = new YAHOO.widget.DataTable("json", myColumnDefs, myDataSource, {formatRow: myRowFormatter});
	
	        var mySuccessHandler = function() {
	            this.set("sortedBy", null);
	            this.onDataReturnAppendRows.apply(this,arguments);
	        };
	        var myFailureHandler = function() {
	            this.showTableMessage(YAHOO.widget.DataTable.MSG_ERROR, YAHOO.widget.DataTable.CLASS_ERROR);
	            this.onDataReturnAppendRows.apply(this,arguments);
	        };
	        var callbackObj = {
	            success : mySuccessHandler,
	            failure : myFailureHandler,
	            scope : myDataTable
	        };
	
	        return {
	            oDS: myDataSource,
	            oDT: myDataTable
	        };
	    }();
	});
