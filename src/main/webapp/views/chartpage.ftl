<!DOCTYPE html>
<html>

<head>
    <!-- Standard Meta -->
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
    <!-- Site Properities -->
    <title>Anti-Crawler Dashboard</title>
    <link rel="stylesheet" type="text/css" href="../dist/semantic.css">
    <link rel="stylesheet" type="text/css" href="../dist/nv.d3.css">
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
    .grid .column{
      padding-bottom: 0px !important;
      padding-right: 0px !important;
    }
    .ui.main.container{
      width:100%;
      margin-top:0px;
    }

    </style>
</head>

<body>
    <div class="ui fixed  menu" style="z-index:2000">
        <div class="ui container" style="width:100%;">
            <a class="item" id="project"> <i class="icon large sidebar" style="margin-right: 0px;"></i></a>
            <div class="header item">
                <i class="icon bar chart outline large"></i> Chart
            </div>
            <div class="item chartname"> <i class="icon large remove bookmark"></i><font class="name">New Chart</font></div>
            <div class="right menu">   
              <div class="ui dropdown item" id="switchChart">
                <i class="icon large exchange"></i>
                Switch Chart <i class="dropdown icon"></i>
                <div class="menu">
                  
                </div>
              </div>             
              <a class="item next" id="project">
                <div class="ui toggle checkbox" id="resultTableSwitch">
                  <input name="public" type="checkbox">
                  <label>Display Result Table</label>
                </div>
              </a>
              <a class="item previous" id="project" onclick="fullResult()"> <i class="icon large external"></i>View Full Results</a>
            </div>
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
                    <a class="item" href="../status">
          Status
        </a>
                    <a class="item" href="../joblist">
          Job list
        </a>
                    <a class="item" href="../schedulelist">
          Schedule list
        </a>
                    <a class="item" href="../queryui">
          Presto Query UI
        </a>
                </div>
            </a>
    </div>
    <div class="pusher" style="padding-top: 50px;">
        <!-- Site content !-->
        <div class="ui main container" >

            <div class="ui grid">
              <div class="sixteen wide column cdisplay" style="padding-left: 0px; padding-right: 0px;">
                <div class="ui  segment" style="height: 100%;padding-top: 10px; padding-bottom: 20px;">                  
                  <p>
                  <div id="chart1" >
                    <svg style="width:100%;height: 300px;"></svg>
                  </div></p>
                </div>
              </div>              
            </div>
            <div class="rowdata" style="width:100%;max-height:250px;overflow: auto;margin-top: 14px;">
              <div class="ui active inverted dimmer">
                <div class="ui text large loader">Loading</div>
              </div>
              <table class="ui striped table selectable celled tablesorter tablesorter-default" style="margin-top: 0px;" role="grid">
                <thead></thead>
                <tbody aria-live="polite" aria-relevant="all">
                </tbody>
              </table>

            </div>
            

           
        </div>


      <div class="footer"> </div>
    </div>
    <script src="../dist/jquery.js"></script>
    <script src="../dist/semantic.js"></script>
    <script src="../dist/jquery.cookie.js"></script>
    <script src="../dist/jquery.md5.js"></script>
    <script src="../dist/jquery.base64.js"></script>
    <script src="../dist/account.js"></script>
    <script src="../dist/querystring.js"></script>
    <script type="text/javascript" src="../dist/d3.js"></script>
    <script type="text/javascript" src="../dist/nv.d3.js"></script>
    <script src="../dist/drawchart.js"></script>
    <script type="text/javascript">
    var yid=0;
    $("#project").click(function() {
        $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
    });
    $('.ui.dropdown').dropdown();

    $(document).ready(function() {
      $(".column.cdisplay").css("height",($(window).height()-47-240)+"px");
      $(".ui.checkbox").checkbox();      
      var jhid=QueryString.jhid;
      var jid=QueryString.jid;
      var chartid=QueryString.chartid;
      if(jhid!=null&&(chartid==null||chartid=="")){
        
        //loadTableSchemaToDropdown(jhid);
        loadResultPage(jhid,1);
      }else if(jid!=null&&jid!=""&&chartid==null){
        
        var last_jhid=getLastHistoryID(jid);
        if(last_jhid!=0){
          //loadTableSchemaToDropdown(last_jhid);
          loadResultPage(last_jhid,1);
        }
      }else if(chartid!=null&&chartid!=""){
        
        if(jhid!=null&jhid!=0){
          //loadTableSchemaToDropdown(jhid);
          loadResultPage(jhid,1);          
          loadChartSetting(chartid,jhid);
        }else{
          var last_jhid=getLastHistoryID(jid);
          if(last_jhid!=0){
            //loadTableSchemaToDropdown(last_jhid);
            loadResultPage(last_jhid,1);          
            loadChartSetting(chartid,last_jhid);
            
          }
        }
      }
      //switch      
      $("#resultTableSwitch").checkbox({
        onChecked: function() {
          showResultTable();
        },
        onUnchecked:function() {
          hideResultTable();
        }
      });
    });

    $( window ).resize( function(){
      $(".column.cdisplay").css("height",($(window).height()-47-240)+"px");
    });
    //hide Result Table
    function hideResultTable(){
      $(".rowdata").hide();
      $(".ui.segment").height($(window).height()-85);
      $("svg").height($(window).height()-85-40);
      if(nv.graphs.length>0)
        nv.graphs[0].update();
      window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"display_result",false));
    }
    //Show Result Table
    function showResultTable(){
      $(".rowdata").show();          
      $(".ui.segment").height($(window).height()-85-250);
      $("svg").height($(window).height()-85-40-250);
      if(nv.graphs.length>0)
        nv.graphs[0].update();
      window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"display_result",true));
    }
    //Get Chart Setting
    function loadChartSetting(chartid,jhid){
      $.ajax({
        url: '../chart/manage/get/'+chartid, 
        //url: '/test.json',               
        type:"GET",        
        beforeSend: function (request)
        {
          request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType:'json',                
        success: function(JData){
          try {
            var chartSetting=JSON.parse( JData["ChartInfo"]["Chart_Setting"] );  
            drawChart(jhid,chartSetting);
            $(".chartname .name").html(chartSetting.chart.name);
            loadSwitchChart(JData["ChartInfo"]["JobID"],chartid);
          }catch(err) {
            alert("get chart setting error:\n"+err.message);
            alert(JData["message"]);
          }  
        },
        error:function(xhr, ajaxOptions, thrownError){
          alert("get chart setting error:\n"+xhr.status+"-"+thrownError);
        }
      });
    }
    //Load Switch Chart
    function loadSwitchChart(jid,chartid){
      $.ajax({
       url: '../chart/manage/list/' + jid,
       type: "GET",
       beforeSend: function(request) {
           request.setRequestHeader("Authorization", $.cookie('token'));
       },
       dataType: 'json',
       success: function(JData) {
          if(JData["status"]!=null){
            if(JData["status"]=="success"){
              var html='';
              for(var i=0;i<JData["ChartInfo"].length;i++){
                  if(JData["ChartInfo"][i]["ChartID"]!=chartid)
                    html+=getChartItem(JData["ChartInfo"][i]["ChartID"],JData["ChartInfo"][i]["Chart_Name"],JData["ChartInfo"][i]["Type"]);
              }
              if(html!=""){
                $("#switchChart .menu").html(html);
                $("#switchChart .menu a").click(function(){
                  var chartid=$(this).attr("chartid");
                  if(chartid!=""&&chartid!=0){
                    window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"chartid",chartid));
                    location.reload();
                  }
                });
              }else{
                $("#switchChart").hide();
              }
            }else if(JData["status"]=="failed"){
              $("#switchChart").hide();
            }else{
              alert("Get List Fail:"+JData["message"]);
            }
          }
       },
       error: function(xhr, ajaxOptions, thrownError) {
           alert(xhr.status + "-" + thrownError);
       }
      });
    }
    //Load Job Result
    function loadResultPage(jhid,page){
      $(".rowdata .dimmer").show();
      $.ajax({
        url: '../control/get/result/'+jhid+'/'+page, 
        //url: '/test.json',               
        type:"GET",
        beforeSend: function (request)
        {
          request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType:'json',                
        success: function(JData){
          if(JData["status"]!=null){
            if(JData["status"]!="error"){ 
              //update table 
              var tableHtml="";  
              var tableHeader="<tr>"; 
              for(var i=0;i<JData["header"].length;i++){
                tableHeader+="<th>"+JData["header"][i]+"</th>";
              } 
              tableHeader+="</tr>";
              for(var i=0;i<JData["row"].length;i++){                
                tableHtml+='<tr>';
                for(var j=0;j<JData["row"][i].length;j++){
                  tableHtml+='<td>'+JData["row"][i][j]+'</td>';
                }  
                tableHtml+='</tr>';
              }
              $(".rowdata .ui.table thead").html(tableHeader);
              $(".rowdata .ui.table tbody").html(tableHtml);
              $(".rowdata .dimmer").hide();
              if(getQueryString().display_result=="true"){
                $("#resultTableSwitch").checkbox("set checked");
                showResultTable();
              }else{
                $("#resultTableSwitch").checkbox("set onchecked");
                hideResultTable();
              }
          }else{
              alert("["+JData["status"]+"]\n"+JData["message"]);
              $(".rowdata .dimmer").hide();
            }
          }

        },
        error:function(xhr, ajaxOptions, thrownError){
          alert(xhr.status+"-"+thrownError);
          $(".rowdata .dimmer").hide();
        }
      });
    }
    function getLastHistoryID(jobID){
      var jhid=0;
      $.ajax({
        url: '../job/manage/run/history/has/result/'+jobID, 
        type:"GET",
        async:false,
        beforeSend: function (request)
        {
          request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType:'json',                
        success: function(JData){
          if(JData["status"]!=null){
            if(JData["status"]!="error"){ 
              if(JData["list"].length>0){
                jhid=JData["list"][0]["jhid"];
                window.history.pushState({},"", updateQueryStringParameter(window.location.href ,"jhid",jhid));                
              }else{
                alert("There were no query result in 7 days! ");
              }              
          }else{
              alert("["+JData["status"]+"]\n"+JData["message"]);
            }
          }

        },
        error:function(xhr, ajaxOptions, thrownError){
          alert(xhr.status+"-"+thrownError);
        }
      });

      return jhid;
    }
    /**
     * getChartItem to Switch Chart
     * @return {[type]} [description]
     */
    function getChartItem(chartid,name,type){
      var icon="";
      if(type=="line"){
        icon="line chart";
      }else if(type=="bar"){
        icon="bar chart";
      }else if(type=="pie"){
        icon="pie chart";
      }
      return '<a class="item" chartid="'+chartid+'"><i class="icon '+icon+'"></i> '+name+'</a>';
    }
    function fullResult(){
      window.location.href="../resultview#"+getQueryString().jhid;
    }

    </script>
</body>

</html>
