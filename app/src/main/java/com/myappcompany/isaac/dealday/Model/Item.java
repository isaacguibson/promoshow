package com.myappcompany.isaac.dealday.Model;

import java.util.Date;

/**
 * Created by isaac on 19/03/18.
 */

//Classe Item
public class Item
{
    private String title;
    private Date pubDate;
    private String description;
    private String urlImage;
    private String link;
    private String category;
    private String platform;
    private boolean favorite;
    private boolean activeAdMob;


    public Item(String title, Date pubDate, String description,
                String urlImage, String link, String category, String platform, boolean favorite, boolean activeAdMob) {
        this.title = title;
        this.pubDate = pubDate;
        this.description = description;
        this.urlImage = urlImage;
        this.link = link;
        this.category = category;
        this.platform = platform;
        this.favorite = favorite;
        this.activeAdMob = activeAdMob;
    }


    //Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isActiveAdMob() {
        return activeAdMob;
    }

    public void setActiveAdMob(boolean activeAdMob) {
        this.activeAdMob = activeAdMob;
    }
}
