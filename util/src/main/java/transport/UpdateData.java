package transport;

public class UpdateData {

    Data removed;
    Data added;

    UpdateData (Data removed, Data added){
        this.removed=removed;
        this.added=added;
    }

    public Data getAdded() {
        return added;
    }

    public void setAdded(Data added) {
        this.added = added;
    }

    public Data getRemoved() {
        return removed;
    }

    public void setRemoved(Data removed) {
        this.removed = removed;
    }

}
