package dev.shroysha.scada.ejb;

import lombok.Data;
import lombok.Getter;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.BitVector;

import java.io.Serializable;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

@Data
public class ScadaSite implements Serializable, Comparable {

    public static final int CRITICAL_STATUS = 3;
    public static final int WARNING_STATUS = 2;
    public static final int NOTNORMAL_STATUS = 1;
    public static final int NORMAL_STATUS = 0;


    @Getter
    private final int id;
    private final String name;
    private final double lon;
    private final double lat;
    private final ArrayList<ScadaComponent> components = new ArrayList<>();
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private final ArrayList<ScadaAlert> alerts = new ArrayList<>();
    private final Stack<ScadaUpdateListener> listeners = new Stack<>();
    private boolean connected = true;
    private Date date = new Date();
    private boolean justDisconnected = false;
    private int status = NORMAL_STATUS;


    public ScadaSite(int aId, String aName, double aLat, double aLon) {
        id = aId;
        name = aName;
        lon = aLon;
        lat = aLat;
    }

    // Checking for alarms by going through all of the ScadaComponents
    public synchronized void checkAlarms() {

        for (int siteid = 0; siteid < components.size(); siteid++) {
            ScadaComponent sc = components.get(siteid);
            if (sc.isModBus()) {
                try {
                    ArrayList<ScadaComponent.ScadaDiscrete> discretes = sc.getDiscretes();
                    ArrayList<ScadaComponent.ScadaRegister> registers = sc.getRegisters();

                    InetAddress astr = sc.getIP();
                    ModbusTCPMaster mbm = new ModbusTCPMaster(astr.getHostAddress(), 502);
                    mbm.connect();

                    for (ScadaComponent.ScadaDiscrete currentD : discretes) {
                        int addy = currentD.getPort();
                        String dname = currentD.getName();

                        int DISCRETE_OFFSET = 10001;
                        BitVector bv = mbm.readInputDiscretes(addy - DISCRETE_OFFSET, 1);

                        /* checks for critical alarm*/
                        if (bv.getBit(0) && currentD.getWarningType() == 2) {
                            status = CRITICAL_STATUS;

                            if (!inQueue(currentD)) {
                                alerts.add(new ScadaAlert(this, currentD, dateFormat.format(date)));
                            }
                        } else if (bv.getBit(0) && currentD.getWarningType() == 1) {
                            status = WARNING_STATUS;

                            if (!inQueue(currentD)) {
                                alerts.add(new ScadaAlert(this, currentD, dateFormat.format(date)));
                            }
                        } else if (bv.getBit(0) && currentD.getWarningType() == 0) {
                            status = NOTNORMAL_STATUS;

                            if (!inQueue(currentD)) {
                                alerts.add(new ScadaAlert(this, currentD, dateFormat.format(date)));
                            }
                        } else {
                            status = NORMAL_STATUS;
                            if (inQueue(currentD)) {
                                for (int j = 0; j < alerts.size(); j++) {

                                    if (alerts.get(j).getDiscrete() == currentD) {
                                        alerts.remove(j);
                                        j = alerts.size();
                                    }
                                }
                            }
                        }
                    }

                    for (ScadaComponent.ScadaRegister register : registers) {
                        int addy = register.getPort();
                        String rname = register.getName();

                        int REGISTER_OFFSET = 30001;
                        InputRegister[] ir = mbm.readInputRegisters(addy - REGISTER_OFFSET, 1);

                    }
                    // Got through connections
                    date = new Date();
                    justDisconnected = false;
                    connected = true;

                    mbm.disconnect();
                } catch (Exception e) {
                    System.out.println("Disconnected");
                    // if just disconnected
                    justDisconnected = !justDisconnected;

                    status = WARNING_STATUS;

                    connected = false;
                    siteid = components.size();
                }
            }
        }

    }

    private boolean inQueue(ScadaComponent.ScadaDiscrete check) {
        for (ScadaAlert a : alerts) {
            if (a.getDiscrete() == check) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<ScadaAlert> getAlerts() {
        return alerts;
    }

    public String getStatus() {
        return "" + status;
    }

    public boolean isWarning() {
        return status == WARNING_STATUS;
    }

    public boolean isCritical() {
        return status == CRITICAL_STATUS;
    }

    public boolean isNormal() {
        return status == NORMAL_STATUS;
    }

    public int compareTo(Object o) {
        if (o instanceof ScadaSite) {
            ScadaSite ss = (ScadaSite) o;

            return ss.getId() - this.getId();
        } else {
            return -1;
        }
    }

    public void addScadaUpdateListener(ScadaUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeScadaUpdateListener(ScadaUpdateListener listener) {
        listeners.remove(listener);
    }
  /*
      private void clearStatus() {
          notNormal = false;
          critical = false;
          warning = false;
          normal = false;
      }

      private void assignOlds() {
          oldNormal = normal;
          oldNotNormal = notNormal;
          oldWarning = warning;
          oldCritical = critical;
  =======
      public void removeScadaUpdateListener(ScadaUpdateListener listener) {
          listeners.remove(listener);
  >>>>>>> Scada-Framework-Update*/
    // }

    public void notifyAllScadaListeners(ScadaSite site) {
        for (ScadaUpdateListener listener : listeners) {
            listener.update(site);
        }
    }

}
