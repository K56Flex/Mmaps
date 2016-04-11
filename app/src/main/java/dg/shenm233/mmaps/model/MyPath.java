/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
