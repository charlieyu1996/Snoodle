package cloudcode.guestbook.backend;

/**
 * defines the data associated with a single guest book entry
 */
public class CalendarEntry3 {
    private String id;

    private String eventSummary;
    private String emailList;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String location;

    private String eventLink;

    private long createDate;

    public final String getId(){
        return id;
    }

    public final void setId(String id){
        this.id = id;
    }

    public final String getEventSummary() {
        return eventSummary;
    }

    public final void setEventSummary(String eventSummary) {
        this.eventSummary = eventSummary;
    }

    public final String getEmailList() {
        return emailList;
    }

    public final void setEmailList(String emailList) {
        this.emailList = emailList;
    }

    public final String getStartDate() {
        return this.startDate;
    }

    public final void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public final String getEndDate() {
        return this.endDate;
    }

    public final void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public final String getStartTime() {
        return this.startTime;
    }

    public final void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public final String getEndTime() {
        return this.endTime;
    }

    public final void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public final String getLocation() {
        return this.location;
    }

    public final void setLocation(String location) {
        this.location = location;
    }

    public final long getCreateDate() {
        return this.createDate;
    }

    public final void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public final String getEventLink(){
        return eventLink;
    }

    public final void setEventLink(String eventLink){
        this.eventLink = eventLink;
    }
}
