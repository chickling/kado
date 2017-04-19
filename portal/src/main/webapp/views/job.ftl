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
    <link rel="stylesheet" type="text/css" href="../dist/components/reset.css">
    <link rel="stylesheet" type="text/css" href="../dist/components/site.css">
    <link rel="stylesheet" type="text/css" href="../dist/semantic.css">
    <!--Email input tag CSS-->
    <link rel="stylesheet" type="text/css" href="../dist/multiple-emails.css">
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
    #editor {
    margin: 0;
    height: 300px;
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
    padding-right: 20px;
    padding-top: 20px;
    padding-bottom: 20px;
    }
    .ui.container{
    width: 95%;
    }
    </style>
  </head>
  <body>
  <#-- INCLUDE NAV -->
  <#include "nav.ftl">
    <div class="pusher" style="margin-top:47px">
      <!-- Site content !-->
      <div class="full height">
        <div class="toc" style="min-height: 100%;">
          <div class="ui vertical inverted sticky menu" style="left: 0px; top: 0px; width: 200px ! important; height: 100% ! important;">
            <div class="item" >
              <a ><h4>
                <i class="large icons" style="margin-right: 10px;">
                <i class="suitcase icon"></i>
                <i class="corner add icon"></i>
                </i>
                Job Add</h4>
              </a>
            </div>
            <#-- <div class="ui input item small ">
              <input placeholder="Search..." type="text">
            </div> -->
            <#-- <a class="item">
              <b><i class="icon level down"></i> From Saved Queries</b>
            </a> -->
            <a class="item" href="../queryui">
              <b><i class="icon compress"></i>Open Query UI</b>
            </a>
          </div>
        </div>
        <div class="ui main"  style="width: 100%;">
          <div style=" background-color: #EBEBEB; padding: 7px; text-align: right;">
            <h3 style="float:left;margin-left: 10px; margin-top: 5px;">Job SQL</h3>
            <button class="ui labeled icon primary button " onclick="pushToQueryUI();" id="drop">
            <i class="rocket icon"></i>
            Test in Query UI
            </button>
            <a class="ui labeled icon  button  vk" href="../joblist#back">
              <i class="remove icon"></i>
              Cancel
            </a>
            <button class="ui labeled icon  button  vk" onclick="saveJob();" >
            <i class="save icon"></i>
            Save Job
            </button>
          </div>
        <div id="editor_form"><pre id="editor" style="margin-top: 0px;"></pre><div class="ui dimmer">
        <div class="content">
          <div class="center">
            <div class="ui loader text large">Submit Job...</div>
          </div>
        </div>
      </div>
    </div>
    <div class="jobfunc" >
      <h3>Job Setting</h3>
      <div id="log"></div>
      <div class="ui styled fluid accordion">
        <div class="title active">
          <i class="dropdown icon"></i>
          Basic Setting
        </div>
        <div class="content active">
          <form class="ui form job basic">
            <div class="two fields">
              <div class="field">
                <label>Job Name</label>
                <input  placeholder="Job Name..." type="text" name="jobname">
              </div>
              <div class="field">
                <label>Job Visibility Level</label>
                <div class="ui selection dropdown level">
                  <input name="jobLevel" type="hidden">
                  <i class="dropdown icon"></i>
                  <div class="default text">Level</div>
                  <div class="menu">
                    <div class="item" data-value="0"><i class="icon lock"> </i>Private<br></div>
                    <div class="item" data-value="1"><i class="icon world"> </i>Public </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="field">
              <label>Job Memo</label>
              <input name="memo" placeholder="Memo..." type="text">
            </div>
            <div class="field">
              <div class="ui checkbox toggle">
                <input class="hidden" tabindex="0" type="checkbox" name="notification">
                <label>Failure Notification Me</label>
              </div>
            </div>
            <#-- <div class="two fields">
              <div class="field">
                <label>How many hours ago?</label>
                <input  placeholder="24" type="text" name="replace_value">
              </div>
              <div class="field">
                <label>Search Value</label>
                <input  placeholder="$last 24 hour$" type="text" name="replace_sign">
              </div>
            </div> -->
            <div class="ui card"  style="width: 100%;">
              <div class="content">
                <div class="header" style="margin-top: 0px;"><div class="ui checkbox toggle">
                <input class="hidden" tabindex="0" type="checkbox" name="Report">
                <label>Send Report</label>
              </div></div>
                <div class="meta">This function wolud send the report made by  query results to the setting email addresses.</div>
                <div class="description">
                  <div class="field">
                  <label>Email List</label>
                  <input type='text' id='ReportEmail' name='ReportEmail' class='form-control' value=''>
                  </div>
                  <div class="three fields">
              <div class="field">
                <label>Report Title</label>
                <input  placeholder="Title..." type="text" name="ReportTitle">
              </div>
              <div class="field">
                <label>Report Max Row</label>
                <div class="ui selection dropdown level">
                  <input name="ReportLength" type="hidden">
                  <i class="dropdown icon"></i>
                  <div class="default text">Max Row</div>
                  <div class="menu">
                    <div class="item" data-value="0"><i class="icon list layout"> </i>0</div>
                    <div class="item" data-value="100"><i class="icon list layout"> </i>100</div>
                    <div class="item" data-value="200"><i class="icon list layout"> </i>200</div>
                    <div class="item" data-value="300"><i class="icon list layout"> </i>300</div>
                    <div class="item" data-value="1000"><i class="icon list layout"> </i>1000</div>
                  </div>
                </div>
              </div>
              <div class="field">
                <label>Added Information</label>
                <div class="ui selection dropdown level">
                  <input name="ReportFileType" type="hidden">
                  <i class="dropdown icon"></i>
                  <div class="default text">Level</div>
                  <div class="menu">
                    <div class="item" data-value="0"><i class="icon minus circle"> </i>None</div>
                    <div class="item" data-value="1"><i class="icon linkify"> </i>CSV Download Link</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="field">
              <div class="header" style="margin-top: 0px;"><div class="ui checkbox toggle">
                <input class="hidden" tabindex="0" type="checkbox" name="ReportWhileEmpty">
                <label>Report While Empty</label>
              </div></div>
            </div>
                </div>
                
              </div>
            </div>
          </form>
        </div>
        <div class="title active">
          <i class="dropdown icon"></i>
          Results storage
        </div>
        <div class="content active">

          <div style="margin-top:10px;">
            <i class="icon disk outline big" style="margin-right: 8px;"></i>
            <div class="ui  checkbox csv storage">
              <input name="storage"  type="checkbox" value="1">
              <label style="width:180px;">Save CSV File in LocalFS</label>
            </div>
            <div class="ui left icon input file fpath">
              <input placeholder="File Path" type="text" id="labelname">
              <i class="folder outline icon"></i>
            </div>
            <div class="ui  icon input right labeled file fname">
              <input placeholder="File Name" type="text" id="labelname">
              
              <div class="ui label">
                @{Timestamp}.csv
              </div>
            </div>
          <p style="text-align: right;">Local File Output Path:${localpath}</p>
          </div>
          <div style="margin-top:10px;" id="save_to_db">
            <i class="icon database big" style="margin-right: 8px;"></i>
            <div class="ui  checkbox db storage">
              <input name="storage"  type="checkbox"  value="2">
              <label style="width:180px;">Save to Database</label>
            </div>
            
            <div class="ui right action left input labeled insert sql" style="width:75%;">
              <div class="ui label">
                <i class="icon terminal"></i>
                SQL
              </div>
              <input placeholder="insert into..." type="text">
              <div class="ui basic floating dropdown button location db">
                <div class="text">Replace by index</div>
                <i class="dropdown icon"></i>
                <div class="menu">
                  <div class="item">Replace by index</div>
                  <#-- <div class="item">Replace by column name</div> -->
                </div>
              </div>
            </div>
            
          </div>
        </div>
        <div class="title active">
          <i class="dropdown icon"></i>
          SQL Template
        </div>
        <div class="content active" style="text-align:right">
        <button class="ui right labeled icon button primary addParameter">
          <i class="right plus icon"></i>
          Add Parameter
        </button>

        <table class="ui celled table sqlTemplate">
          <thead>
            <tr>
              <th>URL Key</th>
              <th>SQL Replace Key</th>
              <th>Default Value</th>  
              <th style="width:50px">Function</th>            
            </tr>
          </thead>
          <tbody>                      
          </tbody>
        </table>
         

        </div>
      </div>
    </div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
  </div>
  <!-- LOG MODAL -->
  <!--  MODAL -->
  <div class="ui modal add_sqlTemplate">
    <i class="close icon"></i>
    <div class="header">
      Add SQL Template
    </div>
    <div class="content">
      <form class="ui form  basic">
        <div class="two fields">
          <div class="field">
            <label>URL Key</label>
            <input placeholder="URL Key..." name="urlkey" type="text">
          </div>
          <div class="field">
            <label>SQL Replace Key</label>
            <input placeholder="SQL Replace Key..." name="sqlkey" type="text">
          </div>
        </div>
        
        <div class="field">
          <label>Default Value</label>
          <input placeholder="Default Value..." name="defaultvalue" type="text">
        </div>          
        
      </form>
    </div>
    <div class="actions">
      <div class="ui black deny button">
        Cancel
      </div>
      <div class="ui red right labeled icon button" onclick="addSQLTemplate()">
        Add
        <i class="checkmark icon"></i>
      </div>
    </div>
  </div>
  <script src="../dist/jquery.js"></script>
  <script src="../dist/semantic.js"></script>
  <!-- load ace -->
  <script src="../dist/ace-src/ace.js"></script>
  <!-- load ace language tools -->
  <script src="../dist/ace-src/ext-language_tools.js"></script>
  <script src="../dist/jquery.cookie.js"></script>
  <script src="../dist/jquery.md5.js"></script>
  <script src="../dist/jquery.base64.js"></script>
  <!--Email input tag JS-->
  <script src="../dist/multiple-emails.js"></script>
  <#-- Account -->
  <script src="../dist/account.js"></script>
  <#-- Job Add Function -->
  <script src="../dist/jobadd.js"></script>
</body>
</html>