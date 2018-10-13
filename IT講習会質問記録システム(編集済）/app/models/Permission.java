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
public class Permission extends Model {

    @Required
    public String user_id;

    public enum PermitOperation {
        UserManager,
        PermissionManager,
        QuestionItemManager,
        LocationManager,
        SystemManager
    }

    @Required
    public PermitOperation permit_op;

    public Permission(String user_id, PermitOperation permit_op) {
        this.user_id = user_id;
        this.permit_op = permit_op;
    }

    public static List<Permission> getListByUserID(String user_id) {
        return Permission.find("byUser_id", user_id).fetch();
    }
    public static List<PermitOperation> getDenyListByUserID(String user_id){
        List<Permission> _p_list = Permission.find("byUser_id", user_id).fetch();
        List<PermitOperation> allow_list = new ArrayList<>();
        for(Permission _p : _p_list){
            allow_list.add(_p.permit_op);
        }

        List<PermitOperation> deny_list = new ArrayList<>();
        for(PermitOperation op : PermitOperation.values()){
            if(!allow_list.contains(op)){
                deny_list.add(op);
            }
        }
        return deny_list;
    }

    public static boolean isPermit(String user_id, PermitOperation permit_op){
        return Permission.find("byUser_idAndPermit_op", user_id, permit_op).first() != null;
    }

    public static void deletePermit(String user_id, PermitOperation permit_op){
        Permission _permit = Permission.find("byUser_idAndPermit_op", user_id, permit_op).first();
        if(_permit != null){
            _permit.delete();
        }
    }

}
