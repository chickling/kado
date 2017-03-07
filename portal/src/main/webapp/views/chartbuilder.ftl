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
                <i class="icon bar chart outline large"></i> Chart Builder
            </div>
            <div class="item chartname"> <i class="icon large angle right"></i><font class="name">New Chart</font></div>
            <div class="right menu">                
                <a class="item next" id="saveChart"><i class="icon large save" ></i>Save Chart</a>
                <a class="item previous" id="buildChart" jhid=""> <i class="icon large paint brush"></i>Build Chart</a>
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
              <div class="two wide column cstyle" style="">

                <div class="ui inverted vertical pointing menu charttype" style="width: 100%;height:100%">
                  <a class="item line" id="loadLineChart">
                    <i class="line chart icon"></i>Line Chart
                  </a>
                  <a class="item active bar" id="loadBarChart">
                    <i class="bar chart icon"></i>Bar Chart
                  </a>
                  <a class="item pie" id="loadPieChart">
                    <i class="pie chart icon"></i>Pie Chart
                  </a>
                </div>
              </div>
              <div class="four wide column csetting" style="padding-left: 0px;overflow:auto">
                <!--Line Chart-->
                <div class="ui vertical menu linechart setting" style="width: 100%;">
                  <div class="item">                    
                    <i class="setting icon"></i>Line Chart Setting               
                    
                  </div>
                  <div class="item">
                    <div class="header">Chart Style</div>
                    <div class="menu chartstyle linechart">     
                      <div class="item">                                   
                        <div class="ui  toggle checkbox chartstyle" key="showLegend">
                          <input type="checkbox">
                          <label>ShowLegend</label>
                        </div>   
                      </div>             
                    </div>
                  </div>
                  <div class="item">
                    <div class="header">X Axis</div>
                    <div class="x axis items">
                    <div class="ui center aligned segment axis setting x" style="padding: 5px;">                      
                      <div class="ui floating dropdown labeled icon button " style="width:100%;text-align: center; ">
                        <i class="resize horizontal icon"></i>
                        <span class="text " >Select X Axis Column</span>
                        <div class="menu">
                          <div class="ui icon search input">
                            <i class="search icon"></i>
                            <input placeholder="Search tags..." type="text">
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="sort numeric ascending icon"></i>
                            Number Column
                          </div>
                          <div class="scrolling menu number">
                           
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="font icon"></i>
                            String Column
                          </div>
                          <div class="scrolling menu string">
                            
                          </div>
                        </div>
                      </div>
                      <div class="ui horizontal divider" style="margin-top: 5px;margin-bottom: 5px;">
                          <i class="icon random" style="transform:rotate(270deg);"></i>
                      </div>
                      <div class="ui left icon input display" style="width:100%">
                          <i class="font icon"></i>
                          <input placeholder="Display Name" type="text">
                      </div>                      
                    </div>
                    </div>

                  </div>
                  <div class="item">
                    <div class="header">Y Axis</div>
                    <div class="ui icon buttons mini " style="margin-bottom: 10px;float: right; ">                  
                      <button class="ui button icon blue addY" ctype="linechart">
                        <i class="plus icon"></i>
                      </button>
                    </div>
                    <div class="y axis items" style="padding-top: 45px">
                    <div class="ui center aligned segment axis setting y" style="padding: 5px;margin-bottom:10px" name="yaxis-0">                      
                      <div class="ui floating dropdown labeled icon button " style="width:100%;text-align: center;">
                        <i class="resize horizontal icon"></i>
                        <span class="text " >Select Y Axis Column</span>
                        <div class="menu" style="margin-bottom:7px;">
                          <div class="ui icon search input">
                            <i class="search icon"></i>
                            <input placeholder="Search tags..." type="text">
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="sort numeric ascending icon"></i>
                            Number Column
                          </div>
                          <div class="scrolling menu number">
                          
                          </div>
                        </div>
                      </div>
                      <div class="ui horizontal divider" style="margin-top: 5px;margin-bottom: 5px;">
                          <i class="icon random" style="transform:rotate(270deg);"></i>
                      </div>
                      <div class="ui left icon input display" style="width:100%">
                          <i class="font icon"></i>
                          <input placeholder="Display Name" type="text">
                      </div>    
                      <button class="circular ui icon button mini  basic" style="margin-top: 5px" onclick="delYAxisColumn('yaxis-0','linechart')">
                        <i class="icon remove tiny"></i>
                      </button>               
                    </div>
                    </div>


                  </div>
                </div>

                <!--Bar Chart-->
                <div class="ui vertical menu barchart setting" style="width: 100%;margin-top: 0px;">
                  <div class="item">                    
                    <i class="setting icon"></i>Bar Chart Setting               
                    
                  </div>
                  <div class="item">
                    <div class="header">Chart Style</div>
                    <div class="menu chartstyle barchart">     
                      <div class="item">                                   
                        <div class="ui  toggle checkbox chartstyle" key="showControls">
                          <input type="checkbox">
                          <label>ShowControls</label>
                        </div>   
                      </div>             
                    </div>
                  </div>
                  <div class="item">
                    <div class="header">X Axis</div>
                    <div class="x axis items">
                    <div class="ui center aligned segment axis setting x" style="padding: 5px;">                      
                      <div class="ui floating dropdown labeled icon button " style="width:100%;text-align: center; ">
                        <i class="resize horizontal icon"></i>
                        <span class="text " >Select X Axis Column</span>
                        <div class="menu">
                          <div class="ui icon search input">
                            <i class="search icon"></i>
                            <input placeholder="Search tags..." type="text">
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="sort numeric ascending icon"></i>
                            Number Column
                          </div>
                          <div class="scrolling menu number">
                           
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="font icon"></i>
                            String Column
                          </div>
                          <div class="scrolling menu string">
                            
                          </div>
                        </div>
                      </div>
                      <div class="ui horizontal divider" style="margin-top: 5px;margin-bottom: 5px;">
                          <i class="icon random" style="transform:rotate(270deg);"></i>
                      </div>
                      <div class="ui left icon input display" style="width:100%">
                          <i class="font icon"></i>
                          <input placeholder="Display Name" type="text">
                      </div>                      
                    </div>
                    </div>

                  </div>
                  <div class="item">
                    <div class="header">Y Axis</div>
                    <div class="ui icon buttons mini " style="margin-bottom: 10px;float: right; ">                  
                      <button class="ui button icon blue addY"  ctype="barchart">
                        <i class="plus icon"></i>
                      </button>
                    </div>
                    <div class="y axis items" style="padding-top: 45px">
                    <div class="ui center aligned segment axis setting y" style="padding: 5px;margin-bottom:10px" name="yaxis-0">                      
                      <div class="ui floating dropdown labeled icon button " style="width:100%;text-align: center;">
                        <i class="resize horizontal icon"></i>
                        <span class="text " >Select Y Axis Column</span>
                        <div class="menu" style="margin-bottom:7px;">
                          <div class="ui icon search input">
                            <i class="search icon"></i>
                            <input placeholder="Search tags..." type="text">
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="sort numeric ascending icon"></i>
                            Number Column
                          </div>
                          <div class="scrolling menu number">
                          
                          </div>
                        </div>
                      </div>
                      <div class="ui horizontal divider" style="margin-top: 5px;margin-bottom: 5px;">
                          <i class="icon random" style="transform:rotate(270deg);"></i>
                      </div>
                      <div class="ui left icon input display" style="width:100%">
                          <i class="font icon"></i>
                          <input placeholder="Display Name" type="text">
                      </div>    
                      <button class="circular ui icon button mini  basic" style="margin-top: 5px" onclick="delYAxisColumn('yaxis-0','barchart')">
                        <i class="icon remove tiny"></i>
                      </button>               
                    </div>
                    </div>


                  </div>
                </div>

                <!--Pie Chart-->
                <div class="ui vertical menu piechart setting" style="width: 100%;margin-top: 0px;">
                  <div class="item">                    
                    <i class="setting icon"></i>Pie Chart Setting               
                    
                  </div>
                  <div class="item">
                    <div class="header">Chart Style</div>
                    <div class="menu chartstyle piechart">     
                      <div class="item">                                   
                        <div class="ui  toggle checkbox chartstyle" key="showLabels">
                          <input type="checkbox">
                          <label>ShowLabels</label>
                        </div>   
                      </div>             
                    </div>
                  </div>
                  <div class="item">
                    <div class="header">Axis</div>
                    <div class="ui icon buttons mini " style="margin-bottom: 10px;float: right; ">                  
                      <button class="ui button icon blue addY" ctype="piechart">
                        <i class="plus icon"></i>
                      </button>
                    </div>
                    <div class="y axis items" style="padding-top: 45px">
                    <div class="ui center aligned segment axis setting y" style="padding: 5px;margin-bottom:10px" name="yaxis-0">                      
                      <div class="ui floating dropdown labeled icon button " style="width:100%;text-align: center;">
                        <i class="resize horizontal icon"></i>
                        <span class="text " >Select Y Axis Column</span>
                        <div class="menu" style="margin-bottom:7px;">
                          <div class="ui icon search input">
                            <i class="search icon"></i>
                            <input placeholder="Search tags..." type="text">
                          </div>
                          <div class="divider"></div>
                          <div class="header">
                            <i class="sort numeric ascending icon"></i>
                            Number Column
                          </div>
                          <div class="scrolling menu number">                          
                          </div>
                        </div>
                      </div>
                      <div class="ui horizontal divider" style="margin-top: 5px;margin-bottom: 5px;">
                          <i class="icon random" style="transform:rotate(270deg);"></i>
                      </div>
                      <div class="ui left icon input display" style="width:100%">
                          <i class="font icon"></i>
                          <input placeholder="Display Name" type="text">
                      </div>    
                      <button class="circular ui icon button mini  basic" style="margin-top: 5px" onclick="delYAxisColumn('yaxis-0','piechart')">
                        <i class="icon remove tiny"></i>
                      </button>               
                    </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="ten wide column cdisplay" style="padding-left: 0px; padding-right: 0px;">
                <div class="ui  segment" style="height: 100%;padding-top: 10px; padding-bottom: 20px;">
                  <h4 class="ui header">Chart Preview</h4>
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
      <!--  MODAL SAVE AS-->
      <div class="ui modal save_as">
        <i class="close icon"></i>
        <div class="header">
          Save New Chart
        </div>
        <div class="content">
          <div class="ui form  basic">            
            <div class="field">
              <label>Chart Name</label>
              <input placeholder="Input Chart Name..." name="chartname" type="text">
            </div>      
          </div>
        </div>
        <div class="actions">
          <div class="ui black deny button" onclick="$('.ui.modal.save_as').hide();">
            Cancel
          </div>
          <div class="ui red right labeled icon button" onclick="addChart()">
            Save
            <i class="save icon"></i>
          </div>
        </div>
      </div>
      <!--  MODAL -->
      <div class="ui modal save">
        <i class="close icon"></i>
        <div class="header">
          Save Chart
        </div>
        <div class="content">
          <div class="ui form  basic">            
            <div class="field">
              <label>Chart Name</label>
              <input placeholder="Input Chart Name..." name="chartname" type="text">
            </div>      
          </div>
        </div>
        <div class="actions">
          <div class="ui black deny button" onclick="$('.ui.modal.save').hide();">
            Cancel
          </div>
          <div class="ui red right labeled icon button save" onclick="updateChart()">
            Save
            <i class="save icon"></i>
          </div>
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
      $(".column.csetting").css("max-height",($(window).height()-47-240)+"px");
      $(".column.csetting").css("height",($(window).height()-47-240)+"px");
      $(".column.cstyle").css("height",($(window).height()-47-240)+"px");
      $(".column.cdisplay").css("height",($(window).height()-47-240)+"px");
      $(".ui.checkbox").checkbox();
      //load default chart type
      loadLineChart();
      //load table schema      
      var jhid=QueryString.jhid;
      var jid=QueryString.jid;
      var chartid=QueryString.chartid;
      if(jhid!=null&&(chartid==null||chartid=="")){
        
        loadTableSchemaToDropdown(jhid);
        loadResultPage(jhid,1);
        $("#buildChart").attr("jhid",jhid);

      }else if(jid!=null&&jid!=""&&chartid==null){
        
        var last_jhid=getLastHistoryID(jid);
        if(last_jhid!=0){
          loadTableSchemaToDropdown(last_jhid);
          loadResultPage(last_jhid,1);
        }
      }else if(jid!=null&&jid!=""&&chartid!=null&&chartid!=""){
        
        if(jhid!=null&jhid!=0){
          loadTableSchemaToDropdown(jhid);
          loadResultPage(jhid,1);          
          loadChartSetting(chartid,jhid);
          $("#buildChart").attr("jhid",jhid);
        }else{
          var last_jhid=getLastHistoryID(jid);
          if(last_jhid!=0){
            loadTableSchemaToDropdown(last_jhid);
            loadResultPage(last_jhid,1);          
            loadChartSetting(chartid,last_jhid);
            
          }
        }
      }
      $("#loadBarChart").click(loadBarChart);
      $("#loadLineChart").click(loadLineChart);
      $("#loadPieChart").click(loadPieChart);
      $("#saveChart").click(saveChart);
      $("#buildChart").click(function(){
        var jhid=$(this).attr("jhid");
        if(jhid!=""){
          drawChart(jhid,null);
        }
      });
      $(".button.addY").click(function(){
        addYAxisColumnSetting($(this).attr("ctype"));
      });

      
    });

    $( window ).resize( function(){
      $(".column.csetting").css("max-height",($(window).height()-47-240)+"px");
      $(".column.csetting").css("height",($(window).height()-47-240)+"px");
      $(".column.cstyle").css("height",($(window).height()-47-240)+"px");
      $(".column.cdisplay").css("height",($(window).height()-47-240)+"px");
    });
    function loadLineChart(a){
      $(".menu.linechart.setting").show();
      $(".menu.barchart.setting").hide();
      $(".menu.piechart.setting").hide();
      $(".menu.charttype .item").removeClass("active");
      $(".menu.charttype .item.line").addClass("active");
    }
    function loadBarChart(a){
      $(".menu.linechart.setting").hide();
      $(".menu.barchart.setting").show();
      $(".menu.piechart.setting").hide();
      $(".menu.charttype .item").removeClass("active");
      $(".menu.charttype .item.bar").addClass("active");
    }
    function loadPieChart(a){
      $(".menu.linechart.setting").hide();
      $(".menu.barchart.setting").hide();
      $(".menu.piechart.setting").show();
      $(".menu.charttype .item").removeClass("active");
      $(".menu.charttype .item.pie").addClass("active");
    }

    function addYAxisColumnSetting(charttype){
      yid++;
      $("."+charttype+".setting .axis.y.items").append('<div class="ui center aligned segment axis setting y" name="yaxis-'+yid+'" style="padding: 5px; margin-bottom: 10px">'+$("."+charttype+".setting .axis.y.setting.segment").html()+'</div>');
      $('.'+charttype+'.setting .axis.y.items [name="yaxis-'+yid+'"] .dropdown').dropdown('clear');
      $('.'+charttype+'.setting .axis.y.items [name="yaxis-'+yid+'"] .dropdown').dropdown('set text',"Select Y Axis Column");
      $('.'+charttype+'.setting .axis.y.items [name="yaxis-'+yid+'"] .button.mini.basic').attr('onclick',"delYAxisColumn('yaxis-"+yid+"','"+charttype+"')");
    }
    function delYAxisColumn(name,charttype){
      if($("."+charttype+".setting .axis.y.setting.segment").length>=2){
        $('.'+charttype+'.setting .axis.y.items [name="'+name+'"]').remove();
      }else{
        alert("At least one Column!");
      }
    }
    function getChaerStyle(charttype){
      var chartStyle={};
      if(charttype=="linechart"||charttype=="barchart"||charttype=="piechart"){
        $.each($("."+charttype+".menu.chartstyle .item"),function(index,value){
          chartStyle[$(value).children(".checkbox").attr("key")]=$(value).children(".checkbox").checkbox("is checked");
        });        
      }
      return chartStyle;
    }
    function getXAxisQbject(charttype){
      var value=$("."+charttype+".setting .axis.x.setting.segment");
      var selitem=$(value).children(".dropdown").dropdown('get text');
      var display_name=$(value).children(".ui.icon.input.display").children("input").val();
      if(selitem!="Select X Axis Column"){
        return {
          name:selitem,
          type:$(value).children(".dropdown").dropdown('get value'),
          display_name:display_name!=""?display_name:selitem
        }
      }else{
        alert("please select X Axis Column!");
        return 0;
      }  
      return xAxisColumns;
    }
    function getYAxisQbject(charttype){
      var yAxisColumns=[];
      $.each($("."+charttype+".setting .axis.y.setting.segment"), function( index, value ) {
        var selitem=$(value).children(".dropdown").dropdown('get text');
        var display_name=$(value).children(".ui.icon.input.display").children("input").val();
        if(selitem!="Select Y Axis Column"){
          yAxisColumns.push({
            name:selitem,
            type:"number",
            display_name:display_name!=""?display_name:selitem
          })
        }else{
          alert("please select Y Axis Column!");
          return 0;
        }        
      });
      return yAxisColumns;
    }
    function setXAxisQbject(charttype,data_setting){
      var value=$("."+charttype+".setting .axis.x.setting.segment");
      $(value).children(".dropdown").dropdown('set text',data_setting.name);
      $(value).children(".input.display").children("input").val(data_setting.display_name);
    }
    function setYAxisQbject(charttype,data_setting){
      var datayAxisCount=data_setting.length;      
      var yAxisCount=$("."+charttype+".setting .axis.y.setting.segment").length;
      yid=yAxisCount-1;
      
      if(datayAxisCount>yAxisCount){       
        for(var i=0;i<(datayAxisCount-yAxisCount);i++){
          addYAxisColumnSetting(charttype)
        }
      }else if(datayAxisCount<yAxisCount){        
        for(var i=0;i<(yAxisCount-datayAxisCount);i++){
          $('.'+charttype+'.setting .axis.y.items [name="yaxis-'+(yAxisCount-i-1)+'"]').remove();
        }
      }
      $.each(data_setting,function(index,value){

        var item=$('.'+charttype+'.setting .axis.y.items [name="yaxis-'+index+'"]');
        $(item).children(".dropdown").dropdown('set text',value.name);
        $(item).children(".input.display").children("input").val(value.display_name);
      });
    }
    function setChaerStyle(charttype,chart_style){     
      if(charttype=="linechart"||charttype=="barchart"||charttype=="piechart"){
        $.each($("."+charttype+".menu.chartstyle .item"),function(index,value){
          var style=chart_style[$(value).children(".checkbox").attr("key")];
          if(style!=null){
            if(style==true){
              $(value).children(".checkbox").checkbox("set checked");
            }else{
              $(value).children(".checkbox").checkbox("set unchecked");
            }
          }
        });        
      }
      
    }
    function loadTableSchemaToDropdown(jhid){
      $.ajax({
        url: '../presto/table/schemas/job/result/' + jhid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != "error"){  
              var xAxisNumberItemHtml="";
              var xAxisStringItemHtml="";
              var yAxisNumberItemHtml="";            
              $.each(JData["column"],function(index,value){
                if(value["type"]=="bigint"||value["type"]=="int"||value["type"]=="double"){
                  xAxisNumberItemHtml+='<div class="item" data-value="number">'+value["column"]+'</div>';
                  yAxisNumberItemHtml+='<div class="item" data-value="number">'+value["column"]+'</div>';
                }else if(value["type"]=="varchar"){
                  xAxisStringItemHtml+='<div class="item" data-value="string">'+value["column"]+'</div>';
                }
              });
              $(".items.axis.x .dropdown .number.menu").html(xAxisNumberItemHtml);
              $(".items.axis.x .dropdown .string.menu").html(xAxisStringItemHtml);
              $(".items.axis.y .dropdown .number.menu").html(yAxisNumberItemHtml);

            }else{
                JData["message"].checkPermission()
            }
        },
        error: function(xhr, ajaxOptions, thrownError) {
          console.log(thrownError);
        }
      });
    }
    //Get Chart Setting Json
    function getChartSettingObject(name){
      var chartSetting={};
      if($(".menu.charttype .item.active").hasClass("line")){
        var chart={};
        chart["name"]=name;
        chart["type"]="line";
        chart["setting"]=getChaerStyle("linechart");

        var data_setting={};
        var xAxis=getXAxisQbject("linechart");
        var yAxis=getYAxisQbject("linechart");
        if(xAxis!=0&&yAxis!=0){
          data_setting["xAxis"]={format:",f",column:xAxis};
          data_setting["yAxis"]={format:",.1f",column:yAxis};
        }else{
          return 0;
        }

        chartSetting["chart"]=chart;
        chartSetting["data_setting"]=data_setting;      

      }else if($(".menu.charttype .item.active").hasClass("bar")){
        var chart={};
        chart["name"]=name;
        chart["type"]="bar";
        chart["setting"]=getChaerStyle("barchart");

        var data_setting={};
        var xAxis=getXAxisQbject("barchart");
        var yAxis=getYAxisQbject("barchart");
        if(xAxis!=0&&yAxis!=0){
        data_setting["xAxis"]={format:",f",column:xAxis};
        data_setting["yAxis"]={format:",.1f",column:yAxis};
        }else{
          return 0;
        }
        chartSetting["chart"]=chart;
        chartSetting["data_setting"]=data_setting;  

      }else if($(".menu.charttype .item.active").hasClass("pie")){
        var chart={};
        chart["name"]=name;
        chart["type"]="pie";
        chart["setting"]=getChaerStyle("piechart");

        var data_setting={};
        var yAxis=getYAxisQbject("piechart");
        if(yAxis!=0){
          data_setting["yAxis"]={format:",.1f",column:yAxis};
        }else{
          return 0;
        }
        chartSetting["chart"]=chart;
        chartSetting["data_setting"]=data_setting;  
      }
      return chartSetting;
    }
    //Save Chart
    var chartSettingObject={};
    function saveChart(){
      var chartSetting=getChartSettingObject("");
      if(chartSetting!=0){
        if(getQueryString().chartid!=null){
          //update chart setting
          chartSettingObject=chartSetting;
          $(".ui.modal.save").show();
        }else if(getQueryString().jid!=null){
          //add chart setting
          //get chart setting  Json
          chartSettingObject=chartSetting;
          $(".ui.modal.save_as").show();
          
        }else{

        }
      }
    }
    function addChart(){
      var addChartObject={};
      var chartName=$('.save_as [name="chartname"]').val();
      if(chartName!=null&&chartName!=""){
        addChartObject["JobID"]=getQueryString().jid;
        addChartObject["Type"]=chartSettingObject.chart.type;
        addChartObject["Chart_Name"]=chartName;
        chartSettingObject.chart.name=chartName;
        addChartObject["Chart_Setting"]=JSON.stringify(chartSettingObject);
        //POST to Update Job
        $.ajax({
           url: '../chart/manage/add/',
           data: JSON.stringify(addChartObject),
           beforeSend: function(request) {
               request.setRequestHeader("Authorization", $.cookie('token'));
           },
           type: "POST",
           dataType: 'json',
           contentType: "application/json; charset=utf-8",
           success: function(JData) {
               if (JData["status"] != null) {
                   if (JData["status"] == "success") {
                      $(".ui.modal.save_as").hide();
                      alert("Chart Save Success!");
                      window.close();
                   } else {
                       if (JData["message"].checkPermission())
                           alert("[" + JData["status"] + "]\n" + JData["message"]);
                   }
               }
           },

           error: function(xhr, ajaxOptions, thrownError) {
               alert(xhr.status + "-" + thrownError);
           }
         });
        
      }else{
        alert("Chart Name can't empty");
      }
    }
    function updateChart(chartID,jobID){
      var updateChartObject={};
      var chartName=$('.save [name="chartname"]').val();
      if(chartName!=null&&chartName!=""){
        updateChartObject["JobID"]=getQueryString().jid;
        updateChartObject["Type"]=chartSettingObject.chart.type;
        updateChartObject["Chart_Name"]=chartName;
        chartSettingObject.chart.name=chartName;
        updateChartObject["Chart_Setting"]=JSON.stringify(chartSettingObject);
        updateChartObject["ChartID"]=chartID;
        //POST to Update Job
        $.ajax({
           url: '../chart/manage/update/'+chartID+'/'+jobID,
           data: JSON.stringify(updateChartObject),
           beforeSend: function(request) {
               request.setRequestHeader("Authorization", $.cookie('token'));
           },
           type: "POST",
           dataType: 'json',
           contentType: "application/json; charset=utf-8",
           success: function(JData) {
               if (JData["status"] != null) {
                   if (JData["status"] == "success") {
                      $(".ui.modal.save").hide();
                      alert("Chart Update Success!");
                       location.reload();
                   } else {
                       if (JData["message"].checkPermission())
                           alert("[" + JData["status"] + "]\n" + JData["message"]);
                   }
               }
           },

           error: function(xhr, ajaxOptions, thrownError) {
               alert(xhr.status + "-" + thrownError);
           }
         });
        
      }else{
        alert("Chart Name can't empty");
      }
    }
    //Load Chart Setting
    function loadChartSettingToPage(chartSetting){
      if(chartSetting.chart.type=="line"){
        setXAxisQbject("linechart",chartSetting.data_setting.xAxis.column);
        setYAxisQbject("linechart",chartSetting.data_setting.yAxis.column);
        setChaerStyle("linechart",chartSetting.chart.setting);
        loadLineChart();

      }else if(chartSetting.chart.type=="bar"){
        setXAxisQbject("barchart",chartSetting.data_setting.xAxis.column);
        setYAxisQbject("barchart",chartSetting.data_setting.yAxis.column);
        setChaerStyle("barchart",chartSetting.chart.setting);
        loadBarChart();
      }else if(chartSetting.chart.type=="pie"){
       
        setYAxisQbject("piechart",chartSetting.data_setting.yAxis.column);
        setChaerStyle("piechart",chartSetting.chart.setting);
        loadPieChart();
      }
      $(".menu .chartname .name").html(chartSetting.chart.name);
      $('.modal.save [name="chartname"]').val(chartSetting.chart.name);

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
          $(".modal.save .button.save").attr('onclick',"updateChart("+JData["ChartInfo"]["ChartID"]+","+JData["ChartInfo"]["JobID"]+")");
          var chartSetting=JSON.parse( JData["ChartInfo"]["Chart_Setting"] );
          loadChartSettingToPage(chartSetting); 
          drawChart(jhid,chartSetting);
        },
        error:function(xhr, ajaxOptions, thrownError){
          alert(xhr.status+"-"+thrownError);
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
        //url: '/test.json',               
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
                $("#buildChart").attr("jhid",jhid);
              }else{
                alert("There were no query result in 7 days! ");
                window.close();
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



    </script>
</body>

</html>
