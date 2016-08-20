package com.eson.domain;

/**
 * 联系人实体类
 * Created by Administrator on 2016/8/20.
 */
public class Contact {
    public int id;
    public String name;
    public String image;

    public Contact(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public Contact() {
    }
}
