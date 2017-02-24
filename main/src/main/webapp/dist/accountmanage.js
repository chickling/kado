//function btn click
$("#project").click(function() {
    $('.ui.sidebar').sidebar('setting', 'transition', 'scale down').sidebar('toggle');
});
$('.ui.dropdown').dropdown();
$('.menu .item').tab();
//load
$(document).ready(function() {

    loadUserList();
    loadGroupList();
});
/**
 * Get Level Name
 * @param  {num} level [0:General;1:Manager;2:Admin]
 * @return {str} level string []
 */
function getLevel(level) {
    if (level == 1) {
        return "Manager";
    } else if (level == 2) {
        return "Admin";
    } else {
        return "General";
    }
}
/**
 * Load User List to page
 * @return {[type]} [description]
 */
function loadUserList() {
    $.ajax({
        url: './account/user/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var tableHtml = "";

                    for (var i = 0; i < JData["userlist"].length; i++) {
                        tableHtml += "<tr>";
                        tableHtml += '<td>' + JData["userlist"][i]["userid"] + '</td>';
                        tableHtml += '<td><h4 class="ui header"><div class="content">' + JData["userlist"][i]["account"] + '<div class="sub header">' + JData["userlist"][i]["username"] + '</div></div></h4></td>';
                        tableHtml += '<td>' + JData["userlist"][i]["group"] + '</td>';
                        tableHtml += '<td>' + JData["userlist"][i]["email"] + '</td>';
                        tableHtml += '<td>' + getLevel(JData["userlist"][i]["level"]) + '</td>';
                        tableHtml += '<td><button class="ui labeled icon button red del user" uid="' + JData["userlist"][i]["userid"] + '"><i class="trash icon"></i>Delete</button><button class="ui labeled icon button edit user" uid="' + JData["userlist"][i]["userid"] + '"><i class="edit icon"></i>Edit</button></td>';
                        tableHtml += "</tr>";
                    }
                    $("#user_body").html(tableHtml);
                    //Edit click
                    $(".edit.user").click(function() {
                        showEditUser($(this).attr("uid"));
                    });
                    //del click
                    $(".del.user").click(function() {
                        delUser($(this).attr("uid"));
                    });
                } else {
                    if (JData["message"].checkPermission())
                        alert("[" + JData["status"] + "]\n" + JData["message"]);
                }
            }

        },

        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });

}
/**
 * Show Add user modal
 * 
 */
function showAddUser() {
    $('.ui.modal.users .header').html("Add User");
    $(".ui.modal.users .button.save").attr("onclick", "addUser();");
    $('.ui.modal.users').modal('show');
    loadGrouptoSel(0);
}
/**
 * Show Add group modal
 * @return {[type]} [description]
 */
function showAddGroup() {
    $('.ui.modal.group .header').html("Add Group");
    $(".ui.modal.group .button.save").attr("onclick", "addGroup();");
    $(".ui.modal.group [name='group']").val("");
    $(".ui.modal.group [name='group_info']").val("");
    $('.ui.modal.group').modal('show');
}
/**
 * Show Edit user modal 
 * @param  {num} uid [user id]
 */
function showEditUser(uid) {
    $(".ui.modal.users .button.save").attr("onclick", "");
    $('.ui.modal.users .header').html("Edit User Info");
    $('.ui.modal.users').modal('show');
    loadGrouptoSel(uid);
}
/**
 * Show Edit group modal 
 * @param  {num} gid [group id]
 */
function showEditGroup(gid) {
    $(".ui.modal.group .button.save").attr("onclick", "");
    $('.ui.modal.group .header').html("Edit Group Info");
    $('.ui.modal.group').modal('show');
    loadGroupInfo(gid)
}
/**
 * Clear input 
 */
function clearInput() {
    $(".ui.modal.users input").each(function(index) {

        if ($(this).attr("name") == "group" || $(this).attr("name") == "level") {
            $(this).parent().dropdown('clear');
            $(this).parent().dropdown('restore defaults');
        } else {
            $(this).val("");
        }
    });
}
/**
 * Load Group Sel to Dropdown
 * @param  {num} uid [user id]
 */
