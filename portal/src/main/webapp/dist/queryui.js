String.prototype.hashCode = function() {
        var hash = 0;
        if (this.length == 0) return hash;
        for (i = 0; i < this.length; i++) {
            char = this.charCodeAt(i);
            hash = ((hash << 5) - hash) + char;
            hash = hash & hash; // Convert to 32bit integer
        }
        return hash;
    }
//message
Messenger.options = {
    extraClasses: 'messenger-fixed messenger-on-bottom messenger-on-right',
    theme: 'air'
};
// trigger extension
var autoCom_table = [];
var langTools = ace.require("ace/ext/language_tools");

var editor = ace.edit("editor");
editor.session.setMode("ace/mode/sql");
editor.setTheme("ace/theme/crimson_editor");
// enable autocompletion and snippets
editor.setOptions({
    enableBasicAutocompletion: true,
    enableSnippets: true,
    enableLiveAutocompletion: true
});
document.getElementById('editor').style.fontSize = '18px';
//set page 
var pageIndex=1;
//set line number
var lineNumber=15;
loadLineNumber();
//load Task Bar Hide Set
loadTaskBar();
/**
 * load page info
 */
//load in Cookie SQL
fromJob();
//load table list
loadTable();
//load query history and status
loadQueryStatus();
//load table name to Auto COM keyword lsit
loadTableAutoCom();
//set timer every 2s to update
var timeoutId = setInterval(function() {
    loadQueryStatus();
}, 2000);


//ui init
$("#project").click(function() {
    $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
});
$('.ui.dropdown').dropdown();
$('#pg').progress();
$(".progress").progress();

/**
 * load presto table
 * @return 
 */
function loadTable() {
    ///presto/table/list
    $.ajax({
        url: './presto/table/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var dropdownHtml = "";
                    for (var i = 0; i < JData["list"].length; i++) {
                        dropdownHtml += '<div class="item" data-value="' + JData["list"][i]["tablename"] + '">' + JData["list"][i]["tablename"] + '</div>';
                    }
                    $(".prestotable .menu").html(dropdownHtml);
                    $(".prestotable input").val("");
                    dropdownHtml=null;

                } else {
                    if (JData["message"].checkPermission())
                        alert("[" + JData["status"] + "]\n" + JData["message"]);
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.status + "-" + thrownError);
        }
    });
}
/**
 * show schema to table and menu
 * @return {[type]} [description]
 */
function showSchema() {
    if ($(".prestotable input").val() != "") {
        $(".ui.modal.schema .header").html("Table-Schema:" + $(".prestotable input").val());
        $.ajax({
            url: './presto/table/schemas/' + $(".prestotable input").val(),
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        var tableHtml = "";
                        var menuHtml = '';
                        for (var i = 0; i < JData["column"].length; i++) {
                            var color = "#FFFFFF";
                            if (JData["column"][i]["partition_key"] == true) {
                                color = "orange";
                            }
                            menuHtml += '<a class="item schema" si="' + i + '" stype="' + JData["column"][i]["type"] + '" partition="' + JData["column"][i]["partition_key"] + '" style="color:' + color + ';"><b>' + JData["column"][i]["column"] + '</b></a>';
                            tableHtml += '<tr><td>' + (i+1) + '</td>';
                            tableHtml += '<td>' + JData["column"][i]["column"] + '</td>';
                            tableHtml += '<td>' + JData["column"][i]["type"] + '</td>';
                            tableHtml += '<td>' + JData["column"][i]["partition_key"] + '</td>';
                            tableHtml += '<td><div class="ui animated button insert" tabindex="0"><div class="visible content"><i class="share icon"></i></div><div class="hidden content">Insert</div></div></td></tr>';
                        }
                        //Set html to table and menu
                        $(".table.schema tbody").html(tableHtml);
                        $(".ui.menu.schema").html(menuHtml);
                        //add click event
                        $("#tableSchema").click(function() {
                            $('.ui.modal.schema').modal('show');
                        });
                        $("#tableSampleData").click(function() {
                            loadSampleData();
                        });
                        $(".button.insert").click(function() {
                            insertText(" " + $(this).parent().parent().children("td").eq(1).html());
                        });
                        $('.item.schema').click(function() {
                            insertText(" " + $(this).children("b").eq(0).html());
                        });

                        //add schema popup
                        $('.item.schema').popup({
                            position: 'right center',
                            title: 'aaa',
                            content: 'My favorite dog would like other dogs as much as themselves',
                            onShow: function(hovered) {
                                $(this).find('.header').html($(hovered).html());
                                var popupHtml = '<div class="ui label blue "><i class="cube icon"></i>' + $(hovered).attr("stype") + '</div>';
                                if ($(hovered).attr("partition") == "true")
                                    popupHtml += '<div class="ui label red "><i class="disk outline icon"></i>Partition</div>';
                                $(this).find('.content').html(popupHtml);
                            }
                        });
                        tableHtml = null;
                        menuHtml = null;

                    } else {
                        if (JData["message"].checkPermission())
                            alert("[" + JData["status"] + "]\n" + JData["message"]);
                    }
                }
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.status + "-" + thrownError);
            }
        });

    } else {
        alert("No table Select!");
    }
}

