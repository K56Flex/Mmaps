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

import java.util.Map;
import java.util.Stack;

public class ViewContainerManager {
    private Stack<ViewContainerWithId> mBackStack;

    public ViewContainerManager() {
        mBackStack = new Stack<>();
    }

    /**
     * 获取指定id对应的ViewContainer的实例
     *
     * @param id 要获取ViewContainer的id
     * @return id的对应ViewContainer, 可能为null
     */
    public ViewContainer getViewContainer(int id) {
        for (ViewContainerWithId v : mBackStack) {
            if (v.id == id)
                return v.view;
        }
        return null;
    }

    /**
     * 获取栈顶的ViewContainer
     *
     * @return 栈顶的ViewContainer, 可能为null
     */
    public ViewContainer peek() {
        ViewContainerWithId v = mBackStack.peek();
        if (v == null)
            return null;
        else
            return v.view;
    }

    /**
     * 把指定的ViewContainer压栈，并显示
     * 注:原栈顶的ViewContainer不显示
     *
     * @param view 要进栈的ViewContainer
     * @param args 要进栈的ViewContainer的参数
     * @param save 是否保存参数进栈
     * @param id   该ViewContainer的id
     */
    public void putViewContainer(ViewContainer view, Map<String, Object> args, boolean save, int id) {
        ViewContainerWithId v = new ViewContainerWithId(view, save ? args : null, id);
        view.setArguments(args);
        if (!mBackStack.isEmpty()) {
            ViewContainerWithId old = mBackStack.peek();
            if (old != null)
                old.view.exit();
        }
        mBackStack.push(v);
        view.show();
    }

    /**
     * 弹出当前显示的ViewContainer,并设置对应参数,并显示上一个界面
     */
    public void popBackStack() {
        if (!mBackStack.isEmpty()) {
            ViewContainerWithId v = mBackStack.pop();
            v.view.exit();
            if (!mBackStack.isEmpty()) {
                v = mBackStack.peek();
                ViewContainer vv = v.view;
                vv.setArguments(v.args);
                vv.show();
            }
        }
    }

    /**
     * 弹出所有ViewContainer直至显示指定id的ViewContainer
     *
     * @param id 要显示的ViewContainer的id
     */
    public void popBackStack(int id) {
        if (!mBackStack.isEmpty()) {
            ViewContainerWithId v = mBackStack.peek();
            ViewContainer oldvv = v.view;
            while (v.id != id) {
                mBackStack.pop();
                v = mBackStack.peek();
            }
            ViewContainer vv = v.view;
            oldvv.exit();
            vv.setArguments(v.args);
            vv.show();
        }
    }

    private static class ViewContainerWithId {
        private final int id;
        private final ViewContainer view;
        private final Map<String, Object> args;

        private ViewContainerWithId(ViewContainer view, Map<String, Object> args, int id) {
            this.id = id;
            this.args = args;
            this.view = view;
        }
    }

}
