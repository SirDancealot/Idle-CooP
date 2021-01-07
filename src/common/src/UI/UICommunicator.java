package common.src.UI;

public class UICommunicator {
    private static UICommunicator ourInstance = new UICommunicator();

    public static UICommunicator getInstance() {
        return ourInstance;
    }

    private UICommunicator() {

    }



}
