package Master.Strategy;

import Master.Service.CuratorHolder;
import Master.Table;
import Master.ZookeeperManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: LENOVO
 * @since: 2023/5/28  11:26
 * @description:
 */
@Slf4j
public class Monitor implements PathChildrenCacheListener {
    private CuratorHolder client;
    private StrategyProcessor strategyProcessor;
    private Table table;

    private Map<String,String> map = new HashMap<>();
    public Monitor(CuratorHolder curatorClientHolder, Table table) {
        this.table = table;
        this.strategyProcessor= new StrategyProcessor(table);
        this.client = curatorClientHolder;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        String hostName, hostUrl;
        String eventPath = pathChildrenCacheEvent.getData() != null ? pathChildrenCacheEvent.getData().getPath() : null;

        // 接收到事件，对事件类型进行判断并执行相应策略
        switch (pathChildrenCacheEvent.getType()) {
            case CHILD_ADDED:
                log.warn("服务器目录新增节点: " + pathChildrenCacheEvent.getData().getPath());

                eventServerAppear(
                        eventPath.replaceFirst(ZookeeperManager.ZNODE + "/", ""),
                        client.getData(eventPath));
                break;
            case CHILD_REMOVED:
                log.warn("服务器目录删除节点: " + pathChildrenCacheEvent.getData().getPath());
                eventServerDisappear(
                        eventPath.replaceFirst(ZookeeperManager.ZNODE + "/", ""),
                        new String(pathChildrenCacheEvent.getData().getData()));
                break;
            case CHILD_UPDATED:
                log.warn("服务器目录更新节点: " + pathChildrenCacheEvent.getData().getPath());
                eventServerUpdate(
                        eventPath.replaceFirst(ZookeeperManager.ZNODE + "/", ""),
                        client.getData(eventPath));
                break;
            default:
        }
    }

    /**
     * 处理服务器节点出现事件
     *
     * @param hostName
     * @param hostUrl
     */
    public void eventServerAppear(String hostName, String hostUrl) {
        log.warn("新增服务器节点：主机名 {}, 地址 {}", hostName, hostUrl);
        if (strategyProcessor.existServer(hostUrl)) {
            // 该服务器已经存在，即从失效状态中恢复
            log.warn("对该服务器{}执行恢复策略", hostName);
            strategyProcessor.execStrategy(hostUrl, 2);
        } else {
            // 新发现的服务器，新增一份数据
            log.warn("对该服务器{}执行新增策略", hostName);
            strategyProcessor.execStrategy(hostUrl, 1);
        }
    }

    /**
     * 处理服务器节点失效事件
     *  @param hostName
     * @param hostUrl*/
    public void eventServerDisappear(String hostName, String hostUrl) {
        log.warn("服务器节点失效：主机名 {}, 地址 {}", hostName, hostUrl);
        if (!strategyProcessor.existServer(hostUrl)) {
            throw new RuntimeException("需要删除信息的服务器不存在于服务器列表中");
        } else {
            // 更新并处理下线的服务器
            log.warn("对该服务器{}执行失效策略", hostName);
            strategyProcessor.execStrategy(hostUrl, 3);
        }
    }

    /**
     * 处理服务器节点更新事件
     *
     * @param hostName
     * @param hostUrl
     */
    public void eventServerUpdate(String hostName, String hostUrl) {
        log.warn("更新服务器节点：主机名 {}, 地址 {}", hostName, hostUrl);
    }
}
