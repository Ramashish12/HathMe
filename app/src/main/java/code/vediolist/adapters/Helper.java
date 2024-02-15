package code.vediolist.adapters;


import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

public class Helper {



	public static  String Key_view = "Key_view";
	public static  String Key_desc = "Key_desc";
	public static  String Key_title = "Key_title";
	public static  String Key_subdesc = "Key_subdesc";
	public static  String Key_url = "Key_url";
	public static  String Key_time = "Key_time";
	public static  String Key_name = "Key_name";
	public static  String report = "report";
	public static  String Key_category = "Key_category";
	public static Spanned stripHtml(String html) {
		if (!TextUtils.isEmpty(html)) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
			} else {
				return Html.fromHtml(html);
			}
		}
		return null;
	}







}
