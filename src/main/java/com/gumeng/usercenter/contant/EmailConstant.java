package com.gumeng.usercenter.contant;

/**
 * @author 顾梦
 * @description 邮件模板
 * @since 2023/8/3
 */
public interface EmailConstant {

    String EMATL_SUBJECT = "悦配MATE-验证码";

    String EMAIL_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <meta charset=\"UTF-8\">\n" +
            "  <title>验证码通知</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "  <h1>验证码通知</h1>\n" +
            "  <p>尊敬的用户，</p>\n" +
            "  <p>您的验证码为：<strong>[%s]</strong></p>\n" +
            "  <p>此验证码用于验证您的身份和操作，请勿将验证码告知他人。</p>\n" +
            "  <p>如果您未进行任何操作，请忽略此邮件。</p>\n" +
            "  <p>谢谢！</p>\n" +
            "</body>\n" +
            "</html>";
}
