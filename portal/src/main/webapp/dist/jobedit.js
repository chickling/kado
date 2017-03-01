 // trigger extension
 // load ace editor
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
 var jsonUrl = "../views/KW.json";
 var rhymeCompleter = {
     getCompletions: function(editor, session, pos, prefix, callback) {
         if (prefix.length === 0) {
             callback(null, []);
             return
         }
         $.getJSON(jsonUrl, function(wordList) {
             callback(null, wordList.map(function(ea) {
                 return {
                     name: ea.word,
                     value: ea.word,
                     meta: ea.type
                 }
             }));
         })
     }
 }
 langTools.addCompleter(rhymeCompleter);
 //SQLTemplate
 var sqlTemplate_temp=[];

 //load
 $(document).ready(function() {
    //sematic ui init
     $('.ui.accordion').accordion();
     $('.ui.dropdown').dropdown();
     $('.ui.checkbox').checkbox();
     $('#pg').progress();
     $(".progress").progress();
     $("#project").click(function() {
         $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
     });     
     clearInput();
     loadDBLocation();
     $(".card .field").addClass("disabled");
     $('[name="Report"]').parent().checkbox('set unchecked');

     $('.ui.checkbox.storage').change(function() {
         loadRadioStatus();
     });
     //DeleteJob button click
     $(".button.job.delete").click(function() {
         if ($(this).attr("jid") != "" && $(this).attr("jid") != null)
             deleteJob($(this).attr("jid"));
         else
             alert("Lose JID");
     });
     //addPaemeter
     $(".button.addParameter").click(function(){
        showSQLTemplateModal();
     });
     //["a@b.c","d@e.f"]
      $("#ReportEmail").val('');

     //Chart Filter
     $(".chartfilter .item").click(function(){        
        if($(this).hasClass("all")){
            $(".chartfilter .item").removeClass("active");
            $(".chartfilter .item.all").addClass("active");
            loadChartSetting("all");
        }else if($(this).hasClass("line")){
            $(".chartfilter .item").removeClass("active");
            $(".chartfilter .item.line").addClass("active");
            loadChartSetting("line");
        }else if($(this).hasClass("bar")){
            $(".chartfilter .item").removeClass("active");
            $(".chartfilter .item.bar").addClass("active");
            loadChartSetting("bar");
        }else if($(this).hasClass("pie")){
            $(".chartfilter .item").removeClass("active");
            $(".chartfilter .item.pie").addClass("active");
            loadChartSetting("pie");
        }
     });    
     //Add Chart Click
     $("#addchart").click(function(){        
        var newwin = window.open();
        newwin.location = $(this).attr("link");
     });
    
 });
