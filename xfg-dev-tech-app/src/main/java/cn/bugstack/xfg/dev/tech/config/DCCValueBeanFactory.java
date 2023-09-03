package cn.bugstack.xfg.dev.tech.config;

import cn.bugstack.xfg.dev.tech.types.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 Zookeeper 的配置中心实现原理
 */
@Slf4j
@Configuration
public class DCCValueBeanFactory implements BeanPostProcessor {

    private static final String BASE_CONFIG_PATH = "/xfg-dev-tech/config";

    private final CuratorFramework client;

    private final Map<String, Object> dccObjGroup = new HashMap<>();

    public DCCValueBeanFactory(CuratorFramework client) throws Exception {
        this.client = client;

        // 节点判断
        if (null == client.checkExists().forPath(BASE_CONFIG_PATH)) {
            client.create().creatingParentsIfNeeded().forPath(BASE_CONFIG_PATH);
            log.info("DCC 节点监听 base node {} not absent create new done!", BASE_CONFIG_PATH);
        }

        CuratorCache curatorCache = CuratorCache.build(client, BASE_CONFIG_PATH);
        curatorCache.start();

        curatorCache.listenable().addListener((type, oldData, data) -> {
            switch (type) {
                case NODE_CHANGED:
                    String dccValuePath = data.getPath();
                    Object objBean = dccObjGroup.get(dccValuePath);
                    try {
                        // 1. getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
                        // 2. getField 方法用于获取指定类中的公共字段，即只能获取到公共访问修饰符（public）的字段。
                        Field field = objBean.getClass().getDeclaredField(dccValuePath.substring(dccValuePath.lastIndexOf("/") + 1));
                        field.setAccessible(true);
                        field.set(objBean, new String(data.getData()));
                        field.setAccessible(false);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DCCValue.class)) {
                DCCValue dccValue = field.getAnnotation(DCCValue.class);

                try {
                    if (null == client.checkExists().forPath(BASE_CONFIG_PATH.concat("/").concat(dccValue.value()))) {
                        client.create().creatingParentsIfNeeded().forPath(BASE_CONFIG_PATH.concat("/").concat(dccValue.value()));
                        log.info("DCC 节点监听 listener node {} not absent create new done!", BASE_CONFIG_PATH.concat("/").concat(dccValue.value()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                dccObjGroup.put(BASE_CONFIG_PATH.concat("/").concat(dccValue.value()), bean);
            }
        }
        return bean;
    }


}
