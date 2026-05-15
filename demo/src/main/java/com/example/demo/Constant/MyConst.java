package com.example.demo.Constant;

public class MyConst {
    //账号是否被封禁信息
    public static final int STATUS_BAN = 0;
    public static final int STATUS_NORMAL = 1;
    //身份参数信息
    public static final int ROLE_ADMIN = 0;
    public static final int ROLE_USER = 1;
    public static final int ROLE_DRIVER = 2;
    //订单状态信息
    public static final int ORDER_WAIT = 0;
    public static final int ORDER_CONTINUE = 1;
    public static final int ORDER_COMPLETE = 2;
    public static final int ORDER_CANCEL = 3;
    //错误码信息
    public static final int CODE_UNLOGIN = 201;
    public static final int CODE_FORBID = 202;
    //websocket类型码信息
    public static final String WEBSOCKET_POSITION = "position";
    public static final String WEBSOCKET_ORDER_NEW = "order-new";
    public static final String WEBSOCKET_ORDER_RECEIVE = "order-receive";
    public static final String WEBSOCKET_ORDER_CANCEL = "order-cancel";
    public static final String WEBSOCKET_ORDER_COMPLETE = "order-complete";
}
