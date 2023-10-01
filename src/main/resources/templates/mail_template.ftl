<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
<div class="page flex-col">
    <div class="box_3 flex-col"
         style="  display: flex;  position: relative;  width: 100%;  height: 206px;  background: #ef859d2e;  top: 0;  left: 0;  justify-content: center;">
        <div class="section_1 flex-col"
             style="  background-image: url(&quot;${model.avatar}&quot;);  position: absolute;  width: 152px;  height: 152px;  display: flex;  top: 130px;  background-size: cover;">
        </div>
    </div>
    <div class="box_4 flex-col"
         style="  margin-top: 92px;  display: flex;  flex-direction: column;  align-items: center;">
        <div class="text-group_5 flex-col justify-between"
             style="  display: flex;  flex-direction: column;  align-items: center;  margin: 0 20px;">
            <span class="text_1" style="  font-size: 26px;  font-family: PingFang-SC-Bold, PingFang-SC;  font-weight: bold;  color: #000000;  line-height: 37px;  text-align: center;">嘿！你在&nbsp;悦配MATE&nbsp;站点中收到一条新回复。</span>

            <#if model.parentNick??>
                <span class="text_2" style="  font-size: 16px;  font-family: PingFang-SC-Bold, PingFang-SC;  font-weight: bold;
                 color: #00000030;  line-height: 22px;  margin-top: 21px;  text-align: center;">你之前的评论&nbsp;在&nbsp;${model.postName}&nbsp;
                    文章中收到来自&nbsp;${model.nick}&nbsp;的回复
                </span>
            <#else>
                <span class="text_2" style="  font-size: 16px;  font-family: PingFang-SC-Bold, PingFang-SC;  font-weight: bold;
                 color: #00000030;  line-height: 22px;  margin-top: 21px;  text-align: center;">你的&nbsp;${model.postName}&nbsp;
                    文章中收到来自&nbsp;${model.nick}&nbsp;的评论
                </span>
            </#if>

        </div>
        <div class="box_2 flex-row"
             style="  margin: 0 20px;  min-height: 128px;  background: #F7F7F7;  border-radius: 12px;  margin-top: 34px;  display: flex;  flex-direction: column;  align-items: flex-start;  padding: 32px 16px;  width: calc(100% - 40px);">
            <#if model.parentNick??>
                <div class="text-wrapper_4 flex-col justify-between"
                     style="  display: flex;  flex-direction: column;  margin-left: 30px;  margin-bottom: 16px;"><span
                            class="text_3"
                            style="  height: 22px;  font-size: 16px;  font-family: PingFang-SC-Bold, PingFang-SC;  font-weight: bold;  color: #C5343E;  line-height: 22px;">${model.parentNick}</span><span
                            class="text_4"
                            style="  margin-top: 6px;  margin-right: 22px;  font-size: 16px;  font-family: PingFangSC-Regular, PingFang SC;  font-weight: 400;  color: #000000;  line-height: 22px;">${model.parentComment}</span>
                </div>
            </#if>
            <hr
                    style="    display: flex;    position: relative;    border: 1px dashed #ef859d2e;    box-sizing: content-box;    height: 0px;    overflow: visible;    width: 100%;">
            <div class="text-wrapper_4 flex-col justify-between"
                 style="  display: flex;  flex-direction: column;  margin-left: 30px;">
                <hr><span class="text_3"
                          style="  height: 22px;  font-size: 16px;  font-family: PingFang-SC-Bold, PingFang-SC;  font-weight: bold;  color: #C5343E;  line-height: 22px;">${model.nick}</span><span
                        class="text_4"
                        style="  margin-top: 6px;  margin-right: 22px;  font-size: 16px;  font-family: PingFangSC-Regular, PingFang SC;  font-weight: 400;  color: #000000;  line-height: 22px;">${model.comment}</span>
            </div><a class="text-wrapper_2 flex-col"
                     style="  min-width: 106px;  height: 38px;  background: #ef859d38;  border-radius: 32px;  display: flex;  align-items: center;  justify-content: center;  text-decoration: none;  margin: auto;  margin-top: 32px;"
                     href="${model.postUrl}"><span class="text_5" style="  color: #DB214B;">查看详情</span></a>
        </div>
        <div class="text-group_6 flex-col justify-between"
             style="  display: flex;  flex-direction: column;  align-items: center;  margin-top: 34px;"><span
                    class="text_6"
                    style="  height: 17px;  font-size: 12px;  font-family: PingFangSC-Regular, PingFang SC;  font-weight: 400;  color: #00000045;  line-height: 17px;">此邮件由评论服务自动发出，直接回复无效。</span><a
                    class="text_7"
                    style="  height: 17px;  font-size: 12px;  font-family: PingFangSC-Regular, PingFang SC;  font-weight: 400;  color: #DB214B;  line-height: 17px;  margin-top: 6px;  text-decoration: none;"
                    <#--改为上线的域名-->
                    href="https://yp.jishuqin.cn">前往网站</a></div>
    </div>
</div>
</body>
</html>