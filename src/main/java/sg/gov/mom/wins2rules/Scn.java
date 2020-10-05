package sg.gov.mom.wins2rules;
import java.util.*;

public class Scn implements Comparable<Scn>{
    private String name;
    private int percentile95;
    private int percentile90;
    private int minimum;
    private int maximum;
    private int mean;

    public Scn(String name, int percentile95, int percentile90, int minimum, int maximum, int mean) {
        this.name = name;
        this.percentile95 = percentile95;
        this.percentile90 = percentile90;
        this.minimum = minimum;
        this.maximum = maximum;
        this.mean = mean;
    }

    public String getName() {
        return name;
    }

    public int getPercentile95() {
        return percentile95;
    }

    public int getPercentile90() {
        return percentile90;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public int getMean() {
        return mean;
    }

    @Override
    public int compareTo(Scn o) {
        return this.getName().compareTo(o.getName());
    }

}
