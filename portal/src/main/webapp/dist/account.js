/**
 * Check Permission
 * @return {bool} correct [true;false:show modal]
 */
String.prototype.checkPermission = function() {
        if (this == "Permission denied" || this == "Permission Denied") {
            showPermission();
            return false;
        } else {
            return true;
        }
    }
    //load
$(document)
    .ready(function() {
        //Check User Login    
        if ($.cookie('token') != "null" && $.cookie('token') != "undefined" && $.cookie('token') != null) {
            var dname = $.cookie('username').split(".")[0];
            $("#displayName").html(dname);
            $("#fullName").html($.cookie('username') + "<br>" + $.cookie('group'));

        } else {
            $.cookie('refurl', window.location.href, {
                path: '/'
            });
            if (secondFloor())
                document.location = "../login#jump";
            else
                document.location = "./login#jump";
        }
        hideAccountManage();
    });
//logout button click
$("#logout").click(function() {
    logout();
});
/**
 * User Logout 
 */
function logout() {
    if ($.cookie('token') != null) {
        var url = "./account/logout";
        if (secondFloor())
            url = "../account/logout";
        $.ajax({
            url: url,
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        $.removeCookie('token', {
                            path: '/'
                        });
                        $.removeCookie('lgtime', {
                            path: '/'
                        });
                        $.removeCookie('username', {
                            path: '/'
                        });
                        $.removeCookie('group', {
                            path: '/'
                        });
                        $.removeCookie('level', {
                            path: '/'
                        });
                        $.removeCookie('uid', {
                            path: '/'
                        });
                        $.removeCookie('refurl', {
                            path: '/'
                        });
                        if (secondFloor())
                            document.location = "../login";
                        else
                            document.location = "./login";
                    } else {
                        console.log("[" + JData["status"] + "]\n" + JData["message"]);
                        $.removeCookie('token', {
                            path: '/'
                        });
                        $.removeCookie('lgtime', {
                            path: '/'
                        });
                        $.removeCookie('username', {
                            path: '/'
                        });
                        $.removeCookie('group', {
                            path: '/'
                        });
                        $.removeCookie('level', {
                            path: '/'
                        });
                        $.removeCookie('uid', {
                            path: '/'
                        });
                        $.removeCookie('refurl', {
                            path: '/'
                        });
                        if (secondFloor())
                            document.location = "../login";
                        else
                            document.location = "./login";
                    }
                }

            },

            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.status);
                alert(thrownError);
            }
        });
    } else {
        $.removeCookie('token', {
            path: '/'
        });
        $.removeCookie('lgtime', {
            path: '/'
        });
        $.removeCookie('username', {
            path: '/'
        });
        $.removeCookie('group', {
            path: '/'
        });
        $.removeCookie('level', {
            path: '/'
        });
        $.removeCookie('uid', {
            path: '/'
        });
        $.removeCookie('refurl', {
            path: '/'
        });
        document.location = "/login";
    }
}
//change password button click
$("#changePassword").click(function() {
    passwd();
});
/**
 * Show Permission Error modal
 */
function showPermission() {
    var modal = function() {
        /*
              <div class="ui basic modal permission">
          
          <div class="header">
            Permission Denied!
          </div>
          <div class="image content">
            <div class="image">
              <i class="lock icon"></i>
            </div>
            <div class="description">
              <p>You don't have a sufficient authority to access this page. Try to go back the last page or change an account</p>
            </div>
          </div>
          <div class="actions">
            
              <div class="ui red basic inverted button" onclick="history.back()">
                <i class="remove icon"></i>
                Go Back
              </div>
              <div class="ui green basic inverted button" onclick="logout()">
                <i class="sign out icon"></i>
                Change other User
              </div>
            
          </div>
        </div>
            */
    }.toString().slice(14, -3);
    if ($('.ui.modal.basic.permission').length <= 0) {
        $(".footer").html($(".footer").html() + modal);
        $('.ui.modal.basic.permission').modal({
            onHidden: function() {
                $('.ui.modal.basic.permission').remove();
            }
        });
        $('.ui.modal.basic.permission').modal('show');
        $('.lock.icon').transition('shake');
    }
}
/**
 * Change Password modal
 * @return {[type]} [description]
 */
function passwd() {
    var modal = function() {
        /*
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
            */
    }.toString().slice(14, -3);
    $(".footer").html($(".footer").html() + modal);
    $('.ui.modal.change').modal({
        onHidden: function() {

            $('.ui.modal.change').remove();
        }
    });
    $('.ui.modal.change').modal('show');
    $(".ckpassword").change(function() {
        $("#newPassword").parent().removeClass("error");
        $("#rep_newPassword").parent().removeClass("error");
        $("#oldPassword").parent().removeClass("error");
        if ($("#oldPassword").val() == "") {
            $("#oldPassword").parent().addClass("error");
        }
        if ($("#newPassword").val() == "") {
            $("#newPassword").parent().addClass("error");
        }
        if ($("#rep_newPassword").val() == "") {
            $("#rep_newPassword").parent().addClass("error");
        }
        if ($("#rep_newPassword").val() != $("#newPassword").val()) {
            $("#rep_newPassword").parent().addClass("error");
        }
    });
}
/**
 * Change Password
 * @return {[type]} [description]
 */
function changePassword() {
    if ($("#rep_newPassword").val() == $("#newPassword").val() && $("#oldPassword") != "") {
        var json = {
            "oldpassword": $.md5($("#oldPassword").val()),
            "password": $.md5($("#newPassword").val())
        };
        var url = './account/user/update/password/' + $.cookie('uid')
        if (secondFloor())
            url = '../account/user/update/password/' + $.cookie('uid')
        $.ajax({
            url: url,
            data: JSON.stringify(json),
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            type: "POST",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] == "success") {
                        $('.ui.modal.change').modal('hide');
                    } else {
                        alert("[" + JData["status"] + "]\n" + JData["message"]);
                    }
                }
            },

            error: function(xhr, ajaxOptions, thrownError) {
                alert(xhr.status + "-" + thrownError);
            }
        });
    } else {
        alert("Confirm Password.\nPasswords do not match");
    }
}
/**
 * Hide AccountManage
 * if not Admin
 * @return {[type]} [description]
 */
function hideAccountManage() {
    if ($.cookie('level') != "Admin")
        $("#accountManage").hide();
}
/**
 * Page is Second Floor?
 * @return {bool} [isSecondFloor?]
 */
function secondFloor() {
    var url = window.location.href;
    if (url.indexOf("joblist/add") >= 0)
        return true
    else if (url.indexOf("joblist/edit") >= 0)
        return true
    else if (url.indexOf("schedulelist/add") >= 0)
        return true
    else if (url.indexOf("schedulelist/edit") >= 0)
        return true
    else if (url.indexOf("realtime/query") >= 0)
        return true
    else if (url.indexOf("charts/builder") >= 0)
        return true
    else if (url.indexOf("charts/draw") >= 0)
        return true
    else
        return false
}
//title button click
$(".ui.fixed.inverted.menu .header.item").click(function() {
    if (secondFloor())
        document.location = "../index";
    else
        document.location = "./index";
});
