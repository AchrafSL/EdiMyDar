package com.example.edimydar;

public class message {
    public static String SENT_BY_USR = "usr",SENT_BY_BOT="bot";

    String msg;
    String sendbyWho;

    public message(String msg, String sendbyWho) {
        this.msg = msg;
        this.sendbyWho = sendbyWho;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSendbyWho() {
        return sendbyWho;
    }

    public void setSendbyWho(String sendbyWho) {
        this.sendbyWho = sendbyWho;
    }
}
