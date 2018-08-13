package com.example.android.meinforme.notice_class;

public class Notice {
    private String title;
    private String body;
    private String date;

    /*construtor do objeto content*/
    public Notice(){
        super();
    }

    public Notice(String title, String body, String date){
        this.title = title;
        this.body = body;
        this.date = date;
    }

    /*retorna o title*/
    public String getTitle(){
        return title;
    }

    /*atribui o title*/
    public void setTitle(String title){
        this.title = title;
    }

    /*retorna o body*/
    public String getBody(){
        return body;
    }

    /*atribui o body*/
    public void setBody(String body){
        this.body = body;
    }

    /*retorna a data*/
    public String getDate(){
        return date;
    }

    /*atribui a data*/
    public void setDate(String date){
        this.date = date;
    }
}