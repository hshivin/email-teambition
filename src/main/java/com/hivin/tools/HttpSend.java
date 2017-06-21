package com.hivin.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpSend {
  @Value("${teambition.tasklistId}")
  String tasklistId;
  @Value("${teambition.token}")
  String token;

  public final String ADD_URL = "https://api.teambition.com/api/tasks";

  public void appadd(String content) {
    BufferedReader reader = null;
    HttpURLConnection connection = null;
    try {
      //创建连接
      URL url = new URL(ADD_URL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");
      connection.setUseCaches(false);
      connection.setInstanceFollowRedirects(true);

      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Authorization",
          "OAuth2 "+token);

      connection.connect();

      //POST请求
      OutputStreamWriter out = new OutputStreamWriter(
          connection.getOutputStream(), "UTF-8"); // utf-8编码


      JSONObject obj = new JSONObject();
      obj.element("content", content);
      obj.element("_tasklistId", tasklistId);

      out.append(obj.toString());
      out.flush();
      out.close();

      //读取响应
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String lines;
      StringBuffer sb = new StringBuffer("");
      while ((lines = reader.readLine()) != null) {
        lines = new String(lines.getBytes(), "utf-8");
        sb.append(lines);
      }


    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        if (reader != null)
          reader.close();
        if (connection != null)
          connection.disconnect();
      } catch (Exception e) {

      }

      // 断开连接

    }

  }

}
