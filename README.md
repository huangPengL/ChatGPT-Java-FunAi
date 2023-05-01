# 🚀 Funai - Based on ChatGPT and SpringBoot

## 📖 简介

- 欢迎来到FunAi的后端仓库，我们使用Java对了现有AGI进行二次开发。
- 目前是FunAi的第一个版本，已经接入ChatGPT和GPT-4实现一些有意思的应用，详细请见【功能介绍】部分。
- 目前[FunAi网站](http://funai.space/) 可免费使用😆，欢迎进入[FunAi](http://funai.space/)网站一起玩耍吧~

## 🔰 快速开始

🎈第一步：MySQL导入funai.sql文件

🎈第二步：必配项目 （在项目中全局搜 TODO关键字找到必配条目）

- 配置application.properties
  - MySQL
  - Redis
  - 梦网云短信服务

- 配置向量库（二选一，推荐选Pinecone）

  - **Pinecone向量库（第三方）**

    1. 去Pinecone申请apikey

    2. 在`PineconeApi.java`中完善以下信息

       ```java
       private static final String PINECONE_API_URL = "https://xxxxxx.pinecone.io";
       ```

    3. 在MySQL数据库表admin_apikey中插入一条记录，type为4，name为Pinecone的apikey

  - Milvus向量库（本地搭建）

    1. 搭建Milvus

    2. 在`MilvusClientUtil.java`中完善以下信息

       ```java
       private static final MilvusServiceClient milvusClient = new MilvusServiceClient(
                   ConnectParam.newBuilder()
                           .withHost("xx.xx.xx.xx")
                           .withPort(19530)
                           .build());
       ```

       

🎈第三步：配置OpenAI的apikey

1. 创建OpenAI账号，申请apikey
2. 在MySQL数据库admin_apikey中插入一条记录，type为0，name为OpenAI的apikey



🎈第四步：启动 FunAiApplication  或  在测试类TestChatService中测试chatOneShot方法



## 🤖 功能介绍

✅ ChatGPT聊天

  - 支持markdown格式，代码高亮，代码复制，公式和图表展示

  - 流式输出 / 普通输出

  - 无限轮聊天 + 带上下文逻辑

  - 多会话 + 记录存储 + 记录管理（新增/清空/删除）

  - 数据导出

  - 语音聊天 + 语音播放

  - TODO: 接入文生图模型（MidJourney / Stable Diffusion Model）

  ![chat-1.png](mdImg/chat-1.png)

  ![chat-2.png](mdImg/chat-2.png)

✅ PDF智能阅读（论文 / 简历 / 知识文档等）

  - 接入OpenAI的Embedding API，使用Pinecone向量库存储向量。

  - 大文件上传（目前测试阶段暂时最多支持80页）

  - 多会话 + 文件保存 + 记录存储 + 记录管理（新增 / 删除） 

  ![pdf-2.png](mdImg/pdf-2.png)

  ![pdf-3.png](mdImg/pdf-3.png)

✅ 文字冒险游戏

  - 多游戏主题设置

  - 游戏记录存储 + 按键互动

  - TODO: 游戏描述图片展示 + 背景音乐

  ![game-2.png](mdImg/game-2.png)

  ![game-3.png](mdImg/game-3.png)

✅ 专家系统

  - 多领域（投资、广告、小说、计算机、诗歌、哲学、医学、心理学等）

  - 多语言

![expert-2.png](mdImg/expert-2.png)

✅ 智能语言学家

  - 更像人类的翻译官

  - 语法修改器 TODO

![trans-2.png](mdImg/trans-2.png)

✅ 提示库

  - 权限管理

  - 全局搜索

  - 提示库管理

    

✅ 账号管理

- 游客登录（IP）

  - 注册（手机号 + 验证码 + 密码）

  - 登录（手机号 + 用户名 + 密码）

  - 重置密码

  - APIKEY 管理（用户可上传自己的API-Key，不受限制。否则每天有根据用户等级不同设置聊天限制）

  - 用户等级管理（普通用户、vip用户、管理员、游客）

  - 权限管理



✅ 模拟面试 （TODO）

  - 简历分析

  - 根据简历内容进行对应的面试，模拟真实面试过程

  - 支持多次面试

  - 随时结束面试，结束后可以获得本轮面试得分



✅ 口语练习 （TODO）

  - 多种真人语音对话



## ➰ 项目技术





## 📄 License

FunAi is licensed under the MIT License. See the [LICENSE](https://github.com/knuddelsgmbh/jtokkit/blob/main/LICENSE) file for more information.



