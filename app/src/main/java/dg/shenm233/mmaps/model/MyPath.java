package dg.shenm233.mmaps.model;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.Path;

public class MyPath {
    final public LatLonPoint startPoint;
    final public LatLonPoint endPoint;
    final public Path path;

    public MyPath(Path path, LatLonPoint start, LatLonPoint end) {
        this.path = path;
        startPoint = start;
        endPoint = end;
    }
}
