/**
 * Get String hash
 * @return {[num]} [hash]
 */
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

//function menu 
$("#project").click(function() {
    $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
});
//sematic ui init
$('.ui.dropdown').dropdown();
$('.ui.checkbox').checkbox();
$('#pg').progress();
$(".progress").progress();
//page set
var pageIndex=1;
//datetime picker
$('#startwith').daterangepicker({
    "format": "YYYY-MM-DD HH:mm:ss",
    "singleDatePicker": true,
    "showDropdowns": true,
    "timePickerIncrement": 1,
    "timePicker": true,
    "autoApply": true,
    "timePicker24Hour": true,
    "timePicker12Hour": false,
    "timePickerSeconds": true
});
//datetime picker
$('#endwith').daterangepicker({
    "format": "YYYY-MM-DD HH:mm:ss",
    "singleDatePicker": true,
    "showDropdowns": true,
    "timePickerIncrement": 1,
    "timePicker": true,
    "autoApply": true,
    "timePicker24Hour": true,
    "timePicker12Hour": false,
    "timePickerSeconds": true
});
//load url set job log
autoLoadJobLog();
//load Job History
loadJobHistory();
//timer to update Job History
var timeoutId = setInterval(function() {
    loadJobHistory();
}, 5000);
var historyHash = 0;
/**
 * load Job History and update page
 * 
 */
