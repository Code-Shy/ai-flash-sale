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

注意：

- `create.sql` 中的库名是 `ai_flash_sale`
- `application.yml` 默认连接的是 `ai_flash_schema`

二者目前不一致。启动前请二选一：

- 修改本地数据库名为 `ai_flash_schema`
- 或者把 `application.yml` 中的数据源 URL 改成你的实际库名

### 3. 配置文件

默认配置位于 `src/main/resources/application.yml`，包括：

- MySQL 数据源
- Redis 地址
- Kafka 地址
- Qwen 配置
- RAG 知识库配置

当前关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ai_flash_schema
  data:
    redis:
      host: 127.0.0.1
      port: 6379
  kafka:
    bootstrap-servers: 127.0.0.1:9092

qwen:
  api-key: ${DASHSCOPE_API_KEY:}

rag:
  knowledge-base-location: ai/knowledge-base.json
```

### 4. 配置 DashScope Key

如果希望启用通义千问做意图识别和生成，请配置环境变量：

```bash
export DASHSCOPE_API_KEY=your_api_key
```

如果没有配置：

- 意图识别会回退到本地规则版
- 推荐理由 / 问答回答会回退到本地模板版

### 5. 访问 Swagger

启动后访问：

```text
http://localhost:8080/swagger-ui.html
```

## 接口总览

### 商品与门店

- `GET /products` 商品列表
- `GET /products/{skuId}` 商品详情
- `GET /stores` 门店列表
- `GET /stores/{storeId}/skus` 门店商品列表

### 购物车

- `POST /cart/items` 加入购物车
- `GET /cart?userId=1&storeId=1` 查询购物车
- `PUT /cart/items/{itemId}` 修改购物车项数量
- `DELETE /cart/items/{itemId}` 删除购物车项

### 订单

- `GET /orders/token?userId=1` 获取下单幂等 token
- `POST /orders/submit` 提交订单
- `GET /orders/{orderId}` 查询订单详情
- `POST /orders/{orderId}/cancel` 取消订单
- `GET /orders?userId=1` 查询用户订单列表

提交订单时需要请求头：

```text
Idempotency-Token: xxxxx
```

### AI 导购

- `POST /ai/shopping/recommend` AI 推荐
- `POST /ai/shopping/ask` AI 问答

## 示例请求

### 1. 加入购物车

```json
POST /cart/items
{
  "userId": 1,
  "storeId": 1,
  "skuId": 10001,
  "quantity": 2
}
```

### 2. 获取下单 token

```text
GET /orders/token?userId=1
```

### 3. 提交订单

```json
POST /orders/submit
Idempotency-Token: your-token

{
  "userId": 1,
  "storeId": 1,
  "remark": "少冰"
}
```

### 4. AI 推荐

```json
POST /ai/shopping/recommend
{
  "userId": 1,
  "storeId": 1,
  "query": "晚上想吃点清淡的，预算 20 元以内"
}
```

### 5. AI 问答

```json
POST /ai/shopping/ask
{
  "userId": 1,
  "storeId": 1,
  "query": "这个门店现在有什么适合夜宵而且不辣的？"
}
```

## 缓存与异步能力

### Redis

当前 Redis 主要用于：

- 商品列表缓存
- 商品详情缓存
- 门店商品列表缓存
- 订单提交幂等 token

### Kafka

当前 Kafka 主要用于：

- 下单成功后发送订单创建事件
- 消费端先做最小逻辑：
  - 记录日志
  - 模拟发送通知
  - 模拟写入埋点

## 重要实现细节

- 项目当前采用“单门店购物车”模型
- 下单时只会读取当前门店下的有效购物车
- 库存采用 `available_stock + locked_stock` 模式
- 取消订单时会回滚之前锁定的库存
- AI 推荐只把 RAG 用于知识补充，不参与库存和价格的最终事实判断

## 已知限制

当前版本仍有一些明显限制，README 这里直接说明：

- 没有真正的登录态，`userId` 由前端直接传入
- 不是完整的生产级 RAG，只是本地知识库 + 轻量召回
- 还没有支付成功、订单超时关单、库存自动释放等完整状态机
- Kafka 事件链路目前主要用于演示
- 缺少完备测试用例和初始化样例数据
- `application.yml` 当前写了本地数据库账号密码，建议自行改成环境变量方案

## 后续可演进方向

- 接入真正的用户登录与鉴权
- 将知识库迁移到数据库表，并补充知识同步任务
- 将 `KnowledgeRetriever` 替换为向量库 / 混合检索实现
- 增加 Rerank
- 引入支付成功、自动关单、库存解锁任务
- 加入订单超时处理和补偿逻辑
- 为购物车、订单、AI 模块补充集成测试

## 说明

这个仓库目前的定位是：

- 业务侧展示即时零售后端的基本骨架
- AI 侧展示从“意图识别”演进到“轻量 RAG 导购”的路径

如果你后面要继续演进成更完整的版本，建议优先补三块：

1. 认证与权限
2. 订单状态机
3. 数据级知识库与向量检索
