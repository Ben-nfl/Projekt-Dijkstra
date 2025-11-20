package at.htlhl.graphdemo;

public class VertaxData {
    private String name;

    public void VertaxData(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public VertaxData(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }


}
