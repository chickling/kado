<!DOCTYPE html>
<html>
<head>
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properities -->
  <title>Kado Dashboard</title>

  <link rel="stylesheet" type="text/css" href="../dist/semantic.css">
  <link href="https://fonts.googleapis.com/css?family=Raleway:400,300,600,800,900" rel="stylesheet" type="text/css">
  
  <style type="text/css">
      body {
        background-color: #FFFFFF;
      }
      .ui.menu .item img.logo {
        margin-right: 1.5em;
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

    .progress {
        height: 300px;
    }

    .progress > svg {
        height: 100%;
        display: block;
    }
    #container {      
      width: 170px;
      height: 170px;
      position: relative;
      margin:0 auto; 
    }
    .display .ui.cards{
      width: 60%;
      margin:0 auto;
      padding-top: 50px;

    }
    .display .ui.cards .card{      
      width: 100%;
    }
    .card.history{
      background-color: #FFFFE2 !important;
    }
  </style>

</head>
<body>
  
  

  <div class="ui fixed  menu" style="z-index:2000">
    <div class="ui container" style="width:100%;">
      <a class="header item" >
        <i class="icon lightning outline large"></i>
        Real-Time Query
      </a>
      
      <a class="item" id="project"> <i class="icon large sidebar" style="margin-right: 0px;"></i></a>
      
      
      
      

      
      
    </div>

  </div>
  <div class="ui sidebar inverted vertical menu" style="height: 100%">
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
  <div class="pusher" style="">
    <!-- Site content !-->
    <div class="ui main container" style="width:100%;overflow: auto;">
      <div class="display" style="width:100%;margin-top:100px">
        <div id="container"></div>
        <div class="ui cards">
          <div class="card history">
            <div class="content">
              <i class="icon calendar outline right floated"></i>
              <div class="header">
                Similar Querys
              </div>
              <div class="meta">
                Directly using the last query results
              </div>
              <div class="description">
                <table class="ui selectable inverted table celled lastrun">
                  <tbody>
                    <tr>
                      <td >Status</td>
                      <td class="job_status"><label class="ui label"></label></td>                      
                      <td >Progress</td>
                      <td class="progress_num"><label class="ui label"></label></td>
                      <td >Start Time</td>
                      <td class="start_time"><label class="ui label"></label></td>                      
                    </tr>
                  </tbody>
                </table>
                <h4>Last Run SQL</h4>
                <div class="ui ignored warning message lastjobsql"></div>
              </div>
            </div>
            <div class="extra content">
              <div class="ui three buttons">
                <div class="ui basic blue button moreresult" onclick="showMoreResult()">Pause</div>
                <div class="ui basic green button lastresult" onclick="useLastResult()">Use Last Result</div>
                <div class="ui basic red button" onclick="queryNow()"><font id="count_num">10</font>s Continue Query ...</div>
              </div>
            </div>
            <div class="ui bottom attached indicating progress  ">
              <div class="bar"></div>
            </div>
          </div>
          <div class="card jobinfo" style="display:none">
            <div class="content">
              <i class="icon rocket right floated"></i>
              <div class="header">
                Query Info
              </div>
              <div class="meta">
                Directly using the last query results
              </div>
              <div class="description">
                <table class="ui selectable inverted table celled running">
                  <tbody>
                    <tr>
                      <td >Status</td>
                      <td class="job_status"><label class="ui label"></label></td>                      
                      <td >User</td>
                      <td class="user"><label class="ui label"></label></td>
                      <td >Runing Time</td>
                      <td class="runingtimes"><label class="ui label"></label></td>
                      
                    </tr>
                    <tr>
                      <td >Start Time</td>
                      <td class="start_time"><label class="ui label"></label></td>
                      <td >Storage</td>
                      <td class="storage"><label class="ui label"></label></td>
                      <td >Presto ID</td>
                      <td class="presto_id"></td>                      
                    </tr>
                  </tbody>
                </table>
                <h4>Run SQL</h4>
                <div class="ui ignored warning message jobsql"></div>
                <div id="errorlog" style="display:none">
                  <h4>Runing Log<h4>
                  <div class="ui ignored warning message runing log">
                </div>
                <p></p>  
                </div>
              </div>
            </div>
            <div class="extra content">
              <div class="ui one buttons">
                <div class="ui basic red button kill" onclick="killJob()">Kill Query</div>
              </div>
              <div style="float:right">
                <a class="ui basic green button result " onclick="viewResult()">View Result</a>
              </div>
            </div>
          </div>
        </div>
      </div>   

      <div class="footer"> </div>
  </div>
  

 
  <script src="../dist/jquery.js"></script>
  <script src="../dist/semantic.js"></script>
  <script src="../dist/progressbar.min.js"></script>
  <script src="../dist/jquery.cookie.js"></script>
  <script src="../dist/jquery.md5.js"></script>
  <script src="../dist/jquery.base64.js"></script>
  <script src="../dist/account.js"></script>
  <script src="../dist/querystring.js"></script>
  <script type="text/javascript">    
    var bar;
    var timerCount=0;
    var countDownTimer;
    var progressCheckTimer;
    
    $( document ).ready(function() {
      $('.ui.dropdown').dropdown();
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
        from: { color: '#aaa', width: 1 },
        to: { color: '#01DF01', width: 2 },
        // Set default step function for all animate calls
        step: function(state, circle) {
          circle.path.setAttribute('stroke', state.color);
          circle.path.setAttribute('stroke-width', state.width);

          var value = Math.round(circle.value() * 100);
          if (value === 0) {
            circle.setText('wait');
          } else {
            circle.setText(value+"%");
          }

        }
      });
      bar.text.style.fontFamily = '"Raleway", Helvetica, sans-serif';
      bar.text.style.fontSize = '2rem';

      bar.animate(0);  // Number from 0.0 to 1.0
      if(getQueryString().jhid!=null&&getQueryString().jhid!=""){
        loadJobHistory(getQueryString().jhid);
        countDownTimer=setInterval(countDown, 100);
      }else{
        $('.ui.cards .card.history').hide();
        runQuery();
      }
      //function btn click
      $("#project").click(function() {
          $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
      });
      //disabled result button
      $(".ui.button.result").addClass('disabled');
    });
    function countDown() {
        $('.ui.progress').progress({ percent: timerCount});
        $('#count_num').html(Math.round(10-(timerCount/10)));
        if(timerCount>=100){
          window.clearInterval(countDownTimer);
          $('.ui.cards .card.history').transition({
            animation : 'drop',
            reverse   : 'auto', 
            duration  : 500,
            onComplete : function() {
              $('.ui.cards .card.history').hide();
              runQuery();
            }
          });          
        }
        timerCount++;
    }
    function queryNow() {
      window.clearInterval(countDownTimer);
      $('.ui.cards .card.history').transition({
        animation : 'drop',
        reverse   : 'auto', 
        duration  : 500,
        onComplete : function() {
          $('.ui.cards .card.history').hide();
          runQuery();
        }
      });
    }
    function useLastResult(jhid) {
      window.clearInterval(countDownTimer);
      var nextSetp=getQueryString().next;
      if(nextSetp=="resultview"||nextSetp==""||nextSetp==null){
        setTimeout(function(){
          window.location.href="../resultview#"+getQueryString().jhid;
        },500);                    
      }else if(nextSetp=="chart"&&getQueryString().chartid!=null&&getQueryString().chartid!=null){
        setTimeout(function(){
          window.location.href="../charts/draw?jhid="+getQueryString().jhid+"&chartid="+getQueryString().chartid;
        },500);
      }
    }
    function showMoreResult(jhid) {
      window.clearInterval(countDownTimer);
    }
    function runQuery(){   
      if(getQueryString().jid!=null&&getQueryString().jid!=""){
        runJob(getQueryString().jid);  
      }else if(getQueryString().sql!=null&&getQueryString().sql!=""){
        runSQLQuery(getQueryString().sql)
      }else{
        alert("Nothing to do!\n Check your URL!");
      }
      
    }
    /**
     * Run Job Now
     * Execution once
     * @param  {[type]} jid [job id]
     * 
     */
    function runJob(jid) {

        $.ajax({
            url: '../control/job/manage/run/with/template/' + jid,
            data: JSON.stringify(getTemplateList()),
            type: "POST",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                      if(JData["jhid"]!=null&&JData["jhid"]!=""){
                        window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"jhid",JData["jhid"]));
                        progressCheckTimer=setInterval(uploadProgress, 1000);
                      }
                    } else {
                        JData["message"].checkPermission()
                    }
                }
            },
            error: function(xhr, ajaxOptions, thrownError) {
              alert(thrownError);
            }
        });
    }
    /**
     * Send SQL Query to Presto 
     * @return 
     */
    function runSQLQuery(sql) {
      var addJson = {};
      addJson["sql"] = $.base64Encode(sql);
      addJson["jobLevel"] = "public";
      addJson["type"] = "query";
      //POST to Add Query
      $.ajax({
          url: '../query/submit/',
          data: JSON.stringify(addJson),
          beforeSend: function(request) {
              request.setRequestHeader("Authorization", $.cookie('token'));
          },
          type: "POST",
          dataType: 'json',
          contentType: "application/json; charset=utf-8",
          success: function(JData) {
              if (JData["status"] != null) {
                  if (JData["status"] == "success") {
                      if(JData["jhid"]!=null&&JData["jhid"]!=""){
                        window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"jhid",JData["jhid"]));
                        progressCheckTimer=setInterval(uploadProgress, 1000);
                      }
                  } else {
                      JData["message"].checkPermission()
                  }
                  
              }
          },

          error: function(xhr, ajaxOptions, thrownError) {
              alert(xhr.status + "-" + thrownError);
          }
      });
    }
    function killJob(){
      $.ajax({
        url: '../query/kill/' + getQueryString().jhid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {

                } else {
                    JData["message"].checkPermission()
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
          alert(thrownError);
        }
      });
    }
    /**
     * [loadJobHistory description]
     * load Similar Querys History Info
     * @return {[type]} [description]
     */
    function loadJobHistory(jhid){
      if(jhid!=null){
        var url="../job/manage/run/history/get/info/"+jhid;
        $.ajax({
          url: url,
          type: "GET",
          beforeSend: function(request) {
              request.setRequestHeader("Authorization", $.cookie('token'));
          },
          timeout:5000,
          dataType: 'json',
          success: function(JData) {
            if(JData["status"]=="success"){
              
              $(".lastrun .job_status").html(getStatus(JData["job_status"]));
              $(".lastrun .progress_num label").html(JData["progress"]);
              $(".lastrun .start_time label").html(JData["start_time"]);              
              $(".lastjobsql.message").html($.base64Decode(JData["sql"]).replace(/\n/g, "<br>"));
              if(parseInt(JData["progress"])>=100&&JData["job_status"]=="1"){
                $(".button.lastresult").attr("onclick","useLastResult('"+jhid+"')");
              }else if(JData["job_status"]=="2"){
                $(".button.lastresult").addClass("disabled");
              }
            }
          },
          error: function(xhr, ajaxOptions, thrownError) {
            alert(thrownError);
          }
        });
      }
    }
    function uploadProgress(){
      if(getQueryString().jhid!=null){
        var url="../job/manage/run/history/get/info/"+getQueryString().jhid;
        $.ajax({
          url: url,
          type: "GET",
          beforeSend: function(request) {
              request.setRequestHeader("Authorization", $.cookie('token'));
          },
          timeout:5000,
          dataType: 'json',
          success: function(JData) {
            if(JData["status"]=="success"){
              if(!$('.ui.cards .card.jobinfo').is(":visible"))
                $('.ui.cards .card.jobinfo').transition('fade down');
              console.log(JData["progress"]);
              bar.animate(parseInt(JData["progress"])/100);
              $(".running .job_status").html(getStatus(JData["job_status"]));
              $(".running .start_time label").html(JData["start_time"]);
              $(".running .user label").html(JData["user"]);
              $(".running .runingtimes label").html(JData["runingtime"]);
              $(".running .presto_id").html(getPrestoButton(JData["presto_id"],JData["presto_url"]));
              $(".running .storage label").html(JData["storage"]);
              $(".jobsql.message").html($.base64Decode(JData["sql"]).replace(/\n/g, "<br>"));
              if(parseInt(JData["progress"])>=100&&JData["job_status"]=="1"){
                window.clearInterval(progressCheckTimer);
                $(".ui.button.kill").addClass('disabled');
                $(".ui.button.result").removeClass('disabled');
                var queryString=getQueryString();
                var nextSetp=queryString.next;
                if(nextSetp!=null&&nextSetp!=""){
                  if(nextSetp=="resultview"){
                    setTimeout(function(){
                      window.location.href="../resultview#"+queryString.jhid;
                    },500);                    
                  }else if(nextSetp=="chart"&&queryString.chartid!=null&&queryString.chartid!=null){
                    setTimeout(function(){
                      window.location.href="../charts/draw?jhid="+queryString.jhid+"&chartid="+queryString.chartid+"&display_result="+(queryString.display_result=="false"?"false":"true");
                    },500);
                  }
                }
              }else if(JData["job_status"]=="2"){
                window.clearInterval(progressCheckTimer);
                $(".ui.button.kill").addClass('disabled');
                //load log 
                setTimeout(function(){
                  loadRuningLog(getQueryString().jhid);
                  $("#errorlog").show();
                },2500);                
              }
            }
          },
          error: function(xhr, ajaxOptions, thrownError) {
            alert(thrownError);
          }
        });
      }
    }
    /**
     * load Running Log in modal
     * @param  {[type]} jhid [job history id]
     * 
     */
    function loadRuningLog(jhid) {
        $.ajax({
            url: '../control/job/log/' + jhid,
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != "error")
                    $(".ui.runing.log").html(JData["message"]);
                else
                    $(".ui.runing.log").html("no log");
            },
            error: function(xhr, ajaxOptions, thrownError) {
              console.log(thrownError);
            }
        });
    }
    function getTemplateList(){
      var templates={};
      var urlp=getQueryString();
      Object.keys(urlp).map(function(key, index) {
        if(checkReservedWord(key))
          templates[key]=urlp[key];        
      });
      return templates;
    }
    function checkReservedWord(word){
      switch(word) {
          case "sql":
              return false
              break;
          case "jid":
              return false
              break;
          case "jhid":
              return false
              break;
          case "next":
              return false
              break;
          default:
              return true
              break;
      } 
    }
    /**
     * Get Status html
     * @param  {[type]} status [status 0->Run;1->Success;2->Fail]
     * 
     */
    function getStatus(status) {
        if (status == "0") {
            return '<div style="width:100%;text-align: center; " class="ui label orange"><i style="float:left" class="plane icon loading"></i> Run</div>';
        } else if (status == "1") {
            return '<div style="width:100%;text-alagn:center" class="ui label green"><i style="float:left" class="checkmark icon"></i>Success</div>';
        } else {
            return '<div style="width:100%;text-align:center" class="ui label red"><i style="float:left" class="fire icon"></i>Fail</div>';
        }
    }
        /**
     * Get presto button 
     * @param  {str} prestoID  [presto id]
     * @param  {str} prestoURL [presto job url]
     * @return {str}           [html code]
     */
    function getPrestoButton(prestoID,prestoURL){
        return '<a class="ui button" onclick="openPrestoWeb(\''+prestoURL+'\')"><i class="icon linkify"></i>'+prestoID+'</a>';
    }
        /**
     * Open Presto job page in new tab
     * @param  {str} prestoURL [presto job url]
     */
    function openPrestoWeb(prestoURL){
        var newwin = window.open();
        newwin.location = prestoURL;
    }
    function viewResult(){
      if(getQueryString().jhid!=null){
        window.location.href="../resultview#"+getQueryString().jhid;  
      }
    }
  </script>
</body>

</html>