function loadGrouptoSel(uid) {
    $.ajax({
        url: './account/group/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var groupListHtml = "";
                    var defaultsel = "";
                    for (var i = 0; i < JData["grouplist"].length; i++) {
                        if (i == 0)
                            defaultsel = JData["grouplist"][i]["groupid"];
                        groupListHtml += '<div data-value="' + JData["grouplist"][i]["groupid"] + '" class="item">' + JData["grouplist"][i]["group"] + '</div>';
                    }
                    $("#group_sellist").html(groupListHtml);
                    $("#group_dropdown").dropdown('restore defaults');
                    clearInput();

                    if (uid != 0) {
                        //loading user info
                        loadUserInfo(uid);
                    }
                } else {
                    if (JData["message"].checkPermission())
                        alert("[" + JData["status"] + "]\n" + JData["message"]);
                }
            }
        },
        error: function(xhr, ajaxOptions, thrownError) {
            alert(xhr.status);
            alert(thrownError);
        }
    });
}
/**
 * Load Group List to page
 */
function loadGroupList() {
    $.ajax({
        url: './account/group/list',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    var tableHtml = "";

                    for (var i = 0; i < JData["grouplist"].length; i++) {
                        tableHtml += "<tr>";
                        tableHtml += '<td>' + JData["grouplist"][i]["groupid"] + '</td>';
                        tableHtml += '<td>' + JData["grouplist"][i]["group"] + '</td>';
                        tableHtml += '<td>' + JData["grouplist"][i]["group_info"] + '</td>';

                        tableHtml += '<td><button class="ui labeled icon button red del group" gid="' + JData["grouplist"][i]["groupid"] + '"><i class="trash icon"></i>Delete</button><button class="ui labeled icon button edit group" gid="' + JData["grouplist"][i]["groupid"] + '"><i class="edit icon"></i>Edit</button></td>';
                        tableHtml += "</tr>";
                    }
                    $("#group_body").html(tableHtml);
                    //Edit click
                    $(".edit.group").click(function() {
                        showEditGroup($(this).attr("gid"));
                    });
                    //del click
                    $(".del.group").click(function() {
                        delGroup($(this).attr("gid"));
                    });
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
/**
 * Load User Info to User edut modal
 * @param  {num} uid [user id]
 */
function loadUserInfo(uid) {
    $.ajax({
        url: './account/user/get/' + uid,
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    $(".ui.modal.users .button.save").attr("onclick", "updateUser(" + uid + ")");
                    $(".ui.modal.users [name='account']").val(JData["account"]);
                    $(".ui.modal.users [name='username']").val(JData["username"]);
                    $(".ui.modal.users [name='email']").val(JData["email"]);
                    $(".ui.modal.users [name='group']").parent().dropdown('set selected', JData["groupid"]);
                    $(".ui.modal.users [name='level']").parent().dropdown('set selected', JData["level"].toString());
                    if(JData["chartbuilder"]){
                        $(".ui.modal.users [name='chartbuilder']").parent().checkbox('check');
                    }else{
                        $(".ui.modal.users [name='chartbuilder']").parent().checkbox('uncheck');
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
/**
 * Load Group Info to Group edit modal
 * @param  {num} gid [group id]
 */
function loadGroupInfo(gid) {
    $.ajax({
        url: './account/group/list/',
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Authorization", $.cookie('token'));
        },
        dataType: 'json',
        success: function(JData) {
            if (JData["status"] != null) {
                if (JData["status"] != "error") {
                    for (var i = 0; i < JData["grouplist"].length; i++) {
                        if (JData["grouplist"][i]["groupid"] == gid) {
                            $(".ui.modal.group .button.save").attr("onclick", "updateGroup(" + gid + ")");

                            $(".ui.modal.group [name='group']").val(JData["grouplist"][i]["group"]);
                            $(".ui.modal.group [name='group_info']").val(JData["grouplist"][i]["group_info"]);
                        }
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
/**
 * Add user
 */
function addUser() {
    var addJson = {};
    var message = "";
    $(".ui.modal.users input").each(function(index) {
        if ($(this).val() == ""&& $(this).attr("name") != "chartbuilder") {
            message += " " + $(this).attr("placeholder") + " ";
        } else {
            if ($(this).attr("name") == "password") {
                addJson[$(this).attr("name")] = $.md5($(this).val());
            } else if ($(this).attr("name") == "group" || $(this).attr("name") == "level") {
                addJson[$(this).attr("name")] = parseInt($(this).val());
            } else if($(this).attr("name") == "chartbuilder"){
                addJson[$(this).attr("name")] = $(this).parent().checkbox('is checked');
            } else {
                addJson[$(this).attr("name")] = $(this).val();
            }
        }

    });
    if (message != "") {
        alert("please enter:\n" + message);
    } else {
        //POST to Add User
        $.ajax({
            url: './account/user/add',
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
                        $('.ui.modal.users').modal('hide');
                        loadUserList();
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
/**
 * Add Group
 */
function addGroup() {
    var addJson = {};
    var message = "";

    $(".ui.modal.group input").each(function(index) {
        if ($(this).val() == "") {
            message += " " + $(this).attr("placeholder") + " ";
        } else {
            addJson[$(this).attr("name")] = $(this).val();
        }
    });

    if (message != "") {
        alert("please enter:\n" + message);
    } else {
        //POST to Add User
        $.ajax({
            url: './account/group/add',
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
                        $('.ui.modal.group').modal('hide');
                        loadGroupList();
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
/**
 * Update User info
 * @param  {num} uid [user id]
 */
function updateUser(uid) {
    var addJson = {};
    var message = "";
    $(".ui.modal.users input").each(function(index) {
        //value!="" but allow password ""
        if ($(this).val() == "" && $(this).attr("name") != "password"&& $(this).attr("name") != "chartbuilder") {
            message += " " + $(this).attr("placeholder") + " ";
        } else {
            if ($(this).attr("name") == "password") {
                if ($(this).val() != "")
                    addJson[$(this).attr("name")] = $.md5($(this).val());
                else
                    addJson[$(this).attr("name")] = "";

            } else if ($(this).attr("name") == "group" || $(this).attr("name") == "level") {
                addJson[$(this).attr("name")] = parseInt($(this).val());
            } else if($(this).attr("name") == "chartbuilder"){
                addJson[$(this).attr("name")] = $(this).parent().checkbox('is checked');
            } else {
                addJson[$(this).attr("name")] = $(this).val();
            }
        }

    });
    if (message != "") {
        alert("please enter:\n" + message);
    } else {
        //POST to Add User
        $.ajax({
            url: './account/user/update/' + uid,
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
                        $('.ui.modal.users').modal('hide');
                        loadUserList();
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
    //alert(JSON.stringify(addJson));
}
/**
 * Update Group info
 * @param  {num} gid [group id]
 */
function updateGroup(gid) {
    var addJson = {};
    var message = "";

    $(".ui.modal.group input").each(function(index) {
        if ($(this).val() == "") {
            message += " " + $(this).attr("placeholder") + " ";
        } else {
            addJson[$(this).attr("name")] = $(this).val();
        }
    });

    if (message != "") {
        alert("please enter:\n" + message);
    } else {
        //POST to Add User
        $.ajax({
            url: './account/group/update/' + gid,
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
                        $('.ui.modal.group').modal('hide');
                        loadGroupList();
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
/**
 * Delete User
 * @param  {num} uid [user id]
 */
function delUser(uid) {
    if (confirm('Are you sure you want to delete User?')) {
        $.ajax({
            url: './account/user/delete/' + uid,
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        loadUserList();
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
/**
 * Delete Group
 * @param  {num} gid [group id]
 */
function delGroup(gid) {
    if (confirm('Are you sure you want to delete User?')) {
        $.ajax({
            url: './account/group/delete/' + gid,
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Authorization", $.cookie('token'));
            },
            dataType: 'json',
            success: function(JData) {
                if (JData["status"] != null) {
                    if (JData["status"] != "error") {
                        loadGroupList();
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


