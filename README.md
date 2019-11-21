[TOC]

# springboot整合elasticjob构成starter
spring-boot-starter-elasticjob利用springboot的starter快速集成elasticjob。

## QuickStart

### step1 引入maven依赖，如有需要，可以自行打包上传自私服

``` java
<dependency>
    <groupId>com.github.foolishboy</groupId>
    <artifactId>spring-boot-starter-elasticjob</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### step2 编写job代码

```java
@Slf4j
@ElasticJobScheduled(jobName = "${simple.job.demo.job.jobName}", cron = "${simple.job.demo.job.cron}", shardingTotalCount = "${simple.job.demo.job.shardingTotalCount}")
public class SimpleJobDemo implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {

        log.info("当前分片项 shardingItem={}, jobName={}", shardingContext.getShardingItem(), shardingContext.getJobName());
        // do something...
    }
}
```

### step3 添加spring配置文件配置项，同时支持yaml和properties文件

- [x] application.properties

  ```properties
  # elasticjob注册中心地址
  spring.elastic-job.reg-center.server-lists=localhost:2181
  # elasticjob注册命名空间
  spring.elastic-job.reg-center.namespace=elastic-job-lite-springboot
  # 作业名称
  simple.job.demo.job.jobName=SimpleJobDemo
  # 任务执行的cron表达式
  simple.job.demo.job.cron=0/5 * * * * ?
  # 任务分片总数
  simple.job.demo.job.shardingTotalCount=128
  ```

- [x] application.yml

  ```yaml
  spring:
    elastic-job:
      reg-center:
        # elasticjob注册中心地址
        server-lists: localhost:2181
        # elasticjob注册命名空间
        namespace: elastic-job-lite-springboot
  simple:
    job:
      demo:
        job:
          # 作业名称
          jobName: SimpleJobDemoTest
          # 任务执行的cron表达式
          cron: 0/5 * * * * ?
          # 任务分片总数
          shardingTotalCount: 1
  ```



### step4 示例代码

​	示例demo已上传至[github](https://github.com/foolishboy66/spring-boot-starter-elasticjob-test.git)，请自行查看。

