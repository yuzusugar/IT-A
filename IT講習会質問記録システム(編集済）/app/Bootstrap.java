import models.*;
import play.*;
import play.jobs.*;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        // 初回起動
        if (User.count() == 0) {
            User _admin = new User("admin", "admin", "システム管理者");
            _admin.is_system_admin = true;
            _admin.save();
        }
    }
}
