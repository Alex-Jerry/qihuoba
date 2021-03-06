package com.yjjr.yjfutures.utils.http;

import com.yjjr.yjfutures.BuildConfig;
import com.yjjr.yjfutures.ui.BaseApplication;

/**
 * HTTP配置
 * Created by guoziwei on 2017/7/5.
 */

public class HttpConfig {


    //    public static final String DOMAIN = "http://www.qihuofa.com";
    public static final String DOMAIN = BuildConfig.DEBUG ? "https://dev.qihuofa.com" : "https://www.qihuofa.com";
    public static final String DEMO_HOST = BuildConfig.DEBUG ? "https://dev.qihuofa.com:9101/" : "https://demo.qihuofa.com:9100/";
    public static final String BIZ_HOST = DOMAIN + ":9300";

    /**
     * socket io url
     */
    public static final String SOCKET_URL = DOMAIN + ":9093";

    public static final String IP_URL = "http://ip.taobao.com/service/getIpInfo.php?ip=myip";
    /**
     * 手机号码的正则
     */
    public static final String REG_PHONE = "^0?(13[0-9]|15[012356789]|17[0678]|18[0-9]|14[57])[0-9]{8}$";
    /**
     * 发送验证码的重试时间
     */
    public static final long SMS_TIME = 60 * 1000;
    /**
     * 《期货吧用户服务协议》
     * http://www.qihuofa.com:9100/serviceInfo/agreement.html
     * 投资人与点买人合作协议
     * http://www.qihuofa.com:9100/serviceInfo/agreement1.html
     * 《资金托管服务协议》
     * http://www.qihuofa.com:9100/serviceInfo/agreement2.html
     * 《期货吧客户协议》
     * http://www.qihuofa.com:9100/serviceInfo/agreement3.html
     * 《免责声明》
     * http://www.qihuofa.com:9100/serviceInfo/disclaimer.html
     * 风险披露
     * http://www.qihuofa.com:9100/serviceInfo/disclosure.html
     * 《隐私政策》
     * http://www.qihuofa.com:9100/serviceInfo/policy.html
     * 《资金托管服务协议》
     * http://www.qihuofa.com:9100/serviceInfo/service.html
     * 《相关监管》
     * http://www.qihuofa.com:9100/serviceInfo/supervise.html
     * 监管资质
     * https://dev.qihuofa.com:9100/serviceInfo/qualification.html
     * 关于我们
     * https://dev.qihuofa.com:9100/serviceInfo/about.html
     * 新手指导
     * https://dev.qihuofa.com:9100/serviceInfo/guide.html
     */
    public static final String URL_AGREEMENT = DOMAIN + ":9100/serviceInfo/agreement.html";
    public static final String URL_AGREEMENT1 = DOMAIN + ":9100/serviceInfo/agreement1.html";
    public static final String URL_AGREEMENT2 = DOMAIN + ":9100/serviceInfo/agreement2.html";
    public static final String URL_AGREEMENT3 = DOMAIN + ":9100/serviceInfo/agreement3.html";
    public static final String URL_DISCLAIMER = DOMAIN + ":9100/serviceInfo/disclaimer.html";
    public static final String URL_DISCLOSURE = DOMAIN + ":9100/serviceInfo/disclosure.html";
    public static final String URL_POLICY = DOMAIN + ":9100/serviceInfo/policy.html";
    public static final String URL_SERVICE = DOMAIN + ":9100/serviceInfo/service.html";
    public static final String URL_SUPERVISE = DOMAIN + ":9100/serviceInfo/supervise.html";
    public static final String URL_QUALIFICATION = DOMAIN + ":9100/serviceInfo/qualification.html";
    public static final String URL_ABOUT = DOMAIN + ":9100/serviceInfo/about.html";
    public static final String URL_GUIDE = DOMAIN + ":9100/serviceInfo/guide1.html";
    public static final String URL_CSCENTER = DOMAIN + ":9100/serviceInfo/CScenter.html";
    public static final String URL_HELP = DOMAIN + ":9100/serviceInfo/help.html";
    public static final String URL_PROMOTION = DOMAIN + ":9300/manage/promote/promotePage.do?account=";
    public static final String URL_WARNING = DOMAIN + ":9100/serviceInfo/warning.html";
    public static final String URL_RULE = DOMAIN + ":9100/serviceInfo/%srules.html";
    public static final String URL_NOTICE = DOMAIN + ":9300/service/notice/html/";
    /**
     * type=1 注册；type=2 修改手机号；type=3 找回密码
     */
    public static final int TYPE_REGISTER = 1;
    public static final int TYPE_ALTER_PHONE = 2;
    public static final int TYPE_FIND_PWD = 3;
    /**
     * 是否开放了交易功能
     */
    public static boolean IS_OPEN_TRADE = true;
    public static String ALIPAY_ACCOUNT_CODE = "";
    /**
     * 客服电话
     */
    public static String SERVICE_PHONE = "0755-86534610";
    public static String QQ = "888666888";
    /**
     * 投诉电话
     */
    public static String COMPLAINT_PHONE = "0755-86534610";



    public static final String MIN = "min";
    public static final String MIN5 = "min5";
    public static final String MIN15 = "min15";
    public static final String HOUR = "hour";
    public static final String DAY = "day";
    public static final String DAY5 = "day5";
    public static final String WEEK = "week";
    public static final String MONTH = "month";

}