/**
 * load table sample table limit 100 row
 * @return no
 */
function loadSampleData() {
    if ($(".prestotable input").val() != "") {
        //get sample data
        $(".ui.modal.sample thead").html("");
        $(".ui.modal.sample tbody").html('<div class="ui active inline loader"></div>'); 
        $(".ui.modal.sample").modal('show');
        $("#sampleDataLoader").show();
        $.ajax({
            url: './presto/table/sample/' + $(".prestotable input").val() + '/100',
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        var tableheadHtml = "<tr>";
                        var tablebodyHtml = '';
                        //get json array
                        for (var i = 0; i < JData["list"].length; i++) {
                            tablebodyHtml += "<tr>";
                            $.each(JData["list"][i], function(key, value) {
                                if (i == 0) {
                                    //get header
                                    tableheadHtml += "<th>" + key + "</th>";
                                }
                                //get table row
                                tablebodyHtml += "<td>" + value + "</td>";
                            });
                            tablebodyHtml += "</tr>";
                        }
                        $(".ui.modal.sample thead").html(tableheadHtml);
                        $(".ui.modal.sample tbody").html(tablebodyHtml);  
                        $("#sampleDataLoader").hide();                      
                        tableheadHtml = null;
                        tablebodyHtml = null;
                    } else {
                        if (JData["message"].checkPermission())
                            alert("[" + JData["status"] + "]\n" + JData["message"]);
                        $("#sampleDataLoader").hide();
                    }
                }
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.status + "-" + thrownError);
                $("#sampleDataLoader").hide();
            }
        });

    } else {
        alert("No table Select!");
    }
}
/**
 * load partition 
 * @return {[type]} [description]
 */
function loadPartition() {
    if ($(".prestotable input").val() != "") {
        //get sample data
        $.ajax({
            url: './presto/table/partitions/list/' + $(".prestotable input").val(),
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        var dropdownHtml = '';
                        //get json array
                        for (var i = 0; i < JData["list"].length; i++) {

                            var valueTemp = "";
                            var textTemp = "";
                            $.each(JData["list"][i], function(key, value) {
                                valueTemp += " " + key + "='" + value + "' AND";
                                textTemp += key + "=" + value + ",";
                            });
                            textTemp = textTemp.substring(0, textTemp.length - 1);
                            valueTemp = valueTemp.substring(0, valueTemp.length - 3);
                            dropdownHtml += '<div class="item" data-value="' + valueTemp + '">' + textTemp + '</div>';
                        }
                        $(".partition .menu").html(dropdownHtml);
                        $(".partition input").val("");
                        dropdownHtml=null;
                    } else {
                        if (JData["message"].checkPermission())
                            alert("[" + JData["status"] + "]\n" + JData["message"]);
                    }
                }
            },
            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.status + "-" + thrownError);
            }
        });

    } else {
        alert("No table Select!");
    }
}
/**
 * insert Text to ace editer
 * @param  {string} text [insert text]
 * @return 
 */
