package com.hivin.tools;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class AnalyzeMail {

    static Logger LOGGER = Logger.getLogger(AnalyzeMail.class);
    private MimeMessage mimeMessage = null;
    private String saveAttachPath = "";//附件下载后的存放目录
    private StringBuffer bodytext = new StringBuffer();
    private String bodycontent = new String();
    //存放邮件内容的StringBuffer对象
    private String dateformat = "yyyy-MM-dd HH:mm:ss";//默认的日前显示格式

    @Value("${endtime.file}")
    private String endTimeFile;

    @Value("${starttime.file}")
    private String starttimeFile;

    @Value("${switch.file}")
    private String switchFile;

    @Value("${email.to}")
    private String emailTo;

    @Autowired
    HttpSend httpSend;


    /**
     * 构造函数,初始化一个MimeMessage对象
     */
    public AnalyzeMail() {
    }

    public AnalyzeMail(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;

    }


    public void setMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }


    /**
     * 获得发件人的地址和姓名
     */
    public String getFrom() throws Exception {
        InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
        String from = address[0].getAddress();
        if (from == null) from = "";
        String personal = address[0].getPersonal();
        if (personal == null) personal = "";
        String fromaddr = personal + "<" + from + ">";
        return fromaddr;
    }

    /**
     * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同
     * "to"----收件人 "cc"---抄送人地址 "bcc"---密送人地址
     */


    public String getMailAddress(String type) throws Exception {
        String mailaddr = "";
        String addtype = type.toUpperCase();
        InternetAddress[] address = null;
        if (addtype.equals("TO") || addtype.equals("CC") || addtype.equals("BCC")) {
            if (addtype.equals("TO")) {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
            } else if (addtype.equals("CC")) {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
            } else {
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
            }
            if (address != null) {
                for (int i = 0; i < address.length; i++) {
                    String email = address[i].getAddress();
                    if (email == null) email = "";
                    else {
                        email = MimeUtility.decodeText(email);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null) personal = "";
                    else {
                        personal = MimeUtility.decodeText(personal);
                    }
                    String compositeto = personal + "<" + email + ">";
                    mailaddr += "," + compositeto;
                }
                if (mailaddr.length() > 1) {
                    mailaddr = mailaddr.substring(1);
                }

            }
        } else {
            throw new Exception("Error emailaddr type!");
        }
        return mailaddr;
    }


    /**
     * 获得邮件主题
     */


    public String getSubject() throws MessagingException {
        String subject = "";
        try {
            subject = MimeUtility.decodeText(mimeMessage.getSubject());
            if (subject == null) subject = "";
        } catch (Exception exce) {
        }
        return subject;
    }


    /**
     * 获得邮件发送日期
     */


    public String getSentDate() throws Exception {
        Date sentdate = mimeMessage.getSentDate();
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        return format.format(sentdate);
    }


    /**
     * 获得邮件正文内容
     */


    public String getBodyText() {
        return bodytext.toString();
    }


    /**
     * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
     * 主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */


    public void getMailContent(Part part) throws Exception {
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1) conname = true;
//    System.out.println("CONTENTTYPE: " + contenttype);
        if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
            bodycontent = (String) part.getContent();
        } else if (part.isMimeType("text/html") && !conname) {
//      bodytext.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
//      System.out.println("--------counts-------" + counts);
            getMailContent((Part) multipart.getBodyPart(0));
            for (int i = 0; i < counts; i++) {
//        getMailContent((Part) multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent((Part) part.getContent());
        }
    }


    /**
     * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
     */
    public boolean getReplySign() throws MessagingException {
        boolean replysign = false;
        String needreply[] = mimeMessage.getHeader("Disposition-Notification-To");
        if (needreply != null) {
            replysign = true;
        }
        return replysign;
    }


    /**
     * 获得此邮件的Message-ID
     */
    public String getMessageId() throws MessagingException {
        return mimeMessage.getMessageID();
    }


    /**
     * 【判断此邮件是否已读，如果未读返回返回false,反之返回true】
     */
    public boolean isNew() throws MessagingException {
        boolean isnew = false;
        Flags flags = ((Message) mimeMessage).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        System.out.println("flags's length: " + flag.length);
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isnew = true;
                System.out.println("seen Message.......");
                break;
            }
        }
        return isnew;
    }


    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachflag = false;
        String contentType = part.getContentType();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE))))
                    attachflag = true;
                else if (mpart.isMimeType("multipart/*")) {
                    attachflag = isContainAttach((Part) mpart);
                } else {
                    String contype = mpart.getContentType();
                    if (contype.toLowerCase().indexOf("application") != -1) attachflag = true;
                    if (contype.toLowerCase().indexOf("name") != -1) attachflag = true;
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachflag = isContainAttach((Part) part.getContent());
        }
        return attachflag;
    }


    /**
     * 【保存附件】
     */


    public void saveAttachMent(Part part) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE)))) {
                    fileName = mpart.getFileName();
                    if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mpart.getInputStream());
                } else if (mpart.isMimeType("multipart/*")) {
                    saveAttachMent(mpart);
                } else {
                    fileName = mpart.getFileName();
                    if ((fileName != null) && (fileName.toLowerCase().indexOf("GB2312") != -1)) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mpart.getInputStream());
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent());
        }
    }


    /**
     * 【设置附件存放路径】
     */


    public void setAttachPath(String attachpath) {
        this.saveAttachPath = attachpath;
    }


    /**
     * 【设置日期显示格式】
     */


    public void setDateFormat(String format) throws Exception {
        this.dateformat = format;
    }


    /**
     * 【获得附件存放路径】
     */


    public String getAttachPath() {
        return saveAttachPath;
    }


    /**
     * 【真正的保存附件到指定目录里】
     */


    private void saveFile(String fileName, InputStream in) throws Exception {
        String osName = System.getProperty("os.name");
        String storedir = getAttachPath();
        String separator = "";
        if (osName == null) osName = "";
        if (osName.toLowerCase().indexOf("win") != -1) {
            separator = "\\";
            if (storedir == null || storedir.equals(""))
                storedir = "c:\\tmp";
        } else {
            separator = "/";
            storedir = "/tmp";
        }
        File storefile = new File(storedir + separator + fileName);
        System.out.println("storefile's path: " + storefile.toString());
//for(int i=0;storefile.exists();i++){
//storefile = new File(storedir+separator+fileName+i);
//}
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(storefile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }
        } catch (Exception exception) {
            System.out.println("保存失败");
        } finally {
            if (bos != null) bos.close();
            if (bis != null) bis.close();
        }
    }

    public static Date stringToDate(String strDate, String dateFormat) {
        Date date = null;
        if (StringUtils.isNotBlank(strDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            try {
                date = sdf.parse(strDate);
            } catch (ParseException e) {
                System.out.println("日期转换错误" + e);
            }
        }
        return date;
    }

    public void sendTeambition(String username, String password) {
        try {
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            Properties props = System.getProperties();
            props.setProperty("mail.imap.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.imap.socketFactory.fallback", "false");
            props.setProperty("mail.store.protocol", "imap");
            props.setProperty("mail.imap.host", "imap.exmail.qq.com");
            props.setProperty("mail.imap.port", "993");
            props.setProperty("mail.imap.socketFactory.port", "993");

            // 创建Session实例对象
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imap");
            session.setDebug(true);


            store.connect(username, password);

            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message message[] = folder.getMessages();
            LOGGER.info("email total=" + message.length);
            AnalyzeMail pmm = null;
            boolean switchT = Boolean.parseBoolean(FileHelper.readFileByLines(switchFile));

            if (!switchT) {
                System.exit(0);
            }


            String textTime = FileHelper.readFileByLines(endTimeFile);
            Date endTime = stringToDate(textTime, "yyyy-MM-dd HH:mm:ss");

            Date starttime = null;
            String starttimeText = FileHelper.readFileByLines(starttimeFile);
            if (starttimeText != null || starttimeText.length() != 0) {
                starttime = stringToDate(starttimeText, "yyyy-MM-dd HH:mm:ss");
            }


            for (int count = message.length, i = count - 1; i >= count - 2000; i--) {

                pmm = new AnalyzeMail((MimeMessage) message[i]);
                pmm.setDateFormat("yyyy-MM-dd HH:mm:ss");

                Date sendTime = stringToDate(pmm.getSentDate(), "yyyy-MM-dd HH:mm:ss");

                if (starttime == null) {
                    if (endTime.getTime() > sendTime.getTime()) {
                        AnalyzeMail m = new AnalyzeMail((MimeMessage) message[count - 1]);
                        String maxTime = m.getSentDate();
                        LOGGER.info("maxTime=" + maxTime);
                        FileHelper.writeIntoFile(endTimeFile, maxTime);
                        break;
                    }
                } else {
                    if (endTime.getTime() < sendTime.getTime()) {
                        continue;
                    } else if (starttime.getTime() > sendTime.getTime()) {
                        break;
                    }
                }


                String bugsMail = pmm.getMailAddress("to");
                String subject = pmm.getSubject();
                String fromSender = pmm.getFrom();
                if (!bugsMail.contains(emailTo) || subject.contains("回复") || subject.contains("Re:")) {
                    continue;
                }

                pmm.getMailContent((Part) message[i]);
//      System.out.println("----bodycontent---------"+pmm.bodycontent);

//      String content = pmm.getBodyText();
                String content = pmm.bodycontent;
                content = StringUtil.handleStr(content);
                if (content.contains("From:") && content.contains("To:") && content.contains("Subject:")) {
                    continue;
                }

                LOGGER.info("email from=" + pmm.getFrom());
                LOGGER.info("email subject=" + pmm.getSubject());
                LOGGER.info("email senddate=" + pmm.getSentDate());
                LOGGER.info("email content=" + content);

                content = fromSender + "-----" + content;
                httpSend.appadd(content);

            }
            FileHelper.writeIntoFile(switchFile, "false");

        } catch (Exception e) {
            LOGGER.info("----" + e);
        }
    }


}