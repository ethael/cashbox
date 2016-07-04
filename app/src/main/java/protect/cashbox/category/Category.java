package protect.cashbox.category;

public class Category {
    private String name;
    private int max;
    private int current;

    public Category() {}

    public Category(final String name, final int max, final int value) {
        this.name = name;
        this.max = max;
        this.current = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