function insertText(text) {
    var session = editor.session
    session.insert({
        row: session.getLength(),
        column: 0
    }, text)
}


/**
 * load Presto table name to Auto COM Keyword
 * @return 
 */
function loadTableAutoCom() {
    var jsonUrl = "./presto/table/list";
    var rhymeCompleter = {
        getCompletions: function(editor, session, pos, prefix, callback) {
            if (prefix.length === 0) {
                callback(null, []);
                return
            }
            $.getJSON(jsonUrl, function(wordList) {
                callback(null, wordList.list.map(function(ea) {
                    return {
                        name: ea.tablename,
                        value: ea.tablename,
                        meta: "Table"
                    }
                }));
            })
        }
    }

    langTools.addCompleter(rhymeCompleter);
    rhymeCompleter=null;
    jsonUrl=null;
}
/**
 * load table schema to AutoComplete Keyword LIST
 * @param  {string} table [table name]
 * @return 
 */
function loadTableSchemaAutoCom(table) {
    if (table != "" && $.inArray(table, autoCom_table) == -1) {
        var jsonUrls = "./presto/table/schemas/" + table;
        var rhymeCompleters = {
            getCompletions: function(editor, session, pos, prefix, callback) {
                if (prefix.length === 0) {
                    callback(null, []);
                    return
                }
                $.getJSON(jsonUrls, function(wordLists) {
                    callback(null, wordLists.column.map(function(ea) {
                        return {
                            name: ea.column,
                            value: ea.column,
                            meta: ea.type
                        }
                    }));
                });

            }
        }
        autoCom_table.push(table);
        langTools.addCompleter(rhymeCompleters);
        jsonUrls=null;
        rhymeCompleters=null;
    }
}
/**
 * load presto query history and status
 * @return 
 */
function loadQueryStatus() {
    $.ajax({
        url: './query/run/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        timeout:5000,
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var tableHtml = [];
                    for (var i = 0; i < JData["list"].length; i++) {
                        if(inPage(i,pageIndex,10)){
                            
                        var htmlTmp="";
                        //htmlTmp += "<tr>";
                        htmlTmp +="<td>" + JData["list"][i]["jobrunid"] + "</td>";
                        if (isBase64(JData["list"][i]["sql"]))
                            htmlTmp +='<td style="word-break: break-all;">' + $.base64Decode(JData["list"][i]["sql"]) + "</td>";
                        else
                            htmlTmp +='<td>' + JData["list"][i]["sql"] + "</td>";
                        var valid='1';
                        if (JData["list"][i]["valid"]!= null)
                            valid=JData["list"][i]["valid"];
                        htmlTmp +="<td>" + JData["list"][i]["user"] + "</td>";
                        htmlTmp +="<td>" + JData["list"][i]["start_time"] + "</td>";
                        htmlTmp +="<td>" + getTimeString(JData["list"][i]["runingtime"]) + "</td>";
                        htmlTmp +=getStatusAnimated(JData["list"][i]["job_status"], JData["list"][i]["jobrunid"]);
                        htmlTmp +=getProgress(JData["list"][i]["progress"], JData["list"][i]["job_status"]);
                        htmlTmp +=getFunction(JData["list"][i]["jobrunid"], JData["list"][i]["sql"], JData["list"][i]["job_status"],valid);
                        //htmlTmp += "</tr>";
                        tableHtml.push(htmlTmp);
                        htmlTmp=null;
                        }else if(i>pageIndex*10)
                            break;                        
                    }
                    loadPageTag(pageIndex, getAllPage(JData["list"].length,10));
                    if($('.ui.celled.table.query tbody tr').length>=10){
                        updateTableRow($(".ui.celled.table.query tbody"),tableHtml,10);
                    }else{
                        $(".ui.celled.table.query tbody").html(convArrayToHtmlString(tableHtml));
                    }
                    $(".button.query.stop").unbind("click");
                    $(".button.query.stop").click(function() {
                        if ($(this).attr("runid") != "") {
                            stopJob($(this).attr("runid"));
                            $(this).addClass("disabled");
                        }
                    });
                    $(".button.result").unbind("click");
                    $(".button.result").click(function() {
                        if ($(this).attr("runid") != "") {
                            var newwin = window.open();
                            newwin.location = "./resultview#" + $(this).attr("runid");
                        }

                    });
                    tableHtml=null;

                } else {
                    if (JData["message"].checkPermission())
                        Messenger().post({
                            message: "[" + JData["status"] + "]\n" + JData["message"],
                            type: "error",
                            showCloseButton: true
                        });
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
            Messenger().post({
                message: xhr.status + "-" + thrownError,
                type: xhr.status + "-" + thrownError,
                showCloseButton: true
            });
        }
    });
}
function updateTableRow(table,dataRows,count){
    if($(table).children("tr").length==dataRows.length&&dataRows.length==count){
        $(table).children("tr").each(function(key,value){
        var mark='<span style="display: none; width: 0px; height: 0px;" id="transmark"></span>';
        var mark2='<span id="transmark" style="display: none; width: 0px; height: 0px;"></span>';
            if($(value).html().replace("&gt;",">").replace(mark,"").replace(mark2,"").hashCode()!=dataRows[key].hashCode()){
                
                //UPDATE ROWS                
                cell=$.parseHTML(dataRows[key]);
                var flag=0;
                
                $(value).children("td").each(function(i,v){
                    if(i==0&&$(v).html().replace(mark,"").replace(mark2,"")!=$(cell).eq(i).html()){                        
                        return false;
                    }                
                    if($(v).html().replace(mark,"").replace(mark2,"")!=$(cell).eq(i).html()){
                        console.log($(v).html().replace(mark,"").replace(mark2,"")+"->"+$(cell).eq(i).html());
                        console.log("UPDATE CELL");
                        $(v).html($(cell).eq(i).html());
                    }
                    flag++;
                });

                if(flag==0){
                    console.log("UPDATE LINE");
                    $(value).html(dataRows[key]);
                }
                    
            }
            mark=null;
            mark2=null;
        });
    }
}
function convArrayToHtmlString(listArr){
    tmpHtml="";
    for (var i = 0; i < listArr.length; i++) {
           tmpHtml+="<tr>"+listArr[i]+"</tr>";
    }
    return tmpHtml;
}
/**
 * Send SQL Query to Presto 
 * @return 
 */
