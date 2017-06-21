package com.hivin.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FileHelper {
  /**
   * 以行为单位读取文件内容
   *
   * @param fileName
   * @return
   */
  public static String readFileByLines(String fileName) {
    File file = new File(fileName);
    BufferedReader reader = null;
    StringBuffer sb = new StringBuffer();
    try {
      reader = new BufferedReader(new FileReader(file));
      String tempString = null;
      int line = 1;
      // 一次读入一行，直到读入null为文件结束
      while ((tempString = reader.readLine()) != null) {
        sb.append(tempString);
        line++;
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
        }
      }
    }
    return sb.toString();
  }

  /**
   * 返回list
   *
   * @param fileName
   * @return
   */
  public static List<String> readFileByLinesToList(String fileName) {
    List<String> list = new ArrayList<String>();
    File file = new File(fileName);
    BufferedReader reader = null;
    StringBuffer sb = new StringBuffer();
    try {
      reader = new BufferedReader(new FileReader(file));
      String tempString = null;
      int line = 1;
      while ((tempString = reader.readLine()) != null) {
        list.add(tempString);
        sb.append(tempString);
        line++;
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e1) {
        }
      }
    }
    return list;
  }

  /**
   * 写入文件
   * @param filePath
   * @param data
   */
  public static void writeIntoFile(String filePath, String data) {
    try {
      File f = new File(filePath);
      if (f.exists()) {
      } else {
        f.createNewFile();// 不存在则创建
      }
      BufferedWriter output = new BufferedWriter(new FileWriter(f,false));
      output.write(data);
      output.close();
    } catch (Exception e) {
      System.out.print("写入文件报错"+ e);
    }

  }


}