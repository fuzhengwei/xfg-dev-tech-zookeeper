package cn.bugstack.xfg.dev.tech.trigger.http;

import cn.bugstack.xfg.dev.tech.types.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.zookeeper.KeeperException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
public class ConfigController {

    @DCCValue("downgradeSwitch")
    private String downgradeSwitch;

    @DCCValue("userWhiteList")
    private String userWhiteList;

    @Resource
    private CuratorFramework curatorFramework;

    /**
     * curl http://localhost:8091/getConfig/downgradeSwitch
     */
    @RequestMapping("/getConfig/downgradeSwitch")
    public String getConfigDowngradeSwitch() {
        return downgradeSwitch;
    }

    /**
     * curl http://localhost:8091/getConfig/userWhiteList
     */
    @RequestMapping("/getConfig/userWhiteList")
    public String getConfigUserWhiteList() {
        return userWhiteList;
    }

    /**
     * curl -X GET "http://localhost:8091/setConfig?downgradeSwitch=true&userWhiteList=xfg,xfg2"
     */
    @GetMapping("/setConfig")
    public void setConfig(Boolean downgradeSwitch, String userWhiteList) throws Exception {
        curatorFramework.setData().forPath("/xfg-dev-tech/config/downgradeSwitch", (downgradeSwitch ? "开" : "关").getBytes(StandardCharsets.UTF_8));
        curatorFramework.setData().forPath("/xfg-dev-tech/config/userWhiteList", userWhiteList.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 事务操作
     * curl -X GET "http://localhost:8091/setConfigExecuteTranSanction?downgradeSwitch=false&userWhiteList=xfg,user2,user3"
     */
    @GetMapping("/setConfigExecuteTranSanction")
    public List<CuratorTransactionResult> setConfigExecuteTranSanction(Boolean downgradeSwitch, String userWhiteList) throws Exception {
        TransactionOp transactionOp = curatorFramework.transactionOp();
        CuratorOp downgradeSwitchCuratorOp = transactionOp.setData().forPath("/xfg-dev-tech/config/downgradeSwitch", (downgradeSwitch ? "开" : "关").getBytes(StandardCharsets.UTF_8));
        CuratorOp userWhiteListCuratorOp = transactionOp.setData().forPath("/xfg-dev-tech/config/userWhiteList", userWhiteList.getBytes(StandardCharsets.UTF_8));
        List<CuratorTransactionResult> transactionResults = curatorFramework.transaction().forOperations(downgradeSwitchCuratorOp, userWhiteListCuratorOp);
        for (CuratorTransactionResult transactionResult : transactionResults) {
            log.info("事务结果：{} - {}", transactionResult.getForPath(), transactionResult.getType());
        }
        return transactionResults;
    }

    /**
     * 测试方法
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void lock() throws InterruptedException, KeeperException {
        DistributedLock lock = new DistributedLock(curatorFramework, "/xfg-dev-tech/config");
        try {
            lock.lock();

            // 执行需要加锁的代码

            lock.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            curatorFramework.close();
        }
    }

}
