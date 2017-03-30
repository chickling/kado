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



  <#-- INCLUDE NAV -->
  <#include "nav.ftl">
<div class="pusher">

  <!-- Site content !-->
  <div class="ui main container" style="padding-top: 50px;">
    <h2>Account Manage</h2>
    <p>
      <div class="ui top attached tabular menu">
        <a class="active item" data-tab="first" onclick="loadUserList();">User List</a>
        <a class="item" data-tab="second" onclick="loadGroupList();">Group List</a>
      </div>
      <div class="ui bottom attached active tab segment" data-tab="first">
        <div style="text-align:right">
          <button class="ui  button basic red" onclick="showAddUser()">
            <i class="add user icon"></i>
            Add User
          </button>
        </div>
        <table class="ui very basic  celled table">
          <thead>
            <tr><th>User ID</th>
              <th>User</th>
              <th>Group</th>
              <th>E-mail</th>
              <th>Level</th>
              <th style="width:250px">Function</th>
            </tr></thead>
            <tbody id="user_body">


            </tbody>
          </table>
        </div>
      <div class="ui bottom attached tab segment" data-tab="second">
        <div style="text-align:right">
          <button class="ui  button basic red" onclick="showAddGroup()">
            <i class="users icon"></i>
            Add Group
          </button>
        </div>
        <table class="ui very basic  celled table">
          <thead>
            <tr><th>Group ID</th>
              <th>Group</th>
              <th>Group info</th>              
              <th style="width:250px">Function</th>
            </tr>
          </thead>
            <tbody id="group_body">              
            </tbody>
          </table>
      </div>

    </p>


  </div>
  <#-- INCLUDE FOOTER -->
  <#include "footer.ftl">
</div>
<div class="ui modal users">
  <i class="close icon"></i>
  <div class="header">
    Add User
  </div>
  <div class="content"> 
  <form class="ui form">
      <h4 class="ui dividing header">User Infomation</h4>
      <div class="field">        
        <div class="two fields">
          <div class="field">
            <label>Account ID[short ID]</label>
            <input type="text" placeholder="Account ID[short ID]" name="account">
          </div>
          <div class="field">
            <label>Full Name</label>
            <input type="text" placeholder="Full name" name="username">
          </div>
        </div>
      </div>
      <div class="field">        
        <div class="fields">          
          <div class="six wide field">
            <label>Password</label>
            <input type="password" placeholder="Password" name="password">
          </div>
          <div class="five wide field">
            <label>Group</label>
            <div class="ui selection dropdown" tabindex="0" id="group_dropdown">
              <input type="hidden" name="group" placeholder="Group">
              <div class="default text">Group</div>
              <i class="dropdown icon"></i>
              <div class="menu" tabindex="-1" id="group_sellist">
                
              </div>
            </div>
          </div>
          <div class="five wide field">
            <label>Level</label>
            <div class="ui selection dropdown" tabindex="0">
              <input type="hidden" name="level" placeholder="Level">
              <div class="default text">Level</div>
              <i class="dropdown icon"></i>
              <div class="menu" tabindex="-1">
                <div data-value="1" class="item">              
                  Manager
                </div>
                <div data-value="2" class="item">              
                  Admin
                </div>
                <div data-value="0" class="item">              
                  General
                </div>
              </div>
            </div>
          </div>
          
        </div>
      </div>
      <div class="field">        
        <div class="fields">  
          <div class="twelve wide field">
            <label>Email Address</label>
            <input type="text" placeholder="Email" name="email">
          </div>
          <div class="four wide field">
            <label>Chart Builder</label>
            <div class="ui toggle checkbox">
              <input type="checkbox" name="chartbuilder">
              <label>Permissions</label>
            </div>
          </div>
        </div>
      </div>
    </form>
  </div>
  <div class="actions">
    <div class="ui black deny button">
      Cancel
    </div>
    <div class="ui right labeled icon button save" onclick="addUser()" >
      <font class="save">Save</font> 
      <i class="checkmark icon"></i>
    </div>
  </div>
</div>
<div class="ui modal group">
  <i class="close icon"></i>
  <div class="header">
    Add Group
  </div>
  <div class="content"> 
  <form class="ui form">
      <h4 class="ui dividing header">Group Infomation</h4>
      <div class="field">        
        <div class="two fields">
          <div class="field">
            <label>Group Name</label>
            <input type="text" placeholder="Group Name" name="group">
          </div>
          <div class="field">
            <label>Group Info</label>
            <input type="text" placeholder="Group info" name="group_info">
          </div>
        </div>
      </div>      
    </form>
  </div>
  <div class="actions">
    <div class="ui black deny button">
      Cancel
    </div>
    <div class="ui right labeled icon button save" onclick="addUser()" >
      <font class="save">Save</font> 
      <i class="checkmark icon"></i>
    </div>
  </div>
</div>

<script src="dist/jquery.js"></script>
<script src="dist/semantic.js"></script>
<script src="dist/jquery.cookie.js"></script>
<script src="dist/jquery.md5.js"></script>
<#-- Account -->
<script src="dist/account.js"></script>
<#-- Account Manage JS -->
<script src="dist/accountmanage.js"></script>

</body>

</html>
