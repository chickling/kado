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
  
  

  <div class="ui fixed  menu" style="z-index:2000">
    <div class="ui container" style="width:100%;">
      <a class="header item" >
        <i class="icon file excel outline large"></i>
        Job Result Viewer
      </a>
      <#-- <a  class="item" > <i class="icon large arrow left" style="margin-right: 0px;"></i></a> -->
      <a class="item" id="project"> <i class="icon large sidebar" style="margin-right: 0px;"></i></a>
      
      
      
      <a  class="item download" > <i class="icon large cloud download" ></i>Download</a>
      
      
      
       <div class="right menu">
        <a  class="item previous" id="project"> <i class="icon large angle left" ></i>Previous</a>
        <a  class="item pagestatus" id="project">Page ? of ?</a>
        <a  class="item next" id="project"> Next<i class="icon large angle right" ></i></a>
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
  <div class="pusher" style="">
  
    <!-- Site content !-->
    <div class="ui main container" style="width:100%;overflow: auto;">
<div class="ui active inverted dimmer">
    <div class="ui text large loader">Loading</div>
  </div>
      <table class="ui striped table selectable celled " style="">
        
  <thead>
   
  </thead>
  <tbody>
    
  </tbody>
</table>




  </div>
   
  </div>
  

 
  <script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/jquery.fileDownload.js"></script>
  <script src="dist/account.js"></script>
  <script type="text/javascript">
  
    fixHeight();
    $("#project").click(function(){
        $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
    });
    $('.ui.dropdown').dropdown();
    
    $( document ).ready(function() {
      if(getUrlStatus()!=""){
        loadResultPage(getUrlStatus(),1);
        $(".item.download").attr("onclick","downloadCSV("+getUrlStatus()+")");
      }else{
        $(".dimmer").hide();
      }    
    });
    function loadResultPage(jhid,page){
      $(".dimmer").show();
      $.ajax({
        url: './control/get/result/'+jhid+'/'+page, 
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
              //check system limit
              
              if(JData["resultCount"]>${limit}){
                $(".item.download").html('<font color="red">The result rows is out of system limit, just display ${limit} row</font>');
                $(".item.download").attr("onclick","");
              }
              //update page info
              $(".item.previous").attr("onclick","loadResultPage("+jhid+","+getPrevious(page)+");");
              $(".item.pagestatus").html("Page "+JData["nowPage"]+" of "+JData["pageCount"]);
              $(".item.next").attr("onclick","loadResultPage("+jhid+","+getNext(page,JData["pageCount"])+");");
              //update table 
              var tableHtml="";  
              var tableHeader="<tr>";
              tableHeader+="<th>#</th>";
              for(var i=0;i<JData["header"].length;i++){
                tableHeader+="<th>"+JData["header"][i]+"</th>";
              } 
              tableHeader+="</tr>";
              for(var i=0;i<JData["row"].length;i++){                
                tableHtml+='<tr>';
                tableHtml+='<td>'+(JData["startRow"]+i)+'</td>';
                for(var j=0;j<JData["row"][i].length;j++){
                  tableHtml+='<td>'+JData["row"][i][j]+'</td>';
                }  
                tableHtml+='</tr>';
              }
              $(".ui.table thead").html(tableHeader);
              $(".ui.table tbody").html(tableHtml);
              $(".dimmer").hide();
          }else{
              alert("["+JData["status"]+"]\n"+JData["message"]);
              $(".dimmer").hide();
            }
          }

        },
        error:function(xhr, ajaxOptions, thrownError){
          alert(xhr.status+"-"+thrownError);
          $(".dimmer").hide();
        }
      });
    }
    function getPrevious(nowPage){
      return (nowPage>1)?nowPage-1:1;
    }
    function getNext(nowPage,allPage){
      return (nowPage<allPage)?nowPage+1:allPage;
    }
    function fixHeight(){
      var body=$(window).height();
      var nav= $(".fixed").height();
      $("body").height(body-nav);
      $(".ui.main.container").height(body-nav);
    }
    function getUrlStatus() {
      var hash =location.hash;
      hash=hash.replace(/#/,"");
      return hash;
    }
    function downloadCSV(jhid){
      $.fileDownload('./query/get/result/file/'+jhid+'/'+$.cookie('token'))
      .done(function () {  })
      .fail(function () { alert('File download failed!'); });
    }
  </script>
</body>

</html>
