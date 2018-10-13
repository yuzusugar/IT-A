package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

@With(Secure.class)
public class Application extends Controller {

    public static void index() {
      List<Location> location_list = Location.getActiveList();　//location_listにLocation.getActiveList()を代入
      render(location_list);　//location_listを受けわたす
    }

    public static void selected_location(Long id_of_Location){
        Location location = Location.findById(id_of_Location);
        List<QuestionItem> qitem_list = QuestionItem.getRoot(id_of_Location);　//QuestionItemに親リストを代入

        List<QuestionData> qData_list = QuestionData.getLatestListByLocation(id_of_Location);　//QuestionDataに最新のLicatonを代入

        // コメント登録後の遷移か？
        if(session.get("id_of_QuestionData") != null){
            renderArgs.put("id_of_QuestionData", session.get("id_of_QuestionData"));　
            session.remove("id_of_QuestionData");
        }

        render(location, qitem_list, qData_list);
    }

    public static void selected_root_question(Long id_of_Location, Long id_of_QuestionItem){
        if(id_of_Location == null){
            index(); //場所が無ければ、indexを呼び出す
        }
        if(id_of_QuestionItem == null){
            flash.put("error", "項目を選択してください");
            selected_location(id_of_Location);
            //QuestionItemが無ければ、select_locationへ
        }
        Location location = Location.findById(id_of_Location);
        QuestionItem qitem_root = QuestionItem.findById(id_of_QuestionItem);
        List<QuestionItem> qitem_list = qitem_root.getChild();
        render(location, qitem_root, qitem_list);　//ロケーション、親項目、詳細項目を送る
    }

    public static void add_question_data(QuestionData fQuestionData, Long id_of_RootQuestionItem){
        User user = Security.getSigninUser();
        Location location = Location.findById(fQuestionData.id_of_Location);
        QuestionItem qitem_root = QuestionItem.findById(id_of_RootQuestionItem);
        QuestionData qData = new QuestionData(fQuestionData, user.user_id, request.remoteAddress).save();


        QuestionItem qItem = QuestionItem.findById(fQuestionData.id_of_QuestionItem);
        if(!qItem.is_comment_field){
        	flash.put("info", qItem.question + " を記録しました");
            selected_location(qData.id_of_Location);
        }

        render(location, qitem_root, qData);
    }

    public static void comment_editor(Long id_of_QuestionData){
        QuestionData qData = QuestionData.findById(id_of_QuestionData);
        Location location = Location.findById(qData.id_of_Location);
        QuestionItem qitem_root = qData.getQuestionItem().getParent();
        renderTemplate("Application/add_question_data.html", location, qitem_root, qData);　//add_question_data.html.
    }

    public static void add_comment(Long id_of_QuestionData, String comment){
        QuestionData qData = QuestionData.findById(id_of_QuestionData);
        qData.comment = comment;
        qData.save();
        session.put("id_of_QuestionData", id_of_QuestionData);
        selected_location(qData.id_of_Location);
    }


    public static void cancel_add_question_data(Long id_of_QuestionData){
        if(id_of_QuestionData == null){
            flash.put("error", "不正なアクセスです");
            index();
        }

        QuestionData qData = QuestionData.findById(id_of_QuestionData);
        if(qData == null){
            flash.put("error", "不正なアクセスです");
            index();
        }
        qData.is_cancel = true;
        qData.save();
        flash.put("info", "キャンセルしました．慌てないで．");
        selected_location(qData.id_of_Location);
    }
}
