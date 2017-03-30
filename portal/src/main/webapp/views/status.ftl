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

<link href="dist/vis.css" rel="stylesheet" type="text/css" />
  <link rel="stylesheet" type="text/css" href="dist/semantic.css">
  <link href="dist/daterangepicker.css" rel="stylesheet" type="text/css">
  <link href="dist/messenger.css" rel="stylesheet" type="text/css">
  <link href="dist/messenger-theme-air.css" rel="stylesheet" type="text/css">
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
    margin: 5em 0em 0em;
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
  .ui.ignored.warning.message.runing.log{
    min-height: 600px;
  }
  .ui.main{
    min-height: 800px;
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
    <div class="ui top  attached progress" data-percent="100" id="pg" style="margin-top:50px">
      <div class="bar"></div>
    </div>
    
    <!-- Site content !-->
    <div class="full height">
      
      <div class="ui main"  style="width: 100%;">
        
        <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
            
            <a class="ui labeled icon primary button filter"  onclick="HistoryFilter()">
              <i class="filter icon"></i>
              History Filter
            </a>
            
          </div>

        <div class="jobfunc">

          <h3>Runing List</h3>
          <div id="log"></div>
        </div> 
  <table class="ui celled table runlist selectable ">
  <thead>
    <tr>
    <th style="width:80px;">ID</th>
    <th style="width:80px;">Job ID</th>
    <th>Name</th>
    <th style="width:80px;">Type</th>
    <th style="width:100px;">User</th>

    <th style="width:160px;">Start Time</th>    
    <th style="width:10%;">Runing Time</th>
    <th style="width:130px;">Progress</th>
    <th style="width:140px;">Function</th>
  </tr></thead>
  <tbody>
    
  </tbody>
  
</table> 
<div class="jobfunc">
   <h3>History List</h3>
          <div id="log"></div>
        </div> 
  <table class="ui celled table historylist selectable ">
  <thead>
    <tr>
    <th style="width:80px;">ID</th>
    <th style="width:80px;">Job ID</th>
    <th>Name</th>
    <th style="width:80px;">Type</th>
    <th style="width:100px;">User</th>
    <th style="width:120px;">Status</th>
    <th style="width:160px;">Start Time</th>    
    <th style="width:10%;">Runing Time</th>
    <th style="width:130px;">Progress</th>
    <th style="width:140px;">Function</th>
  </tr></thead>
  <tbody>
   
  </tbody>
  <tfoot>
    <tr><th colspan="10">
      <div class="ui right floated pagination menu pagenav">
        <a class="icon item" onclick="">
          <i class="left chevron icon"></i>
        </a>
           
        <a class="icon item" onclick="">
          <i class="right chevron icon"></i>
        </a>
      </div>
    </th>
  </tr></tfoot>
</table> 
</div>
<div id="visualization"></div>
      </div>
    </div>
    
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">

  </div>
  <!-- LOG MODAL -->
  <div class="ui modal fullscreen">
  <i class="close icon"></i>
  <div class="header">
    Job Log
  </div>
  <div class="content">
    
    <div class="description">
    <h3 class="jobname"></h3>
    <div class="ui header">Result</div>
      <div class="ui labeled  button result">
        <div class="ui button">
          <i class="browser icon"></i>
          View Result   
        </div>
        <a class="ui basic label resultcount left pointing">
          ?
        </a> 
      </div>
      <div id="viewchart">
        
      </div>
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
            <td class="log"><label class="ui label"></label></td>
            <td >Result Count</td>
            <td class="ResultCount"><label class="ui label"></label></td>
            <td >Presto ID</td>
            <td class="presto_id"></td>
          </tr>
        </tbody>
      </table>
      
      <div class="ui header">Runing Log</div>
      <div class="ui ignored warning message runing log">
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
<#-- FILTER -->
<div class="ui basic modal">
  <i class="close icon"></i>
  <div class="header">
    History Filter
  </div>
  <div class="image content">
    <div class="image">
      <i class="filter icon"></i>
    </div>
    <div class="description">
      <p>Use the time range filter:</p>
      <div class="ui  icon input labeled">
        <div class="ui label">
          <i class="icon play"></i>
          Start
        </div>
        <input type="text" class="" name="reservation" id="startwith">
        <i class="calendar icon "></i>
      </div>
      <div class="ui  icon input labeled">
        <div class="ui label">
          <i class="icon stop"></i>
          End 
        </div>
        <input type="text" class="" name="reservation" id="endwith">
        <i class="calendar icon "></i>
      </div>    
    <p>
      <div class="ui checkbox toggle onlyme">
          <input type="checkbox" tabindex="0" class="hidden">
          <label><font color="#FFFFFF">Only show my Job</font></label>
      </div>
    </p>
    <div style="height:150px"></div>
     <#--  </div> -->
    </div>
  </div>
  <div class="actions">
    
      <div class="ui red basic inverted button" onclick="clearFilter()">
        <i class="remove icon"></i>
        Clear Filter
      </div>
      <div class="ui green basic inverted button" onclick="setFilter()">
        <i class="checkmark icon"></i>
        Filter
      </div>
    
  </div>
</div>


  <script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <!-- load ace -->
  <script src="dist/ace-src/ace.js"></script>
  <!-- load ace language tools -->
  <script src="dist/ace-src/ext-language_tools.js"></script>
  <script src="dist/vis.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/jquery.base64.js"></script>
  <script src="dist/moment.js"></script>
  <script src="dist/daterangepicker.js"></script>
  <script src="dist/messenger.min.js"></script>
  <#-- Account -->
  <script src="dist/account.js"></script>
  <#-- Status JS -->
  <script src="dist/status.js"></script>
  </body>

  </html>
