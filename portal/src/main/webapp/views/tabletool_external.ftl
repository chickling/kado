<!DOCTYPE html>
<html>

<head>
    <#-- INCLUDE NAV -->
        <#include "header.ftl">
            <!-- Standard Meta -->
            <meta charset="utf-8" />
            <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <!-- Site Properities -->
            <title>Kado Dashboard</title>
            <link href="./../dist/vis.css" rel="stylesheet" type="text/css" />
            <link rel="stylesheet" type="text/css" href="./../dist/semantic.css">
            <link href="./../dist/messenger.css" rel="stylesheet" type="text/css">
            <link href="./../dist/messenger-theme-air.css" rel="stylesheet" type="text/css">
            <link href="./../dist/jquery.fileupload.css" rel="stylesheet" type="text/css">
            <style type="text/css">
            body {
                background-color: #FFFFFF;
            }

            .ui.menu .item img.logo {
                margin-right: 1.5em;
            }

            .main.container {
                margin-top: 3em;
            }

            .wireframe {
                margin-top: 2em;
            }

            .ui.footer.segment {
                margin: 0em 0em 0em;
                padding: 5em 0em;
            }

            .itemnotclick {
                background: none repeat scroll 0 0 transparent !important;
                box-shadow: none !important;
                color: rgba(0, 0, 0, 0.87) !important;
                font-size: 1em !important;
                font-weight: normal !important;
                margin: 0;
                padding: 0.714286em 1.14286em !important;
                text-align: left;
                text-transform: none !important;
                transition: none 0s ease 0s !important;
            }


            .full.height>.toc {
                background-color: #1b1c1d;
                flex: 0 0 auto;
                position: relative;
                width: 200px;
                z-index: 1;
            }

            .full.height {
                display: flex;
                flex-direction: row;
                min-height: 800px;
            }

            .ui.container {
                width: 95%;
            }

            .ui.basic.button.mini {
                width: 100%;
            }

            #uploadTitle {
                text-align: center;
            }

            #container {
                width: 170px;
                height: 170px;
                position: relative;
                margin: 0 auto;
            }

            .modal.basic .content {
                text-align: center;
                color: red;
            }

            .fullwidth {
                width: 100%;
            }
            </style>
</head>

<body>
    <#-- INCLUDE NAV -->
    <#include "nav.ftl">
<div class="ui active dimmer load" id="scheduleHistoryLoader" style="display:none">
    <div class="ui huge text loader">Loading</div>
