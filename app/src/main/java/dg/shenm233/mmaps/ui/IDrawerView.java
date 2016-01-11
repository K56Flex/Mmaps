package dg.shenm233.mmaps.ui;

public interface IDrawerView {
    void openDrawer();

    void closeDrawer();

    void enableDrawer(boolean enable);

    boolean onBackKeyPressed();
}
