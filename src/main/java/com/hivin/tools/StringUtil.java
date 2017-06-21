package com.hivin.tools;

/**
 * Created by LP-566 on 2016/7/14.
 */
public class StringUtil {
  public static String handleStr(String content) {
    String res = "";
    content = content.replaceAll("----.+\\n?", "");
    content = content.replaceAll("W:.+\\n?", "");
    content = content.replaceAll("A:.+\\n?", "");
    content = content.replaceAll("F:.+\\n?", "");
    res = content.replaceAll("\\n", "").replaceAll("\\t", "").replaceAll("\\r","");


//    content = content.replaceAll("<html>.+</html>", "");
    return res;
  }

}
