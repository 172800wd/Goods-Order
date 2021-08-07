# 商品下单系统开发文档

1. 使用SpringBoot，SpringCloud，Mybatis-Plus框架开发
2. 中间件使用Redis，MySQL，RabbitMQ，Consul

一个简单的商品下单系统，系统包含两个微服务：

## 1. order-web

主要用于前台展示，提供以下的接口:

1. 根据关键字查询某个卖家的商品列表接口。
2. 按照创建时间倒序排序，并且支持分页查询。
3. 根据商品ID查询商品详情。
4. 加入购物车。
   1. 加入购物车的数据和商品库存的数量保证事务一致。
   2. 当购物车存在当前商品时，只能加数量，不能新增新的商品条目。
5. 根据买家ID查询购物车的商品信息，支持分页查询，通过添加时间倒序排序。
6. 提交订单。
   1. 提交订单成功之后，对应商品的购物车条目清空，订单的数据新增，保证事务。

## 2. order-admin
商品订单管理后台，主要用于商品管理功能，提供一下接口：

1. 新增某个商家的商品。
2. 根据商家ID查询商品列表信息
3. 根据商品ID删除商品信息，逻辑删除、由于客户端order-web查询商品有Redis缓存，而且为了验证数据和缓存的一致性的处理，所以需要处理缓存。具体实现是通过发送消息到RabbitMQ中，通知客户端order-web将Redis中的缓存删除，保证数据一致性。

## 接口设计

所有接口统一返回数据格式

```json
{
    "code": "状态码 0 成功，5000 失败",
    "data": "业务数据",
    "msg" : "描述信息"
}
```

### 一、order-web服务

#### 1、根据关键字查询指定卖家商品列表接口

##### 接口地址

```http
/api/goods/web/list/v1
```

##### 接口请求参数

```http
pageIndex: 页码
pageSize：每页显示的条数
queryKey：查询关键字(如果关键字为纯数字，将其视为goodsId进行精准查询，否则视为商品名称模糊查询，如果为空，表示查询所有商品)
sellerId：卖家ID
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data":{
        "pageIndex":1,
        "records":[
            {
                "goodsId":1,
                "goodsName":"衣服",
                "goodsPrice":10.24,
                "goodsStockNumber":100
            }
        ],
        "size":10,
        "pageTotal":100
    },
    "msg":"执行成功"
}
```

查询失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	1.该接口首先查询Redis缓存数据，如果Redis中存在对应的缓存数据，则直接取出该缓存数据，返回给用户；
    2.如果Redis中没有缓存数据，则会通过远程服务调用order-admin服务提供的接口/api/goods/list/v1查询商品数据，order-admin查询到数据后，会将其放进Redis缓存中，设置过期时间为12小时，然后再将数据返回给用户。
```

#### 2、根据商品ID查询商品详情

##### 接口地址

```http
/api/goods/getById/v1
```

##### 接口请求参数

```http
id: 商品ID
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data": {
       "goodsId":1,
       "goodsName":"衣服",
       "goodsPrice":10.24,
       "goodsStockNumber":100
    },
    "msg":"执行成功"
}
```

查询失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	该接口直接查询数据库，查询到数据后再放进Redis缓存，缓存时间12小时。
```

#### 3、加入购物车

##### 接口地址

```http
/api/shopping/cart/add/v1
```

##### 接口请求参数

```http
goodsId: 商品ID
buyNumber: 购买数量
buyerId: 购买者用户Id，随机传入[1,5]之间的数字即可
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data": {},
    "msg":"执行成功"
}
```

操作失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	相同的卖家，相同的卖家店铺商品加入购物车只能加数据量。
```

#### 4、查询购物车的商品信息

##### 接口地址

```http
/api/shopping/cart/list/v1
```

##### 接口请求参数

```http
buyerId: 需要查询的买家的用户Id
pageIndex: 页码
pageSize：每页显示的条数
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data":{
        "pageIndex":1,
        "records":[
            {
                "id":1,
                "goodsId":1,
                "goodsName":"衣服",
                "goodsPrice":10.24,
                "goodsStockNumber":100,
                "description":""
            }
        ],
        "size":10,
        "pageTotal":100
    },
    "msg":"执行成功"
}
```

操作失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	通过用户ID查询对应的购物车中的商品列表，支持分页查询。
```

#### 5、提交订单

##### 接口地址

```http
/api/order/add/v1
```

##### 接口请求参数

