<!DOCTYPE html>
<html>
<head>
  <link rel="shortcut icon" type="image/png" href="dist/image/kado.png">
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properities -->
  <title>Login Kado</title>

  <link rel="stylesheet" type="text/css" href="dist/semantic.min.css">

  <script src="dist/library/jquery.min.js"></script>
  <script src="dist/semantic.min.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <style type="text/css">
    body {
      background-color: #DADADA;
    }
    body > .grid {
      height: 100%;
    }
    .image {
      margin-top: -100px;
    }
    .column {
      max-width: 450px;
    }
  </style>

</head>
<body>

<div class="ui middle aligned center aligned grid">
  <div class="column">
    <h2 class="ui teal image header">
      <i class="icon dashboard big"></i>
      <div class="content">
        Log-in to Kado
      </div>
    </h2>
    <div class="ui large form">
      <div class="ui stacked segment">
        <div class="field">
          <div class="ui left icon input">
            <i class="user icon"></i>
            <input type="text" name="account" placeholder="Account ID">
          </div>
        </div>
        <div class="field">
          <div class="ui left icon input">
            <i class="lock icon"></i>
            <input type="password" name="password" placeholder="Password">
          </div>
        </div>
        <a class="ui fluid large teal  button">Login</a>
      </div>

      <div class="ui error message"></div>

    </div>

    <div class="ui message">
      New to us? <a href="#">Contact Admin</a>
    </div>
  </div>
</div>
<script>
  $(document)
    .ready(function() {
      $('.ui.form')
        .form({
          fields: {
            email: {
              identifier  : 'account',
              rules: [
                {
                  type   : 'empty',
                  prompt : 'Please enter your Account ID'
                }
              ]
            },
            password: {
              identifier  : 'password',
              rules: [
                {
                  type   : 'empty',
                  prompt : 'Please enter your password'
                }                
              ]
            }
          }
        })
      ;
    })
  ;

  $("[name='password']").keypress(function(e){
    code = (e.keyCode ? e.keyCode : e.which);
    if (code == 13)
    {
      login();
    }
  });
  $(".button").click(function(){    
    login();
  });

  function login(){
    if($("[name='account']").val()!=""&&$("[name='password']").val()!=""){
      var json = { "account":$("[name='account']").val(), "password": $.md5($("[name='password']").val()) };
      
      $.ajax({
                url: 'account/login',
                data: JSON.stringify(json),
                type:"POST",
                dataType:'json',
                contentType: "application/json; charset=utf-8",
                success: function(JData){
                    if(JData["status"]!=null){
                      if(JData["status"]=="success"){
                        $.cookie('token', JData["token"], { expires: 7,path: '/' });
                        $.cookie('lgtime', JData["time"], { expires: 7,path: '/' });
                        $.cookie('username', JData["username"], { expires: 7,path: '/' });
                        $.cookie('group', JData["group"], { expires: 7,path: '/' });
                        $.cookie('level', JData["level"], { expires: 7,path: '/' });
                        $.cookie('uid', JData["uid"], { expires: 7,path: '/' });
                        if(getUrlStatus()=="jump"&&$.cookie('refurl')!=""&&$.cookie('refurl')!=null){
                          window.location.href=$.cookie('refurl');
                        }else{
                          document.location="index";
                        }
                      }else{
                        alert("["+JData["status"]+"]\n"+JData["message"]);
                      }
                    }
                },

                 error:function(xhr, ajaxOptions, thrownError){
                    alert(xhr.status);
                    alert(thrownError);
                 }
            });
    }
  }
  function getUrlStatus() {
      var hash =location.hash;
      hash=hash.replace(/#/,"");
      return hash;
    }
  </script>
</body>

</html>
