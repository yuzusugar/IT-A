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
public class QuestionData extends Model {

    @Required
    public Long id_of_QuestionItem;

    @Required
    public Long id_of_Location;

    @Required
    public String user_id;

    @Required
    public String comment = "";
    
    @Required
    public String time ="";
    
    @Required
    public String guide ="";

    @Required
    public String remoteAddress;

    @Required
    public Date timestamp;

    public Boolean is_cancel = false;

    // id_of_Parentが0Lだとroot
    public QuestionData(QuestionData fQuestionData, String user_id, String remoteAddress) {
        this.id_of_QuestionItem = fQuestionData.id_of_QuestionItem;
        this.id_of_Location = fQuestionData.id_of_Location;
        this.user_id = user_id;
        this.comment = fQuestionData.comment;
        this.time = fQuestionData.time;
        this.question =fQuestionData.question;
        this.remoteAddress = remoteAddress;
        this.timestamp = Calendar.getInstance().getTime();
    }
    public QuestionData(Date timestamp, Long id_of_QuestionItem, Long id_of_Location, String user_id, String comment, Long time ,String remoteAddress){
        this.timestamp = timestamp;
        this.id_of_QuestionItem = id_of_QuestionItem;
        this.id_of_Location = id_of_Location;
        this.user_id = user_id;
        this.comment = comment;
        this.time = time;
        this.guide = guide;
        this.remoteAddress = remoteAddress;
    }

    public static List<QuestionData> getLatestList(){
      return QuestionData.find("is_cancel is false order by timestamp desc").fetch(100);
    }
    public static List<QuestionData> getLatestListByLocation(Long id_of_Location){
      return QuestionData.find("is_cancel is false and Id_of_Location is ?1 order by timestamp desc", id_of_Location).fetch(100);
    }
    public static List<QuestionData> getMonthList(Date start, Date end, Long id_of_Location){
      return QuestionData.find("is_cancel is false and timestamp between ?1 and ?2 and id_of_Location = ?3", start, end, id_of_Location).fetch();
    }

    public QuestionItem getQuestionItem(){
        return QuestionItem.findById(this.id_of_QuestionItem);
    }
    public Location getLocation(){
        return Location.findById(this.id_of_Location);
    }
    public User getUser(){
        return User.find("byUser_id", this.user_id).first();
    }

    public static List<String> getMonthSelectList(){
      QuestionData qData_old = QuestionData.find("is_cancel is false order by timestamp asc").first();
      QuestionData qData_new = QuestionData.find("is_cancel is false order by timestamp desc").first();
      List<String> month_select_list = new ArrayList<>();
      if(qData_old == null || qData_new == null){ return month_select_list; }

      Calendar cal = Calendar.getInstance();
      cal.setTime(qData_old.timestamp);
      int day_start = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
      cal.set(Calendar.DAY_OF_MONTH, day_start);
      month_select_list.add(cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1));

      Calendar cal_end = Calendar.getInstance();
      cal_end.setTime(qData_new.timestamp);
      int day_end = cal_end.getActualMinimum(Calendar.DAY_OF_MONTH);
      cal_end.set(Calendar.DAY_OF_MONTH, day_end);

      while(cal.getTimeInMillis() < cal_end.getTimeInMillis()){
        cal.add(Calendar.MONTH, 1);
        month_select_list.add(cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1));
      }

      return month_select_list;
    }
}
