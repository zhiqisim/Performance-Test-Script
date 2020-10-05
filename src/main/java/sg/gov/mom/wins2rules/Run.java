package sg.gov.mom.wins2rules;
import java.util.*;

public class Run implements Comparable<Run>{
    private Long date;
    private List<Scn> scn;

    public Run(Long date, List<Scn> scn) {
        this.date = date;
        this.scn = scn;
    }

    public Long getDate() {
        return date;
    }

    public List<Scn> getScn() {
        return scn;
    }

    @Override
    public int compareTo(Run o) {
        return this.getDate().compareTo(o.getDate());
    }

}
