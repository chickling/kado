<!DOCTYPE html>
<html>
<head>
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properities -->
  <title>Anti-Crawler Dashboard</title>

  <link rel="stylesheet" type="text/css" href="dist/semantic.css">
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
      .urlbuilder{
        top:5% !important;
      }
      .sqlTemplate{
        margin-top: 10px;
        margin-right: 10px;         
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
      <div class="toc" style="min-height: 100%;">
        <div class="ui vertical inverted sticky menu" style="left: 0px; top: 0px; width: 200px ! important; height: 100% ! important;">
          <div class="item" >            
            <a ><h4><i class="icon terminal large"></i>Job List</h4></a>
           
          </div>
          <a class="item job self active" onclick="getSelf();">
            <b><i class="icon star"></i>My Job</b>
          </a>
          <a class="item job other" onclick="getOther();">
            <b><i class="icon bookmark"></i>Other User Job</b>
          </a>
        </div>
      </div>
      <div class="ui main"  style="width: 100%;">
        
        <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
            
            
            <a class="ui labeled icon primary button " id="drop" href="joblist/add">
              <i class="plus icon"></i>
              Add Job
            </a>
          </div>
        <div class="jobfunc" >
          <h3>Job List</h3>
          <div id="log"></div>
        </div> 
  <table class="ui celled table job selectable ">
  <thead>
    <tr>
    <th style="width:80px;">Job ID</th>
    <th>Job Name</th>
    <th style="width:100px;">User</th>
    <th style="width:160px;">Last Run Time</th>    
    <th style="width:10%;">Last Runing Time</th>
    <th style="width:115px;">History</th>
    <th style="width:250px;">Function</th>
  </tr></thead>
  <tbody>    
    
  </tbody>

</table> 

      </div>
    </div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
  </div>

  <!-- HISTORY MODAL -->
  <div class="ui modal job history fullscreen">
    <i class="close icon"></i>
    <div class="header">
      Job History
    </div>
    <div class="content">
      
      <div class="description">
        <table class="ui celled table schedule">
          <thead>
            <tr>
              <th style="width:80px;">Run ID</th>
              <th style="width:80px;">job ID</th>
              <th>job name</th>
              <th style="width:100px;">User</th>
              <th style="width:130px;">Job Status</th>
              <th style="width:160px;">Start Time</th>    
              <th style="width:10%;">Runing Time</th>
              <th style="width:160px;">Stop Time</th>  
                       
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
        <div class="ui button view"><i class="icon unhide"></i>View Job</div>
        <div class="ui button orange edit"><i class="icon write"></i>Edit Job</div>
        <div class="ui button purple log"><i class="icon history"></i>View Job Log</div>
      </div>   
    </div>
  </div>
    </div>
  </div>
  <!-- URL BUILDER MODAL -->
  <div class="ui modal urlbuilder">
    <i class="close icon"></i>
    <div class="header">
      URL Builder
    </div>
    <div class="content">      
      <div class="description">
      <div class="ui icon ignored message">
        <i class="linkify icon"></i>
        <div class="content">
          <div class="header">
            Use the URL directly to execute job
          </div>
          <p>You can use the URL running real-time query,and you can specify the next step after completion.</p>
        </div>
      </div>
        <div class="ui header">Next Step after completion</div>
        <div style="margin-top:10px;">
          <i class="icon grid layout big" style="margin-right: 8px;"></i>
          <div class="ui  checkbox" id="resulttable">
            <input  name="table"  type="checkbox" value="1" tabindex="0" class="hidden">
            <label style="width:180px;">Show Result Table</label>
          </div>         
        </div>
        <div style="margin-top:20px;" id="drawchart_panel">
          <i class="icon area chart big" style="margin-right: 8px;"></i>
          <div class="ui  checkbox" id="drawchart">
            <input name="storage"  type="checkbox" value="2" tabindex="0" class="hidden">
            <label style="width:150px;">Draw Chart</label>
          </div>          
          <div class="ui right action left input labeled charts disabled" style="width:45%;">
            <div class="ui label">
              <i class="icon checkmark"></i>
              Usable Chart
            </div>
            <div class="ui basic floating dropdown button charts" tabindex="-1">
              <div class="text">Select Chart</div>
              <i class="dropdown icon"></i>
              <div class="menu" tabindex="-1">
                               
              </div>
            </div>
          </div> 
                  
        </div>
      <div class="ui header">SQL Template Setting</div>
      <div id="sqlTemplate">
        
      </div>
      <div class="ui header">URL</div>
      <textarea  class="ui icon ignored message urlcontent">
        http://
      </textarea >
      <div class="ui icon button blue" id="copytoclipboard">
        Copy To Clipboard
        <i class="copy icon"></i>
      </div>
      <div class="ui icon button orange" id="gotourl">
        Go To URL
        <i class="arrow right icon"></i>
      </div>
      </div>
    </div>
    <div class="actions">
      
      <div class="ui positive right labeled icon button close">
        Close
        <i class="checkmark icon"></i>
      </div>
    </div>
  </div>
  <script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <!-- load ace -->
  <script src="dist/ace-src/ace.js"></script>
  <!-- load ace language tools -->
  <script src="dist/ace-src/ext-language_tools.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/messenger.min.js"></script>
  <#-- Account -->
  <script src="dist/account.js"></script>
  <#-- Job List JS -->
  <script src="dist/joblist.js"></script>
  </body>

  </html>
