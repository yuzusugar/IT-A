package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class QuestionItemManager extends Controller {

    @Check("QuestionItemManager")
    public static void index() {
        String id_of_Location_str = session.get("QuestionItemManager-id_of_Location");
        if(id_of_Location_str != null){
            Long id_of_Location = Long.parseLong(id_of_Location_str);
            renderArgs.put("id_of_Location", id_of_Location);
            Location location = Location.findById(id_of_Location);
            renderArgs.put("location", location);
            List<QuestionItem> qitem_list = QuestionItem.getRoot(id_of_Location);
            renderArgs.put("qitem_list", qitem_list);
        }
        
        List<Location> location_list = Location.getAllList();
        render(location_list);
    }

    @Check("QuestionItemManager")
    public static void select_Location(Long id_of_Location){
        session.put("QuestionItemManager-id_of_Location", id_of_Location);
        index();
    }

    @Check("QuestionItemManager")
    public static void create(Long id_of_Location, Long id_of_Parent, String questions){
        if(id_of_Location == null || id_of_Parent == null || questions == null){
            flash.put("error", "必要項目が入力されていません");
            index();
        }

        if(id_of_Parent == 0L){
            for(String question : questions.split("\n|\r")){
                if(question.length() < 1){ continue; }
                // 親項目もセットで保存か？
                String[] csv = question.split("%,%");
                if(csv.length >= 2){
                    QuestionItem qitemP = QuestionItem.find("byId_of_LocationAndQuestion", id_of_Location, csv[0]).first();
                    if(qitemP == null){
                        qitemP = new QuestionItem(id_of_Location, 0L, csv[0]).save();
                    }
                    new QuestionItem(id_of_Location, qitemP.id, csv[1]).save();
                } else {
                    new QuestionItem(id_of_Location, 0L, question).save();
                }
            }
            flash.put("success", "親項目を追加しました");
        } else {
            for(String question : questions.split("\n|\r")){
                if(question.length() < 1){ continue; }
                new QuestionItem(id_of_Location, id_of_Parent, question).save();
            }
            flash.put("success", "質問項目を追加しました");
        }

        index();
    }

    @Check("QuestionItemManager")
    public static void edit(Long id_of_QuestionItem){
        if(id_of_QuestionItem == null){
            flash.put("error", "必要項目が入力されていません");
            index();
        }

        QuestionItem qitem = QuestionItem.findById(id_of_QuestionItem);
        List<QuestionItem> qitem_list = QuestionItem.getRoot(qitem.id_of_Location);

        render(qitem_list, qitem);
    }

    @Check("QuestionItemManager")
    public static void edit_save(QuestionItem fQuestionItem){
        if(fQuestionItem == null){
            flash.put("error", "必要項目が入力されていません");
            index();
        }

        QuestionItem qitem = QuestionItem.findById(fQuestionItem.id);
        qitem.id_of_Parent = fQuestionItem.id_of_Parent;
        qitem.question = fQuestionItem.question;
        qitem.is_hide = fQuestionItem.is_hide;
        qitem.is_comment_field = fQuestionItem.is_comment_field;
        qitem.sort_priority = fQuestionItem.sort_priority;
        qitem.save();

        flash.put("success", "更新しました");

        edit(fQuestionItem.id);
    }
}
