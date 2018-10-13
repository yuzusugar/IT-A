package controllers;

import models.*;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.directory.InitialDirContext;

public class Security extends Secure.Security {

    static boolean authenticate(String username, String password) {
        if (username.equals("admin")) {
            return User.find("byUser_idAndPassword", "admin", User.getPasswordHash(password)).first() != null;
        }

        // DB上にいなかったら弾く
        User _user = User.find("byUser_id", username).first();
        if(_user == null){ return false; }

        // ローカルユーザー認証
        if(_user.auth_type == User.AuthType.Local){
          String hashed_pass = User.getPasswordHash(password);
          if(_user.password.equals(hashed_pass)){
            return true;
          } else {
            return false;
          }
        }

        // LDAP接続
        if(SystemConfig.count() == 0){ return false; }
        SystemConfig _sc = SystemConfig.find("").first();
        if(_sc.is_ldap_test_passed == false){ return false; }

        // 接続オブジェクトは毎回作成，破棄する
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");// Contextファクトリ
        env.put(Context.PROVIDER_URL, _sc.LDAP_PROVIDER_URL);// 接続先URL
        env.put(Context.SECURITY_AUTHENTICATION, "simple");// セキュリティレベル
        env.put(Context.SECURITY_PRINCIPAL, username + _sc.LDAP_PRINCIPAL);// ユーザID＋ドメイン
        env.put(Context.SECURITY_CREDENTIALS, password);// パスワード
        try {
            // 認証チェック
            InitialDirContext context = new InitialDirContext(env);
            return true;
        } catch (AuthenticationException ex) {
            // 認証エラー
            String msg = ex.getExplanation();
            if (msg.indexOf("AcceptSecurityContext") > 0
                    && msg.substring(msg.indexOf("AcceptSecurityContext")).indexOf("775") > 0) {
                //System.out.println("ユーザがロックアウト");
            } else {
                //System.out.println("認証エラー");
            }
        } catch (NamingException ex) {
            // その他通信エラー等
            //ex.printStackTrace();
        }
        return false;
    }

    public static User getSigninUser() {
        return User.find("byUser_id", Security.connected()).first();
    }

    static boolean check(String profile){
        User _user = getSigninUser();
        if(_user.is_system_admin){ return true; }

        return Permission.isPermit(_user.user_id, Permission.PermitOperation.valueOf(profile));

    }
}
