<div class="ui fixed inverted menu">
    <div class="ui container" style="width:100%">
        <a  class="item" id="project"> <i class="icon large sidebar"></i></a>
        <a href="#" class="header item">
            <i class="icon send large"></i>Presto Job Manager
        </a>        
        <a href="${layer}status" class="item <#if status??>${status}</#if>"> <i class="icon large browser"></i>Status</a>
        <a href="${layer}joblist" class="item <#if job??>${job}</#if>"> <i class="icon large suitcase"></i>Job</a>
        <a href="${layer}schedulelist" class="item <#if schedule??>${schedule}</#if>"> <i class="icon large wait"></i>Schedule</a>
        <a href="${layer}queryui" class="item <#if queryui??>${queryui}</#if>"> <i class="icon large rocket"></i>QueryUI</a>
        <div class=" right ui simple dropdown item">
            <i class="icon user large"></i>
            <font id="displayName">Eugene</font><i class="dropdown icon"></i>
            <div class="menu">
                <a class="item" href="#" id="fullName">Eugene.Y.Yan<br>Dev</a>
                <div class="divider"></div>
                <div class="header">Account</div>
                <a class="item" href="${layer}usermanage" id="accountManage"><i class="icon unlock alternate"></i>Account Manage</a>
                <a class="item" id="changePassword"><i class="icon undo"></i>Change Password</a>
                <a class="item" id="logout"><i class="icon sign out"></i> Logout</a>
            </div>
        </div>
    </div>
</div>
<div class="ui sidebar inverted vertical menu" style="transition-duration: 0.25s;">
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
            <a class="item" href="${layer}status">
          Status
        </a>
            <a class="item" href="${layer}joblist">
          Job list
        </a>
            <a class="item" href="${layer}schedulelist">
          Schedule list
        </a>
            <a class="item" href="${layer}queryui">
          Presto Query UI
        </a>
        </div>
    </a>
</div>
