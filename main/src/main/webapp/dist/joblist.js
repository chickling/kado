//load
 $(document).ready(function() {
    //message
    Messenger.options = {
        extraClasses: 'messenger-fixed messenger-on-bottom messenger-on-right',
        theme: 'air'
    };
    //function btn click
    $("#project").click(function() {
        $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
    });
    //sematic ui init
    $('.ui.dropdown').dropdown();
    $('#pg').progress();
    $(".progress").progress();
    $(".ui.checkbox").checkbox();

    //Load job list
    loadJobList();
});

/**
 * Load Job List show in page table
 * 
 */
function loadJobList() {
    $.ajax({
        url: './job/manage/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var tableHtml = "";
                    for (var i = 0; i < JData["list"].length; i++) {
                        if (!userFilter()) {
                            if (JData["list"][i]["user"] != $.cookie('username'))
                                continue;
                        } else {
                            if (JData["list"][i]["user"] == $.cookie('username'))
                                continue;
                        }
                        tableHtml += '<tr>';
                        tableHtml += '<td>' + JData["list"][i]["jobid"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["jobname"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["user"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["last_runtime"] + '</td>';
                        tableHtml += '<td>' + getTimeString(JData["list"][i]["runingtime"]) + '</td>';
                        //Function 
                        tableHtml += '<td><button class="ui basic button orange mini jobhistory" jid="' + JData["list"][i]["jobid"] + '"><i class="icon search"></i>View</button></td>';
                        tableHtml += '<td style=" border-left:0px "><a class=" ui icon button mini red jobrun" jid="' + JData["list"][i]["jobid"] + '"><i class="icon play"></i>Run Job</a><a class=" ui icon button mini blue jobedit" jid="' + JData["list"][i]["jobid"] + '"><i class="icon edit"></i>Edit</a><a class=" ui icon button mini green urlquery" jid="' + JData["list"][i]["jobid"] + '"><i class="icon linkify"></i>URL</a></td>';
                        tableHtml += '</tr>';
                    }
                    $(".table.job tbody").html(tableHtml);
                    $(".jobedit").click(function() {
                        location.href = "joblist/edit#" + $(this).attr("jid");
                    });
                    $(".jobrun").click(function() {
                        if ($(this).attr("jid") != "") {
                            runJob($(this).attr("jid"));
                        }
                    });
                    $(".jobhistory").click(function() {
                        if ($(this).attr("jid") != "") {
                            loadHistory($(this).attr("jid"));
                        }
                    });
                    $(".urlquery").click(function() {
                        if ($(this).attr("jid") != "") {
                            showURLBuilder($(this).attr("jid"));
                        }
                    });

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
/**
 * Run Job Now
 * Execution once
 * @param  {[type]} jid [job id]
 * 
 */
function runJob(jid) {

    $.ajax({
        url: './control/job/manage/runnow/' + jid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(Data) {
            if (Data["status"] != null) {
                if (Data["status"] != "error") {
                    //update popup info
                    // $(".column.countcard label").html(Data["count"]); 
                    // $(".ui.active.inverted.dimmer").hide(); 
                    Messenger().post({
                        message: "Job Submit Success!",
                        type: 'info',
                        showCloseButton: true
                    });
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
/**
 * Load job run history and show modal
 * @param  {num} jid [job id]
 * 
 */
function loadHistory(jid) {
    //get schedule history trigger 
    $.ajax({
        url: './job/manage/run/history/range/' + jid,
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
                        tableHtml += '<td>' + JData["list"][i]["jobrunid"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["jobid"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["jobname"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["user"] + '</td>';
                        tableHtml += getStatus(JData["list"][i]["job_status"],JData["list"][i]["jobrunid"]);
                        tableHtml += '<td>' + JData["list"][i]["start_time"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["runingtime"] + '</td>';
                        tableHtml += '<td>' + JData["list"][i]["stop_time"] + '</td>';
                        tableHtml += '</tr>';
                    }
                    $(".ui.modal.job.history tbody").html(tableHtml);
                    $(".ui.modal.job.history").modal("show");

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
/**
  * User Filter
  * filter show self job or other user job
  * @return {[type]} [self->false;other->true]
  */
function userFilter() {
    if (getUrlStatus() == "" || getUrlStatus() == "self") {
      //Save filter set
        $.cookie('jindex', "self", {
            path: '/'
        });
        return false;
    } else if (getUrlStatus() == "other") {
      //Save filter set
        $.cookie('jindex', "other", {
            path: '/'
        });
        $(".item.job.self").removeClass("active");
        $(".item.job.other").addClass("active");
        return true;
    } else if (getUrlStatus() == "back") {
      //Load last filter set
        if ($.cookie('jindex') == "other") {
            $(".item.job.self").removeClass("active");
            $(".item.job.other").addClass("active");
            location.href = "joblist#other";
            return true;
        } else {
            $(".item.job.self").addClass("active");
            $(".item.job.other").removeClass("active");
            location.href = "joblist#self";
            return false;
        }
    } else {
      //Save filter set
        $.cookie('jindex', "self", {
            path: '/'
        });
        return false;
    }
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
 * Show URLBuilder Modal
 * @return {[type]} [description]
 */
function showURLBuilder(jid){
    //set default
    $("#resulttable").checkbox("set unchecked");
    $("#drawchart").checkbox("set unchecked");
    $(".modal.urlbuilder .input").addClass("disabled");
    $(".modal.urlbuilder .dropdown").dropdown("restore defaults").dropdown("set text","Select Chart").dropdown({
        onChange:function(value, text){
            buildURL(jid);
        }
    });
    $(".modal.urlbuilder").show();
    $(".modal.urlbuilder .close").click(function(){
        $(".modal.urlbuilder").hide();
    });
    //load Info
    loadChartInfo(jid);
    loadSQLTemplateInfo(jid);

    $("#drawchart").checkbox({
        onChecked: function() {
            $("#resulttable").checkbox("set unchecked");
            $(".modal.urlbuilder .input.charts").removeClass("disabled");
            buildURL(jid);
        },
        onUnchecked: function() {
            buildURL(jid);
        }
    });
    $("#resulttable").checkbox({
        onChecked: function() {
            $("#drawchart").checkbox("set unchecked");
            $(".modal.urlbuilder .input.charts").addClass("disabled");
            buildURL(jid);
        },
        onUnchecked: function() {
            buildURL(jid);
        }
    });
    $("#gotourl").click(function(){
        var url=$(".modal.urlbuilder .message.urlcontent").val();
        if(url!=""&&url!="Please Select Chart!"){
            openWeb(url);
        }else{
            alert("URL wrong!");
        }
    });
    $("#copytoclipboard").click(function(){
        var copyTextarea = document.querySelector('.urlcontent');
        copyTextarea.select();

        try {
            var successful = document.execCommand('copy');
        } catch (err) {
            alert('unable to copy');
        }
    });
    buildURL(jid);
}
function buildURL(jid){
    var url=document.location.protocol+"//"+document.location.hostname+":"+document.location.port+document.location.pathname.replace("joblist","realtime/query")+"?jid="+jid;
    if($("#resulttable").checkbox("is checked")){
        url+="&next=resultview";
    }else if($("#drawchart").checkbox("is checked")){
        var selValue=$(".modal.urlbuilder .dropdown").dropdown("get value");
        if(selValue!=null&&selValue!=","){
            url+="&next=chart";
            url+="&chartid="+selValue.toString().replace(",","");
        }else{
            url="Please Select Chart!";
        }
    }
    if(url!="Please Select Chart!"){
        $(".input.sqlTemplate").each(function(index){
            var input=$(this).children("input");
            if(input.val()!=null&&input.val()!=""){
                url+="&"+input.attr("urlKey")+"="+input.val();
            }
        });
    }
    $(".modal.urlbuilder .message.urlcontent").val(url);

}
/**
 * load Chart Info to page
 * 
 */
 function loadChartInfo(jid) {
    $.ajax({
     url: 'chart/manage/list/' + jid,
     type: "GET",
     beforeSend: function(request) {
         request.setRequestHeader("Authorization", $.cookie('token'));
     },
     dataType: 'json',
     success: function(JData) {
        if (JData["status"] != null) {
            if (JData["status"] == "success") {
                var html='';
                for(var i=0;i<JData["ChartInfo"].length;i++){
                    html+=getChartItemHtml(JData["ChartInfo"][i]["ChartID"],JData["ChartInfo"][i]["Chart_Name"],JData["ChartInfo"][i]["Type"]);
                }
                $(".dropdown.charts .menu").html(html);
            }else if(JData["status"]=="failed"){
                $("#drawchart_panel").hide();
            }else{
                alert("Get Chart List Fail!:"+JData["message"]);
            }
        }

     },
     error: function(xhr, ajaxOptions, thrownError) {
         alert(xhr.status + "-" + thrownError);
     }
    });

 }
 /**
 * load Job Info to page
 * 
 */
 function loadSQLTemplateInfo(jid) {
    $.ajax({
     url: 'job/manage/get/' + jid,
     type: "GET",
     beforeSend: function(request) {
         request.setRequestHeader("Authorization", $.cookie('token'));
     },
     dataType: 'json',
     success: function(JData) {
        if(JData["status"]!=null){
            if(JData["status"]=="success"){
                var html='';        
                for(var i=0;i<JData["SQLTemplate"].length;i++){
                    html+=getSQLTemplateInput(JData["SQLTemplate"][i]["URLKey"],JData["SQLTemplate"][i]["DefaultValue"]);
                }
                $("#sqlTemplate").html(html);
                $('.input.sqlTemplate input').unbind("input");
                $('.input.sqlTemplate input').on('input', function() { 
                    buildURL(jid);
                });

            }else{
                alert("Load SQL Template Fail");
            }
        }

     },
     error: function(xhr, ajaxOptions, thrownError) {
         alert(xhr.status + "-" + thrownError);
     }
    });

 }
 function getSQLTemplateInput(urlKey,defaultValue){
    return '<div class="ui labeled input sqlTemplate" ><div class="ui label">'+urlKey+'</div><input urlkey="'+urlKey+'" placeholder="'+defaultValue+'" type="text"></div>';
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
    $(".item.job.self").addClass("active");
    $(".item.job.other").removeClass("active");
    location.href = "joblist#self";
    loadJobList();
}
/**
* Get Other User Schedule
* 
*/
function getOther() {
    $(".item.job.self").removeClass("active");
    $(".item.job.other").addClass("active");
    location.href = "joblist#other";
    loadJobList();
}
/**
* Get Status Html
* @param  {[type]} stat [Status]
* @return {[type]} html [html code]
*/
function getStatus(status,jhid) {
    if (status == "0") {
        return '<td><a style="width:100%;text-align: center; " class="ui orange button tiny" data-content="View Job Log" onclick="openWeb(\'status#'+jhid+'\')"><i style="float:left" class="plane icon loading"></i> Run</a></td>';
    } else if (status == "1") {
        return '<td><a style="width:100%;text-alagn:center" class="ui green button tiny" data-content="View Job Log" onclick="openWeb(\'status#'+jhid+'\')"><i style="float:left" class="checkmark icon"></i>Success</a></td>';
    } else {
        return '<td><a style="width:100%;text-align:center" class="ui red button tiny" data-content="View Job Log" onclick="openWeb(\'status#'+jhid+'\')"><i style="float:left" class="fire icon"></i>Fail</as></td>';
    }
}
function getChartItemHtml(cid,chartname,charttype){
    if(charttype=="line"){
        return '<div class="item" data-value="'+cid+'"><i class="line chart icon"></i>'+chartname+'</div>';
    }else if(charttype=="bar"){
        return '<div class="item" data-value="'+cid+'"><i class="bar chart icon"></i>'+chartname+'</div>';
    }else if(charttype=="pie"){
        return '<div class="item" data-value="'+cid+'"><i class="pie chart icon"></i>'+chartname+'</div>';
    }
}
/**
 * Open page in new tab
 * @param  {str} prestoURL [presto job url]
 */
function openWeb(URL){
    var newwin = window.open();
    newwin.location = URL;
}