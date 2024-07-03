import java.util.Date;

public class ReminderTask{
    private int ID_Task;
    private String userID;
    private String title;
    private Date deadline;

    public ReminderTask(int ID_Task, String userID, String title, Date deadline) {
        this.ID_Task = ID_Task;
        this.userID = userID;
        this.title = title;
        this.deadline = deadline;
    }

    public int getID_Task() {
        return ID_Task;
    }

    public void setID_Task(int ID_Task) {
        this.ID_Task = ID_Task;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
