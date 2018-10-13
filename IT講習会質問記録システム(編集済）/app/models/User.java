package models;

import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;
import play.data.validation.*;

/**
 *
 * @author maruyama
 */
@Entity
public class User extends Model {

    public String user_id;

    @Password
    public String password;
    public String fullname;
    public Date regist_date;

    public enum AuthType {Local, ActiveDirectory}
    public AuthType auth_type;
    public boolean is_system_admin = false;
    public boolean is_delete;

    // ローカルユーザー
    public User(String user_id, String password, String fullname) {
        this.user_id = user_id;
        this.password = getPasswordHash(password);
        this.fullname = fullname;
        this.auth_type = AuthType.Local;
        this.is_delete = false;
        this.regist_date = Calendar.getInstance().getTime();
    }

    // ADユーザー
    public User(String user_id) {
        this.user_id = user_id;
        this.auth_type = AuthType.ActiveDirectory;
        this.is_delete = false;
        this.regist_date = Calendar.getInstance().getTime();
    }

    public static List<User> getActiveList() {
        return User.find("byIs_delete", false).fetch();
    }

    public List<Permission> getPermissionList(){
        return Permission.getListByUserID(this.user_id);
    }

    public static String getPasswordHash(String password) {
        password = play.libs.Crypto.passwordHash(password);
        password = play.libs.Crypto.passwordHash(password + "AIM");
        password = play.libs.Crypto.passwordHash("IT-A" + password);
        return password;
    }

    public boolean isAnyAdmin(){
      if(is_system_admin){return true;}

      List<Permission> permission_list = Permission.find("byUser_id", this.user_id).fetch();
      if(permission_list.size() > 0){
        return true;
      } else {
        return false;
      }
    }
}
