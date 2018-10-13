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
public class QuestionItem extends Model {

    @Required
    public Long id_of_Location;
    
    public Long id_of_Parent = null;

    @Required
    public String question;

    public Boolean is_hide = false;
    
    public Boolean is_comment_field = true;
    
    public Long sort_priority = 0L;

    // id_of_Parentが0Lだとroot
    public QuestionItem(Long id_of_Location, Long id_of_Parent, String question) {
        this.id_of_Location = id_of_Location;
        this.id_of_Parent = id_of_Parent;
        this.question = question;
    }

    public static List<QuestionItem> getRoot(Long id_of_Location){
        return QuestionItem.find("id_of_Location = ?1 and id_of_Parent = 0L and is_hide = false order by sort_priority asc", id_of_Location).fetch();
    }

    public List<QuestionItem> getChild(){
        return QuestionItem.find("id_of_Parent = ?1 and is_hide = false order by sort_priority asc", this.id).fetch();
    }
    
    public Boolean hasChild(){
    	QuestionItem qItem = QuestionItem.find("byId_of_ParentAndIs_hide", this.id, false).first();
    	if(qItem == null){
    		return false;
    	} else {
    		return true;
    	}
    }

    public QuestionItem getParent(){
    	if(this.id_of_Parent != null){
    		return QuestionItem.findById(id_of_Parent);
    	}
      return null;
    }
    
    public Boolean getIs_comment_field(){
    	if(this.is_comment_field == null){
    		this.is_comment_field = true;
    		this.save();
    	}
    	return this.is_comment_field;
    }
    
    public Long getSort_priority(){
    	if(this.sort_priority == null){
    		this.sort_priority = this.id * 5L;
    		this.save();
    	}
    	return this.sort_priority;
    }

    // 既に記録があったら削除できないようにする
    public QuestionItem delete(){
        return null;
    }

}
