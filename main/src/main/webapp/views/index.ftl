<!DOCTYPE html>
<html>
<head>
  <!-- Standard Meta -->
  <meta charset="utf-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">

  <!-- Site Properities -->
  <title>Anti-Crawler Dashboard</title>

  <link rel="stylesheet" type="text/css" href="dist/components/reset.css">
  <link rel="stylesheet" type="text/css" href="dist/components/site.css">

  <link rel="stylesheet" type="text/css" href="dist/components/container.css">
  <link rel="stylesheet" type="text/css" href="dist/components/grid.css">
  <link rel="stylesheet" type="text/css" href="dist/components/header.css">
  <link rel="stylesheet" type="text/css" href="dist/components/image.css">
  <link rel="stylesheet" type="text/css" href="dist/components/menu.css">
  <link rel="stylesheet" type="text/css" href="dist/components/sidebar.css">
  <link rel="stylesheet" type="text/css" href="dist/components/divider.css">
  <link rel="stylesheet" type="text/css" href="dist/components/list.css">
  <link rel="stylesheet" type="text/css" href="dist/components/segment.css">
  <link rel="stylesheet" type="text/css" href="dist/components/dropdown.css">
  <link rel="stylesheet" type="text/css" href="dist/components/icon.css">
  <link rel="stylesheet" type="text/css" href="dist/semantic.css">
  <link href="build/nv.d3.css" rel="stylesheet" type="text/css">
  <script src="dist/d3.min.js" charset="utf-8"></script>
  <script src="build/nv.d3.js"></script>
  
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
      <#-- <div class="ui simple dropdown item">
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
      </div> -->
      
      <div class=" right ui simple dropdown item">
       <i class="icon user large"></i><font id="displayName">Eugene</font><i class="dropdown icon"></i>
        <div class="menu">          
          <a class="item" href="#" id="fullName">Eugene.Y.Yan<br>Dev</a>
          <div class="divider"></div>
          <div class="header">Account</div>   
          <a class="item" href="usermanage" id="accountManage"><i class="icon unlock alternate"></i>Account Manage</a>
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

    
    <table class="ui celled table">
  <thead>
    <tr><th><div class="ui ribbon label">First</div>Org info</th>
    <th>Total PageView</th>
    <th>IP Range</th>
    <th>Total PageView</th>
    <th>IP</th>
    <th>Page View</th>
    <th>Session Count</th>
    <th>PageView/Session</th>
  </tr></thead>
  <tbody>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
    <tr>
      <td>Amazon</td>
      <td>304000</td>
      <td>10.1.1.0-10.1.1.24</td>
      <td>158000</td>
      <td>10.1.1.1</td>
      <td>54000</td>
      <td>100</td>
      <td>540</td>
    </tr>
  </tbody>
  <tfoot>
    <tr><th colspan="8">
      <div class="ui right floated pagination menu">
        <a class="icon item">
          <i class="left chevron icon"></i>
        </a>
        <a class="item">1</a>
        <a class="item">2</a>
        <a class="item">3</a>
        <a class="item">4</a>
        <a class="icon item">
          <i class="right chevron icon"></i>
        </a>
      </div>
    </th>
  </tr></tfoot>
</table>
<div id="chart" style="height: 500px;">
    <svg></svg>
</div>
  </div>
<div class="ui inverted vertical footer segment"  style="margin-top: 0px;">
      <div class="ui center aligned container">
        <div class="ui stackable inverted divided grid">
          <div class="three wide column">
            <h4 class="ui inverted header"></h4>
            <div class="ui inverted link list">
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
            </div>
          </div>
          <div class="three wide column">
            <h4 class="ui inverted header"></h4>
            <div class="ui inverted link list">
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
            </div>
          </div>
          <div class="three wide column">
            <h4 class="ui inverted header"></h4>
            <div class="ui inverted link list">
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
              <a href="#" class="item"></a>
            </div>
          </div>
          <div class="seven wide column">
            <h4 class="ui inverted header"></h4>
            <p></p>
          </div>
        </div>
        <div class="ui inverted section divider"></div>
        <img src="logo.png" class="ui centered mini image">
        <div class="ui horizontal inverted small divided link list">
          <a class="item" href="http://www.newegg.com">Newegg Company 2016</a>  
          <a class="item" >TC Big Data Team</a> 
        </div>
      </div>
    </div>
  </div>
  

 
  <script src="dist/jquery.js"></script>
  <script src="dist/semantic.js"></script>
  <script src="dist/jquery.cookie.js"></script>
  <script src="dist/jquery.md5.js"></script>
  <script src="dist/account.js"></script>
  <script type="text/javascript">
  
    d3.json('views/cumulativeLineData.json', function(data) {
  nv.addGraph(function() {
    var chart = nv.models.cumulativeLineChart()
                  .x(function(d) { return d[0] })
                  .y(function(d) { return d[1]/100 }) //adjusting, 100% is 1.00, not 100 as it is in the data
                  .color(d3.scale.category10().range())
                  .useInteractiveGuideline(true)
                  ;

     chart.xAxis
        .tickValues([1078030800000,1122782400000,1167541200000,1251691200000])
        .tickFormat(function(d) {
            return d3.time.format('%x')(new Date(d))
          });

    chart.yAxis
        .tickFormat(d3.format(',.1%'));

    d3.select('#chart svg')
        .datum(data)
        .call(chart);

    //TODO: Figure out a good way to do this automatically
    nv.utils.windowResize(chart.update);

    return chart;
  });
});
    $("#project").click(function(){
        $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
    });
    $('.ui.dropdown')
  .dropdown()
;
$('#pg').progress();
  </script>
</body>

</html>