/**
 * load Job Info to page
 * 
 */
 function loadJobInfo() {
     if (getUrlStatus() != "") {
         $.ajax({
             url: '../job/manage/get/' + getUrlStatus(),
             type: "GET",
             beforeSend: function(request) {
                 request.setRequestHeader("Authorization", $.cookie('token'));
             },
             dataType: 'json',
             success: function(JData) {
                 if (JData["status"] != null) {
                     if (JData["status"] != "error") {
                         //loading basic
                         $(".ui.form.job.basic input").each(function(index) {
                             if ($(this).attr("name") == "jobLevel") {
                                 if (JData[$(this).attr("name")] != null) {
                                     $(this).parent().dropdown('set value', JData[$(this).attr("name")].toString());
                                     $(this).parent().dropdown();
                                 }
                             } else if ($(this).attr("name") == "notification") {
                                 if (JData[$(this).attr("name")] != null) {
                                     if (JData[$(this).attr("name")] == "0")
                                         $(this).parent().checkbox('uncheck');
                                     else
                                         $(this).parent().checkbox('check');
                                 }
                             } else if($(this).attr("name")=="ReportEmail"){
                                if(JData["ReportEmail"]!=null&&JData["ReportEmail"]!=""&&JData["ReportEmail"]!="[]"){
                                    var email=JData["ReportEmail"].split(";");
                                    var emailArr=[];
                                    for(var i=0;i<email.length;i++){
                                        if(email[i]!=""){
                                            emailArr.push(email[i]);
                                        }
                                    }
                                    $("#ReportEmail").val(JSON.stringify(emailArr));    
                                }
                                $('#ReportEmail').multiple_emails({
                                              theme: "SemanticUI"
                                    });
                             }else if($(this).attr("name")=="Report"){
                                if(JData["Report"]){
                                    $('[name="Report"]').parent().checkbox("check");
                                }else{
                                    $('[name="Report"]').parent().checkbox("uncheck");
                                }
                             }else if($(this).attr("name")=="ReportWhileEmpty"){
                                if(JData["ReportWhileEmpty"]){
                                    $('[name="ReportWhileEmpty"]').parent().checkbox("check");
                                }else{
                                    $('[name="ReportWhileEmpty"]').parent().checkbox("uncheck");
                                }
                             }else if($(this).attr("name") == "ReportLength"||$(this).attr("name") == "ReportFileType") {
                                if (JData[$(this).attr("name")] != null) {
                                     $(this).parent().dropdown('set value', JData[$(this).attr("name")].toString());
                                     $(this).parent().dropdown();
                                }
                             }else {
                                 if (JData[$(this).attr("name")] != null)
                                     $(this).val(JData[$(this).attr("name")]);
                             }
                         });
                         setStorageStatusBySaveType(JData["save_type"],JData);
                         //set Job SQL

                         editor.setValue($.base64Decode(JData["sql"]));
                         //set save button jobID
                         $(".button.icon.save").attr("onclick", "saveJob(" + getUrlStatus() + ")");
                         //set delete jid
                         $(".button.job.delete").attr("jid", getUrlStatus());
                         //load SQL Template
                         sqlTemplate_temp=JData["SQLTemplate"];
                         loadSQLTemplate(sqlTemplate_temp);
                         loadChartSetting("all");

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
 * Save Job change
 * @param  {[type]} jid [job id]
 * 
 */
 function saveJob(jid) {
     if (checkInputInfo()) {
         // $('#editor_form').dimmer('show');

         var addJson = {};
         var message = "";
         // Add job basic info
         $(".ui.form.job.basic input").each(function(index) {
             if ($(this).attr("name") != "notification"&&$(this).attr("name") != "ReportWhileEmpty") {
                 addJson[$(this).attr("name")] = $(this).val();
             } else if($(this).attr("name") != "ReportWhileEmpty") {
                 addJson[$(this).attr("name")] = $(this).parent().checkbox('is checked');
             }
         });
         // Add Report INFO
          if ($('[name="Report"]').is(":checked")) {
            addJson["Report"]="true";
            if ($("#ReportEmail").val() != "" || $("#ReportEmail").val() != "[]"){
              var json=JSON.parse($("#ReportEmail").val());
              var email="";
              for(var i=0;i<json.length;i++){
                email+=json[i]+";";
              } 
              addJson["ReportEmail"]=email;
            }else{
              addJson["ReportEmail"]="";
            }              
            addJson["ReportTitle"]=$('[name="ReportTitle"]').val();
            addJson["ReportLength"]=$('[name="ReportLength"]').val();
            addJson["ReportFileType"]=$('[name="ReportFileType"]').val();       
            addJson["ReportWhileEmpty"]=$('[name="ReportWhileEmpty"]').is(":checked")==true?"true":"false";      
              
          }else{
            addJson["Report"]="false";
            addJson["ReportEmail"]="";
            addJson["ReportTitle"]="";
            addJson["ReportLength"]="0";
            addJson["ReportFileType"]="0";
            addJson["ReportWhileEmpty"]="false";
          }

         //Add Storage Setting
         // var storage = $('[name="storage"]:checked').val();
         // if (storage == 1) {
         //     //
         //     addJson["storage"] = true;
         //     addJson["save_type"] = "HDFS";
         //     addJson["insertsql"] = "";
         //     addJson["location_id"] = "0";
         //     //Set Value
         //     addJson["filepath"] = $(".input.file.fpath input").val();
         //     addJson["filename"] = $(".input.file.fname input").val();

         // } else if (storage == 2) {
         //     addJson["storage"] = true;
         //     addJson["save_type"] = "DB";
         //     addJson["filepath"] = "";
         //     addJson["filename"] = "";
         //     addJson["location_id"] = $(".dropdown.db.location").dropdown("get value");
         //     //Set Value
         //     addJson["insertsql"] = $.base64Encode($(".input.insert.sql input").val());
         // } else {
         //     addJson["storage"] = false;
         //     addJson["save_type"] = "";
         //     addJson["location_id"] = "0";
         // }
         //Add Storage Setting
         if($(".checkbox.db").checkbox('is checked')||$(".checkbox.csv").checkbox('is checked')){
            //clear
            addJson["storage"] = true;
            addJson["filepath"] = "";
            addJson["filename"] = "";
            addJson["insertsql"] = "";
            addJson["location_id"] = "0";
            var stype=0;
            if($(".checkbox.db").checkbox('is checked')){
                stype+=4;                
                addJson["location_id"] = $(".dropdown.db.location").dropdown("get value");
                //Set Value
                addJson["insertsql"] = $.base64Encode($(".input.insert.sql input").val());
            }
            if($(".checkbox.csv").checkbox('is checked')){
                if($(".checkbox.hdfs").checkbox('is checked')){
                    stype+=1;
                }
                if($(".checkbox.local").checkbox('is checked')){
                    stype+=2;
                }
                //Set Value
                addJson["filepath"] = $(".input.file.fpath input").val();
                addJson["filename"] = $(".input.file.fname input").val();
            }
            addJson["save_type"] = stype;
         }else{
            addJson["storage"] = false;
            addJson["save_type"] = 0;
            addJson["location_id"] = "0";
         }
         //add Job SQL
         addJson["sql"] = $.base64Encode(editor.getValue());
         //add type
         addJson["type"] = "userjob";
         addJson["SQLTemplate"]=sqlTemplate_temp;

         //POST to Update Job
         $.ajax({
             url: '../job/manage/update/' + jid,
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
                         location.href = "../joblist#back";
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
 * Clear Error alert
 * 
 */
 function clearError() {

     $(".error").each(function() {
         $(this).removeClass("error");
     });
 }
/**
 * Clear Input
 * 
 */
 function clearInput() {
     $("input").each(function() {
         if ($(this).attr("name") != "jobLevel" && $(this).attr("name") != "storage")
             $(this).val("");
     });

 }
 function loadDBLocation() {
      $.ajax({
          url: '../job/manage/location/list/',
          type: "GET",
          beforeSend: function(request) {
              request.setRequestHeader("Authorization", $.cookie('token'));
          },
          dataType: 'json',
          success: function(JData) {
              if (JData["status"] != null) {
                  if (JData["status"] != "error") {
                    var itemHtml="";
                    var defaultValue="";
                    for(var i=0;i<JData["list"].length;i++){
                      if(i==0)
                        defaultValue=JData["list"][i]["id"];
                      itemHtml+='<div class="item" data-value="'+JData["list"][i]["id"]+'">'+JData["list"][i]["name"]+'</div>';
                    }
                    $(".dropdown.location.db .menu").html(itemHtml);
                    //$(".dropdown.location.db").dropdown();
                    //alert(defaultValue);
                    $(".db.dropdown").dropdown();
                    loadJobInfo();
                    loadRadioStatus();
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
 * Check Input Correct
 * 
 */
 function checkInputInfo() {
     clearError();
     //check storage
     //var storage = $('[name="storage"]:checked').val();
     var message = "";
     if ($(".checkbox.csv").checkbox("is checked")) {
         if ($(".input.file.fname input").val() == "") {
             message += "File Name can not be empty \n";
             $(".input.file.fname").addClass("error");
         }
         if ($(".input.file.fpath input").val() == ""&&$('.file.segment.hdfs').checkbox("is checked")) {
             message += "File Path can not be empty \n";
             $(".input.file.fpath").addClass("error");
         }
         if(!$('.file.segment.hdfs').checkbox("is checked")&&!$('.file.segment.local').checkbox("is checked")) {
            message += "Please select at least one csv Location (HDFS or Local) \n";
             $(".file.segment").addClass("error");
         }
     }
    if ($(".checkbox.db").checkbox("is checked")) {
         if ($(".input.insert.sql input").val() == "") {
             message += "Insert SQL can not be empty \n";
             $(".input.insert.sql").addClass("error");
         }
     }


     //check Job Visibility Level
     if ($(".dropdown.level input").val() == "") {
         $(".dropdown.level").addClass("error");
         message += "Please select Job Visibility Level \n";
     }

     //check Job Name
     if ($('[name="jobname"]').val() == "") {
         $('[name="jobname"]').parent().addClass("error");
         message += "Job Name can not be empty \n";
     }

     //check Job SQL
     if (editor.getValue() == "") {
         message += "Job SQL can not be empty \n";
     }

     //check Send Report Info
      //IF checkbox checked
      if ($('[name="Report"]').is(":checked")) {
          //check email
          if ($("#ReportEmail").val() == "" || $("#ReportEmail").val() == "[]")
              message += "Report Email can not be empty \n";
          //check title
          if ($('[name="ReportTitle"]').val() == "") {
              $('[name="ReportTitle"]').parent().addClass("error");
              message += "Report Title can not be empty \n";
          }
          //check report max row sel
          if ($('[name="ReportLength"]').val() == "") {
              $('[name="ReportLength"]').parent().addClass("error");
              message += "Please select Report Max Row \n";
          }
          //check report added information
          if ($('[name="ReportFileType"]').val() == "") {
              $('[name="ReportFileType"]').parent().addClass("error");
              message += "Please select Report Added Information \n";
          }

      }
     //SHOW MESSAGE
     if (message != "") {
         alert(message);
         return false;
     } else {
         return true;
     }
 }
/**
 * Delete Job
 * @param  {num} jid [job id]
 * 
 */
 function deleteJob(jid) {
     if (confirm('Are you sure you want to delete Job?')) {
         $.ajax({
             url: '../job/manage/delete/' + jid,
             type: "GET",
             beforeSend: function(request) {
                 request.setRequestHeader("Authorization", $.cookie('token'));
             },
             dataType: 'json',
             success: function(JData) {
                 if (JData["status"] != null) {
                     if (JData["status"] != "error") {
                         location.href = "../joblist#back"
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
 * Load SQLTemplate 
 *
 */
 function loadSQLTemplate(sqlTemplate) {
    var tableHtml="";
    for(var i=0;i<sqlTemplate.length;i++){
        tableHtml+="<tr>";
        tableHtml+="<td>"+sqlTemplate[i]["URLKey"]+"</td>";
        tableHtml+="<td>"+sqlTemplate[i]["SQLKey"]+"</td>";
        tableHtml+="<td>"+sqlTemplate[i]["DefaultValue"]+"</td>";
        //Function Button
        tableHtml+='<td><button class="ui icon button" onclick="deleteSQLTemplate('+i+')"><i class="remove icon"></i></button></td>';
        tableHtml+="</tr>";
    }
    $(".table.sqlTemplate tbody").html(tableHtml);
 }
 function deleteSQLTemplate(index){    
    if(sqlTemplate_temp[index]!=null&&index > -1){
        sqlTemplate_temp.splice(index, 1)
        loadSQLTemplate(sqlTemplate_temp);
    }else{
        alert("SQLTemplate index not exist!")
    }
 }
 function showSQLTemplateModal(){
    $(".add_sqlTemplate").modal("show");
    $('.modal.add_sqlTemplate [name="urlkey"]').val("");
    $('.modal.add_sqlTemplate [name="sqlkey"]').val("");
    $('.modal.add_sqlTemplate [name="defaultvalue"]').val("");
 }
 function addSQLTemplate(){
    var msg=checkAddSQLTemplate();
    if(msg==""){
        sqlTemplate_temp.push({
            URLKey:$('.modal.add_sqlTemplate [name="urlkey"]').val(),
            SQLKey:$('.modal.add_sqlTemplate [name="sqlkey"]').val(),
            DefaultValue:$('.modal.add_sqlTemplate [name="defaultvalue"]').val()
        });
        $(".add_sqlTemplate").modal("hide");
        loadSQLTemplate(sqlTemplate_temp);
    }else{
        alert(msg);
    }
 }
 function checkAddSQLTemplate(){
    var msg="";
    if($('.modal.add_sqlTemplate [name="urlkey"]').val()==""||$('.modal.add_sqlTemplate [name="urlkey"]').val()==null){
        msg+="Please Input URLKey\n";
    }
    if($('.modal.add_sqlTemplate [name="sqlkey"]').val()==""||$('.modal.add_sqlTemplate [name="sqlkey"]').val()==null){
        msg+="Please Input SQLKey\n";
    }
    return msg;
 }
 function loadChartSetting(type){
    var jobID=getUrlStatus();
    if(jobID!=""){
        $.ajax({
                 url: '../chart/manage/list/' + jobID,
                 type: "GET",
                 beforeSend: function(request) {
                     request.setRequestHeader("Authorization", $.cookie('token'));
                 },
                 dataType: 'json',
                 success: function(JData) {
                    if(JData["status"]!=null){
                        if(JData["status"]=="success"){
                            var html="";
                            JData["ChartInfo"].map(function(data){
                                if(type!="all"){
                                    if(data["Type"]==type){
                                        html+='<tr>';
                                        html+='<td>'+data["Chart_Name"]+'</td>';
                                        html+='<td>'+data["Type"]+' chart</td>';
                                        var axis=getAxisInfo(data["Chart_Setting"]);
                                        html+='<td>'+axis.x+'</td>';
                                        html+='<td>'+axis.y+'</td>';
                                        html+='<td>'+getChartEditButton(data["ChartID"],data["JobID"])+'</td>';                                
                                        html+='</tr>';
                                    }
                                }else{
                                    html+='<tr>';
                                    html+='<td>'+data["Chart_Name"]+'</td>';
                                    html+='<td>'+data["Type"]+' chart</td>';
                                    var axis=getAxisInfo(data["Chart_Setting"]);
                                    html+='<td>'+axis.x+'</td>';
                                    html+='<td>'+axis.y+'</td>';
                                    html+='<td>'+getChartEditButton(data["ChartID"],data["JobID"])+'</td>';                                
                                    html+='</tr>';
                                }
                                
                            });
                            $(".table.chart tbody").html(html);
                            $("#addchart").attr("link","../charts/builder?jid="+jobID);
                        }else if(JData["status"]=="failed"){
                            $(".chart.title").hide();
                            $(".chart.content").hide();
                        }else{
                            alert("Get Chart List Failed!"+JData["message"]);
                        }
                    }
                 },

                 error: function(xhr, ajaxOptions, thrownError) {
                     alert(xhr.status + "-" + thrownError);
                 }
        });
    }
 }
 function getChartEditButton(chartID,jobID){
    var html="";
    html+='<div class="ui icon buttons">';
    html+='<a class="ui icon button" onclick="editChart('+chartID+','+jobID+')"><i class="write icon"></i></a>';
    html+='<a class="ui icon button" onclick="deleteChart('+chartID+','+jobID+')"><i class="remove icon"></i></a>';
    html+='</div>';
    return html;                
 }
 function getAxisInfo(chartSettingString){
    var chartSetting = JSON.parse( chartSettingString );
    var x=chartSetting.data_setting.xAxis!=null?chartSetting.data_setting.xAxis.column.name:"";
    var y=chartSetting.data_setting.yAxis.column.map(function(value){
        return value.name;
    }).join(",");
    return {x:x,y:y}
 }
 function deleteChart(chartID,jobID){
    if (confirm('Are you sure you want to delete Chart?')) {
         $.ajax({
             url: '../chart/manage/delete/' + chartID+"/"+jobID,
             type: "GET",
             beforeSend: function(request) {
                 request.setRequestHeader("Authorization", $.cookie('token'));
             },
             dataType: 'json',
             success: function(JData) {
                 if (JData["status"] != null) {
                     if (JData["status"] != "error") {
                         reloadChart();
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
 function editChart(chartID,jobID){
    var newwin = window.open();
    newwin.location = "../charts/builder?chartid="+chartID+"&jid="+jobID;
 }
 function reloadChart(){
    var selchart=$(".menu.chartfilter .item.active");
    if($(selchart).hasClass("all")){
        loadChartSetting("all");
    }else if($(selchart).hasClass("line")){
        loadChartSetting("line");
    }else if($(selchart).hasClass("bar")){
        loadChartSetting("bar");
    }else if($(selchart).hasClass("pie")){
        loadChartSetting("pie");
    }
 }
/**
 * Set radio sel status
 * 
 */
 function loadRadioStatus() {     
   
     if ($(".checkbox.csv").checkbox("is checked")) {
         $(".input.file").removeClass("disabled");
         $(".file.hdfs").removeClass("disabled");
         $(".file.local").removeClass("disabled");         
     } else{
        $(".input.file").addClass("disabled");
         $(".file.hdfs").addClass("disabled");
         $(".file.local").addClass("disabled");
     }
     if($(".checkbox.db").checkbox("is checked")) {         
         $(".input.insert.sql").removeClass("disabled");         
     }else{
         $(".input.insert.sql").addClass("disabled");   
     }
 }
   /**
   * Send Report Button Change
   */
  $('.card input:checkbox').change(function() {
      if ($('[name="Report"]').is(":checked")) {
          $(".card .field").removeClass("disabled");
      } else {
          $(".card .field").addClass("disabled");
      }
  });
/**
 * Copy Sql Jump to Query UI
 * 
 */
 function pushToQueryUI() {
     if (editor.getValue() != "") {
         $.removeCookie('sql');
         $.cookie('sql', $.base64Encode(editor.getValue()), {
             path: '/'
         });
         var newwin = window.open();
         newwin.location = "../queryui#fromJob";
     } else {
         alert("SQL can not be empty");
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

 $(".file.location.hdfs").click(function(){
    if($('.file.segment.hdfs').checkbox("is checked")&&!$('.file.segment.local').checkbox("is checked")) {
        //$('.file.segment.hdfs').checkbox("uncheck");
        $('.file.segment.local').checkbox("check");
        }
//        $('.ui.left.icon.input.file.fpath').addClass('disabled');
//    }else{
//             $(".file.location.hdfs").one("click",function(){
//                 if(!$('.file.segment.hdfs').checkbox("is checked")){
//                    $('.ui.left.icon.input.file.fpath').addClass('disabled');
//                  }else
//                     $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//               })
//           if($('.file.segment.local').checkbox("is checked"))
//                   $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//    }

 });
$(".file.location.local").click(function(){
    if(!$('.file.segment.hdfs').checkbox("is checked")&&$('.file.segment.local').checkbox("is checked")){
            //$('.file.segment.local').checkbox("uncheck");
        $('.file.segment.hdfs').checkbox("check");
        }
//        $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//    }else{
//        if($('.file.segment.hdfs').checkbox("is checked"))
//           $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//        else
//           $('.ui.left.icon.input.file.fpath').addClass('disabled');
//    }

 });
function setStorageStatusBySaveType(save_type,JData){
    $(".checkbox.segment").checkbox("uncheck");
    $(".ui.checkbox.storage").checkbox("uncheck");
    var stype=parseInt(save_type);

    if((stype/4)>=1){
        $(".checkbox.db").checkbox('check');
         $(".input.insert.sql input").val($.base64Decode(JData["insertsql"]));
         $(".db.dropdown").dropdown('set selected',JData["location_id"].toString());
    }
    if(((stype%4)/2)>=1){
        $(".checkbox.csv").checkbox('check');
        $(".checkbox.local").checkbox('check');
        $(".input.file.fpath input").val(JData["filepath"]);
        $(".input.file.fname input").val(JData["filename"]);
        $(".db.dropdown").dropdown('set selected','0');
                            
    }
    if((((stype%4)%2)/1)>=1){
        $(".checkbox.csv").checkbox('check');
        $(".checkbox.hdfs").checkbox('check');
        $(".input.file.fpath input").val(JData["filepath"]);
        $(".input.file.fname input").val(JData["filename"]);
        $(".db.dropdown").dropdown('set selected','0');
    }
}
