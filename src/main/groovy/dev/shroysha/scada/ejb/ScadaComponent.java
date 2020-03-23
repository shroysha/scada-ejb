package dev.shroysha.scada.ejb;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ScadaComponent implements Serializable {

    private final String name;
    private final ArrayList<ScadaRegister> registers;
    private InetAddress IP;
    private boolean ModBus = false;
    private ArrayList<ScadaDiscrete> discretes;

    public ScadaComponent(
            String aName,
            String aIP,
            int aIsModBus,
            ArrayList<ScadaDiscrete> aDiscretes,
            ArrayList<ScadaRegister> aRegisters) {
        name = aName;
        try {
            IP = InetAddress.getByName(aIP);
        } catch (UnknownHostException ex) {
            Logger.getLogger(ScadaComponent.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (aIsModBus == 1) {
            ModBus = true;
        }

        discretes = aDiscretes;
        registers = aRegisters;
    }

    public String getName() {
        return name;
    }

    public InetAddress getIP() {
        return IP;
    }

    public boolean isModBus() {
        return ModBus;
    }

    public ArrayList<ScadaDiscrete> getDiscretes() {
        return discretes;
    }

    public ArrayList<ScadaRegister> getRegisters() {
        return registers;
    }


    public String toString() {
        return "\n\nName: " + name + "\nIP: " + IP + "\nModBus Enabled: " + ModBus;
    }

    public static class ScadaRegister implements Serializable {

        final String name;
        final int port;
        final int warningType;

        public ScadaRegister(String aName, int aPort, int aWarning) {
            name = aName;
            port = aPort;
            warningType = aWarning;
        }

        public int getPort() {
            return port;
        }

        public String getName() {
            return name;
        }

        public int getWarning() {
            return warningType;
        }
    }

    public static class ScadaDiscrete implements Serializable {

        private final String name;
        private final int port;
        private final int warningType;

        public ScadaDiscrete(String name, int port, int warningType) {
            this.name = name;
            this.port = port;
            this.warningType = warningType;
        }

        public String getName() {
            return name;
        }

        public int getPort() {
            return port;
        }

        public int getWarningType() {
            return warningType;
        }
    }
}
