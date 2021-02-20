package lambdasinaction.practice;

/**
 * Created by hunshou on 2021/2/20.
 */
public class City {

    private String name;
    private String state;
    private String pulation;

    public City(String name, String state, String pulation) {
        this.name = name;
        this.state = state;
        this.pulation = pulation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPulation() {
        return pulation;
    }

    public void setPulation(String pulation) {
        this.pulation = pulation;
    }

    public static Object getState(Object o) {
        return null;
    }
}