function loadJobHistory() {
    //if Filter is set
    var url = './job/manage/run/list/100';
    if ($(".button.filter").hasClass('red') && $("#startwith").val() != "" && $("#endwith").val() != "") {
        //Change range api
        url = './job/manage/run/history/range/' + $("#startwith").val() + '/' + $("#endwith").val() + '/';
    }
    $.ajax({
        url: url,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        timeout:5000,
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var runTableHtml = "";
                    var historyTableHtml = "";
                    var runArray = [];
                    var updateArray = [];
                    for (var i = 0; i < JData["list"].length; i++) {
                        if (($(".button.filter").hasClass('red') == true && $(".onlyme").checkbox('is checked') == true && JData["list"][i]["user"] == $.cookie("username")) || $(".button.filter").hasClass('red') == false || ($(".button.filter").hasClass('red') == true && $(".onlyme").checkbox('is checked') == false)) {
                            var tableTemp = "";
                            tableTemp += "<tr>";
                            tableTemp += "<td>" + JData["list"][i]["jobrunid"] + "</td>";
                            tableTemp += "<td>" + JData["list"][i]["jobid"] + "</td>";
                            tableTemp += "<td>" + JData["list"][i]["jobname"] + "</td>";
                            tableTemp += getType(JData["list"][i]["type"]);
                            tableTemp += "<td>" + JData["list"][i]["user"] + "</td>";

                            if (JData["list"][i]["job_status"] != 0)
                                tableTemp += getStatus(JData["list"][i]["job_status"]);

                            tableTemp += "<td>" + JData["list"][i]["start_time"] + "</td>";
                            tableTemp += "<td>" + getTimeString(JData["list"][i]["runingtime"]) + "</td>";
                            tableTemp += getProgress(JData["list"][i]["progress"], JData["list"][i]["job_status"]);

                            if (JData["list"][i]["job_status"] == 0) {
                                //Running Job
                                tableTemp += getFunction(JData["list"][i]["jobrunid"], JData["list"][i]["jobid"], false);
                                runTableHtml += tableTemp;
                                runArray.push(JData["list"][i]["jobrunid"].toString());
                                updateArray.push({
                                    jhid: JData["list"][i]["jobrunid"],
                                    time: getTimeString(JData["list"][i]["runingtime"]),
                                    progress: JData["list"][i]["progress"]
                                });
                            } else {
                                //History Job
                                if(inPage(i,pageIndex,50)){
                                    tableTemp += getFunction(JData["list"][i]["jobrunid"], JData["list"][i]["jobid"], true);
                                    historyTableHtml += tableTemp;
                                }
                            }
                        }
                    }
                    loadPageTag(pageIndex, getAllPage(JData["list"].length,50));
                    //if history change
                    if (historyHash != historyTableHtml.hashCode()) {
                        //update
                        $(".historylist tbody").html(historyTableHtml);
                        //$('.button.icon').popup({
                        //    inline: true                           
                        //});
                        historyHash = historyTableHtml.hashCode();
                    }
                    //if running item change
                    if (!checkRunning(runArray)) {
                        //update all running table                               
                        $(".runlist tbody").html(runTableHtml);
                        $('.button.icon').popup({
                            inline: true,                            
                        });
                    } else {
                        //only up date Progress
                        updateProgress(updateArray);
                    }

                    //Add view job history button click
                    $(".button.viewinfo").unbind("click");
                    $(".button.viewinfo").click(function() {
                        showJobHistory($(this).attr("jhid"));
                    });

                    //Add stop job  button click
                    $(".button.stop").unbind("click");
                    $(".button.stop").click(function() {
                        if ($(this).attr("jhid") != "") {
                            stopJob($(this).attr("jhid"));
                            $(this).addClass("disabled");
                        }
                    });

                    //Edit stop job  button click
                    $(".button.edit").unbind("click");
                    $(".button.edit").click(function() {
                        if ($(this).attr("jid") != "") {
                            var newwin = window.open();
                            newwin.location = "./joblist/edit#" + $(this).attr("jid");
                        }
                    });

                    runTableHtml = null;
                    historyTableHtml = null;
                    runArray = null;
                    updateArray = null;
                } else {
                    if (JData["message"].checkPermission())
                        Messenger().post({
                            message: "[" + JData["status"] + "]\n" + JData["message"],
                            type: 'error',
                            showCloseButton: true
                        });
                    //alert("["+JData["status"]+"]\n"+JData["message"]);
                }
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
}
/**
 * Show Job History on modal
 * @param  {[type]} jhid [job history id]
 * 
 */
function showJobHistory(jhid) {

    $.ajax({
        url: './job/manage/run/history/get/info/' + jhid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    $(".ui.modal.fullscreen .header").eq(0).html("Job Log-"+jhid);
                    $(".jobsql.message").html($.base64Decode(JData["sql"]).replace(/\n/g, "<br>"));
                    $(".jobname").html(JData["jobname"]);
                    $(".jobLevel label").html(JData["jobLevel"]);
                    $(".job_status").html(getStatus(JData["job_status"]).replace("<td>", "").replace("</td>", "").replace("width:100%;",""));
                    $(".progress label").html(JData["progress"]);
                    $(".start_time label").html(JData["start_time"]);
                    $(".stop_time label").html(JData["stop_time"]);
                    $(".runingtimes label").html(JData["runingtime"]);
                    $(".user label").html(JData["user"]);
                    $(".storage label").html(JData["storage"]);

                    var saveType=getStorageLabelBySaveType(JData["save_type"]);
                    $(".save_type").html(saveType);
                    if(saveType.indexOf("Database")>0)
                        $(".location_id label").html(JData["location_name"]);
                    $(".log label").html(JData["log"]);
                    $(".presto_id").html(getPrestoButton(JData["presto_id"],JData["presto_url"]));
                    $(".ResultCount label").html(JData["ResultCount"]);
                    $(".label.resultcount").html(JData["ResultCount"]+" Rows");
                    loadResultChart(JData["jobid"],jhid);
                    $('.ui.modal.fullscreen').modal({
                        onHidden: function() {
                            //clear url status
                            location.href = "status#";
                        }
                    });
                    $(".ui.modal.fullscreen").modal('show');

                    $(".button.result").unbind("click");
                    if (JData["ResultCount"] != "0" && JData["job_status"] == "1"&&JData["Valid"]==1){
                        $(".button.result").show();
                        $("#viewchart").show();
                    }else{
                        $(".button.result").hide();
                        $("#viewchart").hide();
                    }
                    $(".button.result").click(function() {
                        if (JData["ResultCount"] != "0" && JData["job_status"] == "1") {

                            var newwin = window.open();
                            newwin.location = "./resultview#" + jhid;
                        }
                    });

                    //add status to url
                    location.href = "status#"+jhid;
                    //load job runing log
                    loadRuningLog(jhid);
                } else {
                    if (JData["message"].checkPermission())
                        Messenger().post({
                            message: "[" + JData["status"] + "]\n" + JData["message"],
                            type: 'error',
                            showCloseButton: true
                        });
                    //alert("["+JData["status"]+"]\n"+JData["message"]);
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
            Messenger().post({
                message: xhr.status + "-" + thrownError,
                type: 'error',
                showCloseButton: true
            });
            //alert(xhr.status+"-"+thrownError);
        }
    });
}
function loadResultChart(jid,jhid){
    $("#viewchart").html("");
    $.ajax({
     url: 'chart/manage/list/' + jid,
     type: "GET",
     beforeSend: function(request) {
         request.setRequestHeader("Authorization", $.cookie('token'));
     },
     dataType: 'json',
     success: function(JData) {
        if(JData["status"]!=null){
            if(JData["status"]=="success"){
                var html='';
                for(var i=0;i<JData["ChartInfo"].length;i++){            
                    html+=getChartButton(JData["ChartInfo"][i]["Chart_Name"],JData["ChartInfo"][i]["ChartID"],JData["ChartInfo"][i]["Type"],jhid);
                }
                html+=getAddChartButton(jid,jhid);
                $("#viewchart").html(html);
                $("#viewchart .button.chart").unbind("click");
                $("#viewchart .button.chart").click(function(){
                    var newwin = window.open();
                    newwin.location = "./charts/draw?jhid="+$(this).attr("jhid")+"&chartid="+$(this).attr("chartid")+"&display_result=true";
                });
            }
        }


     },
     error: function(xhr, ajaxOptions, thrownError) {
         alert(xhr.status + "-" + thrownError);
     }
    }); 
}
function getChartButton(chartname,chartid,charttype,jhid){
    var icon="";
    var typeString="";
    if(charttype=="line"){
        icon="line chart";
        typeString="Line";
    }else if(charttype=="bar"){
        icon="bar chart";
        typeString="Bar";
    }else if(charttype=="pie"){
        icon="pie chart";
        typeString="Pie";
    }
    return '<div class="ui labeled  button chart" jhid="'+jhid+'" chartid="'+chartid+'" style="margin-top: 10px;"><div class="ui button"><i class="'+icon+' icon"></i>View '+typeString+' Chart</div><a class="ui basic label resultcount left pointing">'+chartname+'</a></div>';
}
function getAddChartButton(jid,jhid){
    return '<a class="ui labeled icon button addchart" href="./charts/builder?jid='+jid+'&jhid='+jhid+'" style="margin-top: 10px;"><i class="plus icon"></i>Add Chart</a>';
}
/**
 * load Running Log in modal
 * @param  {[type]} jhid [job history id]
 * 
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
            if (JData["status"] != "error")
                $(".ui.runing.log").html(JData["message"]);
            else
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
 * Do Stop Job 
 * @param  {[type]} jhid [job history id]
 * 
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
 * Get Type Html by typeid
 * @param  {num} type [0->QueryUI;1->Job;2->Schedule]
 * @return {str} html [html code]
 */
