package com.campusmail.campusmail;

/**
 * Created by John on 31-Oct-16.
 */
public class Search {

    private String community, faculty, image, mail_no, name;

    public Search() {

    }

    public Search(String community, String faculty, String image, String mail_no, String name) {

        this.community = community;
        this.faculty = faculty;
        this.image = image;
        this.mail_no = mail_no;
        this.name = name;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getMail_no() {
        return mail_no;
    }

    public void setMail_no(String mail_no) {
        this.mail_no = mail_no;
    }
}
