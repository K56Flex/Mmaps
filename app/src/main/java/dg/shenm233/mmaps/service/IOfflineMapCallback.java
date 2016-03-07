package dg.shenm233.mmaps.service;

public interface IOfflineMapCallback {
    /**
     * 由于建立OfflineMapManager对象所需时间较长，建立完后就通知回调
     */
    void onOfflineMapManagerReady();

    /**
     * 下载状态回调，在调用downloadByCityName 等下载方法的时候会启动
     *
     * @param status       参照OfflineMapStatus
     * @param completeCode 下载进度，下载完成之后为解压进度
     * @param name         当前所下载的城市的名字
     */
    void onDownload(int status, int completeCode, String name);

    /**
     * 当调用updateOfflineMapCity 等检查更新函数的时候会被调用
     *
     * @param hasNew true表示有更新，说明官方有新版或者本地未下载
     * @param name   被检测更新的城市的名字
     */
    void onCheckUpdate(boolean hasNew, String name);

    /**
     * 当调用OfflineMapManager.remove(String)方法时，如果有设置监听，会回调此方法
     * 当删除省份时，该方法会被调用多次，返回省份内城市删除情况。
     *
     * @param success  true为删除成功
     * @param name     所删除的城市的名字
     * @param describe 删除描述，如 删除成功 "本地无数据"
     */
    void onRemove(boolean success, String name, String describe);
}
