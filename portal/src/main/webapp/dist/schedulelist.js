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
 // DOM element where the Timeline will be attached
 var container = document.getElementById('visualization');

 // Create a DataSet (allows two way data-binding)
 var items = new vis.DataSet([]);

 // Configuration for the Timeline
 var options = {
     height: '200px',
     showCurrentTime: true
 };

 // Create a Timeline
 var timeline = new vis.Timeline(container, items, options);
 var today = new Date();
 today = today.getFullYear() + '-' + padLeft(today.getMonth() + 1, 2) + '-' + padLeft(today.getDate(), 2);
 timeline.setWindow(today + ' 00:00:00', today + ' 23:59:59', {
     animate: true
 });
 $("#project").click(function() {
     $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
 });
 $('.ui.dropdown')
     .dropdown();
 $('#pg').progress();
 $(".progress").progress();


 loadScheduleList();
 var timeoutId = setInterval(function() {
     loadScheduleList();
 }, 3000);

 function loadScheduleList() {

     $.ajax({
         url: './schedule/manage/list',
         type: "GET",
         beforeSend: function(request) {
             request.setRequestHeader("Authorization", $.cookie('token'));
         },
         dataType: 'json',
         success: function(JData) {
             if (JData["status"] != null) {
                 if (JData["status"] != "error") {
                     var tableHtml = "";
                     var tableList=[];
                     //GET LIST TO UPDATE TABLE
                     for (var i = 0; i < JData["list"].length; i++) {
                         if (!userFilter()) {
                             if (JData["list"][i]["user"] != $.cookie('username'))
                                 continue;
                         } else {
                             if (JData["list"][i]["user"] == $.cookie('username'))
                                 continue;
                         }
                         var tableRow=[];
                         tableHtml += '<tr>';
                         tableRow.push('<td>' + JData["list"][i]["schedule_id"] + '</td>');
                         tableRow.push('<td>' + JData["list"][i]["schedule_name"] + '</td>');
                         tableRow.push('<td>' + JData["list"][i]["user"] + '</td>');
                         tableRow.push('<td>' + ((JData["list"][i]["last_runtime"] != undefined) ? JData["list"][i]["last_runtime"] : "") + '</td>');
                         tableRow.push('<td>' + ((JData["list"][i]["runingtime"] != undefined) ? getTimeString(JData["list"][i]["runingtime"]) : "") + '</td>'); 
                         tableRow.push(getStatusHtml(JData["list"][i]["status"]));
                         tableRow.push(getFunction(JData["list"][i]["schedule_id"], JData["list"][i]["status"]));
                         tableList.push(tableRow);
                         tableHtml += tableRow.join("");                      
                         tableHtml += '</tr>';                         
                     }
                     if($(".table.schedule.list tbody").children("tr").length==tableList.length){
                        updateTableRow($(".table.schedule.list tbody"),tableList);
                     }else{
                        $(".table.schedule.list tbody").html(tableHtml);
                     }

                     $(".scheduleedit").unbind('click');
                     $(".scheduleedit").click(function() {
                         location.href = "schedulelist/edit#" + $(this).attr("sid");
                     });
                     $(".schedulerun").unbind('click');
                     $(".schedulerun").click(function() {
                         if ($(this).attr("sid") != "") {
                             if ($(this).attr("status") == "0")
                                 startSchedule($(this).attr("sid"));
                             else
                                 stopSchedule($(this).attr("sid"));
                         }

                     });

                     //GET TIME SET

                     if (items.length == 0) {
                         $("#pg").progress({
                             total: JData["list"].length
                         });
                         for (var i = 0; i < JData["list"].length; i++) {
                             if (JData["list"][i]["schedule_mode"] == "single") {
                                 var tList = getTimeList(JData["list"][i]["mod_set"]);
                                 setSingleView(tList, JData["list"][i]["schedule_name"]);
                             } else if (JData["list"][i]["schedule_mode"] == "interval") {
                                 setIntervalView(JData["list"][i]["startwith"], JData["list"][i]["every"], JData["list"][i]["unit"], JData["list"][i]["schedule_name"]);
                             } else if (JData["list"][i]["schedule_mode"] == "cycle") {
                                 var hour = Math.floor(parseInt(JData["time"]) / 60);
                                 var min = parseInt(JData["time"]) % 60;
                                 setTimeView(JData["list"][i]["startwith"], hour, min, padLeft(JData["list"][i]["each"].toString(), 7), JData["list"][i]["schedule_name"]);
                             }

                             $("#pg").progress('increment');

                         }
                     }
                     tableHtml=null;

                 } else {
                     if (JData["message"].checkPermission())
                         Messenger().post({
                             message: "[" + JData["status"] + "]\n" + JData["message"],
                             type: 'error',
                             showCloseButton: true
                         });

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
 function updateTableRow(table,dataRows){
    if($(table).children("tr").length==dataRows.length){
        $(table).children("tr").each(function(key,value){
        var mark='<span style="display: none; width: 0px; height: 0px;" id="transmark"></span>';
        var mark2='<span id="transmark" style="display: none; width: 0px; height: 0px;"></span>';

            if($(value).html().replace("&gt;",">").replace(mark,"").replace(mark2,"").replace(/&amp;/g, '&').hashCode()!=dataRows[key].join("").hashCode()){
               
                //UPDATE ROWS               
               
                var flag=0;
                
                $(value).children("td").each(function(i,v){                    
                    var tdContent=$.parseHTML(dataRows[key][i]);
                    tdContent=$(tdContent).html();                    
                    if(i==0&&$(v).html().replace(mark,"").replace(mark2,"")!=tdContent){                        
                        return false;
                    }  
                                  
                    if($(v).html().replace(mark,"").replace(mark2,"")!=tdContent){            
                        console.log("UPDATE CELL");
                        $(v).html(tdContent);
                    }
                    flag++;
                });

                if(flag==0){
                    console.log("UPDATE LINE");
                    $(value).html(dataRows[key].join(""));
                }
                    
            }
            mark=null;
            mark2=null;
        });
    }
}

 function showScheduleHistory(sid) {
     //get schedule history trigger
     $("#scheduleHistoryLoader").css("position","fixed");
     $("#scheduleHistoryLoader").show();

     $.ajax({
         url: './schedule/manage/run/history/range/' + sid,
         type: "GET",
         beforeSend: function(request) {
             request.setRequestHeader("Authorization", $.cookie('token'));
         },
         dataType: 'json',
         success: function(JData) {
             if (JData["status"] != null) {
                 if (JData["status"] != "error") {
                     //update in modal table 
                     var tableHtml = "";
                     for (var i = 0; i < JData["list"].length; i++) {

                         tableHtml += '<tr>';
                         tableHtml += '<td>' + JData["list"][i]["schedule_runid"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["schedule_id"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["schedule_name"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["user"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["start_time"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["runingtime"] + '</td>';
                         tableHtml += '<td>' + JData["list"][i]["stop_time"] + '</td>';
                         tableHtml += getRunJob(JData["list"][i]["runjob"], JData["list"][i]["runHistoryjob"]);
                         //tableHtml+=getScheduleRunJob(JData["list"][i]["runHistoryjob"]);
                         tableHtml += getViewScheduleLog(JData["list"][i]["schedule_runid"]);
                         tableHtml += '</tr>';
                     }
                     $(".ui.schedule.history .description").css("max-height", ($(window).height() - 180) + "px");
                     $(".ui.modal.schedule.history tbody").html(tableHtml);
                      $("#scheduleHistoryLoader").hide();
                     $(".ui.modal.schedule.history").modal("show");
                     //popup show job basic info and function
                     $('.ui.label.job').popup({
                         popup: $('.ui.popup'),
                         on: 'click',
                         onShow: function(hovered) {
                             var jid = $(hovered).attr("jid");
                             var jhid = $(hovered).attr("jhid");
                             if (jid != "") {
                                 //realtime get job basic info
                                 $.ajax({
                                     url: './job/manage/get/' + jid,
                                     type: "GET",
                                     beforeSend: function(request) {
                                         request.setRequestHeader("Authorization", $.cookie('token'));
                                     },
                                     dataType: 'json',
                                     success: function(Data) {
                                         if (Data["status"] != null) {
                                             if (Data["status"] != "error") {
                                                 //update popup info
                                                 $(".column.jobcard .header").html('<i class="icon suitcase"></i>' + Data["jobname"]);
                                                 $(".column.jobcard p").html(Data["memo"]);
                                                 //update popup button onclick event
                                                 $(".column.jobcard .button.view").attr("onclick", "doJobView(" + jid + ");");
                                                 $(".column.jobcard .button.edit").attr("onclick", "doJobEdit(" + jid + ");");
                                                 $(".column.jobcard .button.log").attr("onclick", "doJobViewLog(" + jhid + ");");
                                             } else {
                                                 if (JData["message"].checkPermission())
                                                     Messenger().post({
                                                         message: "[" + JData["status"] + "]\n" + JData["message"],
                                                         type: 'error',
                                                         showCloseButton: true
                                                     });

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
                         }
                     });
                    tableHtml=null;

                 } else {
                     if (JData["message"].checkPermission())
                         Messenger().post({
                             message: "[" + JData["status"] + "]\n" + JData["message"],
                             type: 'error',
                             showCloseButton: true
                         });
                         $("#scheduleHistoryLoader").hide();


                 }
             }

         },
         error: function(xhr, ajaxOptions, thrownError) {

             Messenger().post({
                 message: xhr.status + "-" + thrownError,
                 type: 'error',
                 showCloseButton: true
             });
             $("#scheduleHistoryLoader").hide();
         }
     });
 }
 /**
  * return run job label html
  * @param  {[type]} runjob    [description]
  * @param  {[type]} runjobHis [description]
  * @return {[type]}           [description]
  */
 function getRunJob(runjob, runjobHis) {
     var labelHtml = "<td>";
     if (runjobHis.length >= runjob.length) {
         for (var i = 0; i < runjob.length; i++) {
             //add lable
             labelHtml += '<a class="ui label job" jid="' + runjob[i] + '" jhid="' + runjobHis[i] + '"><i class="icon location arrow"></i>' + runjob[i] + '</a>';
         }
     }
     return labelHtml + '</td>';
 }

 function getScheduleRunJob(runjob) {
     var labelHtml = "<td>";
     for (var i = 0; i < runjob.length; i++) {
         labelHtml += '<label class="ui label red">' + runjob[i] + '</label>';
     }
     return labelHtml + '</td>';
 }
 /**
  * set Single time rule to timeline
  * @param {[type]} tList [description]
  * @param {[type]} sname [description]
  */
 function setSingleView(tList, sname) {
     if (tList.length != 0) {         
         for (var i = 0; i < tList.length; i++) {
             items.add([{
                 id: items.length + 1,
                 content: sname + "-" + tList[i].tab,
                 start: tList[i].runtime
             }]);
         }
     }
 }
 /**
  * set Interval time rule to timeline (max 100 item)
  * @param {[type]} startwith [description]
  * @param {[type]} every     [description]
  * @param {[type]} unit      [description]
  * @param {[type]} sname     [description]
  */
 function setIntervalView(startwith, every, unit, sname) {
     if (startwith != "" && every != "") {
         //var startTime=getStartTime(startwith);
         var timeString = startwith.replace(/-/ig, "/");
         var myDate = new Date(timeString);
         every = parseInt(every);
         unit = getUnitTime(unit);

         for (var i = 0; i < 20; i++) {
             var pTime = new Date(myDate.getTime() + (every * unit * i));
             var timePoint = pTime.getFullYear() + '-' + padLeft(pTime.getMonth() + 1, 2) + '-' + padLeft(pTime.getDate(), 2) + ' ' + padLeft(pTime.getHours(), 2) + ':' + padLeft(pTime.getMinutes(), 2) + ':' + padLeft(pTime.getSeconds(), 2);
             items.add([{
                 id: items.length + 1,
                 content: sname + "-Interval Cycle-" + i,
                 start: timePoint
             }]);
         }
     }
 }
 /**
  * Set Cycle Time rule to timeline(max 100 item)
  * @param {[type]} startwith [description]
  * @param {[type]} hour      [description]
  * @param {[type]} min       [description]
  * @param {[type]} week      [description]
  * @param {[type]} sname     [description]
  */
 function setTimeView(startwith, hour, min, week, sname) {
     var startTime = getStartTime(startwith);
     var weekArr = getWeekArray(week);
     var i = 0;
     var days = 0;
     while (i < 20) {
         var tempDate = new Date(startTime.date.getTime() + (86400 * 1000 * days));
         if (weekArr[tempDate.getDay()] == "1") {
             var timePoint = tempDate.getFullYear() + '-' + padLeft(tempDate.getMonth() + 1, 2) + '-' + padLeft(tempDate.getDate(), 2) + ' ' + padLeft(hour, 2) + ':' + padLeft(min, 2) + ':00';
             var startString = (startTime.started == true) ? "(...Previously Omission)Time Cycle" : "Time Cycle";
             var content = (i == 0) ? startString + "-Start" : "Time Cycle-" + i;
             items.add([{
                 id: items.length + 1,
                 content: sname + "-" + content,
                 start: timePoint
             }]);
             i++;
         }
         days++;
     }
 }
 /**
  * get Time tag Start Time(startwith<today? today:startwith)
  * @param  {[type]} startwith [description]
  * @return {[type]}           [description]
  */
 function getStartTime(startwith) {
     var timeString = startwith.replace(/-/ig, "/");
     var startDate = new Date(timeString);
     var toDay = new Date();
     if (toDay > startDate)
         return {
             started: true,
             date: toDay
         };
     else
         return {
             started: false,
             date: startDate
         };
 }
 /**
  * get unit sec
  * @param  {[type]} unit [description]
  * @return {[type]}      sec
  */
 function getUnitTime(unit) {
     if (unit == "hour") {
         return 60 * 60 * 1000;
     } else if (unit == "day") {
         return 24 * 60 * 60 * 1000;
     } else {
         return 60 * 1000;
     }
 }
 /**
  * Get time list 
  * @param  {[type]} arr [time array]
  * @return {[type]} time list [time list {runtime,tab}]
  */
 function getTimeList(arr) {
     var tList = [];
     for (var i = 0; i < arr.length; i++) {
         for (var k in arr[i]) {
             if (arr[i].hasOwnProperty(k)) {
                 tList.push({
                     runtime: k,
                     tab: arr[i][k]
                 });
             }
         }
     }
     return tList;
 }
 /**
  * Show Job History Info
  * @param  {[type]} jhid [job history id]
  * 
  */
 function showJobHistory(jhid) {
     $(".ui.modal.schedule.history").modal("hide");
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
                     $(".ui.modal.fullscreen.log .header").eq(0).html("Job Log-"+jhid);
                     $(".jobsql p").html($.base64Decode(JData["sql"]));
                     $(".jobname").html(JData["jobname"]);
                     $(".jobLevel label").html(JData["jobLevel"]);
                     $(".job_status").html(getStatus(JData["job_status"]));
                     $(".progress label").html(JData["progress"]);
                     $(".start_time label").html(JData["start_time"]);
                     $(".stop_time label").html(JData["stop_time"]);
                     $(".runingtimes label").html(JData["runingtime"]);
                     $(".user label").html(JData["user"]);
                     $(".storage label").html(JData["storage"]);
                     $(".save_type").html(getStorageLabelBySaveType(JData["save_type"]));
                     $(".location_id label").html(JData["location_name"]);
                     $(".log.job label").html(JData["log"]);
                     $(".presto_id").html(getPrestoButton(JData["presto_id"],JData["presto_url"]));
                     $(".ResultCount label").html(JData["ResultCount"]);
                     if (JData["ResultCount"] != "0" && JData["job_status"] == "1"&&JData["Valid"]==1)
                         $(".button.result").show();
                     else
                         $(".button.result").hide();
                     $(".button.result").click(function() {
                         if (JData["ResultCount"] != "0" && JData["job_status"] == "1") {

                             var newwin = window.open();
                             newwin.location = "./resultview#" + jhid;
                         }
                     });
                     $(".ui.modal.fullscreen.log").modal('show');
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
 * Do Start Schedule
 * @param  {[type]} sid [schedule id]
 * 
 */
 function startSchedule(sid) {
     $.ajax({
         url: './control/schedule/manage/start/' + sid,
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
  * Do Stop Schedule
  * @param  {[type]} sid [schedule id]
  * @return {[type]}     [description]
  */
 function stopSchedule(sid) {
     $.ajax({
         url: './control/schedule/manage/stop/' + sid,
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
  * User Filter
  * filter show self job or other user job
  * @return {[type]} [self->false;other->true]
  */
 function userFilter() {
     if (getUrlStatus() == "" || getUrlStatus() == "self") {
        //Save filter set 
         $.cookie('sindex', "self", {
             path: '/'
         });
         return false;
     } else if (getUrlStatus() == "other") {
        //Save filter set
         $.cookie('sindex', "other", {
             path: '/'
         });
         $(".item.schedule.self").removeClass("active");
         $(".item.schedule.other").addClass("active");
         return true;
     } else if (getUrlStatus() == "back") {
        //Load last filter set
         if ($.cookie('sindex') == "other") {
             location.href = "schedulelist#other";
             $(".item.schedule.self").removeClass("active");
             $(".item.schedule.other").addClass("active");
             return true;
         } else {
             location.href = "schedulelist#self";
             $(".item.schedule.self").addClass("active");
             $(".item.schedule.other").removeClass("active");
             return false;
         }
     } else {
        //Save filter set
         $.cookie('sindex', "self", {
             path: '/'
         });
         return false;
     }
 }
/**
 * Get Function Html
 * @param  {[type]} sid  [schedule id]
 * @param  {[type]} stat [status]
 * @return {[type]} html [html code]
 */
 function getFunction(sid, stat) {
     if (stat == "0")
         return '<td style=" border-left:0px "><button class=" ui icon button mini blue schedulerun" sid="' + sid + '" status="' + stat + '"><i class="icon play"></i>Start</button><button class=" ui icon button mini yellow scheduletest" sid="' + sid + '" onclick="showScheduleHistory(\'' + sid + '\')"><i class="icon rocket"></i>View History</button><a class=" ui icon button mini blue scheduleedit" sid="' + sid + '"><i class="icon edit"></i>Edit</a></td>';
     else
         return '<td style=" border-left:0px "><button class=" ui icon button mini red schedulerun" sid="' + sid + '" status="' + stat + '"><i class="icon stop"></i>Stop</button><button class=" ui icon button mini yellow scheduletest" sid="' + sid + '" onclick="showScheduleHistory(\'' + sid + '\')"><i class="icon rocket"></i>View History</button><a class=" ui icon button mini blue scheduleedit" sid="' + sid + '"><i class="icon edit"></i>Edit</a></td>';
 }
 /**
  * Get Status Html
  * @param  {[type]} stat [Status]
  * @return {[type]} html [html code]
  */
 function getStatusHtml(stat) {
     var status = "";
     var icon = ""
     if (stat == "0") {
         status = "Stop";
         icon = "stop";
     } else {
         status = "Start";
         icon = "checkmark ";
     }
     return '<td><div style="width:100%;text-alagn:center" class="ui label green"><i style="float:left" class="' + icon + ' icon"></i>' + status + '</div></td>';
 }
 /**
 * Get job history Status html
 * @param  {[type]} status [status 0->Run;1->Success;2->Fail]
 * 
 */
function getStatus(status) {
    if (status == "0") {
        return '<div style="text-align: center; " class="ui label orange"><i style="float:left" class="plane icon loading"></i> Run</div>';
    } else if (status == "1") {
        return '<div style="text-alagn:center" class="ui label green"><i style="float:left" class="checkmark icon"></i>Success</div>';
    } else {
        return '<div style="text-align:center" class="ui label red"><i style="float:left" class="fire icon"></i>Fail</div>';
    }
}
 /**
  * Load Schedule Log 
  * @param  {[type]} shid [schedule id]
  * 
  */
 function loadRuningLog(shid) {
     $.ajax({
         url: './control/schedule/log/' + shid,
         type: "GET",
         beforeSend: function(request) {
             request.setRequestHeader("Authorization", $.cookie('token'));
         },
         dataType: 'json',
         success: function(JData) {
             if (JData["status"] != "error") {
                 $(".ui.runing.log").html(JData["message"]);
                 $(".ui.schlog").modal("show");
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
  * Get View Schedule Log Html
  * @param  {[type]} shid [schedule id]
  * @return {[type]} html [html code]
  */
 function getViewScheduleLog(shid) {
     return '<td><a class="ui button" onclick="loadRuningLog(' + shid + ')">View</a></td>';
 }
 /**
  * Get Week Array
  * conv week string to array
  * ex:1011011->[1,0,1,1,0,1,1]
  * @param  {[type]} value [week string]
  * @return {[type]}       [week array]
  */
 function getWeekArray(value) {
     return padLeft(value, 7).split("");
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
 /**
  * Get Self Schedule 
  * 
  */
 function getSelf() {
     location.href = "schedulelist#self";
     $(".item.schedule.self").addClass("active");
     $(".item.schedule.other").removeClass("active");
     loadScheduleList();
 }
 /**
  * Get Other User Schedule
  * @return {[type]} [description]
  */
 function getOther() {
     location.href = "schedulelist#other";
     $(".item.schedule.self").removeClass("active");
     $(".item.schedule.other").addClass("active");
     loadScheduleList();
 }
 /**
  * Jump to Job Edit Page
  * @param  {[type]} jid [job id]
  * 
  */
 function doJobEdit(jid) {
     if (jid != "") {
         var newwin = window.open();
         newwin.location = "joblist/edit#" + jid;
     }
 }
 /**
  * Show Job Log
  * @param  {[type]} jhid [job history id]
  * 
  */
 function doJobViewLog(jhid) {
     if (jhid != "") {
         showJobHistory(jhid);
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
 * Get presto button 
 * @param  {str} prestoID  [presto id]
 * @param  {str} prestoURL [presto job url]
 * @return {str}           [html code]
 */
function getPrestoButton(prestoID,prestoURL){
    return '<a class="ui blue button" onclick="openPrestoWeb(\''+prestoURL+'\')"><i class="icon linkify"></i>'+prestoID+'</a>';
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
  * padLeft 
  * @param  {[type]} str []
  * @param  {[type]} len []
  * @return {[type]}     []
  */
 function padLeft(str, len) {
     str = '' + str;
     return str.length >= len ? str : new Array(len - str.length + 1).join("0") + str;
 }
