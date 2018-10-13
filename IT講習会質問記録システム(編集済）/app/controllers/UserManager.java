package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class UserManager extends Controller {

    @Check("UserManager")
    public static void index(Boolean all) {
        List<User> user_list;
        if (all != null && all == true) {
            user_list = User.findAll();
        } else {
            all = false;
            user_list = User.getActiveList();
        }

        render(user_list, all);
    }

    @Check("UserManager")
    public static void view_add_user() {
        render();
    }

    @Check("UserManager")
    public static void add_user_from_tsv(String tsv) {
        /*
        * ユーザーID    氏名    メールアドレス
        */
        int _count = 0;
        int _exist = 0;
        String error_message = "";
        for(String line :tsv.split("\r|\n")){
            if(line.equals("")){ continue; }
            if(line.startsWith("#")){ continue; }
            line = line + "\t";
            String[] csv = line.split("\t");
            if(csv.length <= 1){
                error_message += line + "<br />";
                continue;
            }

            // 重複チェック
            if(User.find("byUser_id", csv[0]).first() != null){ _exist++; continue; }

            User _user = new User(csv[0]);
            _user.fullname = csv[1];
            _user.save();
            _count++;
        }

        if(error_message.length() > 0 && _exist == 0){
            flash.put("error", "解釈できない行があります．<br />" + error_message);
        }
        if(error_message.length() == 0 && _exist > 0){
            flash.put("error", _exist + " 件のユーザーは既に存在します．");
        }
        if(error_message.length() > 0 && _exist > 0){
            flash.put("error", "解釈できない行があります．<br />" + error_message + "" + _exist + " 件のユーザーは既に存在します．");
        }

        if(_count > 0){
            flash.put("success", _count+" 件のユーザーを登録しました");
        }
        index(false);
    }


    @Check("UserManager")
    public static void add_local_user_from_tsv(String tsv) {
        /*
        * ユーザーID    氏名    パスワード
        */
        int _count = 0;
        int _exist = 0;
        String error_message = "";
        for(String line :tsv.split("\r|\n")){
            if(line.equals("")){ continue; }
            if(line.startsWith("#")){ continue; }
            line = line + "\t";
            String[] csv = line.split("\t");
            if(csv.length <= 2){
                error_message += line + "<br />";
                continue;
            }

            // 重複チェック
            if(User.find("byUser_id", csv[0]).first() != null){ _exist++; continue; }

            User _user = new User(csv[0], csv[2], csv[1]);
            _user.save();
            _count++;
        }

        if(error_message.length() > 0 && _exist == 0){
            flash.put("error", "解釈できない行があります．<br />" + error_message);
        }
        if(error_message.length() == 0 && _exist > 0){
            flash.put("error", _exist + " 件のユーザーは既に存在します．");
        }
        if(error_message.length() > 0 && _exist > 0){
            flash.put("error", "解釈できない行があります．<br />" + error_message + "" + _exist + " 件のユーザーは既に存在します．");
        }

        if(_count > 0){
            flash.put("success", _count+" 件のユーザーを登録しました");
        }
        index(false);
    }

    public static void profile() {
        User user = Security.getSigninUser();
        render(user);
    }

    @Check("UserManager")
    public static void profile_by_admin(Long id_of_User){
        if(id_of_User == null){
            flash.put("error", "不明なアクセスです");
            index(false);
        }
        User user = User.findById(id_of_User);
        if(user == null){
            flash.put("error", "不明なアクセスです");
            index(false);
        }
        render(user);
    }

    public static void change_profile(@Valid User fUser, String old_pass, String new_pass1, String new_pass2) {
        if(validation.hasErrors()){
            flash.put("error", "項目にエラーがあります");
            profile();
        }

        User user = Security.getSigninUser();
        user.fullname = fUser.fullname;

        if(old_pass != null && old_pass.length() > 0){
            if(!new_pass1.equals(new_pass2)){
                flash.put("error", "新しいパスワードが一致しません");
                profile();
            }

            if(!user.password.equals(User.getPasswordHash(old_pass))){
                flash.put("error", "古いパスワードが一致しません");
                profile();
            }

            user.password = User.getPasswordHash(new_pass1);
            user.save();
        }

        user.save();
        flash.put("success", "変更しました");
        profile();
    }

    @Check("UserManager")
    public static void change_profile_by_admin(@Valid User fUser, String new_pass1, String new_pass2) {
        if(validation.hasErrors()){
            flash.put("error", "項目にエラーがあります");
            profile_by_admin(fUser.id);
        }

        User user = User.findById(fUser.id);
        user.fullname = fUser.fullname;

        if(new_pass1 != null && new_pass1.length() > 0){
            if(!new_pass1.equals(new_pass2)){
                flash.put("error", "新しいパスワードが一致しません");
                profile_by_admin(fUser.id);
            }

            user.password = User.getPasswordHash(new_pass1);
            user.save();
        }

        user.save();
        flash.put("success", "変更しました");
        profile_by_admin(fUser.id);
    }

    @Check("UserManager")
    public static void delete_user(Long id_of_User){
        if(id_of_User == null){
            renderText("不明なアクセスです");
        }
        User user = User.findById(id_of_User);
        if(user == null){
            renderText("不明なアクセスです");
        } else {
            if(Permission.getListByUserID(user.user_id).isEmpty() && user.id != 1L){
                user.is_delete = true;
                user.save();
                renderText("無効化済み");
            } else {
                renderText("管理権限を持つユーザーは無効化できません");
            }
        }
    }
    @Check("UserManager")
    public static void activate_user(Long id_of_User){
        if(id_of_User == null){
            renderText("不明なアクセスです");
        }
        User user = User.findById(id_of_User);
        if(user == null){
            renderText("不明なアクセスです");
        } else {
            user.is_delete = false;
            user.save();
            renderText("有効化済み");
        }
    }
}
