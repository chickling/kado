  // trigger extension
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
  fromQueryUI();
  langTools.addCompleter(rhymeCompleter);
  //load
  $(document).ready(function() {
      $("#project").click(function() {
          $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
      });
      $('.ui.accordion').accordion();
      $('.ui.dropdown').dropdown();
      $('.ui.checkbox').checkbox();
      $('#pg').progress();
      $(".progress").progress();
      $(".card .field").addClass("disabled");
      $('[name="Report"]').parent().checkbox('set unchecked');
      loadRadioStatus();
      clearInput();
      loadDBLocation();
      $('.ui.checkbox.storage').change(function() {
          loadRadioStatus();
      });
      //["a@b.c","d@e.f"]
      $("#ReportEmail").val('');
      $('#ReportEmail').multiple_emails({
          theme: "SemanticUI"
      });
      //addPaemeter
     $(".button.addParameter").click(function(){
        showSQLTemplateModal();
     });
  });
  //SQLTemplate
  var sqlTemplate_temp=[];

  function saveJob() {
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
          //     addJson["location_id"] = $(".dropdown.db.location").dropdown("get value");;
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
          //POST to Add User
          $.ajax({
              url: '../job/manage/add',
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
  /**
   * Check Input Correct
   * 
   */
  function checkInputInfo() {
      clearError();
      //check storage
      //var storage = $('[name="storage"]:checked').val();
      var message = "";
      // if (storage == 1) {
      //     if ($(".input.file.fname input").val() == "") {
      //         message += "File Name can not be empty \n";
      //         $(".input.file.fname").addClass("error");
      //     }
      //     if ($(".input.file.fpath input").val() == "") {
      //         message += "File Path can not be empty \n";
      //         $(".input.file.fpath").addClass("error");
      //     }
      // } else if (storage == 2) {
      //     if ($(".input.insert.sql input").val() == "") {
      //         message += "Insert SQL can not be empty \n";
      //         $(".input.insert.sql").addClass("error");
      //     }
      // }
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
                      var itemHtml = "";
                      var defaultValue = "";
                      for (var i = 0; i < JData["list"].length; i++) {
                          if (i == 0)
                              defaultValue = JData["list"][i]["id"];
                          itemHtml += '<div class="item" data-value="' + JData["list"][i]["id"] + '">' + JData["list"][i]["name"] + '</div>';
                      }
                      $(".dropdown.location.db .menu").html(itemHtml);
                      //$(".dropdown.location.db").dropdown();
                      //alert(defaultValue);
                      $(".db.dropdown").dropdown();
                      $(".db.dropdown").dropdown('set selected', defaultValue.toString());
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
   * Set radio sel status
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
   * Set SQL from Query UI
   * 
   */
  function fromQueryUI() {
      if (getUrlStatus() == "fromQueryUI") {
          editor.setValue($.base64Decode($.cookie("sql")));
      }
  }

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
//         $('.ui.left.icon.input.file.fpath').addClass('disabled');
//     }else{
//              $(".file.location.hdfs").one("click",function(){
//                  if(!$('.file.segment.hdfs').checkbox("is checked")){
//                     $('.ui.left.icon.input.file.fpath').addClass('disabled');
//                   }else
//                      $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//                })
//            if($('.file.segment.local').checkbox("is checked"))
//                    $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//     }

  });
 $(".file.location.local").click(function(){
     if(!$('.file.segment.hdfs').checkbox("is checked")&&$('.file.segment.local').checkbox("is checked")){
             //$('.file.segment.local').checkbox("uncheck");
         $('.file.segment.hdfs').checkbox("check");
    }
//         $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//     }else{
//         if($('.file.segment.hdfs').checkbox("is checked"))
//            $('.ui.left.icon.input.file.fpath').removeClass('disabled');
//         else
//            $('.ui.left.icon.input.file.fpath').addClass('disabled');
//     }

  });