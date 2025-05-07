package backend;


import java.util.Date;

public class Announcement {
    private int id;
    private String title;
    private String content;
    private Date date;
    private int publisherId;
    
    
    public Announcement() {
    }
    
    public Announcement(int id, String title, String content, Date date, int publisherId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.publisherId = publisherId;
    }
    
    public int getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public String getContent(){
        return content;
    }
    public Date getDate(){
        return date;
    }
    public int getPublisherId(){
        return publisherId;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setContent(String content){
        this.content = content;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public void setPublisherId(int publisherId){
        this.publisherId = publisherId;
    }
}
