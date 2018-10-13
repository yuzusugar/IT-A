package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class LocationManager extends Controller {

    @Check("LocationManager")
    public static void index() {
        List<Location> locaton_list = Location.getAllList();
        render(locaton_list);
    }


    @Check("LocationManager")
    public static void create(Location fLocation){
        if(fLocation == null || fLocation.name == null){
            flash.put("error", "必要項目が入力されていません");
            index();
        }

        new Location(fLocation).save();
        flash.put("success", "ロケーションを作成しました");
        index();
    }
    
    public static void ajax_change_name(Long id_of_Location, String name){
        if(id_of_Location == null || name == null){
            renderText("error:必要情報が入力されていません");
        }
        Location location = Location.findById(id_of_Location);
        location.name = name;
        location.save();
        renderText("success:");
    }
    
    public static void change_hide(Long id_of_Location, Boolean is_hide){
        if(id_of_Location == null || is_hide == null){
            flash.put("error", "不正なアクセスです");
            index();
        }
        Location location = Location.findById(id_of_Location);
        location.is_hide = is_hide;
        location.save();
        flash.put("success", location.name + "の表示／非表示を切り替えました");
        index();
    }

}
