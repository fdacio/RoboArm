package br.com.daciosoftware.roboarm.ui.bateria;

public class BateriaDTO {
    private static BateriaDTO instance;
    private String perc;
    private String volt;
    private BateriaDTO() {
        this.perc = "0";
        this.volt = "0";
    }
    public static BateriaDTO getInstance() {
        if (instance == null) {
            instance = new BateriaDTO();
        }
        return instance;
    }
    public String getPerc() {
        return perc;
    }
    public void setPerc(String perc) {
        this.perc = perc;
    }
    public void setVolt(String volt) {
        this.volt = volt;
    }
    public String getVolt() {
        return volt;
    }


}
