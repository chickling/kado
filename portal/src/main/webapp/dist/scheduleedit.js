//Save time and job set
var timeList = [];
var jobList = [];
var allJobList = [];

//load time cycle
genTimeCycle();
/**
 * gen Time Cycle 
 * Set Time to dropdown list
 */
function genTimeCycle() {
    var itemHtml = "";
    for (var i = 0; i < 60; i++) {
        itemHtml += '<div class="item">' + padLeft(i, 2) + '</div>';
        if (i == 23)
            $("#timeCycleHour .menu").html(itemHtml);
    }
    $("#timeCycleMinute .menu").html(itemHtml);
    $('.ui.dropdown').dropdown();
}

//function btn click
$("#project").click(function() {
    $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
});
//sematic ui init
$('.ui.accordion').accordion();
$('.ui.dropdown').dropdown();
$('.ui.checkbox').checkbox();
$('#pg').progress();
$('#eachTime').dropdown('clear');
$('#eachTime .default.text').html("Each ...");
var onchangeFlag = 0;
//load week dropdown
loadDropdown();

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
//datetime picker init
$('#reservation').daterangepicker({
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
$('#startwithcycle').daterangepicker({
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
//time line event
var vv;
timeline.on('doubleClick', function(properties) {
    vv = properties;
    alert('selected items: ' + properties.item);
});

//Set time line view range today
var today = new Date();
today = today.getFullYear() + '-' + padLeft(today.getMonth() + 1, 2) + '-' + padLeft(today.getDate(), 2);
timeline.setWindow(today + ' 00:00:00', today + ' 23:59:59', {
    animate: true
});
/**
 * Add single time Item to timeline
 */
function addItem() {
    timeList.push({
        runtime: $('#reservation').val(),
        tab: $('#labelname').val()
    });
    items.add([{
        id: items.length + 1,
        content: $('#labelname').val(),
        start: $('#reservation').val()
    }]);
    //updateView();
    var timeString = $('#reservation').val().replace(/-/ig, "/");
    var myDate = new Date(timeString);
    var startTime = new Date(myDate.getTime() - (5 * 60 * 1000));
    var stopTime = new Date(myDate.getTime() + (5 * 60 * 1000));
    var start = startTime.getFullYear() + '-' + padLeft(startTime.getMonth() + 1, 2) + '-' + padLeft(startTime.getDate(), 2) + ' ' + padLeft(startTime.getHours(), 2) + ':' + padLeft(startTime.getMinutes(), 2) + ':' + padLeft(startTime.getSeconds(), 2);
    var stop = stopTime.getFullYear() + '-' + padLeft(stopTime.getMonth() + 1, 2) + '-' + padLeft(stopTime.getDate(), 2) + ' ' + padLeft(stopTime.getHours(), 2) + ':' + padLeft(stopTime.getMinutes(), 2) + ':' + padLeft(stopTime.getSeconds(), 2);
    timeline.setWindow(start, stop, {
        animate: true
    });
}
//Delete schedule click
$(".schedule.delete").click(function() {
    deleteSchedule($(this).attr("sid"));
});
/**
 * Clear Error alert
 * 
 */
function clearError() {

    $(".error").each(function() {
        $(this).removeClass("error");
    });
}
/**
 * Check input info 
 * 
 */
function checkInputInfo() {
    clearError();
    //check storage
    var timemode = $('[name="timemode"]:checked').val();
    var message = "";
    if (timemode == 0) {        
        if (timeList.length == 0) {
            $(".input.icon.stime").addClass("error");
            $(".input.icon.tags").addClass("error");
            message += "Please Add time tag \n";
        }
    } else if (timemode == 1) {
        if ($(".intervaltime.startwith input").val() == "") {
            message += "Start With can not be empty \n";
            $(".intervaltime.startwith").addClass("error");
        }
        if ($(".intervaltime.every input").val() == "") {
            message += "Every can not be empty \n";
            $(".intervaltime.every").addClass("error");
        }
    } else if (timemode == 2) {
        if ($(".cycletime.startwith input").val() == "") {
            message += "Start With can not be empty \n";
            $(".cycletime.startwith").addClass("error");
        }
        if ($(".cycletime.each").dropdown('get value') == "") {
            message += "Each can not be empty \n";
            $(".cycletime.each").addClass("error");
        }
    }
    //check Job Visibility Level
    if ($(".dropdown.level input").val() == "") {
        $(".dropdown.level").addClass("error");
        message += "Please select Schedule Visibility Level \n";
    }

    //check Job Name
    if ($('[name="schedule_name"]').val() == "") {
        $('[name="schedule_name"]').parent().addClass("error");
        message += "Schedule name Name can not be empty \n";
    }

    //check Job sel
    if (jobList.length == 0) {
        message += "Job Progress can not be empty \n";
    }

    //SHOW MESSAGE
    if (message != "") {
        alert(message);
        return false;
    } else {
        return true;
    }
}
//load Radio Status
loadRadioStatus();
$('input:radio').change(function() {
    loadRadioStatus();
});
/**
 * Set radio sel status
 * 
 */
function loadRadioStatus() {
    var storage = $('[name="timemode"]:checked').val();
    if (storage == 0) {
        $(".singletime").removeClass("disabled");
        $(".intervaltime").addClass("disabled");
        $(".cycletime").addClass("disabled");
    } else if (storage == 1) {
        $(".singletime").addClass("disabled");
        $(".intervaltime").removeClass("disabled");
        $(".cycletime").addClass("disabled");
    } else {
        $(".singletime").addClass("disabled");
        $(".intervaltime").addClass("disabled");
        $(".cycletime").removeClass("disabled");
    }
}
//load all job list
loadJobList();
/**
 * load job list to dropdown 
 * @return {[type]} [description]
 */
function loadJobList() {
    $.ajax({
        url: '../job/manage/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var selfItemHtml = "";
                    var otherItemHtml = "";
                    for (var i = 0; i < JData["list"].length; i++) {
                        if (JData["list"][i]["user"] == $.cookie("username")) {
                            selfItemHtml += '<div class="item" data-value="' + JData["list"][i]["jobid"] + '"><div class="ui red empty circular label"></div>' + JData["list"][i]["jobname"] + '</div>';
                        } else {
                            otherItemHtml += '<div class="item" data-value="' + JData["list"][i]["jobid"] + '"><div class="ui blue empty circular label"></div>' + JData["list"][i]["jobname"] + '</div>';
                        }
                        allJobList.push({
                            jid: JData["list"][i]["jobid"].toString(),
                            name: JData["list"][i]["jobname"]
                        });
                    }
                    $(".menu.self").html(selfItemHtml);
                    $(".menu.other").html(otherItemHtml);
                    loadScheduleInfo();



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
 * Add job to schedule job list
 */
function addJobtoList() {
    //alert($(".dropdown.icon.joblist").dropdown('get value'));

    if (parseInt($(".dropdown.icon.joblist").dropdown('get value')) > 0) {
        var jobid = $(".dropdown.icon.joblist").dropdown('get value');
        var jobname = $(".dropdown.icon.joblist").dropdown('get text');
        jobList.push({
            jid: jobid.toString(),
            name: jobname
        });
        showJobProcess();
    } else {
        alert("job not select");
    }
}
/**
 * Show job process in view
 * 
 */
function showJobProcess() {
    var endHtml = '<div class="completed  active step"><div class="content "><div class="title">End of Step  </div><div class="description"></div></div></div>';
    var stepHtml = "";
    for (var i = 0; i < jobList.length; i++) {
        stepHtml += '<div class="step"><div class="content"><div class="title">' + jobList[i].name + '</div><div class="description"><a class="ui button orange mini jobdel" pindex="' + i + '"><i class="icon remove"></i> Delete</a></div></div></div>';
    }
    $(".ui.steps").html(stepHtml + endHtml);
    $(".jobdel").click(function() {
        jobList.splice(parseInt($(this).attr("pindex")), 1);
        showJobProcess();
    });
}
/**
 * Save Schedule
 * @return {[type]} [description]
 */
function saveSchedule(sid) {
    if (checkInputInfo()) {
        // $('#editor_form').dimmer('show');

        var addJson = {};
        var message = "";
        // Add job basic info
        addJson["schedule_name"] = $('[name="schedule_name"]').val();
        addJson["schedule_level"] = $(".dropdown.level input").val();
        addJson["memo"] = $('[name="memo"]').val();
        addJson["notification"] = getNotification($('[name="notification"]').parent().checkbox('is checked').toString());
        var timemode = $('[name="timemode"]:checked').val();
        var message = "";
        if (timemode == 0) {
            addJson["schedule_mode"] = "single";
            addJson["starttwith"] = "";
            addJson["every"] = "";
            addJson["unit"] = "";
            addJson["mod_set"] = timeList;

            if (timeList.length == 0) {
                $(".input.icon.stime").addClass("error");
                $(".input.icon.tags").addClass("error");
                message += "Please Add time tag \n";
            }
        } else if (timemode == 1) {
            addJson["schedule_mode"] = "interval";
            addJson["starttwith"] = $(".intervaltime.startwith input").val();
            addJson["every"] = $(".intervaltime.every input").val();
            addJson["unit"] = $(".intervaltime.every .unit").dropdown('get text');

        } else if (timemode == 2) {
            addJson["schedule_mode"] = "cycle";
            addJson["starttwith"] = $(".cycletime.startwith input").val();
            addJson["every"] = "";
            addJson["unit"] = "";
            var hour = parseInt($(".cycletime .timehour").dropdown("get text"));
            var min = parseInt($(".cycletime .timemin").dropdown("get text"));
            addJson["time"] = ((hour * 60) + min).toString();
            addJson["each"] = parseWeek($(".cycletime.each").dropdown('get value'));

        }
        addJson["runjob"] = getRunJob();
        //POST to Add User
        $.ajax({
            url: '../schedule/manage/update/' + sid,
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
                        location.href = "../schedulelist#back";
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

        //alert(JSON.stringify(addJson));

    }
}
/**
 * Load Schedule info and show in page
 * 
 */
function loadScheduleInfo() {
    if (getUrlStatus() != "" && getUrlStatus() != "undefined") {
        $.ajax({
            url: '../schedule/manage/get/' + getUrlStatus(),
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        // Add job basic info
                        $('[name="schedule_name"]').val(JData["schedule_name"]);
                        $(".dropdown.level").dropdown('set value', JData["schedule_level"].toString());
                        $(".dropdown.level").dropdown();
                        $('[name="memo"]').val(JData["memo"]);
                        if (JData["notification"] == "0") {
                            $('[name="notification"]').parent().checkbox('uncheck');
                        } else {
                            $('[name="notification"]').parent().checkbox('check');
                        }

                        var timemode = JData["schedule_mode"];
                        $('[name="timemode"]:eq(' + getModeIndex(timemode) + ')').parent().checkbox('check');

                        if (timemode == "single") {

                            getTimeList(JData["mod_set"]);
                            setSingleView();

                        } else if (timemode == "interval") {

                            $(".intervaltime.startwith input").val(JData["startwith"]);
                            $(".intervaltime.every input").val(JData["every"]);
                            $(".intervaltime.every .unit").dropdown('set value', JData["unit"]);
                            $(".intervaltime.every .unit").dropdown();
                            setIntervalView(JData["startwith"], JData["every"], $(".intervaltime.every .unit").dropdown('get value'));

                        } else if (timemode == "cycle") {

                            $(".cycletime.startwith input").val(JData["startwith"]);

                            var hour = Math.floor(parseInt(JData["time"]) / 60);
                            var min = parseInt(JData["time"]) % 60;
                            $(".cycletime .timehour").dropdown("set text", hour);
                            $(".cycletime .timemin").dropdown("set text", min);
                            setWeek(JData["each"].toString());
                            //JData["each"]=parseWeek($(".cycletime.each").dropdown('get value'));
                            setTimeView(JData["startwith"], hour, min, padLeft(JData["each"].toString(), 7));
                        }
                        loadJob(JData["runjob"]);
                        // JData["runjob"]=getRunJob();
                        //  //set save button jobID
                        $(".button.icon.save").attr("onclick", "saveSchedule(" + getUrlStatus() + ")");
                        //  //set delete jid
                        $(".button.schedule.delete").attr("sid", getUrlStatus());

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
        alert("lose url hash");
    }
}
/**
 * Delete schedule
 * @param  {[type]} sid [schedule id]
 * 
 */
function deleteSchedule(sid) {
    if (confirm('Are you sure you want to delete Schedule?')) {
        $.ajax({
            url: '../schedule/manage/delete/' + sid,
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        location.href = "../schedulelist#back"
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
}
/**
 * Set Single view timeline
 */
function setSingleView() {
    if (timeList.length != 0) {
        var timeString = "";
        for (var i = 0; i < timeList.length; i++) {
            timeString = $(".intervaltime.startwith input").val().replace(/-/ig, "/");
            items.add([{
                id: items.length + 1,
                content: timeList[i].tab,
                start: timeList[i].runtime
            }]);
        }
        if (timeString != "") {
            var myDate = new Date(timeString);
            var startTime = new Date(myDate.getTime() - (12 * 60 * 60 * 1000));
            var stopTime = new Date(myDate.getTime() + (12 * 60 * 60 * 1000));
            var start = startTime.getFullYear() + '-' + padLeft(startTime.getMonth() + 1, 2) + '-' + padLeft(startTime.getDate(), 2) + ' ' + padLeft(startTime.getHours(), 2) + ':' + padLeft(startTime.getMinutes(), 2) + ':' + padLeft(startTime.getSeconds(), 2);
            var stop = stopTime.getFullYear() + '-' + padLeft(stopTime.getMonth() + 1, 2) + '-' + padLeft(stopTime.getDate(), 2) + ' ' + padLeft(stopTime.getHours(), 2) + ':' + padLeft(stopTime.getMinutes(), 2) + ':' + padLeft(stopTime.getSeconds(), 2);
            timeline.setWindow(start, stop, {
                animate: true
            });
        }
    }
}
/**
 * Set Interval view timeline
 * @param {time} startwith []
 * @param {num} every     []
 * @param {str} unit      []
 */
function setIntervalView(startwith, every, unit) {
    if (startwith != "" && every != "") {
        var timeString = startwith.replace(/-/ig, "/");
        var myDate = new Date(timeString);
        every = parseInt(every);
        unit = getUnitTime(unit);

        for (var i = 0; i < 100; i++) {
            var pTime = new Date(myDate.getTime() + (every * unit * i));
            var timePoint = pTime.getFullYear() + '-' + padLeft(pTime.getMonth() + 1, 2) + '-' + padLeft(pTime.getDate(), 2) + ' ' + padLeft(pTime.getHours(), 2) + ':' + padLeft(pTime.getMinutes(), 2) + ':' + padLeft(pTime.getSeconds(), 2);
            items.add([{
                id: items.length + 1,
                content: "Interval Cycle-" + i,
                start: timePoint
            }]);
        }

        var startTime = new Date(myDate.getTime() - (12 * 60 * 60 * 1000));
        var stopTime = new Date(myDate.getTime() + (12 * 60 * 60 * 1000));
        var start = startTime.getFullYear() + '-' + padLeft(startTime.getMonth() + 1, 2) + '-' + padLeft(startTime.getDate(), 2) + ' ' + padLeft(startTime.getHours(), 2) + ':' + padLeft(startTime.getMinutes(), 2) + ':' + padLeft(startTime.getSeconds(), 2);
        var stop = stopTime.getFullYear() + '-' + padLeft(stopTime.getMonth() + 1, 2) + '-' + padLeft(stopTime.getDate(), 2) + ' ' + padLeft(stopTime.getHours(), 2) + ':' + padLeft(stopTime.getMinutes(), 2) + ':' + padLeft(stopTime.getSeconds(), 2);
        timeline.setWindow(start, stop, {
            animate: true
        });
    }
}
/**
 * Set Time to timeline
 * @param {time} startwith []
 * @param {num} hour      []
 * @param {num} min       []
 * @param {str} week      []
 */
function setTimeView(startwith, hour, min, week) {
    var startTime = getStartTime(startwith);

    var weekArr = getWeekArray(week);
    var i = 0;
    var days = 0;
    while (i < 100) {
        var tempDate = new Date(startTime.date.getTime() + (86400 * 1000 * days));
        if (weekArr[tempDate.getDay()] == "1") {
            var timePoint = tempDate.getFullYear() + '-' + padLeft(tempDate.getMonth() + 1, 2) + '-' + padLeft(tempDate.getDate(), 2) + ' ' + padLeft(hour, 2) + ':' + padLeft(min, 2) + ':00';
            var startString = (startTime.started == true) ? "(...Previously Omission)Time Cycle" : "Time Cycle";
            var content = (i == 0) ? startString + "-Start" : "Time Cycle-" + i;
            items.add([{
                id: items.length + 1,
                content: content,
                start: timePoint
            }]);
            i++;
        }
        days++;
    }
    //show view in the range
    var startTimes = new Date(startTime.date.getTime() - (24 * 60 * 60 * 1000));
    var stopTimes = new Date(startTime.date.getTime() + (24 * 60 * 60 * 1000));
    var start = startTimes.getFullYear() + '-' + padLeft(startTimes.getMonth() + 1, 2) + '-' + padLeft(startTimes.getDate(), 2) + ' ' + padLeft(startTimes.getHours(), 2) + ':' + padLeft(startTimes.getMinutes(), 2) + ':' + padLeft(startTimes.getSeconds(), 2);
    var stop = stopTimes.getFullYear() + '-' + padLeft(stopTimes.getMonth() + 1, 2) + '-' + padLeft(stopTimes.getDate(), 2) + ' ' + padLeft(stopTimes.getHours(), 2) + ':' + padLeft(stopTimes.getMinutes(), 2) + ':' + padLeft(stopTimes.getSeconds(), 2);
    timeline.setWindow(start, stop, {
        animate: true
    });
}
/**
 * conv Start time format 
 * if start before now then use now
 * @param  {time} startwith []
 * @return {time}           []
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
 * Get unit time 
 * Conv unit string to sec
 * @param  {[type]} unit []
 * @return {[type]}      []
 */
function getUnitTime(unit) {
    if (unit == "Hour") {
        return 60 * 60 * 1000;
    } else if (unit == "Day") {
        return 24 * 60 * 60 * 1000;
    } else {
        return 60 * 1000;
    }
}
/**
 * load Job to list
 * @param  {arr} seljob [schedule sel job]
 * 
 */
function loadJob(seljob) {
    for (var i = 0; i < seljob.length; i++) {
        $.grep(allJobList, function(e) {
            if (e.jid.toString() == seljob[i].toString()) {
                jobList.push({
                    jid: e.jid.toString(),
                    name: e.name
                });
            }

        });
    }
    showJobProcess();
}
/**
 * Get Mode Index
 * @param  {str} mod [mod string]
 * @return {num}     [mod num]
 */
function getModeIndex(mod) {
    if (mod == "single") {
        return 0;
    } else if (mod == "interval") {
        return 1;
    } else {
        return 2;
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
/**
 * Get Schedule run job list
 * @return {arr} runJob [scedule run job list]
 */
function getRunJob() {
    var runJob = [];
    for (var i = 0; i < jobList.length; i++) {
        runJob.push(jobList[i].jid.toString());
    }
    return runJob;
}
/**
 * Parse Dropdown sel value to week string
 * ex:'w1,w2'->0110000
 * @param  {str} value [sel value]
 * @return {str} week  [week string]
 */
function parseWeek(value) {
    if (value == "everyday") {
        return "1111111";
    }
    var week = [];
    var we = "";
    for (var i = 0; i <= 6; i++) {

        if (value.indexOf("w" + i) != -1)
            we += "1";
        else
            we += "0";
    }
    return we;
}
/**
 * Set week to Dropdown
 * conv week string to set dropdown sel
 * @param {str} value [week string]
 */
function setWeek(value) {
    if (value == "1111111") {

        $("#eachTime").dropdown('set value', 'everyday');
        loadDropdown();
    } else {
        var week = [];
        var we = padLeft(value, 7).split("");
        for (var i = 0; i <= we.length; i++) {

            if (we[i] == 1)
                week.push("w" + i);
        }
        if (week.length != 0) {
            $("#eachTime").dropdown('set value', week);
            loadDropdown();
        }
    }

}
/**
 * Load Dropdown 
 * if sel everyday then clear single week sel
 * 
 */
function loadDropdown() {
    $('#eachTime').dropdown({

        onChange: function(value, text, $selectedItem) {
            //alert(value);
            if (value.indexOf("everyday") >= 0 && onchangeFlag == 0) {
                var selArray = value.split(",");
                onchangeFlag = 1;
                for (var i = 0; i < selArray.length; i++) {
                    if (selArray[i] != "everyday") {
                        $("#eachTime").dropdown('remove selected', selArray[i]);
                    }
                }
                onchangeFlag = 0;
                $(".week").addClass("disabled");
            }
            if (value == "") {
                $(".week").removeClass("disabled");
            }
        }
    });
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
  * Get time list 
  * @param  {[type]} arr [time array]
  * @return {[type]} time list [time list {runtime,tab}]
  */
function getTimeList(arr) {
    for (var i = 0; i < arr.length; i++) {
        for (var k in arr[i]) {
            if (arr[i].hasOwnProperty(k)) {
                timeList.push({
                    runtime: k,
                    tab: arr[i][k]
                });
            }
        }
    }
}
/**
 * Parse Notification on/off to num
 * @param  {[type]} not [on/off]
 * @return {[type]}     [0:1]
 */
function getNotification(not) {
    if (not == "true") {
        return "1";
    } else {
        return "0";
    }
}
/* padLeft 0 */
function padLeft(str, len) {
    str = '' + str;
    return str.length >= len ? str : new Array(len - str.length + 1).join("0") + str;
}
