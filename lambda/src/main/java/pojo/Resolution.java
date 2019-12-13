package pojo;

public class Resolution {
    public Resolution(){

    }

    public Resolution(int width,int height){

        this.width = width;
        this.height = height;
    }
    private int width;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private int height;
}
