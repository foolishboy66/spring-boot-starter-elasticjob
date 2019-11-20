package com.github.foolishboy.elasticjob;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.github.foolishboy.elasticjob.annotation.ElasticJobScheduled;
import com.github.foolishboy.elasticjob.config.JobConfig;
import com.github.foolishboy.elasticjob.config.ZookeeperRegCenterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.util.Map;

/**
 * elastic-job自动配置启动类
 *
 * @author wangjinbo[wangjinbo.six@bytedance.com]
 * @date 2019-11-20 16:42
 */
@Configuration
@EnableConfigurationProperties(ZookeeperRegCenterProperties.class)
public class ElasticJobAutoConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ElasticJobAutoConfiguration.class);

    private static final String PLACE_HOLDER = "$";

    @Resource
    private ZookeeperRegCenterProperties zookeeperRegCenterProperties;

    @Resource
    private ApplicationContext applicationContext;

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {

        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(zookeeperRegCenterProperties.getServerLists(), zookeeperRegCenterProperties.getNamespace());
        zkConfig.setBaseSleepTimeMilliseconds(zookeeperRegCenterProperties.getBaseSleepTimeMilliseconds());
        zkConfig.setConnectionTimeoutMilliseconds(zookeeperRegCenterProperties.getConnectionTimeoutMilliseconds());
        zkConfig.setDigest(zookeeperRegCenterProperties.getDigest());
        zkConfig.setMaxRetries(zookeeperRegCenterProperties.getMaxRetries());
        zkConfig.setMaxSleepTimeMilliseconds(zookeeperRegCenterProperties.getMaxSleepTimeMilliseconds());
        zkConfig.setSessionTimeoutMilliseconds(zookeeperRegCenterProperties.getSessionTimeoutMilliseconds());
        return new ZookeeperRegistryCenter(zkConfig);
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, Object> beansMap = applicationContext.getBeansWithAnnotation(ElasticJobScheduled.class);
        for (Object bean : beansMap.values()) {

            resolveElasticJob(bean, applicationContext);
        }
    }

    private void resolveElasticJob(Object bean, ApplicationContext applicationContext) {

        Class<?> clazz = bean.getClass();
        ElasticJobScheduled annotation = clazz.getAnnotation(ElasticJobScheduled.class);
        validateElasticJobScheduledAnnotation(annotation);

        JobConfig jobConfig = buildJobConfig(annotation);

        JobCoreConfiguration coreConfiguration = JobCoreConfiguration.newBuilder(jobConfig.getJobName(), jobConfig.getCorn(), jobConfig.getShardingTotalCount())
                .shardingItemParameters(jobConfig.getShardingItemParameters())
                .jobParameter(jobConfig.getJobParameter())
                .failover(jobConfig.isFailover())
                .misfire(jobConfig.isMisfire())
                .description(jobConfig.getDescription())
                .build();

        JobTypeConfiguration jobTypeConfiguration = null;
        if (bean instanceof SimpleJob) {
            jobTypeConfiguration = new SimpleJobConfiguration(coreConfiguration, clazz.getName());
        } else if (bean instanceof DataflowJob) {
            jobTypeConfiguration = new DataflowJobConfiguration(coreConfiguration, clazz.getName(), false);
        } else if (bean instanceof ScriptJob) {
            jobTypeConfiguration = new ScriptJobConfiguration(coreConfiguration, clazz.getName());
        }
        if (jobTypeConfiguration == null) {
            logger.error("unknown job type [" + clazz.getName() + " ], allowed type is [SimpleJob, DataflowJob, ScriptJob].");
            throw new RuntimeException("unknown job type [" + clazz.getName() + " ], allowed type is [SimpleJob, DataflowJob, ScriptJob].");
        }

        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(jobTypeConfiguration).overwrite(true).build();
        SpringJobScheduler springJobScheduler = new SpringJobScheduler((ElasticJob) bean, applicationContext.getBean(ZookeeperRegistryCenter.class), liteJobConfiguration);
        springJobScheduler.init();

        logger.info("【" + annotation.jobName() + "】\t" + clazz.getName() + "\tinit success!");
    }

    private JobConfig buildJobConfig(ElasticJobScheduled annotation) {

        String jobName = getValueFromEnvironment(annotation.jobName());
        String corn = getValueFromEnvironment(annotation.corn());
        int shardingTotalCount = Integer.parseInt(getValueFromEnvironment(annotation.shardingTotalCount()));
        String shardingItemParameters = getValueFromEnvironment(annotation.shardingItemParameters());
        String jobParameter = getValueFromEnvironment(annotation.jobParameter());
        String description = getValueFromEnvironment(annotation.description());

        return new JobConfig(jobName, corn, shardingTotalCount, shardingItemParameters, jobParameter, description);
    }

    private String getValueFromEnvironment(String val) {

        if (val != null && val.contains(PLACE_HOLDER)) {
            String key = trimPlaceHolder(val);
            Environment environment = applicationContext.getEnvironment();
            return environment.getProperty(key);
        }

        return val;
    }

    /**
     * 去除占位符
     * ${spring.value} -> spring.value
     *
     * @param val 要变换的值
     * @return 去掉占位符的key
     */
    private static String trimPlaceHolder(String val) {

        return val.substring(2, val.length() - 1);
    }

    private void validateElasticJobScheduledAnnotation(ElasticJobScheduled annotation) {

        if (isBlank(annotation.corn())) {
            logger.error("corn expression can't be null.");
            throw new RuntimeException("corn expression can't be null.");
        }

        if (isBlank(annotation.jobName())) {
            logger.error("jobName can't be null.");
            throw new RuntimeException("corn expression can't be null.");
        }
    }

    private static boolean isBlank(String str) {

        if (str == null) {
            return true;
        }
        str = str.trim();

        return "".equals(str);
    }
}
    