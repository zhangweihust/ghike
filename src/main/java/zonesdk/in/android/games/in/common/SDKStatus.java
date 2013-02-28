package zonesdk.in.android.games.in.common;

public enum SDKStatus {
    SUCESS(0),
    ERROR(1),
    CANCELED(2),
    ;
    
    private int value;
    private SDKStatus(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }

}
