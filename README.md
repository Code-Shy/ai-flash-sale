# AI Flash Sale

一个基于 Spring Boot 3 的即时零售后端 Demo，目标是模拟“淘宝闪购 / 小时达”类场景中的核心链路：

- 商品与门店查询
- 单门店购物车
- 提交订单、锁定库存、取消订单
- Redis 缓存与幂等 token
- Kafka 订单创建事件
- AI 导购推荐
- 轻量 RAG 导购问答

## 技术栈

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring Validation
- MyBatis-Plus
- MySQL
- Redis
- Kafka
- Springdoc OpenAPI
- Lombok
- Hutool

## 项目结构

```text
src/main/java/com/weijinchuan/aiflashsale
├── common          # 通用返回、常量、异常
├── config          # OpenAPI、Kafka Topic 配置
├── controller      # HTTP 接口层
├── domain          # 数据库实体
├── dto             # 入参对象
├── event           # Kafka 事件消息、生产者、消费者
├── filter          # 请求日志过滤器
├── mapper          # MyBatis-Plus Mapper
├── service
│   ├── impl        # 业务实现
│   ├── llm         # 意图识别与文案生成 Provider
│   ├── rag         # 轻量知识检索
│   └── tool        # 供 AI 调用的工具层
└── vo              # 返回对象

src/main/resources
├── ai/knowledge-base.json   # 轻量知识库
├── application.yml          # 项目配置
└── db/*.sql                 # 建表脚本
```

## 快速开始

### 1. 环境准备

- JDK 17
- Maven 3.9+
- MySQL 8+
- Redis 6+
- Kafka 3+

### 2. 初始化数据库

项目提供了建表 SQL，位于 `src/main/resources/db`：

- `create.sql`
- `store.sql`
- `spu.sql`
- `sku.sql`
- `store_sku.sql`
- `inventory.sql`
- `cart.sql`
- `cart_item.sql`
- `order.sql`
- `order_item.sql`
- `order_operate_log.sql`

建议顺序：

1. 先创建数据库
2. 再按上面的顺序建表
3. 最后自行插入一些门店、商品、库存样例数据


### 3. 配置文件

默认配置位于 `src/main/resources/application.yml`，包括：

- MySQL 数据源
- Redis 地址
- Kafka 地址
- Qwen 配置
- RAG 知识库配置

### 4. 配置 DashScope Key

如果希望启用通义千问做意图识别和生成，请配置环境变量：

```bash
export DASHSCOPE_API_KEY=your_api_key
```

### 5. 访问 Swagger

启动后访问：

```text
http://localhost:8080/swagger-ui.html
```

