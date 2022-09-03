package spontanicus.users;

public class User {
    private final long id;
    private String notificationMessage;
    private boolean notifyAutomatically;
    private final static String DEFAULT_NOTIFICATION_MESSAGE = "{user.name} hat einen Stream gestartet <3";


    public User(long id){
        this.id = id;
        notificationMessage = DEFAULT_NOTIFICATION_MESSAGE;
        notifyAutomatically = true;
    }

    public long getId() {
        return id;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public boolean isNotifyAutomatically() {
        return notifyAutomatically;
    }

    public void setNotifyAutomatically(boolean notifyAutomatically) {
        this.notifyAutomatically = notifyAutomatically;
    }

    @Override
    public boolean equals(Object other){
        if(other == null)
            return false;
        if(!(other instanceof User))
            return false;
        return id == ((User) other).getId()
                && notificationMessage.equals(((User) other).getNotificationMessage())
                && notifyAutomatically == ((User) other).isNotifyAutomatically();
    }

    @Override
    public String toString(){
        return "[UserId: " + id + "]";
    }
}
