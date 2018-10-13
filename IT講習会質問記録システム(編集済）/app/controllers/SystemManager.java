package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class SystemManager extends Controller {

    @Check("SystemManager")
    public static void index() {
        SystemConfig sc = SystemConfig.get();
        render(sc);
    }

    @Check("SystemManager")
    public static void save_ldap(SystemConfig sc) {
        SystemConfig.save_ldap(sc);
        flash.put("success", "ActiveDirectory接続設定を保存しました");
        index();
    }

    @Check("SystemManager")
    public static void test_ldap(String username, String password) {
        SystemConfig sc = SystemConfig.get();
        boolean is_passwd = sc.test_ldap(username, password);
        if(is_passwd == true){
          flash.put("success", "接続テストに成功しました");
        } else {
          flash.put("error", "接続テストに失敗しました");
        }
        index();
    }
}