function runPrestoSQL() {
    $(".ui.dimmer.sql").dimmer('show');
    if (editor.getValue() != "") {

        var addJson = {};
        addJson["sql"] = $.base64Encode(editor.getValue());
        addJson["jobLevel"] = "public";
        addJson["type"] = "query";
        //POST to Add Query
        $.ajax({
            url: './query/submit/',
            data: JSON.stringify(addJson),
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            type: "POST",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] == "success") {

                        Messenger().post({
                            message: "SQL Submit Success",
                            type: 'info',
                            showCloseButton: true
                        });
                    } else {
                        if (JData["message"].checkPermission())
                            Messenger().post({
                                message: "SQL Submit ERROR" + "[" + JData["status"] + "]\n" + JData["message"],
                                type: 'error',
                                showCloseButton: true
                            });

                    }
                    $(".ui.dimmer.sql").dimmer('hide');
                }
            },

            error: function(xhr, ajaxOptions, thrownError) {
                Messenger().post({
                    message: xhr.status + "-" + thrownError,
                    type: 'error',
                    showCloseButton: true
                });
            }
        });
        editor.setValue("");

    } else {
        $(".ui.dimmer.sql").dimmer('hide');
        Messenger().post({
            message: "SQL can not be empty",
            type: 'error',
            showCloseButton: true
        });
    }
}
/**
 * Edit SQL Query History(copy sql to ace editor)
 * @param  {String} sql [SQL]
 * @return 
 */
function editSQL(sql) {
    if (isBase64(sql)){
        sqlDecode=$.base64Decode(sql);
        if(sqlDecode.indexOf("CREATE TABLE presto_temp")==0){            
            editor.setValue(sqlDecode.substring(sqlDecode.indexOf("WITH (format='ORC' ) AS")+23,sqlDecode.length));
        }else{
            editor.setValue(sqlDecode);
        }        
    }else
        editor.setValue(sql);
}
/**
 * Push Query SQL to add Job
 * save input sql in cookie,open new tab to job add page 
 * @return 
 */
