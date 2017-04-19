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


  <link rel="stylesheet" type="text/css" href="../dist/semantic.css">
 
  <link href="../dist/daterangepicker.css" rel="stylesheet" type="text/css">
  
  <link href="../dist/vis.css" rel="stylesheet" type="text/css" />
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
  #visualization{
    background-color: #F9FAFB;
  }
  .timesel{
    max-height: 300px;
    overflow:auto;
  }
  
  .ui.ordered.steps{
    width: 100%;
    height: 70px;
  }
  .ui.ordered.steps .content{
    padding: 0px;
    
  }
  
  .ui.ordered.steps .content .title{
    padding: 3px;
    border-top-width: 0px;
  }
  .full.height {
    min-height: 800px;
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
  <div id="visualization"></div>
  <!-- Site content !-->  
  <div class="ui main  container">
    <div style="text-align: right;margin-bottom:20px;padding-left: 10px; padding-right: 10px;"> 
      <h3 style="float:left">Add Schedule</h3>
      <a href="../schedulelist#back" class="ui labeled icon  button  vk">
            <i class="remove icon"></i>
            Cancel
      </a>
      <button class="ui labeled icon button vk" onclick="saveSchedule()"><i class="icon save"></i> Save Schedule</button>
    </div> 
    
    <div class="ui styled fluid accordion">
  <div class="title active">
    <i class="dropdown icon"></i>
    Basic
  </div>
  <div class="content active">
    <form class="ui form">
      <div class="two fields">
        <div class="field">
          <label>Schedule Name</label>
          <input name="schedule_name" placeholder="Schedule Name..." type="text">
        </div>
        <div class="field">
          <label>Schedule Visibility Level</label>
          <div class="ui selection dropdown level" >
            <input name="gender" type="hidden" name="schedule_level">
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
        <label>Schedule Memo</label>
        <input name="memo" placeholder="Memo..." type="text">
      </div>
      <div class="field">
        <div class="ui checkbox toggle">
          <input class="hidden" tabindex="0" type="checkbox" name="notification">
          <label>Failure Notification Me</label>
        </div>
      </div>
    </form>
  </div>
  <div class="title active">
    <i class="dropdown icon"></i>
    Time
  </div>
  <div class="content active">
     <div>
      <i class="icon wait big" style="margin-right: 8px;"></i>
      <div class="ui radio checkbox">
        <input name="timemode" checked="checked" type="radio" value="0">
        <label style="width:120px;">Single Time:</label>
      </div>
      <div class="ui left icon input singletime stime">
        <input type="text"  id="reservation" name="reservation" class="">
        <i class="calendar icon"></i>
      </div>
      <div class="ui left icon input singletime tags">
        <input type="text" id="labelname">
        <i class="tag icon"></i>
      </div>
      <button class="ui primary button singletime" onclick="addItem();">
        Add Time
      </button>
    </div>
    <div style="margin-top:10px;">
      <i class="icon undo big" style="margin-right: 8px;"></i>
      <div class="ui radio checkbox">
        <input name="timemode" checked="checked" type="radio" value="1">
        <label style="width:120px;">Interval Cycle:</label>
      </div>
      
      <div class="ui  icon input labeled intervaltime startwith">
        <div class="ui label">
          <i class="icon play"></i>
          Start With
        </div>
        <input type="text"  id="startwith" name="reservation" class="">
        <i class="calendar icon "></i>
      </div>
      <div class="ui right action left input labeled intervaltime every">
        <div class="ui label">
          <i class="icon flag"></i>
          Every
        </div>
        <input placeholder="how long.." type="text">
        <div class="ui basic floating dropdown button unit">
          <div class="text">Hour</div>
          <i class="dropdown icon"></i>
          <div class="menu">
            <div class="item">Minute</div>
            <div class="item">Hour</div>
            <div class="item">Day</div>
          </div>
        </div>
      </div>
      
    </div>
    <div style="margin-top:10px;">
      <i class="icon history big" style="margin-right: 8px;"></i>
      <div class="ui radio checkbox">
        <input name="timemode" checked="checked" type="radio" value="2">
        <label style="width:120px;">Time Cycle:</label>
      </div>
      
      <div class="ui  icon input labeled cycletime startwith">
        <div class="ui label">
          <i class="icon play"></i>
          Start With
        </div>
        <input type="text"  id="startwithcycle" name="reservation" class="">
        <i class="calendar icon"></i>
      </div>
      <div class="ui right action left input labeled cycletime ">
        <div class="ui label">
          <i class="icon wait"></i>
          Time
        </div>
        
        <div class="ui basic floating dropdown button timehour" id="timeCycleHour">
          <div class="text">00</div>
          <i class="dropdown icon"></i>
          <div class="menu timesel">
            <div class="item">00</div>            
          </div>
        </div>
        <div class="ui label">
          :
        </div>
        <div class="ui basic floating dropdown button timemin" id="timeCycleMinute">
          <div class="text">00</div>
          <i class="dropdown icon"></i>
          <div class="menu timesel">
            <div class="item">00</div>
          </div>
        </div>
      </div>
      <div class="ui multiple dropdown cycletime each" id="eachTime">
        <input name="filters" type="hidden">
        <i class="filter icon"></i>
        <span class="text">Each ...</span>
        <div class="menu">
          <div class="ui icon search input">
            <i class="search icon"></i>
            <input placeholder="Search tags..." type="text">
          </div>
          <div class="divider"></div>
          
          <div class="header">
            <i class="tags icon"></i>
            Week
          </div>
          <div class="scrolling menu">
            <div class="item" data-value="everyday">
              <div class="ui red empty circular label"></div>
              Everyday
            </div>
            <div class="item week" data-value="w0">
              <div class="ui red empty circular label"></div>
              Sunday
            </div>            
            <div class="item week" data-value="w1">
              <div class="ui blue empty circular label"></div>
              Monday
            </div>
            <div class="item week" data-value="w2">
              <div class="ui black empty circular label"></div>
              Tuesday
            </div>
            <div class="item week" data-value="w3">
              <div class="ui purple empty circular label"></div>
              Wednesday
            </div>
            <div class="item week" data-value="w4">
              <div class="ui orange empty circular label"></div>
              Thursday
            </div>
            <div class="item week" data-value="w5">
              <div class="ui yellow empty circular label"></div>
              Friday
            </div>
            <div class="item week" data-value="w6">
              <div class="ui pink empty circular label"></div>
              Saturday
            </div>
            
            
          </div>
        </div>
      </div>
      
    </div>
  </div>
  <div class="title active">
    <i class="dropdown icon"></i>
    Job to be executed
  </div>
  <div class="content active">
    <div>
      <div class="ui floating dropdown labeled icon button joblist">
        <i class="filter icon"></i>
        <span class="text">Choose Job</span>
        <div class="menu">
          <div class="ui icon search input">
            <i class="search icon"></i>
            <input placeholder="Search tags..." type="text">
          </div>
          <div class="divider"></div>
          <div class="header">
            <i class="tags icon"></i>
            My Job
          </div>
          <div class="scrolling menu self">
            <div class="item">
              <div class="ui red empty circular label"></div>
              Important
            </div>
            <div class="item">
              <div class="ui blue empty circular label"></div>
              Announcement
            </div>
          </div>
          <div class="header">
            <i class="tags icon"></i>
            Public Job
          </div>
          <div class="scrolling menu other">
            <div class="item">
              <div class="ui red empty circular label"></div>
              Important
            </div>
            <div class="item">
              <div class="ui blue empty circular label"></div>
              Announcement
            </div>
          </div>
        </div>
      </div>
      <button class="ui primary button" onclick="addJobtoList()">
        Add Job
      </button>
    </div>
    <div class="ui ordered steps" style="overflow:auto;height:100px">
      <div class=" step">
        <div class="content ">
          <div class="title">NO Job !</div>
          <div class="description">Please add Job...</div>
        </div>
      </div>
      
      <div class="completed  active step">
        <div class="content ">
          <div class="title">End of Step  </div>
          <div class="description"></div>
        </div>
      </div>
    </div>
  </div>
  
 
</div>
   

  

 
</div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
</div>



<script src="../dist/jquery.js"></script>
<script src="../dist/semantic.js"></script>
<script src="../dist/d3.min.js" charset="utf-8"></script>
<script src="../dist/jquery.cookie.js"></script>
 <script src="../dist/jquery.md5.js"></script>
<script src="../dist/vis.js"></script>
<script src="../dist/moment.js"></script>
<script src="../dist/daterangepicker.js"></script>
<#-- Account -->
<script src="../dist/account.js"></script>
<#-- Add Schedule JS -->
<script src="../dist/schedule.js"></script>
</body>

</html>
