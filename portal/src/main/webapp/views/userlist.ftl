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
  <title>Kado User Manage</title>
  <link rel="stylesheet" type="text/css" href="/dist/semantic.css">

  
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
  </style>

</head>
<body>
  
  

  <div class="ui fixed inverted menu">
    <div class="ui container">
      <a href="#" class="header item" >
       
        
        <i class="icon world large"></i>
        NewEgg
      </a>
      <a href="#" class="item" id="project"> <i class="icon large sidebar" ></i>Function</a>
      <!--<div class="ui simple dropdown item">
       <i class="icon options large"></i> 
Setting <i class="dropdown icon"></i>
        <div class="menu">
          <a class="itemnotclick">
            <label>sss</label><div class="ui fitted toggle checkbox">
    <input type="checkbox">
    <label></label>
  </div></a>
          <a class="item" href="#">Link Item</a>
          <div class="divider"></div>
          <div class="header">Header Item</div>
          <div class="item">
            <i class="dropdown icon"></i>
            Sub Menu
            <div class="menu">
              <a class="item" href="#">Link Item</a>
              <a class="item" href="#">Link Item</a>
            </div>
          </div>
          <a class="item" href="#">Link Item</a>
        </div>
      </div>-->
      
      <div class=" right ui simple dropdown item">
       <i class="icon user large"></i><font id="displayName">Eugene</font><i class="dropdown icon"></i>
        <div class="menu">          
          <a class="item" href="#" id="fullName">Eugene.Y.Yan<br>Dev</a>
          <div class="divider"></div>
          <div class="header">Account</div>   
          <a class="item" href="/usermanage" id="accountManage"><i class="icon unlock alternate"></i>Account Manage</a>
          <a class="item" id="changePassword"><i class="icon undo"></i>Change Password</a>
          <a class="item" id="logout"><i class="icon sign out"></i> Logout</a>
        </div>
      </div>

    </div>

  </div>
  <div class="ui sidebar inverted vertical menu">

    <a class="item">      
      <div class="header">Pages</div>
      <div class="menu">

        <a class="item" href="index">
          Index
        </a>

      </div>

    </a>
    <#-- <a class="item">      
      <div class="header">Domain Category</div>
      <div class="menu">

        <a class="item" href="">
          Newegg.com
        </a>

        <a class="item" href="">
          Newegg B2B
        </a>
        <a class="item" href="">
          Newegg Mobile
        </a>

      </div>

    </a>
     <a class="item">      
      <div class="header">Price Page</div>
      <div class="menu">

        <a class="item" href="">
          Price Page
        </a>

        <a class="item" href="">
          Login Page
        </a>

      </div>

    </a> -->
    <a class="item">      
      <div class="header">Job Manager</div>
      <div class="menu">
        <a class="item" href="status">
          Status
        </a>
        <a class="item" href="joblist">
          Job list
        </a>
        <a class="item" href="schedulelist">
          Schedule list
        </a>
        <a class="item" href="queryui">
          Presto Query UI
        </a>
      </div>
    </a>

  </div>
  <div class="pusher">
  <div class="ui top  attached progress" data-percent="100" id="pg" style="margin-top:50px">
    <div class="bar"></div>
  </div>
    <!-- Site content !-->
    <div class="ui main  container">
      <h2>Newegg.com</h2>
  <p>
    <div class="ui form">
  <div class="inline fields">
    <i class="icon wait big" style="margin-right: 8px;"></i>
    <div class="field">
      <div class="ui radio checkbox">
        <input name="frequency" checked="checked" type="radio">
        <label>Last 1 Hour</label>
      </div>
    </div>
    <div class="field">
      <div class="ui radio checkbox">
        <input name="frequency" type="radio">
        <label>Last 4 Hour</label>
      </div>
    </div>
    <div class="field">
      <div class="ui radio checkbox">
        <input name="frequency" type="radio">
        <label>Last 24 Hour</label>
      </div>
    </div>
    <i class="icon checkered trophy big"  style="margin-left: 20px;margin-right: 8px;"></i>
     <div class="field">
      <div class="ui radio checkbox">
        <input name="frequencys" checked="checked" type="radio">
        <label>top 10</label>
      </div>
    </div>
    <div class="field">
      <div class="ui radio checkbox">
        <input name="frequencys" type="radio">
        <label>top 100</label>
      </div>
    </div>
      
      <div class="ui floating dropdown labeled icon button tiny">
  <i class="filter icon"></i>
  <span class="text">Customize Filter</span>
  <div class="menu">
    <div class="ui icon  input">
      <i class="terminal icon"></i>
      <input placeholder=">=?" type="text">
    </div>
    <div class="divider"></div>
   
    <div class="scrolling menu" style="display:none">
      <div class="item">
        <div class="ui red empty circular label"></div>
        GO
      </div>
      
    </div>
  </div>
</div>
  </div>  
    
  </div>

  </p>


  </div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
  </div>
  

 
  <script src="/dist/jquery.js"></script>
  <script src="/dist/semantic.js"></script>
  <script src="/dist/jquery.cookie.js"></script>
  <script src="/dist/jquery.md5.js"></script>
  <script src="/dist/account.js"></script>
  <script type="text/javascript">
  
   
    $("#project").click(function(){
        $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
    });
    $('.ui.dropdown')
  .dropdown()
;

  </script>
</body>

</html>
