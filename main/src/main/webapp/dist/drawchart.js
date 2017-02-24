//Draw Chart 
function drawChart(jhid,chartSetting){
  chartSettingObject=chartSetting!=null?chartSetting:getChartSettingObject("");
  if(chartSettingObject!=0){
    var drawRequest={};
    if(chartSettingObject.chart.type=="line"||chartSettingObject.chart.type=="bar"){
      drawRequest["xAxis"]=chartSettingObject.data_setting.xAxis.column.name;
      drawRequest["yAxis"]=chartSettingObject.data_setting.yAxis.column.map(function(value){
        return value.name;
      });
      drawRequest["sort"]="";
      drawRequest["limit"]=1000;

      //POST to get draw data
      $.ajax({
         url: '../chart/draw/'+jhid,
         data: JSON.stringify(drawRequest),
         beforeSend: function(request) {
             request.setRequestHeader("Authorization", $.cookie('token'));
         },
         type: "POST",
         dataType: 'json',
         contentType: "application/json; charset=utf-8",
         success: function(JData) {
             if (JData["status"] != null) {
                 if (JData["status"] == "success") {
                    if(JData["count"]>0){
                      console.log(JData["data"]);
                      if(chartSettingObject.chart.type=="line"){
                        drawLineChart(chartSettingObject,JData["data"]);
                      }else if(chartSettingObject.chart.type=="bar"){
                        drawBarChart(chartSettingObject,JData["data"]);
                      }

                    }else{
                      alert("Data count = 0");
                    }
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
    }else if(chartSettingObject.chart.type=="pie"){          
      drawRequest["axis"]=chartSettingObject.data_setting.yAxis.column.map(function(value){
        return value.name;
      });
      drawRequest["limit"]=1;
      //POST to get draw data
      $.ajax({
         url: '../chart/draw/pie/'+jhid,
         data: JSON.stringify(drawRequest),
         beforeSend: function(request) {
             request.setRequestHeader("Authorization", $.cookie('token'));
         },
         type: "POST",
         dataType: 'json',
         contentType: "application/json; charset=utf-8",
         success: function(JData) {
             if (JData["status"] != null) {
                 if (JData["status"] == "success") {
                    if(JData["count"]>0){
                      drawPieChart(chartSettingObject,JData["data"][0])
                    }else{
                      alert("Data count = 0");
                    }
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
    }

  }
}
function drawBarChart(chartSetting,data){     
  d3.selectAll("#chart1 svg").remove();
  $("#chart1").html('<svg style="width:100%;height: 300px;"></svg>');
  nv.addGraph(function() {
    $("#chart1 svg").css("height",($(window).height()-47-240-80)+"px");
    var chart = nv.models.multiBarChart()
      .transitionDuration(350)
      .reduceXTicks(true)   //If 'false', every single x-axis tick label will be rendered.
      .rotateLabels(0)      //Angle to rotate x-axis labels.
      .showControls(chartSetting.chart.setting["showControls"])   //Allow user to switch between 'Grouped' and 'Stacked' mode.
      .groupSpacing(0.1)    //Distance between each group of bars.
    ;
    var datas=buildBarChartData(chartSetting,data);
    var xAxisArray=[];
    var xAxisArrayIndex=[];
    datas.map(function(dvalue,dindex){
      xAxisArray=dvalue.values.map(function(value,index){
        var temp=value.x.toString();
        datas[dindex].values[index].x=index;
        if(dindex==0)
          xAxisArrayIndex.push(index)
        return temp;
      });
    });
    chart.xAxis
        .tickValues(xAxisArrayIndex)
        .tickFormat(function(d){
          return xAxisArray[d]!=null?xAxisArray[d]:""
        });

    chart.yAxis
        .tickFormat(d3.format(',.1f'));
    chart.margin({top: 5, right: 15, bottom: 20, left: 100});
    d3.select('#chart1 svg')
        .datum(buildBarChartData(chartSetting,data))
        .call(chart);

    nv.utils.windowResize(chart.update);

    return chart;
  });
}
function drawLineChart(chartSetting,data){      
  d3.selectAll("#chart1 svg").remove();
  $("#chart1").html('<svg style="width:100%;height: 300px;"></svg>');
  nv.addGraph(function() {
    $("#chart1 svg").css("height",($(window).height()-47-240-80)+"px");
    var chart = nv.models.lineChart()
      .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
      .transitionDuration(350)  //how fast do you want the lines to transition?
      .showLegend(chartSetting.chart.setting["showLegend"])       //Show the legend, allowing users to turn on/off line series.
      .showYAxis(true)        //Show the y-axis
      .showXAxis(true)        //Show the x-axis
      .x(function(d) { return d.x}) 
      .y(function(d) { return d.y})

    var datas=buildBarChartData(chartSetting,data);
    var xAxisArray=[];
    var xAxisArrayIndex=[];
    datas.map(function(dvalue,dindex){
      xAxisArray=dvalue.values.map(function(value,index){
        var temp=value.x.toString();
        datas[dindex].values[index].x=index;
        if(dindex==0)
          xAxisArrayIndex.push(index)
        return temp;
      });
    });

    chart.xAxis
        .tickValues(xAxisArrayIndex)
        .tickFormat(function(d){
          return xAxisArray[d]!=null?xAxisArray[d]:""
        });

    chart.yAxis
        .tickFormat(d3.format(',.1f'));
    chart.margin({top: 5, right: 40, bottom: 20, left: 100});
    d3.select('#chart1 svg')
        .datum(datas)
        .call(chart);

    nv.utils.windowResize(chart.update);

    return chart;
  });
}
function drawPieChart(chartSetting,data){
  d3.selectAll("#chart1 svg").remove();
  $("#chart1").html('<svg style="width:100%;height: 300px;"></svg>');
  nv.addGraph(function() {
    $("#chart1 svg").css("height",($(window).height()-47-240-80)+"px");
    var chart = nv.models.pieChart()
      .x(function(d) { return d.label })
      .y(function(d) { return d.value })
      .showLabels(chartSetting.chart.setting["showLabels"])     //Display pie labels
      .labelThreshold(.05)  //Configure the minimum slice size for labels to show up
      .labelType("percent") //Configure what type of data to show in the label. Can be "key", "value" or "percent"
      .donut(true)          //Turn on Donut mode. Makes pie chart look tasty!
      .donutRatio(0.35)     //Configure how big you want the donut hole size to be.
      ;
    
    d3.select("#chart1 svg")
        .datum(pieChartData(chartSetting,data))
        .transition().duration(350)
        .call(chart);



    nv.utils.windowResize(chart.update);

    return chart;
  });
}
function pieChartData(chartSetting,data){
  return chartSetting.data_setting.yAxis.column.map(function(value,index){
      return {
        "label":value.display_name,
        "value":data[value.name]
      }
  });
}
function buildBarChartData(chartSetting,data){
  return chartSetting.data_setting.yAxis.column.map(function(value,index){
    return{
      key: value.display_name,
      values: data[value.name]
    }
  })
}
function stringToNumber(str){
  return str.split('')
  .map(function (char) {
    return char.charCodeAt(0);
  })
  .reduce(function (current, previous) {
    return previous + current;
  });
}