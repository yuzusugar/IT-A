package controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.nio.charset.Charset;
import java.io.*;
import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.data.validation.*;

@With(Secure.class)
public class QuestionHistory extends Controller {

    public static void index() {
        List<QuestionData> qData_list = QuestionData.getLatestList();
        List<Location> location_list = Location.getActiveList();

        String yyyymm = session.get("question_history_month");
        String id_of_Location_str = session.get("question_history_location");
        if (yyyymm != null && id_of_Location_str != null) {
//          Date now = Calendar.getInstance().getTime();
//          DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/mm");
//          yyyymm = dateTimeFormat.format(now);
            String year = yyyymm.split("/")[0];
            String month = yyyymm.split("/")[1];

            Long id_of_Location = Long.parseLong(id_of_Location_str);
            Calendar cal_start = Calendar.getInstance();
            cal_start.set(Calendar.YEAR, Integer.parseInt(year));
            cal_start.set(Calendar.MONTH, Integer.parseInt(month) - 1);
            int day_start = cal_start.getActualMinimum(Calendar.DAY_OF_MONTH);
            cal_start.set(Calendar.DAY_OF_MONTH, day_start);
            cal_start.set(Calendar.SECOND, 0);
            cal_start.set(Calendar.MILLISECOND, 0);
            cal_start.set(Calendar.HOUR_OF_DAY, 0);
            cal_start.set(Calendar.MINUTE, 0);
            Calendar cal_end = Calendar.getInstance();
            cal_end.set(Calendar.YEAR, Integer.parseInt(year));
            cal_end.set(Calendar.MONTH, Integer.parseInt(month) - 1);
            int day_end = cal_end.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal_end.set(Calendar.DAY_OF_MONTH, day_end + 1);
            cal_end.set(Calendar.SECOND, 0);
            cal_end.set(Calendar.MILLISECOND, 0);
            cal_end.set(Calendar.HOUR_OF_DAY, 0);
            cal_end.set(Calendar.MINUTE, 0);

            List<QuestionData> qData_month_list = QuestionData.getMonthList(cal_start.getTime(), cal_end.getTime(), id_of_Location);

            renderArgs.put("id_of_Location", id_of_Location);
            renderArgs.put("qData_month_list", qData_month_list);
            session.remove("question_history_month");
            session.remove("question_history_location");
        }

        List<String> yyyymm_list = QuestionData.getMonthSelectList();

        render(qData_list, yyyymm, yyyymm_list, location_list);
    }

    public static void index_month(String yyyymm, String id_of_Location) {
        session.put("question_history_month", yyyymm);
        session.put("question_history_location", id_of_Location);
        index();
    }

    @Check("QuestionItemManager")
    public static void download_csv(String yyyymm, Long id_of_Location) {
        if (yyyymm == null || id_of_Location == null) {
            index();
        }
        String year = yyyymm.split("/")[0];
        String month = yyyymm.split("/")[1];
        Calendar cal_start = Calendar.getInstance();
        cal_start.set(Calendar.YEAR, Integer.parseInt(year));
        cal_start.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        int day_start = cal_start.getActualMinimum(Calendar.DAY_OF_MONTH);
        cal_start.set(Calendar.DAY_OF_MONTH, day_start);
        cal_start.set(Calendar.SECOND, 0);
        cal_start.set(Calendar.MILLISECOND, 0);
        cal_start.set(Calendar.HOUR_OF_DAY, 0);
        cal_start.set(Calendar.MINUTE, 0);
        Calendar cal_end = Calendar.getInstance();
        cal_end.set(Calendar.YEAR, Integer.parseInt(year));
        cal_end.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        int day_end = cal_end.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal_end.set(Calendar.DAY_OF_MONTH, day_end + 1);
        cal_end.set(Calendar.SECOND, 0);
        cal_end.set(Calendar.MILLISECOND, 0);
        cal_end.set(Calendar.HOUR_OF_DAY, 0);
        cal_end.set(Calendar.MINUTE, 0);

        List<QuestionData> qData_month_list = QuestionData.getMonthList(cal_start.getTime(), cal_end.getTime(), id_of_Location);
        Location location = Location.findById(id_of_Location);

        StringBuffer sb = new StringBuffer();
        sb.append("No,ロケーション,時刻,質問,質問詳細,コメント,対応時間,登録者,IPアドレス\n");
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        for (QuestionData qData : qData_month_list) {
            QuestionItem qItem = QuestionItem.findById(qData.id_of_QuestionItem);
            QuestionItem qItemParent = QuestionItem.findById(qItem.id_of_Parent);
            if(qData.comment == null){ qData.comment = ""; }
            String parentQuestion = "-";
            if(qItemParent != null){
            	parentQuestion = qItemParent.question;
            }
            sb.append(qData.id + "," + location.name + "," + dateTimeFormat.format(qData.timestamp) + "," + parentQuestion + "," + qItem.question + "," + qData.comment + "," + qData.time+"," + qData.user_id + "," + qData.remoteAddress + "\n");
        }

        Calendar cal_now = Calendar.getInstance();
        DateFormat dateTimeFormat_filename = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
        String filename = dateTimeFormat_filename.format(cal_now.getTime()) + ".csv";
        renderBinary(new ByteArrayInputStream(sb.toString().getBytes(Charset.forName("Shift_JIS"))), filename, "application/csv", false);
    }




    @Check("QuestionItemManager")
    public static void history_import(String question_history) {
        if (question_history == null) {
            flash.put("error", "必要なデータが入力されていません");
            index();
        }

        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        List<String> error_list = new ArrayList<>();
        for (String line : question_history.split("\n|\r")) {
            String[] csv = (line + ",").split(",");

            if (csv.length < 3) {
                continue;
            }
            try {
                Date date = dateTimeFormat.parse(csv[0]);
                QuestionItem qItem = QuestionItem.find("byQuestion", csv[2]).first();
                Location location = Location.find("byName", csv[1]).first();
                Long time = Long.parseLong(csv[3]);
                if (qItem == null || location == null) {
                    throw new Exception("質問が不明．または場所が不明");
                }

                new QuestionData(date, qItem.id, location.id, "admin", "", time, "127.0.0.1").save();
            } catch (Exception ex) {
                error_list.add(line);
            }
        }

        if (error_list.size() > 0) {
            flash.put("error", "インポートできなかった行があります．<br />" + error_list);
        }

        flash.put("success", "インポートが完了しました");
        index();
    }

}
