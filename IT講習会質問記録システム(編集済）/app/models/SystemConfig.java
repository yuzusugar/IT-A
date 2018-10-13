package models;

import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;
import play.data.validation.*;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.AuthenticationException;
import javax.naming.directory.InitialDirContext;

/**
 *
 * @author maruyama
 */
@Entity
public class SystemConfig extends Model {

    @Required
    public String LDAP_PROVIDER_URL = "";

    @Required
    public String LDAP_PRINCIPAL = "";

    @Required
    public Boolean is_ldap_test_passed = false;

    public SystemConfig(){
      this.LDAP_PROVIDER_URL = "ldap://dummy";
      this.LDAP_PRINCIPAL = "@dummy";
    }

    public static SystemConfig get(){
      SystemConfig sc;
      if(SystemConfig.count() == 0){
        sc = new SystemConfig();
        sc.save();
      } else {
        sc = SystemConfig.find("").first();
      }
      return sc;
    }

    public static void save_ldap(SystemConfig sc){
      SystemConfig _sc = SystemConfig.find("").first();

      _sc.LDAP_PROVIDER_URL = sc.LDAP_PROVIDER_URL;
      _sc.LDAP_PRINCIPAL = sc.LDAP_PRINCIPAL;
      _sc.is_ldap_test_passed = false;
      _sc.save();
    }

    public boolean test_ldap(String username, String password){
      this.is_ldap_test_passed = false;

      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, this.LDAP_PROVIDER_URL);
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, username + this.LDAP_PRINCIPAL);
      env.put(Context.SECURITY_CREDENTIALS, password);
      try {
          // 認証チェック
          InitialDirContext context = new InitialDirContext(env);
          this.is_ldap_test_passed = true;
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
      this.save();
      return this.is_ldap_test_passed;
    }

}
