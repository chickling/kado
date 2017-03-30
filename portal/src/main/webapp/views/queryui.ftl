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

  <link rel="stylesheet" type="text/css" href="dist/semantic.css">
  <link rel="stylesheet" type="text/css" href="dist/scroll.css" />


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
  .ui.modal.sample{    
    top:0px;
    margin-top:3em !important; 
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
      .ui.menu.vertical.inverted.sticky.schema > a{
        padding-top: 5px;
        padding-bottom: 5px;
        padding-left: 5px;
      }
      .dropdown.partition{
        width:180px !important;
        margin-left: 10px !important;
        margin-top: 10px!important;
        margin-bottom: 10px !important;
      }
      .dropdown.prestotable{
        width:180px !important;
        margin-left: 10px !important;        
      }
      .search.dropdown > .menu{
        width:280px !important;
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
        <div class="ui vertical inverted sticky menu" style="left: 0px; top: 0px; width: 200px ! important; ">
          <div class="item" >            
            <a ><h4><i class="icon terminal large"></i>Run Pretso Job</h4></a>

          </div>
          
          <div class="ui fluid search selection dropdown prestotable">
            <input value="" name="presto_table" type="hidden">
            <i class="dropdown icon"></i>
            <div class="default text">Select Table</div>
            <div class="menu">  
              <div class="item" data-value="NODATA">NO DATA</div>
            </div>
          </div>
          <div class="ui fluid search selection dropdown partition">
            <input value="" name="presto_table" type="hidden">
            <i class="dropdown icon"></i>
            <div class="default text">Select Partition</div>
            <div class="menu">  
              <div class="item" data-value="NODATA">NO DATA</div>
            </div>
          </div>
          <a class="item" id="tableSampleData">
            <b><i class="icon star"></i>Show Sample Data</b>
          </a>
          <#-- <a class="item" id="tableCount" tablename="">
            <b><i class="icon flag"></i>Count Table Row</b>
          </a> -->
          
          <a class="item" style="margin-bottom: -20px;text-align:center" id="tableSchema">
            
            <b> Table Schema</b>
            <i class="icon expand" style="float:right"></i>
          </a>
        </div>
        <div class="ui flowing popup top left transition hidden count" >
          <div class="ui active inverted dimmer">
                <div class="ui loader text "></div>
              </div>
          <div class="ui one column divided center aligned grid">
            <div class="column countcard"  style="max-width: 200px;overflow: auto;">
              
              <h4 class="ui header countcard"><i class="icon suitcase"></i>Table Name</h4>
              <p><label class="ui label blue">0</label></p>             
            </div>   
          </div>
        </div>
        <div class="ui menu vertical inverted sticky schema" style="height:100%;width: 200px ! important;overflow-y:auto;max-height:600px;overflow-x:hidden;">

        </div>

      </div>
      <div class="ui main"  style="width: 100%;">
        <div id="editor_form"><pre id="editor" style="margin-top: 0px;"></pre><div class="ui dimmer sql">
          <div class="content">
            <div class="center">
             <div class="ui loader text large">Submit Job...</div>

           </div>
         </div>
       </div>
       <div class="ui dimmer inverted height">
          <div class="content">
            <div class="center">
             <h3><font color="#000000">Editer Dispaly Line Number</font></h3>
             
              <div class="ui action input line number">
                <input placeholder="" type="text">                
                <div class="ui hide button" onclick="decLine()"><i class="minus icon" style="margin-left: 0px; margin-right: 0px;"></i></div>
                <div class="ui show button" onclick="addLine()"><i class="plus icon" style="margin-left: 0px; margin-right: 0px;"></i></div>
              </div>
           </div>
         </div>
       </div>
     </div>
     <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
      <div class="ui show button primary basic" style="padding-left: 13px; padding-right: 13px;float: left; " onclick="hideOrShowTaskBar()"><i class="resize horizontal icon" style="margin-left: 0px; margin-right: 0px;"></i></div>
      <div class="ui show button primary basic" style="padding-left: 13px; padding-right: 13px;float: left; " onclick="showLineNumberSetting()"><i class="resize vertical icon" style="margin-left: 0px; margin-right: 0px;"></i></div>
      <button class="ui labeled icon  button  vk"  onclick="pushToSaveJob()">
        <i class="save icon"></i>
        Save
      </button>
      <button class="ui labeled icon primary button "  onclick="runPrestoSQL();">
        <i class="rocket icon"></i>
        Run Presto SQL
      </button>
    </div>
    <div class="jobfunc" >
      <h3>Presto SQL Runing History</h3>
      <div id="log"></div>
    </div> 
    <table class="ui celled table query  ">
      <thead>
        <tr>
          <th style="width:60px;">ID</th>
          <th style="width:500px" class="sqlhead">SQL</th>
          <th style="width:100px;">User</th>
          <th style="width:160px;">Start Time</th>    
          <th style="width:5%;">Runing Time</th>
          <th style="width:130px;">Staus</th>
          <th>Progress</th>
          <th stylek="width:130px;">Function</th>
        </tr></thead>
        <tbody>
         
        </tbody>
        <tfoot>
          <tr><th colspan="8">
            <div class="ui right floated pagination menu pagenav">
              <a class="icon item">
                <i class="left chevron icon"></i>
              </a>
                     
              <a class="icon item">
                <i class="right chevron icon"></i>
              </a>
            </div>
          </th>
        </tr></tfoot>
      </table> 
    </div>
  </div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
</div>
<!-- LOG MODAL -->
<div class="ui modal log">
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
<!-- SCHEMA MODAL -->
<div class="ui modal schema">
  <i class="close icon"></i>
  <div class="header">
    Profile Picture
  </div>
  <div class=" content">    
    <div class="description">      
      <table class="ui celled padded table schema compact">
        <thead>
          <tr>
            <th>#</th>
            <th>Column Name</th>
            <th>Type</th>
            <th>Partition</th>  
            <th style="width:50px">Insert</th>  
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
    </div>
  </div>
  <!-- SHOW SAMPLE DATA MODAL -->
  <div class="ui modal sample fullscreen">
    <i class="close icon"></i>
    <div class="header">
      SAMPLE DATA
    </div>
    <div class="content">  
      <div class="ui active inverted dimmer" id="sampleDataLoader">
        <div class="ui text loader">Loading</div>
      </div>  
      <div class="description" style="overflow-y:auto;max-height:500px;overflow-x:auto;">      
        <table class="ui celled padded table schema compact" >
          <thead>          
          </thead>
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
    </div>
  </div>
  <script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/jquery.base64.js"></script>
  <script src="dist/messenger.min.js"></script>
  <!-- load ace -->
  <script src="dist/ace-src/ace.js"></script>
  <!-- load ace language tools -->
  <script src="dist/ace-src/ext-language_tools.js"></script>
  <#-- Account -->
  <script src="dist/account.js"></script>
  <!-- load Query UI JS -->
  <script src="dist/queryui.js"></script>
</body>

</html>