</div>
<div class="pusher">
    <!-- Site content !-->
    <div class="full height">
        <div class="toc" style="min-height: 100%;">
            <div class="ui vertical inverted sticky menu" style="left: 0px; top: 0px; width: 200px ! important; height: 100% ! important;">
                <div class="item">
                    <a><h4><i class="icon terminal large"></i>Schedule List</h4></a>
                </div>
          <a href="./create" class="item  self schedule">
            <b><i class="icon star"></i>Create Internal Table</b>
          </a>
          <a href="./external" class="item active self schedule">
            <b><i class="icon star"></i>Create External Table</b>
          </a>
          <a class="item self schedule" onclick="$('#difference').modal('show')">
            <b><i class="icon info"></i>Difference between internal and external?</b>
          </a>
            </div>
        </div>
        <div class="ui main" style="width: 100%;padding-left: 20px;padding-right: 20px;">
            <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
                <a class="ui labeled icon primary button " id="drop" href="schedulelist/add">
              <i class="plus icon"></i>
              Add Schedule
            </a>
            </div>
            <h3 class="ui header">
              <i class="upload icon"></i>
              <div class="content">
                Upload CSV
              </div>
            </h3>
                        <div class="ui divider"></div>
                        <div class="ui stacked segment attached">
                            <h4 id="uploadTitle" class="ui header">Please select the CSV file</h4>
                            <div id="progress" class="ui progress" style="margin-bottom: 0px;">
                                <div class="bar progress-bar progress-bar-success">
                                    <div class="progress"></div>
                                </div>
                            </div>
                            <div id="filelist" style="margin-top:30px">
                            </div>
                        </div>
                        <div class="ui bottom attached button fileinput-button" tabindex="0">
                            <i class="plus icon"></i>
                            <span>Select or Drop files...</span>
                            <!-- The file input field used as target for the file upload widget -->
                            <input id="fileupload" name="file" multiple="" type="file">
                        </div>
                        <div id="setp2" style="margin-top:30px;">
                            <div class="ui grid">
                            <div class="fourteen wide column">
                              <h3 class="ui header">
                                <i class="edit icon"></i>
                                <div class="content">
                                  Edit Schema
                                </div>
                              </h3>
                            </div>
                            <div class="two wide column">
                              <button class="ui labeled icon button fullwidth" onclick="addColumn()">
                                  <i class="plus icon"></i> Add
                              </button>
                            </div>
                            </div>
                            <div class="ui divider"></div>
                            <table class="ui celled table schedule list selectable">
                                <thead>
                                    <tr>
                                        <th style="width:70px;"> Index</th>
                                        <th>Column Name</th>
                                        <th style="width:120px;">Column Type</th>
                                        <th style="width:120px;">Row 1 Data</th>
                                        <th style="width:120px;">Row 2 Data</th>
                                        <th style="width:100px;">Is Partition</th>
                                        <th style="width:50px;"></th>
                                    </tr>
                                </thead>
                                <tbody id="tableSchema">
                                </tbody>
                            </table>
                        </div>
                        <div id="setp3" style="margin-top:30px;display: none;">
                            <h3 class="ui header">
                              <i class="setting icon"></i>
                              <div class="content">
                                Table Setting
                              </div>
                            </h3>
                            <div class="ui divider"></div>

                            <h4 class="ui dividing header">File Partition Setting</h4>
                            <div id="partitionSetting" >
                              
                            </div>
                            <form class="ui form">
                                <h4 class="ui dividing header">Internal Table Information</h4>
                                <div class="field">
                                    <div class="fields">
                                        <div class="field wide five">
                                            <label>DB Name</label>
                                            <select class="ui dropdown" id="dbName">
                                            </select>
                                        </div>
                                        <div class="field wide eleven">
                                            <label>Table Name</label>
                                            <input id="tableName" placeholder="Table Name" type="text">
                                        </div>
                                    </div>
                                   
                                </div>
                            </form>
                            <div class="ui icon message red" id="createErrorMessage" style="display: none;">
                                <i class="warning sign icon"></i>
                                <div class="content">
                                    <div class="header">
                                        Create Table Fail!
                                    </div>
                                    <p></p>
                                </div>
                            </div>
                            <button class="ui right labeled icon button large red" style="margin-bottom:20px;margin-top:20px;" onclick="createTable()">
                                <i class="wizard icon"></i> Create Table
                            </button>
                        </div>
                        <div class="ui basic modal">
                            <div class="ui icon header">
                                <div id="container"></div>
                            </div>
                            <div class="ui icon header">
                                <h3 id="insertStatus">Waiting for Upload Data to HDFS</h3>
                            </div>
                            <div class="content">
                            </div>
                            <div class="actions">
                                <div class="ui red basic cancel inverted button">
                                    <i class="remove icon"></i> Close Window
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!--MODAL-->
                <div class="ui modal" id="difference">
                  <i class="close icon"></i>
                  <div class="header">
                    Difference between internal and external
                  </div>
                  <div class="content">
                    <div class="ui header">For External Tables - Suitable for Large files</div>
                    <p>* External table stores files on the HDFS server but tables are not linked to the source file completely.</p>
                    <p>* If you delete an external table the file still remains on the HDFS server. As an example if you create an external table called “table_test” in HIVE using HIVE-QL and link the table to file “file”, then deleting “table_test” from HIVE will not delete “file” from HDFS.</p>
                    <p>* External table files are accessible to anyone who has access to HDFS file structure and therefore security needs to be managed at the HDFS file/folder level.</p>
                    <p>* Meta data is maintained on master node, and deleting an external table from HIVE only deletes the metadata not the data/file.</p>
                    <div class="ui header">For Internal Tables- Suitable for Smaller files</div>
                    <p>* Stored in a directory based on settings in hive.metastore.warehouse.dir, by default internal tables are stored in the following directory “/user/hive/warehouse” you can change it by updating the location in the config file .</p>
                    <p>* Deleting the table deletes the metadata and data from master-node and HDFS respectively.</p>
                    <p>* Internal table file security is controlled solely via HIVE. Security needs to be managed within HIVE, probably at the schema level (depends on organization).</p>
                    <a href="https://www.linkedin.com/pulse/internal-external-tables-hadoop-hive-big-data-island-amandeep-modgil">Internal & external tables in Hadoop- HIVE (the big data island)</a>

                  </div>
                  <div class="actions">
                   
                    <div class="ui positive right labeled icon button">
                      OK
                      <i class="checkmark icon"></i>
                    </div>
                  </div>
                </div>
                <#-- INCLUDE FOOTER -->
                <#include "footer.ftl">
  </div>
  <script src="./../dist/jquery.js"></script>
  <script src="./../dist/semantic.js"></script>
  <script src="./../dist/jquery.cookie.js"></script>
  <script src="./../dist/jquery.md5.js"></script>
  <script src="./../dist/jquery.base64.js"></script>
  <script src="./../dist/messenger.min.js"></script>
  <script src="./../dist/jquery.ui.widget.js"></script>
  <script src="./../dist/jquery.fileupload.js"></script>
  <script src="./../dist/progressbar.min.js"></script>
  <#-- Account -->
  <script src="./../dist/account.js"></script>
  <#-- Schedule list JS -->
      <#-- <script src="./dist/schedulelist.js"></script>-->
          <script type="text/javascript">
          var files = [];
          var percent = 0;
          var csvInfo = {};
          var bar, intervalID;
          var columnIndex = 0;
          $(function() {
              'use strict';

              // Change this to the location of your server-side upload handler:
              var url = '../table/tool/upload';
              $('#fileupload').fileupload({
                      url: url,
                      dataType: 'json',
                      drop: function(e, data) {
                          percent = 0;
                          $('#progress').progress({ percent: 0 });
                          $("#uploadTitle").html("Uploading...");
                          $(".button.fileinput-button").addClass("disabled");
                          loadDBList();
                      },
                      change: function(e, data) {
                          percent = 0;
                          $('#progress').progress({ percent: 0 });
                          $("#uploadTitle").html("Uploading...");
                          $(".button.fileinput-button").addClass("disabled");
                          loadDBList();
                      },
                      done: function(e, data) {
                          /*Upload Done*/
                          //console.log(data.files);
                          $(".button.fileinput-button").removeClass("disabled");
                          $.each(data.files, function(index, file) {
                              $("#uploadTitle").html(file.name + " Upload Succes!");

                              /*Temporarily only support one file... So clear file arr*/
                              //files=[];

                              files.push(data.result.file_name);
                          });
                          percent = 0;
                          csvInfo = data.result;
                          /*Load Schema to page*/
                          loadSchemaToPage("DEFAULT");
                          /*Update upload file list*/
                          uploadFileList();

                          /*Show schema&table block*/
                          $("#setp2").show();
                          $("#setp3").show();
                      },
                      progressall: function(e, data) {
                          var progress = parseInt(data.loaded / data.total * 100, 10);
                          //console.log(progress);
                          if (progress > percent)
                              $('#progress').progress({ percent: progress });
                          percent = progress;
                          if (progress == 100)
                              $("#uploadTitle").html("Processing files...");

                      }
                  }).prop('disabled', !$.support.fileInput)
                  .parent().addClass($.support.fileInput ? undefined : 'disabled');


          });
          function uploadFileList(){
            let fileLabel = "";
            $.each(files, function(index, name) {
                fileLabel += '<div class="ui label"><i class="mail icon"></i>' + getFileName(name) + '</div>';
            })
            $("#filelist").html(fileLabel);
          }
          function getDataType() {
              return '<select class="ui dropdown">' +
                  '<option value="string">string</option>' +
                  '<option value="bigint">bigint</option>' +
                  '<option value="int">int</option>' +
                  '<option value="double">double</option>' +
                  '<option value="boolean">boolean</option>' +
                  '</select>';
          }

          function loadDBList() {
              $.ajax({
                  url: '../table/tool/list/database',
                  beforeSend: function(request) {
                      request.setRequestHeader("Authorization", $.cookie('token'));
                  },
                  type: "GET",
                  dataType: 'json',
                  success: function(JData) {
                      if (JData["status"] != null) {
                          if (JData["status"] == "success") {
                              let html = "";
                              for (let i = 0; i < JData["data"].length; i++) {
                                  html += '<option value="' + JData["data"][i] + '">' + JData["data"][i] + '</option>';
                              }
                              $("#dbName").html(html);
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

          function createTable() {
              /*Clear Message*/
              $("#createErrorMessage .content p").html("");
              $("#createErrorMessage").hide();

              var createObj = {};
              //createObj["file_name"] = csvInfo["file_name"];
              createObj["file_name"] = files;
              createObj["csv_parser"] = $("#csvParser .item.active").attr("name");
              createObj["db_name"] = $("#dbName").val();
              createObj["table_name"] = $("#tableName").val();
              createObj["schemas"] = {};
              createObj["partitionName"]=$("#partitionSetting input").eq(0).val();
              $.each($("#tableSchema tr"), function() {
                  let tmp = $(this).children("td")
                  createObj["schemas"][$(tmp).eq(0).html()] = {
                      column_name: $(tmp).eq(1).children("div").children("input").val(),
                      column_type: $(tmp).eq(2).children("select").val(),
                      is_partition: $(tmp).eq(5).children("div").checkbox("is checked")
                  }
              })
              //console.log(JSON.stringify(createObj));
              $.ajax({
                  url: '../table/tool/create/external/table',
                  data: JSON.stringify(createObj),
                  beforeSend: function(request) {
                      request.setRequestHeader("Authorization", $.cookie('token'));
                  },
                  type: "POST",
                  dataType: 'json',
                  contentType: "application/json; charset=utf-8",
                  success: function(JData) {
                      if (JData["status"] != null) {
                          if (JData["status"] == "success") {
                              showProgress();
                          } else {
                              if (JData["message"].checkPermission()) {
                                  $("#createErrorMessage .content p").html(JData["message"]);
                                  $("#createErrorMessage").show();
                              }
                          }
                      }
                  },

                  error: function(xhr, ajaxOptions, thrownError) {
                      alert(xhr.status + "-" + thrownError);
                  }
              });
          }

          function showProgress() {
              $('.ui.basic.modal').modal('show');
              if (bar == null) {
                  bar = new ProgressBar.Circle(container, {
                      color: '#aaa',
                      // This has to be the same size as the maximum width to
                      // prevent clipping
                      strokeWidth: 4,
                      trailWidth: 1,
                      easing: 'easeInOut',
                      duration: 1400,
                      text: {
                          autoStyleContainer: false
                      },
                      from: { color: '#ffff00', width: 2 },
                      to: { color: '#01DF01', width: 3 },
                      // Set default step function for all animate calls
                      step: function(state, circle) {
                          circle.path.setAttribute('stroke', state.color);
                          circle.path.setAttribute('stroke-width', state.width);

                          var value = Math.round(circle.value() * 100);
                          if (circle.value() === 0) {
                              circle.setText('wait');
                          } else if (circle.value() === -0.001) {
                              circle.setText('fail');
                          } else {
                              circle.setText(value + "%");
                          }

                      }
                  });
              }
              bar.text.style.fontFamily = '"Raleway", Helvetica, sans-serif';
              bar.text.style.fontSize = '2rem';
              bar.animate(0); // Number from 0.0 to 1.0
              /*Clear Error Message*/
              $(".basic.modal .content").html("");
              /*Update Status Timer*/
              intervalID = setInterval(function() {
                  $.ajax({
                      url: '../table/tool/get/job/status/' + getFileListString(files),
                      beforeSend: function(request) {
                          request.setRequestHeader("Authorization", $.cookie('token'));
                      },
                      type: "GET",
                      dataType: 'json',
                      contentType: "application/json; charset=utf-8",
                      success: function(JData) {
                          if (JData["status"] != null) {
                              if (JData["status"] == "success") {
                                  let progressSum=0;
                                  let status=0;
                                  let displayMessage="";
                                  let success=true;
                                  for(let i=0;i<JData["data"].length;i++){
                                    /*Add all progress*/
                                    progressSum+=JData["data"][i]["progress"];
                                    /*Update Status*/
                                    if(JData["data"][i]["status"]==1&&status==0){
                                      status=JData["data"][i]["status"];
                                    }else if(JData["data"][i]["status"]==3){
                                      status=JData["data"][i]["status"];
                                      displayMessage+=JData["data"][i]["message"]+"<br>";
                                    }
                                    success=JData["data"][i]["status"]==2&&success;
                                  }
                                  status=success?2:status;
                                  bar.animate((progressSum/JData["data"].length) / 100);
                                  switch (status) {
                                      case 0:
                                          $("#insertStatus").html("Waiting for Upload Data to HDFS");
                                          break;
                                      case 1:
                                          $("#insertStatus").html("Upload Data to HDFS is Runing");
                                          break;
                                      case 2:
                                          if((progressSum/JData["data"].length)==100){
                                            $("#insertStatus").html("Upload Data to HDFS has been Successful");
                                            clearInterval(intervalID);
                                            $("#setp2").hide();
                                            $("#setp3").hide();
                                            $("#fileupload").find(".files").empty();
                                            $("#uploadTitle").html("Please select the CSV file");
                                            $('#progress').progress({ percent: 0 });
                                            //Clear file list
                                            files=[];
                                            uploadFileList();
                                          }
                                          break;
                                      case 3:
                                          $("#insertStatus").html("Upload Data to HDFS has Fail");
                                          clearInterval(intervalID);
                                          bar.animate(-0.001);
                                          if (displayMessage!= "")
                                              $(".basic.modal .content").html(displayMessage);
                                          break;
                                  }
                              } else {
                                  if (JData["message"].checkPermission()) {
                                      clearInterval(intervalID);
                                      alert("[" + JData["status"] + "]\n" + JData["message"]);
                                  }
                              }
                          }
                      },

                      error: function(xhr, ajaxOptions, thrownError) {
                          clearInterval(intervalID);
                          alert(xhr.status + "-" + thrownError);
                      }
                  });
              }, 4000)
          }
          function getFileListString(filesList){
            return encodeURIComponent(filesList.join(","));
          }
          function loadSchemaToPage(csvParser){
            $("#tableSchema").html("");
            columnIndex=0;
            if(csvInfo[csvParser]!==null&&csvInfo[csvParser].length>0){
              csvInfo[csvParser][0].map(function(value,index){
                var html = "<tr>";
                html += '<td id="columnIndex' + columnIndex + '">' + (columnIndex) + "</td>";
                html += '<td><div class="ui input fluid"><input name="column-name" placeholder="Column Name" type="text" value="'+value+'"></div></td>';
                html += "<td>" + getDataType() + "</td>";
                html+="<td>"+(csvInfo[csvParser][1][index]!==null?csvInfo[csvParser][1][index]:"")+"</td>";
                html+="<td>"+(csvInfo[csvParser][1][index]!==null?csvInfo[csvParser][1][index]:"")+"</td>";
                html += '<td><div class="ui fitted toggle checkbox"><input type="checkbox"><label></label></div></td>';
                html += '<td><button id="removeBtn' + columnIndex + '" class="ui icon button fullwidth red" onclick="removeColumn(' + columnIndex + ')"><i class="remove icon"></i></button></td>';
                html += "</tr>";
                $("#tableSchema").append(html);                
                columnIndex++;
              })
            }
            $("#tableSchema .ui.checkbox").checkbox({onChange:function(){                  
                  partitionCheck($(this).parent().parent().parent().children("td").eq(0).html());
              }});
          }
          function addColumn() {
              var html = "<tr>";
              html += '<td id="columnIndex' + columnIndex + '">' + (columnIndex) + "</td>";
              html += '<td><div class="ui input fluid"><input name="column-name" placeholder="Column Name" type="text"></div></td>';
              html += "<td>" + getDataType() + "</td>";
              html+="<td></td>";
              html+="<td></td>";
              html += '<td><div class="ui fitted toggle checkbox"><input type="checkbox"><label></label></div></td>';
              html += '<td><button id="removeBtn' + columnIndex + '" class="ui icon button fullwidth red" onclick="removeColumn(' + columnIndex + ')"><i class="remove icon"></i></button></td>';
              html += "</tr>";
              $("#tableSchema").append(html);
              $("#tableSchema .ui.checkbox").checkbox({onChange:function(){
                  partitionCheck($(this).parent().parent().parent().children("td").eq(0).html());
              }});              
              columnIndex++;
          }
          function partitionCheck(index){              
              let firstClick=-1;
              let buffer=[];
              $.each($("#tableSchema .checkbox"),function(i,checkbox){            
                if($(checkbox).checkbox("is checked")){
                  if(i<index){
                    $(checkbox).checkbox("set unchecked");
                  }                  
                }else{
                  if(i>index){
                    $(checkbox).checkbox("set checked");
                  }
                }

                if($(checkbox).checkbox("is checked")){
                  buffer.push($(checkbox).parent().parent().children("td").eq(1).children(".input").children("input").val().toLowerCase()+"="+$(checkbox).parent().parent().children("td").eq(3).html());
                }          
              });
              console.log(buffer);

              //Display Partition 
              let partitionSetHtml="";
              files.map(function(value){
                partitionSetHtml+='<div class="ui labeled input" style="width:100%;margin-top:10px;margin-bottom:10px"><div class="ui label">'+value+'</div><input placeholder="partition" type="text" value="'+buffer.join("/")+'"></div>';
              });
              $("#partitionSetting").html(partitionSetHtml);

          }
          function removeColumn(index) {
              $("#removeBtn" + index).parent().parent().remove();
              if (index != columnIndex) {
                  for (let i = 0; i < columnIndex - index; i++) {
                      $("#columnIndex" + (index + i + 1)).attr("id", "columnIndex" + (index + i));
                      $("#removeBtn" + (index + i + 1)).attr("id", "removeBtn" + (index + i));
                      $("#removeBtn" + (index + i)).attr("onclick", "removeColumn(" + (index + i) + ")");
                      $("#columnIndex" + (index + i)).html(index + i);

                  }
              }
              columnIndex--;
          }

          function getFileName(fileName) {
              return fileName.substring(fileName.indexOf("#") + 1, fileName.length);
          }
          </script>
</body>

</html>