import com.chickling.maintenance.DBmaintenance;
import com.chickling.boot.Init;


/**
 * Created by jw6v on 2016/1/13.
 */
public class MaintenanceTest {
    public static void main(String args[]){
        Init.setExpiration("7");
        Init.setSqliteName("PrestoJobPortal.sqlite");
        DBmaintenance dbm=new DBmaintenance();
        dbm.jobHistoryMaintain();
    }
}
