package br.com.daciosoftware.roboarm;

public class BluetoothInstance {

    private static BluetoothConnectionTask mmInstance;

    private BluetoothInstance(){}

    public static BluetoothConnectionTask getInstance() {
        return mmInstance;
    }

    public static void setInstance(BluetoothConnectionTask instance) {
        mmInstance = instance;
    }

    public static boolean isConnected() {
        if (mmInstance == null) return false;
        if (!mmInstance.isConnected()) {
            mmInstance.getListener().setDisconnected();
            return false;
        }
        return true;
    }

}
