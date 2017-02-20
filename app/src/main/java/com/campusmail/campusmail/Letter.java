package com.campusmail.campusmail;

/**
 * Created by John on 31-Oct-16.
 */
public class Letter {

    private String title, story, image, photo, name, time;

    public Letter() {

    }

    public Letter(String title, String story, String image, String photo, String name, String time) {

        this.title = title;
        this.story = story;
        this.image = image;
        this.photo = photo;
        this.name = name;
        this.time = time;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
