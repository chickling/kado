<!DOCTYPE html>
<html>
<head>
  <link rel="shortcut icon" type="image/png" href="dist/image/kado.png">
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properties -->
  <title>Kado</title>
  <link rel="stylesheet" type="text/css" href="dist/semantic.css">

  <style type="text/css">

    .hidden.menu {
      display: none;
    }

    .masthead.segment {
      min-height: 700px;
      padding: 1em 0em;
    }
    .masthead .logo.item img {
      margin-right: 1em;
    }
    .masthead .ui.menu .ui.button {
      margin-left: 0.5em;
    }
    .masthead h1.ui.header {
      margin-top: 3em;
      margin-bottom: 0em;
      font-size: 4em;
      font-weight: normal;
    }
    .masthead h2 {
      font-size: 1.7em;
      font-weight: normal;
    }

    .ui.vertical.stripe {
      padding: 8em 0em;
    }
    .ui.vertical.stripe h3 {
      font-size: 2em;
    }
    .ui.vertical.stripe .button + h3,
    .ui.vertical.stripe p + h3 {
      margin-top: 3em;
    }
    .ui.vertical.stripe .floated.image {
      clear: both;
    }
    .ui.vertical.stripe p {
      font-size: 1.33em;
    }
    .ui.vertical.stripe .horizontal.divider {
      margin: 3em 0em;
    }

    .quote.stripe.segment {
      padding: 0em;
    }
    .quote.stripe.segment .grid .column {
      padding-top: 5em;
      padding-bottom: 5em;
    }

    .footer.segment {
      padding: 5em 0em;
    }

    .secondary.pointing.menu .toc.item {
      display: none;
    }

    @media only screen and (max-width: 700px) {
      .ui.fixed.menu {
        display: none !important;
      }
      .secondary.pointing.menu .item,
      .secondary.pointing.menu .menu {
        display: none;
      }
      .secondary.pointing.menu .toc.item {
        display: block;
      }
      .masthead.segment {
        min-height: 350px;
      }
      .masthead h1.ui.header {
        font-size: 2em;
        margin-top: 1.5em;
      }
      .masthead h2 {
        margin-top: 0.5em;
        font-size: 1.5em;
      }
    }
    .ui.inverted.vertical.masthead.center.aligned.segment{
      background: linear-gradient(45deg, rgb(2, 0, 49) 0%, rgb(109, 51, 83) 100%) repeat scroll 0% 0% transparent;
        background-color: transparent;
        background-image: linear-gradient(45deg, rgb(2, 0, 49) 0%, rgb(109, 51, 83) 100%);
        background-repeat: repeat;
        background-attachment: scroll;
        background-position: 0% 0%;
        background-clip: border-box;
        background-origin: padding-box;
        background-size: auto auto;
    }
    

  </style>

  
</head>
<body>

<!-- Following Menu -->
<div class="ui large top fixed hidden menu">
  <div class="ui container">
    <a class="active item">Home</a>
    <a class="item" onclick="showMenu()">Function</a>
    <a class="item" href="https://github.com/chickling/kado/wiki">Usage</a>
    <div class="right menu">
      <div class=" right ui simple dropdown item user">
         <i class="icon user large"></i><font class="displayName"></font><i class="dropdown icon"></i>
        <div class="menu" style="margin-top: -10px;">          
          <a class="item fullName" href="#" ><br></a>
          <div class="divider"></div>
          <div class="header">Account</div>   
          <a class="item accountManage" href="usermanage" ><i class="icon unlock alternate"></i>Account Manage</a>
          <a class="item changePassword"><i class="icon undo"></i>Change Password</a>
          <a class="item logout"><i class="icon sign out"></i> Logout</a>
        </div>
      </div>
     
    </div>
  </div>
</div>

<!-- Sidebar Menu -->
<div class="ui vertical inverted sidebar menu">
  <a class="active item">Home</a>
  <a class="item" onclick="showMenu()">Function</a>
  <a class="item" href="https://github.com/chickling/kado/wiki">Usage</a>
  
</div>
<div class="ui sidebar inverted vertical menu" style="transition-duration: 0.15s;">

    <a class="item">      
      <div class="header">Pages</div>
      <div class="menu">

        <a class="item" href="index">
          Index
        </a>

      </div>

    </a>
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

