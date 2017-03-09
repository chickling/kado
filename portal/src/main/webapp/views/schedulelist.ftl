<!DOCTYPE html>
<html>
<head>
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properities -->
  <title>Kado Dashboard</title>

<link href="./dist/vis.css" rel="stylesheet" type="text/css" />
  <link rel="stylesheet" type="text/css" href="./dist/semantic.css">

  <link href="./dist/messenger.css" rel="stylesheet" type="text/css">
  <link href="./dist/messenger-theme-air.css" rel="stylesheet" type="text/css">

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
  #editor { 
    margin: 0;
    height: 400px;
        /*position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;*/
      }
      .full.height > .toc {
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
      .jobfunc{
        padding-left: 20px;
        padding-top: 20px;
      }
      .ui.container{
        width: 95%;
      }
      .ui.basic.button.mini{
        width: 100%;
      }
      
      </style>

    </head>
    <body>



  <#-- INCLUDE NAV -->
  <#include "nav.ftl">
  <div class="pusher">
    <div class="ui top  attached progress"  id="pg" style="margin-top:50px">
      <div class="bar"></div>
    </div>
    
    <!-- Site content !-->
    <div class="full height">
      <div class="toc" style="min-height: 100%;">

        <div class="ui vertical inverted sticky menu" style="left: 0px; top: 0px; width: 200px ! important; height: 100% ! important;">
          <div class="item" >            
            <a><h4><i class="icon terminal large"></i>Schedule List</h4></a>
           
          </div>
          <a onclick="getSelf();" class="item active self schedule">
            <b><i class="icon star"></i>My Schedule</b>
          </a>
          
          <a onclick="getOther();" class="item other schedule">
            <b><i class="icon bookmark"></i>Other User Schedule</b>
          </a>
        </div>
      </div>
      <div class="ui main"  style="width: 100%;">
        <div id="visualization"></div>
        <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
            
            <a class="ui labeled icon primary button " id="drop" href="schedulelist/add">
              <i class="plus icon"></i>
              Add Schedule
            </a>
            
          </div>
        <div class="jobfunc" >

          <h3>Schedule List</h3>
          <div id="log"></div>
        </div> 
  <table class="ui celled table schedule list selectable">
  <thead>
    <tr>
    <th style="width:80px;"> ID</th>
    <th>Schedule Name</th>
    <th style="width:100px;">User</th>
    <th style="width:160px;">Start Time</th>    
    <th style="width:10%;">Runing Time</th>
    <th style="width:105px;">Status</th>
    <th style="width:260px;">Function</th>
  </tr></thead>
  <tbody>
  
  </tbody>

</table> 

      </div>
    </div>
    <#-- INCLUDE FOOTER -->
    <#include "footer.ftl">
  </div>
  <!-- LOG MODAL -->
  <div class="ui modal schedule history fullscreen">
  <i class="close icon"></i>
  <div class="header">
    Schedule History
  </div>
  <div class="content">
    
    <div class="description" style="overflow-y:auto;max-height:500px;overflow-x:auto;">
      <table class="ui celled table schedule ">
        <thead>
          <tr>
            <th style="width:80px;">Run ID</th>
            <th style="width:80px;">Sch ID</th>
            <th>Schedule Name</th>
            <th style="width:100px;">User</th>
            <th style="width:160px;">Start Time</th>    
            <th style="width:10%;">Runing Time</th>
            <th style="width:105px;">Stop Time</th>  
            <th style="width:105px;">Run Job ID</th>  
            <th style="width:80px;">ScheduleLog</th>         
          </tr></thead>
          <tbody>

          </tbody>
        </table>
      </div>
    </div>
  <div class="actions">
    
    <div class="ui positive right labeled icon button">
      OK
      <i class="checkmark icon"></i>
    </div>
    <div class="ui flowing popup top left transition hidden">
  <div class="ui one column divided center aligned grid">
    <div class="column jobcard">
      <h4 class="ui header jobcard"><i class="icon suitcase"></i>Job Name</h4>
      <p>Memo</p>
      <div class="ui button view disabled"><i class="icon unhide"></i>View Job</div>
      <div class="ui button orange edit"><i class="icon write"></i>Edit Job</div>
      <div class="ui button purple log"><i class="icon history"></i>View Job Log</div>
    </div>   
  </div>
</div>
  </div>
</div>
<!-- LOG MODAL -->
  <div class="ui modal fullscreen log">
  <i class="close icon"></i>
  <div class="header">
    Job Log
  </div>
  <div class="content">
    
    <div class="description">
    <h3 class="jobname"></h3>
    <h4>Run SQL</h4>
    <div class="ui ignored warning message jobsql">
      <p>select * from truesight_page_orc where cs_user_agent='';</p>  
    </div>

      <div class="ui header">Info</div>
      <table class="ui selectable inverted table celled ">
        <tbody>
          <tr>
            <td >Status</td>
            <td class="job_status"><label class="ui label"></label></td>
            <td >User</td>
            <td class="user"><label class="ui label"></label></td>
            <td >Level</td>
            <td class="jobLevel"><label class="ui label"></label></td>
          </tr>
          <tr>
            <td >Start Time</td>
            <td class="start_time"><label class="ui label"></label></td>
            <td >End Time</td>
            <td class="stop_time"><label class="ui label"></label></td>
            <td >Runing Time</td>
            <td class="runingtimes"><label class="ui label"></label></td>
          </tr>
          <tr>
            <td >Storage</td>
            <td class="storage"><label class="ui label"></label></td>
            <td >Save Type</td>
            <td class="save_type"><label class="ui label"></label></td>
            <td >Location ID</td>
            <td class="location_id"><label class="ui label"></label></td>
          </tr>
          <tr>
            <td >Log</td>
            <td class="log job"><label class="ui label"></label></td>
            <td >Result Count</td>
            <td class="ResultCount"><label class="ui label"></label></td>
            <td >Presto ID</td>
            <td class="presto_id"></td>
          </tr>
        </tbody>
      </table>
      
      <div class="ui header">Result</div>
      <div class="ui labeled icon button result">
      <i class="browser icon"></i>
        View Result      
      </div>
    </div>
  </div>
  <div class="actions">
    
    <div class="ui positive right labeled icon button">
      OK
      <i class="checkmark icon"></i>
    </div>
  </div>
</div>
<!-- Schedule LOG MODAL -->
<div class="ui modal schlog">
  <i class="close icon"></i>
  <div class="header">
    Job Log
  </div>
  <div class="content">

    <div class="description">
      <div class="ui ignored warning message log runing">
        <p></p>  
      </div>
      
      
    </div>
  </div>
  <div class="actions">

    <div class="ui positive right labeled icon button">
      OK
      <i class="checkmark icon"></i>
    </div>
  </div>  
</div>

  <script src="./dist/jquery.js"></script>
  <script src="./dist/semantic.js"></script>
  <!-- load ace -->
  <script src="./dist/ace-src/ace.js"></script>
  <!-- load ace language tools -->
  <script src="./dist/ace-src/ext-language_tools.js"></script>
  <script src="./dist/vis.js"></script>
  <script src="./dist/jquery.cookie.js"></script>
  <script src="./dist/jquery.md5.js"></script>
  <script src="./dist/jquery.base64.js"></script>
  <script src="./dist/messenger.min.js"></script>  
  <script src="./dist/daterangepicker.js"></script>
  <#-- Account -->
  <script src="./dist/account.js"></script>
  <#-- Schedule list JS -->
  <script src="./dist/schedulelist.js"></script>
  </body>

  </html>
