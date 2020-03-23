package dev.shroysha.scada.ejb;


public class ScadaAlert {

    private final ScadaSite ss;
    private final String site;
    private final ScadaComponent.ScadaDiscrete discrete;
    private final String time;

    public ScadaAlert(ScadaSite aSS, ScadaComponent.ScadaDiscrete aDiscrete, String aTime) {
        this.ss = aSS;
        this.site = ss.getName();
        this.discrete = aDiscrete;
        this.time = aTime;
    }

    public ScadaComponent.ScadaDiscrete getDiscrete() {
        return discrete;
    }

    public boolean equals(ScadaAlert other) {
        return other.discrete.getName().equals(this.discrete.getName());
    }
}