<!-- Page Contents -->
<div class="pusher">
  <div class="ui inverted vertical masthead center aligned segment">

    <div class="ui container">
      <div class="ui large secondary inverted pointing menu">
        <a class="toc item">
          <i class="sidebar icon"></i>
        </a>
        <a class="active item">Home</a>
        <a class="item" onclick="showMenu()">Function</a>
        <a class="item" href="https://github.com/chickling/kado/wiki"> Usage </a>
        <div class=" right ui simple dropdown item user top">
         <i class="icon user large"></i><font class="displayName"></font><i class="dropdown icon"></i>
        <div class="menu" style="margin-top: -10px;">          
          <a class="item fullName" href="#" ><br>Dev</a>
          <div class="divider"></div>
          <div class="header">Account</div>   
          <a class="item accountManage" href="usermanage" ><i class="icon unlock alternate"></i>Account Manage</a>
          <a class="item changePassword"><i class="icon undo"></i>Change Password</a>
          <a class="item logout"><i class="icon sign out"></i> Logout</a>
        </div>
      </div>
      </div>
    </div>

    <div class="ui text container">
      
      
      <h1 class="ui inverted header" style="margin-top: 130px;">
      <i class="icon cloud big"></i>
      </h1>
      <h1 class="ui inverted header" style="margin-top: 0px;">      
        Kado
      
      <h2>Submit your query, and "Hey presto !"</h2>
      <a class="ui huge primary button" href="queryui">Query Now <i class="right arrow icon"></i></a>
    </div>

  </div>

  <div class="ui vertical stripe segment" style="padding-top: 50px; padding-bottom: 20px;">
    <div class="ui middle aligned stackable grid container">
      <div class="row">
        <div class="eleven wide column">
          <h3 class="ui header">Querying Presto directly on the Web</h3>
          <p>To be intimate with Presto, support user-friendly interface for real time querying and easy to create job and schedule. 
          </p>
          <h3 class="ui header">Responding in real time</h3>
          <p>Monitoring the status and result in real time, the job status and progress are always under your eyes. 
          </p>
        </div>
        <div class="three wide right floated column">
          <i class="icon rocket massive"></i>
        </div>
      </div>
      <div class="row">
        <div class="center aligned column">
          <a class="ui huge button" href="queryui">Query UI</a>
        </div>
      </div>
    </div>
  </div>


  <div class="ui vertical stripe quote segment">
    <div class="ui equal width stackable internally celled grid">
      <div class="center aligned row">
        <a class="column" href="joblist">
          <h3>Build and manage your Job</h3>
          <p><i class="icon huge suitcase"></i></p>
        </a>
        <a class="column" href="schedulelist">
          <h3>Build and manage your Schedule</h3>
          <p>
           <i class="icon huge calendar"></i>
          </p>
        </a>
      </div>
    </div>
  </div>

  <div class="ui vertical stripe segment" style="padding-bottom: 50px; padding-top: 50px;">
    <div class="ui text container" style="text-align: center;">
      <h3 class="ui header">View your Job execution state</h3>
      <p><i class="icon heartbeat huge"></i></p>
      <a class="ui large button" href="status">View Status</a>
    </div>
  </div>


  <div class="ui inverted vertical footer segment">
    <div class="ui container">
      <div class="ui stackable inverted divided equal height stackable grid">
        <div class="three wide column">
          <h4 class="ui inverted header">Function</h4>
          <div class="ui inverted link list">
            <a href="queryui" class="item">QueryUI</a>
            <a href="joblist" class="item">Job list</a>
            <a href="schedulelist" class="item">Schedule list</a>            
          </div>
        </div>
        <div class="three wide column">
          <h4 class="ui inverted header">Monitor</h4>
          <div class="ui inverted link list">
            <a href="status" class="item">Job Status</a>            
          </div>
        </div>
        <div class="seven wide column">
          <a href="https://github.com/chickling" class="item">Chickling 2017</a>
          <a href="https://github.com/chickling/kado" class="item">Kado Team</a>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>

  <script>
  $(document)
    .ready(function() {
     
      // fix menu when passed
      $('.masthead')
        .visibility({
          once: false,
          onBottomPassed: function() {
            $('.fixed.menu').transition('fade in');
          },
          onBottomPassedReverse: function() {
            $('.fixed.menu').transition('fade out');
          }
        })
      ;
      if($.cookie('token')!="null"&&$.cookie('token')!="undefined"&&$.cookie('token')!=null){
        var dname=$.cookie('username').split(".")[0];
        $(".displayName").html(dname);
        $(".displayName").attr("onclick",'');
        $(".fullName").html($.cookie('username')+"<br>"+$.cookie('group'));
        
      }else{
        $(".displayName").html("Log in");
        $(".displayName").attr("onclick",'document.location="login";');
        $(".ui.simple.item .menu").hide();
      }
      hideAccountManage();
    })
  ;
  function showMenu(){
    $('.ui.sidebar').sidebar('setting', 'transition', 'push').sidebar('toggle');
  }
  $(".item.logout").click(function(){
    logout();
  });
  function logout(){
    if($.cookie('token')!=null){
      var url="./account/logout";
      if(secondFloor())
        url="../account/logout";
      $.ajax({
                url: url,                
                type:"GET",
                beforeSend: function (request)
                {
                  request.setRequestHeader("Authorization", $.cookie('token'));
                },
                dataType:'json',                
                success: function(JData){
                  if(JData["status"]!=null){
                    if(JData["status"]!="error"){ 
                        $.removeCookie('token', { path: '/' });
                        $.removeCookie('lgtime', { path: '/' });
                        $.removeCookie('username', { path: '/' });
                        $.removeCookie('group', { path: '/' });
                        $.removeCookie('level', { path: '/' });   
                        $.removeCookie('uid', { path: '/' });   
                        $.removeCookie('refurl', { path: '/' });
                        if(secondFloor())                   
                          document.location="../login";
                        else
                          document.location="./login";
                      }else{
                        alert("["+JData["status"]+"]\n"+JData["message"]);
                        $.removeCookie('token', { path: '/' });
                        $.removeCookie('lgtime', { path: '/' });
                        $.removeCookie('username', { path: '/' });
                        $.removeCookie('group', { path: '/' });
                        $.removeCookie('level', { path: '/' });   
                        $.removeCookie('uid', { path: '/' });   
                        $.removeCookie('refurl', { path: '/' });
                        if(secondFloor())                   
                          document.location="../login";
                        else
                          document.location="./login";
                      }
                  }
                    
                },

                 error:function(xhr, ajaxOptions, thrownError){
                    alert(xhr.status);
                    alert(thrownError);
                 }
            });
    }else{
      $.removeCookie('token', { path: '/' });
      $.removeCookie('lgtime', { path: '/' });
      $.removeCookie('username', { path: '/' });
      $.removeCookie('group', { path: '/' });
      $.removeCookie('level', { path: '/' });   
      $.removeCookie('uid', { path: '/' });   
      $.removeCookie('refurl', { path: '/' });
      document.location="/login";
    }
  }
  $(".changePassword").click(function(){
    passwd();
  });
  function passwd(){
    var modal = function(){/*
    <div class="ui modal change">
  <i class="close icon"></i>
  <div class="header">
    Change your password !
  </div>
  <div class="image content">
    
    <div class="description">      
      <p>Input your old Password</p>
      <div class="ui icon input">
        <input placeholder="old Password" type="password" id="oldPassword">
        <i class="privacy icon"></i>
      </div>
      <p>Input your new Password</p>
      <div class="ui icon input">
        <input placeholder="new Password" type="password" id="newPassword" class="ckpassword">
        <i class="privacy icon"></i>
      </div>
      <p>Repeat input your new Password</p>
      <div class="ui icon input">
        <input placeholder="repeat new Password" type="password" id="rep_newPassword" class="ckpassword">
        <i class="privacy icon"></i>
      </div>
    </div>
  </div>
  <div class="actions">
    <div class="ui black deny button">
      Cancel
    </div>
    <div class="ui  right labeled icon button" onclick="changePassword()">
      Change 
      <i class="checkmark icon"></i>
    </div>
  </div>
</div>
    */}.toString().slice(14,-3);    
    $(".footer").html($(".footer").html()+modal);
    $('.ui.modal.change').modal({
      onHidden: function(){        
            
          $('.ui.modal.change').remove();
        }
      });
    $('.ui.modal.change').modal('show');
    $(".ckpassword").change(function(){
      $("#newPassword").parent().removeClass("error");
      $("#rep_newPassword").parent().removeClass("error");
      $("#oldPassword").parent().removeClass("error");
       if($("#oldPassword").val()==""){
        $("#oldPassword").parent().addClass("error");
      }
      if($("#newPassword").val()==""){
        $("#newPassword").parent().addClass("error");
      }
      if($("#rep_newPassword").val()==""){
        $("#rep_newPassword").parent().addClass("error");
      }
      if($("#rep_newPassword").val()!=$("#newPassword").val()){
        $("#rep_newPassword").parent().addClass("error");       
      }
    });    
  }
  function changePassword(){
    if($("#rep_newPassword").val()==$("#newPassword").val()&&$("#oldPassword")!=""){
      var json = { "oldpassword":$.md5($("#oldPassword").val()), "password": $.md5($("#newPassword").val()) };
      var url='./account/user/update/password/'+$.cookie('uid')
      if(secondFloor())
        url='../account/user/update/password/'+$.cookie('uid')
      $.ajax({
                url: url,
                data: JSON.stringify(json),
                beforeSend: function (request)
                {
                  request.setRequestHeader("Authorization", $.cookie('token'));
                },
                type:"POST",
                dataType:'json',
                contentType: "application/json; charset=utf-8",
                success: function(JData){
                    if(JData["status"]!=null){
                      if(JData["status"]=="success"){
                        $('.ui.modal.change').modal('hide');
                      }else{
                        alert("["+JData["status"]+"]\n"+JData["message"]);
                      }
                    }
                },

                 error:function(xhr, ajaxOptions, thrownError){
                    alert(xhr.status+"-"+thrownError);
                 }
            });     
    }else{
      alert("Confirm Password.\nPasswords do not match");
    }
  }

  function hideAccountManage(){
    if($.cookie('level')!="Admin")
      $(".accountManage").hide();
  }
  function secondFloor(){
    var url=window.location.href;
    if(url.indexOf("joblist/add")>=0)
      return true
    else if(url.indexOf("joblist/edit")>=0)
      return true
    else if(url.indexOf("schedulelist/add")>=0)
      return true
    else if(url.indexOf("schedulelist/edit")>=0)
      return true
    else
      return false
  }
   
  </script>
</body>

</html>
