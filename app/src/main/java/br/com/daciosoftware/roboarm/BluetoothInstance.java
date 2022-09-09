package br.com.daciosoftware.roboarm;

public class BluetoothInstance {

    private static BluetoothConnection mmInstance;

    public static BluetoothConnection getInstance() {
        return mmInstance;
    }

    public static void setInstance(BluetoothConnection instance) {
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

    public static void disconnect() {
        if (mmInstance != null) {
            mmInstance.disconnect();
        }
    }

}
