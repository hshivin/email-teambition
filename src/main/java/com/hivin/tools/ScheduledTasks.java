package com.hivin.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
  Logger LOGGER = Logger.getLogger(ScheduledTasks.class);

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

  @Autowired
  AnalyzeMail analyzeMail;

//  @Scheduled(cron = "0 15 10 * * ?")
  @Scheduled(fixedRate = 1000*60*60)
  public void reportCurrentTime() {
    LOGGER.info("The time is now: " + dateFormat.format(new Date()));
//    analyzeMail.sendTeambition();
  }
}