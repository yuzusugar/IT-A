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
public class Location extends Model {

    @Required
    public String name;

    public Boolean is_hide = false;
    public Boolean is_delete = false;

    public Location(Location fLocation) {
        this.name = fLocation.name;
    }

    public static List<Location> getActiveList(){
        return Location.find("byIs_hideAndIs_delete", false, false).fetch();
    }
    
    public static List<Location> getAllList(){
        return Location.find("byIs_delete", false).fetch();
    }

}
