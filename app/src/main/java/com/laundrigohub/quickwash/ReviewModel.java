package com.laundrigohub.quickwash;

public class ReviewModel {
    String rating, comment, date, userName, userImage;

    public ReviewModel(String rating, String comment, String date, String userName, String userImage) {
        this.rating = rating;
        this.comment = comment;
        this.date = date;
        this.userName = userName;
        this.userImage = userImage;
    }

    public String getRating() { return rating; }
    public String getComment() { return comment; }
    public String getDate() { return date; }
    public String getUserName() { return userName; }
    public String getUserImage() { return userImage; }
}