```http
id： 购物车ID
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data": {},
    "msg":"执行成功"
}
```

操作失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	保证购物车和订单的数据一致,添加完成之后删除购物车对应条目，保证事务
```

### 二、order-admin服务

#### 1、新增商品信息

##### 接口地址

```http
/api/goods/add/v1
```

##### 接口请求参数

```http
sellerId: 商家ID (请求接口时，可以随机传入[6,10]之间的数字)
goodsName: 商品名称
goodsPrice: 商品价格
stockNumer: 库存数量
description: 备注
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code": 0,
    "msg": "执行成功",
    "data": {}
}
```

查询失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": []
}
```

##### 接口说明

```java
	参数需要放在RequestBody中，格式为json，否则无法请求该接口。该接口通过@RequestBody注解，从请求体中获取数据。
```

#### 2、根据商家ID查询商品列表信息

##### 接口地址

```http
/api/goods/list/v1
```

##### 接口请求参数

```http
sellerId： 商家ID(可随机取[6,10]之间的数字)
pageIndex: 页码
pageSize：每页显示的条数
queryKey：查询关键字(如果为纯数字，视为goodsId精准匹配，否则视为goodsName模糊匹配，为空表示查询所有商品数据)
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code":0,
    "data":{
        "pageIndex":1,
        "size":10,
        "pageTotal":100,
        "records":[
            {
                "goodsId":1,
                "goodsName":"衣服",
                "goodsPrice":10.24,
                "goodsStockNumber":100
            }
        ]
    },
    "msg":"执行成功"
}
```

查询失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	该接口会将查询到的数据放到Redis缓存中，缓存过期时间12小时。
```

#### 3、根据商品ID删除商品信息

##### 接口地址

```http
/api/goods/deleteById/v1
```

##### 接口请求参数

```http
goodsId: 商品ID
```

##### 请求方式：

```http
GET
```

##### 接口返回

查询成功返回格式如下:

```json
{
    "code": 0,
    "msg": "执行成功",
    "data": {}
}
```

查询失败返回格式如下：

```json
{
    "code": 5000,
    "msg": "失败原因",
    "data": {}
}
```

##### 接口说明

```java
	1.该接口提供删除商品的功能，用户通过输入需要删除的商品id，本接口逻辑删除MYSQL数据库中对应的数据的信息。
	2.当有人调用该接口时，该接口需要通过发送消息给RabbitMQ，通知 order-web 服务，order-web 服务收到 RabbitMQ 的删除推送后，删除 redis 中缓存的商品信息，保证数据一致性。
```

### 数据库表结构

数据库核心表为商品表goods、购物车shopping_cart、订单表 order。

```sql
CREATE TABLE `goods` (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`seller_id` bigint(20) NOT NULL COMMENT '卖家ID/商家ID',
	`goods_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
	`goods_price` decimal(10, 2) NOT NULL COMMENT '价格',
	`goods_stock_number` int(11) NOT NULL COMMENT '剩余库存数量',
	`goods_description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商品备注',
	`deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除状态：1：已删除；0：未删除',
	`create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
	`update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY USING BTREE (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT '商品表';

CREATE TABLE `shopping_cart` (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`buy_id` bigint(20) NOT NULL COMMENT '买家ID/客户ID',
	`goods_id` bigint(20) NOT NULL COMMENT '商品ID',
	`goods_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
	`goods_price` decimal(10, 2) NOT NULL COMMENT '商品价格',
	`goods_number` int(11) NOT NULL DEFAULT 1 COMMENT '商品数量',
	`description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注/留言',
	`create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
	`update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY USING BTREE (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT '购物车';

CREATE TABLE `order` (
	`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`buy_id` bigint(20) NOT NULL COMMENT '买家ID/客户ID',
	`seller_id` bigint(20) NOT NULL COMMENT '卖家ID/商家ID',
	`goods_id` bigint(20) NOT NULL COMMENT '商品ID',
	`goods_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
	`goods_price` decimal(10, 2) NOT NULL COMMENT '商品价格',
	`goods_number` int(11) NOT NULL DEFAULT 1 COMMENT '商品数量',
	`pay_status` int(11) NOT NULL DEFAULT 0 COMMENT '支付状态 -2：支付失败 -1：提交失败  0：待支付  1：提交成功  2：支付成功',
	`description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注/留言',
	`create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
	`update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY USING BTREE (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic COMMENT '订单表';
```