function getType(type) {
    var typeName = "";
    if (type == 0) {
        typeName = "QueryUI";
    } else if (type == 1) {
        typeName = "Job";
    } else {
        typeName = "Schedule";
    }
    return '<td><div style="width:100%;text-alagn:center" class="ui label green">' + typeName + '</div></td>';
}
/**
 * Get Progress Html
 * @param  {num} value  [progress value]
 * @param  {str} status [run status]
 * @return {str} html   [html code]
 */
function getProgress(value, status) {
    if (value == "100" && status == 1) {
        return '<td style="border-left:0px"><div data-percent="' + value + '" style="margin-bottom: 0px;" class="ui active progress violet success"><div class="bar" style="transition-duration: 300ms; width: ' + value + '%;"><div class="progress">' + value + '%</div></div></div></td>';
    } else {
        return '<td style="border-left:0px"><div data-percent="' + value + '" style="margin-bottom: 0px;" class="ui active progress violet"><div class="bar" style="transition-duration: 300ms; width: ' + value + '%;"><div class="progress">' + value + '%</div></div></div></td>';
    }
}
/**
 * Get Function Html
 * @param  {num } JHID    [job history id]
 * @param  {num } JID     [job id]
 * @param  {bool} disable [disable]
 * @return {str } html    [html code]
 */