function pushToSaveJob() {
    if (editor.getValue() != "") {
        $.removeCookie('sql');
        $.cookie('sql', $.base64Encode(editor.getValue()), {
            path: '/'
        });
        var newwin = window.open();
        newwin.location = "./joblist/add#fromQueryUI";
    } else {
        Messenger().post({
            message: "SQL can not be empty",
            type: 'error',
            showCloseButton: true
        });
    }
}
/**
 * Stop job
 * @param  {String} jhid [job run id]
 * @return 
 */
function stopJob(jhid) {
    $.ajax({
        url: './query/kill/' + jhid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {

                } else {
                    if (JData["message"].checkPermission())
                        Messenger().post({
                            message: "[" + JData["status"] + "]\n" + JData["message"],
                            type: "error",
                            showCloseButton: true
                        });
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
            Messenger().post({
                message: xhr.status + "-" + thrownError,
                type: xhr.status + "-" + thrownError,
                showCloseButton: true
            });
        }
    });
}
/**
 * load and show query log
 * @param  {string} jhid [job run id]
 * @return 
 */
function loadRuningLog(jhid) {
    $.ajax({
        url: './control/job/log/' + jhid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != "error") {
                $(".ui.runing.log").html(JData["message"]);
                $(".ui.modal.log").modal("show");
            } else
                $(".ui.runing.log").html("no log");
        },
        error: function(xhr, ajaxOptions, thrownError) {
            Messenger().post({
                message: xhr.status + "-" + thrownError,
                type: 'error',
                showCloseButton: true
            });
        }
    });
}
/**
 * Get sql from job page
 * get sql from cookie,show in editor
 * @return 
 */
function fromJob() {
    if (getUrlStatus() == "fromJob") {
        editor.setValue($.base64Decode($.cookie("sql")));
    }
}
/**
 * get url status
 * get status from url hash
 * @return 
 */
