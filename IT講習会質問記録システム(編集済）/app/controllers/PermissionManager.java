package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class PermissionManager extends Controller {

    @Check("PermissionManager")
    public static void index() {
        List<User> user_list = User.getActiveList();

        render(user_list);
    }

    @Check("PermissionManager")
    public static void add(Permission fPermission){
        if(Permission.isPermit(fPermission.user_id, fPermission.permit_op)){
            flash.put("error", fPermission.user_id + "に" + fPermission.permit_op + "の操作権限は存在します");
            index();
        }

        new Permission(fPermission.user_id, fPermission.permit_op).save();
        flash.put("success", fPermission.user_id + "に" + fPermission.permit_op + "の操作権限を追加しました");
        index();
    }
    @Check("PermissionManager")
    public static void delete(Permission fPermission){
        if(!Permission.isPermit(fPermission.user_id, fPermission.permit_op)){
            flash.put("error", fPermission.user_id + "に" + fPermission.permit_op + "の操作権限は存在しません");
            index();
        }

        Permission.deletePermit(fPermission.user_id, fPermission.permit_op);
        flash.put("success", fPermission.user_id + "に" + fPermission.permit_op + "の操作権限を削除しました");
        index();
    }

}
