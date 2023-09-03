package cn.bugstack.xfg.dev.tech.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class ZooKeeperListener {

    @Resource
    private CuratorFramework zkClient;

    @PostConstruct
    public void init() throws Exception {
        // 保证实时性，利用zk的watch机制
        CuratorCache curatorCache = CuratorCache.build(zkClient, "/xfg-dev-tech");
        curatorCache.start();

        // 创建监听器
        curatorCache.listenable().addListener((type, oldData, data) -> {
            switch (type) {
                case NODE_CHANGED:
                    // 1. 获取变更节点的路径名
                    String configName = data.getPath().replace("/xfg-dev-tech/", "");
                    // 监听到zk的zNode发生了数据变更
                    log.info("节点变更 node：{} data: {}", configName, new String(data.getData()));
                    break;
                default:
                    break;
            }
        });
    }

}