function getUrlStatus() {
    var hash = location.hash;
    hash = hash.replace(/#/, "");
    return hash;
}
/**
 * get query status Html
 * combination query status html
 * @param  {string} status [job status 0:run 1:success 2:fail]
 * @param  {string} jhid   [job run id]
 * @return {string} status html
 */
function getStatus(status, jhid) {
    if (status == "0") {
        return '<td><a onclick="loadRuningLog(' + jhid + ')" style="width:100%;text-align: center; " class="ui label orange"><i style="float:left" class="plane icon loading"></i> Run</a></td>';
    } else if (status == "1") {
        return '<td><a onclick="loadRuningLog(' + jhid + ')" style="width:100%;text-alagn:center" class="ui label green"><i style="float:left" class="checkmark icon"></i>Success</a></td>';
    } else {
        return '<td><a onclick="loadRuningLog(' + jhid + ')" style="width:100%;text-align:center" class="ui label red"><i style="float:left" class="fire icon"></i>Fail</div></a>';
    }
}
function getStatusAnimated(status, jhid) {
    if (status == "0") {
        return '<td><div class="ui vertical animated button orange tiny" tabindex="0" style="width:100%;text-alagn:center;float:left" onclick="loadRuningLog('+jhid+')"> <div class="hidden content">View Log</div> <div class="visible content"><i class="checkmark icon" style="float:left"></i>Run</div></div></td>';
    } else if (status == "1") {
        return '<td><div class="ui vertical animated button green tiny" tabindex="0" style="width:100%;text-alagn:center;float:left" onclick="loadRuningLog('+jhid+')"> <div class="hidden content">View Log</div> <div class="visible content"><i class="checkmark icon" style="float:left"></i>Success</div></div></td>';
    } else {
        return '<td><div class="ui vertical animated button red tiny" tabindex="0" style="width:100%;text-alagn:center;float:left" onclick="loadRuningLog('+jhid+')"> <div class="hidden content">View Log</div> <div class="visible content"><i class="checkmark icon" style="float:left"></i>Fail</div></div></td>';
    }
}
/**
 * Get Progress Html
 * combination query progress html
 * @param  {string} value  [progress value 0~100]
 * @param  {string} status [job status 0:run 1:success 2:fail]
 * @return {string} progress bar  html
 */
function getProgress(value, status) {
    if (value == "100" && status == 1) {
        return '<td style="border-left:0px"><div data-percent="' + value + '" style="margin-bottom: 0px;" class="ui active progress violet success"><div class="bar" style="transition-duration: 300ms; width: ' + value + '%;"><div class="progress">' + value + '%</div></div></div></td>';
    } else {
        return '<td style="border-left:0px"><div data-percent="' + value + '" style="margin-bottom: 0px;" class="ui active progress violet"><div class="bar" style="transition-duration: 300ms; width: ' + value + '%;"><div class="progress">' + value + '%</div></div></div></td>';
    }
}
/**
 * Get function button html
 * @param  {string} runid  [job run id]
 * @param  {string} sql    [job sql]
 * @param  {string} status [job status 0:run 1:success 2:fail]
 * @return {string} function html
 */
function getFunction(runid, sql, status,valid) {
    var disabled = "disabled";
    var notdisabled = "disabled";
    if (status == "0")
        disabled = "";
    else if (status == "1"){
        if(valid=='1')
            notdisabled = "";
    }       
    return '<td style=" border-left:0px "><button runid="' + runid + '" class="circular ui icon button mini red query stop ' + disabled + '"><i class="icon stop"></i></button><button runid="' + runid + '" class="result circular ui icon button mini yellow ' + notdisabled + ' query viewer"><i class="icon search"></i></button><button runid="' + runid + '" class="circular ui icon button mini blue query edit" onclick="editSQL(\'' + sql + '\');"><i class="icon edit"></i></button></td>';
}
/**
 * is Base64 ?
 * @param  {string}  code [Base64]
 * @return {Boolean} isBase64
 */
function isBase64(code) {
    var base64Match = new RegExp("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$");
    if (base64Match.test(code))
        return true;
    else
        return false;
}
/**
 * Conv time(sec) to hum string
 * ex:86400s->1d0h0m0s
 * @param  {string} sec [sec]
 * @return {string} time string
 */
function getTimeString(sec) {
    if (sec != "") {
        var secNum = parseInt(sec);
        if (secNum >= 86400) {
            var day = parseInt(secNum / 86400);
            var hour = parseInt((secNum % 86400) / 3600);
            var min = parseInt(((secNum % 86400) % 3600) / 60);
            var secs = ((secNum % 86400) % 3600) % 60;
            return day + "d" + hour + "h" + min + "m" + secs + "s";
        } else if (secNum >= 3600) {
            var hour = parseInt(secNum / 3600);
            var min = parseInt((secNum % 3600) / 60);
            var secs = (secNum % 3600) % 60;
            return hour + "h" + min + "m" + secs + "s";
        } else if (secNum >= 60) {
            var min = parseInt(secNum / 60);
            var secs = secNum % 60;
            return min + "m" + secs + "s";
        } else {
            return secNum + "s";
        }
    } else {
        return "0s";
    }
}
function loadPageTag(nowPage,allPage){
    var previous=(nowPage==1)?1:nowPage-1;
    var next=(nowPage==allPage)?nowPage:nowPage+1;
    var pageTag='<a onclick="toPage('+previous+')" class="icon item"><i class="left chevron icon"></i></a>';
    for (var i = 1; i <= parseInt(allPage); i++) {
            if(parseInt(nowPage)==i)
                pageTag+='<a class="item active" onclick="toPage('+i+')">'+i+'</a>';
            else
                pageTag+='<a class="item" onclick="toPage('+i+')">'+i+'</a>';
    }
    pageTag+='<a onclick="toPage('+next+')" class="icon item"><i class="right chevron icon"></i></a>';
    if($(".menu.pagenav").html()!=pageTag)
        $(".menu.pagenav").html(pageTag);
}
function getAllPage(itemCount,eachPageCount){
    return Math.ceil(parseInt(itemCount)/parseInt(eachPageCount));
}
function inPage(index,nowPage,eachPageCount){
    var start=(nowPage-1)*eachPageCount;
    var end=nowPage*eachPageCount;
    if(index>=start&&index<end)
        return true;
    else
        return false;
}
function toPage(index){
    pageIndex=index;  
    loadQueryStatus();  
}
function loadLineNumber(){
    if ($.cookie('qline') != "null" && $.cookie('qline') != "undefined" && $.cookie('qline') != null) {
        lineNumber=$.cookie('qline');
    }
    $(".input.line.number input").val(lineNumber);
    changeLineNumber();
}
function changeLineNumber(){
    editor.setOptions({
        minLines:lineNumber,
        maxLines:lineNumber
    });
}
function addLine(){
    lineNumber++;
    $(".input.line.number input").val(lineNumber);
    $.cookie('qline', lineNumber, {
            path: '/'
        });
    changeLineNumber();
}
function decLine(){
    if(lineNumber>10){
        lineNumber--;
        $(".input.line.number input").val(lineNumber);
        $.cookie('qline', lineNumber, {
            path: '/'
        });
        changeLineNumber();
    }
}
function showLineNumberSetting(){
    $(".ui.dimmer.height").dimmer('show');
}
function hideOrShowTaskBar(){
    if($(".toc").is(":visible")){
        //$(".toc").hide();
        $('.toc').transition('fade right', '150ms');
        $(".sqlhead").css("width",700);
        $.cookie('queryui_taskbar','Off', {
            path: '/'
        });
    }else{
        //$(".toc").show();
        $('.toc').transition('fade right');
        $(".sqlhead").css("width",500);
        $.cookie('queryui_taskbar','On', {
            path: '/'
        });
    }
}
function loadTaskBar(){
    if($.cookie('queryui_taskbar')=='Off'){
        $(".toc").hide();
        $(".sqlhead").css("width",700);
    }
}
//fix dropdown menu z-index 
//and sel index change call showSchema 
$(".dropdown.prestotable").dropdown({
    onShow: function() {
        $(".ace_gutter").css("z-index", "0");
        $("#editor").css("z-index", "0");
    },
    onHide: function() {
        $(".ace_gutter").css("z-index", "4");
        $("#editor").css("z-index", "10");
    },
    onChange: function(value, text) {
        if (value != "") {
            $(".dropdown.partition").dropdown('clear');
            $('#tableCount').attr("tablename", value);
            showSchema();
            loadPartition();
            loadTableSchemaAutoCom(value);
        }
    }
});
$(".dropdown.partition").dropdown({
    onShow: function() {
        $(".ace_gutter").css("z-index", "0");
        $("#editor").css("z-index", "0");
    },
    onHide: function() {
        $(".ace_gutter").css("z-index", "4");
        $("#editor").css("z-index", "10");
    },
    onChange: function(value, text) {
        if (value != "") {
            insertText(value);
        }
    }
});
//show table count
$('#tableCount').popup({
    popup: $('.ui.popup.count'),
    on: 'click',
    onShow: function(hovered) {
        $(".ui.active.inverted.dimmer").show();
        var tname = $(hovered).attr("tablename");

        if (tname != "") {
            $(".column.countcard .header").html(tname);
            //realtime get job basic info
            $.ajax({
                url: './presto/table/count/' + tname,
                type: "GET",
                beforeSend: function(request) {
                    request.setRequestHeader("Authorization", $.cookie('token'));
                },
                dataType: 'json',
                success: function(Data) {
                    if (Data["status"] != null) {
                        if (Data["status"] != "error") {
                            //update popup info
                            $(".column.countcard label").html(Data["count"]);
                            $(".ui.active.inverted.dimmer").hide();
                        } else {
                            if (JData["message"].checkPermission())
                                alert("[" + JData["status"] + "]\n" + JData["message"]);
                        }
                    }
                },
                error: function(xhr, ajaxOptions, thrownError) {
                    alert(xhr.status + "-" + thrownError);
                }
            });
        } else {
            $(".column.countcard .header").html("No Table");
            $(".ui.active.inverted.dimmer").hide();
        }
    }
});