function getFunction(JHID, JID, disable) {
    if (disable === false)
        return '<td style=" border-left:0px "><button class="circular ui icon button mini red stop" jhid="' + JHID + '" data-content="Kill Job" data-variation="tiny" data-content="Top Right"><i class="icon stop"></i></button><button class="circular ui icon button mini yellow viewinfo" jhid="' + JHID + '" data-variation="tiny" data-content="View Job Info" data-content="Top Left"><i class="icon search"></i></button><button class="circular ui icon button mini blue edit" jid="' + JID + '" jhid="' + JHID + '" data-content="Edit Job" data-variation="tiny" data-content="Top Right"><i class="icon edit"></i></button></td>';
    else
        return '<td style=" border-left:0px "><button class="circular ui icon button mini red stop disabled" jhid="' + JHID + '"><i class="icon stop"></i></button><button class="circular ui icon button mini yellow viewinfo" jhid="' + JHID + '" data-variation="tiny" data-content="View Job Info" data-content="Top Left"><i class="icon search"></i></button><button class="circular ui icon button mini blue edit" jid="' + JID + '" jhid="' + JHID + '" data-content="Edit Job" data-variation="tiny" data-content="Top Right"><i class="icon edit"></i></button></td>';

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
/**
 * Show History Filter Modal
 */
function HistoryFilter() {
    $(".ui.modal.basic").modal('show');
}
/**
 * Set Filter and hide modal
 */
function setFilter() {
    if ($("startwith").val() != "" && $("endwith").val() != "") {
        $(".ui.modal.basic").modal('hide');
        $(".button.filter").removeClass("primary ");
        $(".button.filter").addClass("red");
        $('.button.filter').delay(500).transition('tada');
    } else {
        alert("select start or end time");
    }

}
/**
 * Clear Filter set
 * 
 */
function clearFilter() {
    $(".ui.modal.basic").modal('hide');
    $(".button.filter").addClass("primary ");
    $(".button.filter").removeClass("red");
}
/**
 * Check Running has update?
 * @param  {[type]} runArray [run array]
 * @return {[type]} update   [has update?]
 */
function checkRunning(runArray) {
    var count = 0;
    var allcount = 0;
    //Scan run table
    $(".runlist tbody tr").each(function() {        
        allcount++;
        if ($.inArray($(this).children("td:eq(0)").html(), runArray) != -1)
            count++;
    });
    if (count != runArray.length || runArray.length == 0 || allcount != runArray.length) {
        return false;
    } else {
        return true;
    }
}
/**
 * Update Progress
 * @param  {[type]} updateArray []
 * 
 */
function updateProgress(updateArray) {
    for (var i = 0; i < updateArray.length; i++) {
        $(".runlist tbody tr").each(function() {
            var run = $(this);
            $.grep(updateArray, function(e) {
                if (e.jhid == run.children("td:eq(0)").html()) {
                    run.children("td:eq(6)").html(e.time);
                    if (run.children("td:eq(7)").children(".progress").attr("data-percent") != e.progress)
                        run.children("td:eq(7)").children(".progress").progress({
                            percent: e.progress
                        });
                }
            });
        });
    }
}


/**
 * Get Status html
 * @param  {[type]} status [status 0->Run;1->Success;2->Fail]
 * 
 */
function getStatus(status) {
    if (status == "0") {
        return '<td><div style="width:100%;text-align: center; " class="ui label orange"><i style="float:left" class="plane icon loading"></i> Run</div></td>';
    } else if (status == "1") {
        return '<td><div style="width:100%;text-alagn:center" class="ui label green"><i style="float:left" class="checkmark icon"></i>Success</div></td>';
    } else {
        return '<td><div style="width:100%;text-align:center" class="ui label red"><i style="float:left" class="fire icon"></i>Fail</div></td>';
    }
}
/**
 * get SaveType html
 * @param  {str} type [type id 1->hdfs;2->database;null->not save]
 * @return {str}      [html code]
 */
function getSaveType(type){
    if(type!=""&&type!=null){
        if(type=="1"){
            return '<div style="text-align: center; " class="ui label green"><i style="float:left" class=" icon disk outline"></i> HDFS</div>';
        }else{
            return '<div style="text-align: center; " class="ui label green"><i style="float:left" class=" icon database "></i> Database</div>';
        }
    }else{
        return '<div style="text-align: center; " class="ui label"><i style="float:left" class="icon minus"></i> Not Store</div>';
    }
}
/**
 * Get presto button 
 * @param  {str} prestoID  [presto id]
 * @param  {str} prestoURL [presto job url]
 * @return {str}           [html code]
 */
function getPrestoButton(prestoID,prestoURL){
    return '<a class="ui orange button" onclick="openPrestoWeb(\''+prestoURL+'\')"><i class="icon linkify"></i>'+prestoID+'</a>';
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
    if($(".menu.pagenav").html().hashCode()!=pageTag.hashCode())
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
    loadJobHistory();
}

/**
 * Open Presto job page in new tab
 * @param  {str} prestoURL [presto job url]
 */
function openPrestoWeb(prestoURL){
    var newwin = window.open();
    newwin.location = prestoURL;
}
/**
 * In page load check job-history need to show?
 */
function autoLoadJobLog(){
    if($.isNumeric(getUrlStatus())){
        showJobHistory(getUrlStatus());
    }
}
/**
* Get Url Status
* @return {[type]} [url hash ]
*/
 function getUrlStatus() {
     var hash = location.hash;
     hash = hash.replace(/#/, "");
     return hash;
 }
/* Left add 0 */
function padLeft(str, len) {
    str = '' + str;
    return str.length >= len ? str : new Array(len - str.length + 1).join("0") + str;
}

function getStorageLabelBySaveType(save_type){

    var stype=parseInt(save_type);
    var html="";
    if((stype/4)>=1){
       html+='<div style="text-align: center; " class="ui label green"><i style="float:left" class=" icon database "></i> Database</div>';
    }
    if(((stype%4)/2)>=1){
        html+='<div style="text-align: center; " class="ui label green"><i style="float:left" class=" icon disk outline"></i> Local</div>';   
    }
    if((((stype%4)%2)/1)>=1){
        html+='<div style="text-align: center; " class="ui label green"><i style="float:left" class=" icon disk outline"></i> HDFS</div>';
    }
    return html;
}
