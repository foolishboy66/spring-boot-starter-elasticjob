package com.github.foolishboy.elasticjob.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper注册中心配置类
 *
 * @author wangjinbo[wangjinbo.six@bytedance.com]
 * @date 2019-11-20 16:33
 */
@ConfigurationProperties(prefix = ZookeeperRegCenterProperties.ELASTIC_JOB_REG_CENTER_PREFIX)
public class ZookeeperRegCenterProperties {

    static final String ELASTIC_JOB_REG_CENTER_PREFIX = "spring.elastic-job.reg-center";

    /**
     * 连接Zookeeper服务器的列表
     * 包括IP地址和端口号
     * 多个地址用逗号分隔
     * 如: host1:2181,host2:2181
     */
    private String serverLists;

    /**
     * Zookeeper的命名空间
     */
    private String namespace;

    /**
     * 等待重试的间隔时间的初始值
     * 单位：毫秒
     */
    private int baseSleepTimeMilliseconds = 1000;

    /**
     * 等待重试的间隔时间的最大值
     * 单位：毫秒
     */
    private int maxSleepTimeMilliseconds = 3000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 会话超时时间
     * 单位：毫秒
     */
    private int sessionTimeoutMilliseconds = 60000;

    /**
     * 连接超时时间
     * 单位：毫秒
     */
    private int connectionTimeoutMilliseconds = 15000;

    /**
     * 连接Zookeeper的权限令牌
     * 缺省为不需要权限验证
     */
    private String digest;

    public String getServerLists() {
        return serverLists;
    }

    public void setServerLists(String serverLists) {
        this.serverLists = serverLists;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public int getBaseSleepTimeMilliseconds() {
        return baseSleepTimeMilliseconds;
    }

    public void setBaseSleepTimeMilliseconds(int baseSleepTimeMilliseconds) {
        this.baseSleepTimeMilliseconds = baseSleepTimeMilliseconds;
    }

    public int getMaxSleepTimeMilliseconds() {
        return maxSleepTimeMilliseconds;
    }

    public void setMaxSleepTimeMilliseconds(int maxSleepTimeMilliseconds) {
        this.maxSleepTimeMilliseconds = maxSleepTimeMilliseconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getSessionTimeoutMilliseconds() {
        return sessionTimeoutMilliseconds;
    }

    public void setSessionTimeoutMilliseconds(int sessionTimeoutMilliseconds) {
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
    }

    public int getConnectionTimeoutMilliseconds() {
        return connectionTimeoutMilliseconds;
    }

    public void setConnectionTimeoutMilliseconds(int connectionTimeoutMilliseconds) {
        this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}
    