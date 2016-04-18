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

package dg.shenm233.mmaps.ui.maps;

import java.util.HashMap;
import java.util.Map;

public abstract class ViewContainer {
    protected final Map<String, Object> args = new HashMap<>();

    /**
     * 创建界面
     */
    public abstract void onCreateView();

    /**
     * 显示界面
     */
    public abstract void show();

    /**
     * 退出界面
     */
    public abstract void exit();

    /**
     * 销毁界面
     */
    public abstract void onDestroyView();

    /**
     * 处理返回键的事件
     *
     * @return true为已经处理了该事件, false为忽略该事件
     */
    public abstract boolean onBackPressed();

    /**
     * 设置参数,设置前会清除原有参数
     *
     * @param args
     */
    public void setArguments(Map<String, Object> args) {
        this.args.clear();
        if (args != null)
            this.args.putAll(args);
    }

    public Map<String, Object> getArguments() {
        return args;
    }
}
