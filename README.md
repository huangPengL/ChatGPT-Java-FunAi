# 🚀 FunAi - Based on ChatGPT and SpringBoot
> 声明：FunAi项目只发布于 GitHub，基于 Apache-2.0 协议，免费且作为开源学习使用，未经本人同意不可用于商业项目。欢迎加[微信群](https://funai-hpl.oss-cn-guangzhou.aliyuncs.com/homePage/contact-1.png) 和[个人微信号](https://funai-hpl.oss-cn-guangzhou.aliyuncs.com/homePage/contact-3.png)交流讨论。若有长期定制合作或者内部高级版使用意向请微信联系[funaiboy](https://funai-hpl.oss-cn-guangzhou.aliyuncs.com/homePage/cooperate-1.png
)或邮件552122632@qq.com

> 致谢：感谢「三岁药哥」 和 [「广大昊神」](https://blog.42yeah.is) 对本项目在API访问上的支持；感谢相关合作伙伴对本项目的其他基础支持；感谢团队的四位成员贡献想法和代码[@塔哥](https://github.com/Nagin-Kim) [@俊哥](https://github.com/maomao12345678) [@东哥](https://github.com/Hudee666) [@伟哥](https://github.com/xing-wei-zeng) 

## 📖 简介
- 欢迎来到FunAi的后端仓库，我们正在使用Java对现有AGI进行二次开发。
- 目前的FunAi已经接入ChatGPT、GPT-4、MJ-V4和SD实现一些有意思的应用，详细请见【项目亮点】 和 【功能展示】🎊
- 作为一个新颖的Java学习项目，你可以从【快速开始】和【技术栈】部分得到有效的信息🎉
- [FunAi网站](https://funai.vip/) 完全免费使用，欢迎进入[FunAi](https://funai.vip/)一起玩耍吧😆



## 🤗 持续更新

### FunAi线上项目
- [TODO] 魔鬼社区 & 超越chatPDF的高级File咨询
- [TODO] 迭代血球
- [TODO] 人心镜面
- [2023-06-18] 新增AI助手功能，支持多语言，大量预设prompt的专一领域的AI帮你解决问题。[点击体验](https://funai.vip/#/AIChatHome)
- [2023-06-18] PDF阅读可以上传word、excel、ppt等格式的文件。 [点击体验](https://funai.vip/#/ChatWithFile)
- [2023-06-07] 新增Stable Diffusion画图功能。[点击体验](https://funai.vip/#/ImgGenerate)
- [2023-06-07] 新增AI客服，24/7全天候服务，多语言支持，拟人化交互；新增人工客服。 [点击体验](https://funai.vip/#/AssitantIndex)
- [2023-05-16] 聊天界面可画图，聊天内容包含前缀[画]即可触发该功能。[点击体验](https://funai.vip/#/ChatHome/NormalChat)
- [2023-05-16] 文字游戏可生成场景图，点击聊天条目中右侧的图片按钮即可触发该功能。[点击体验](https://funai.vip/#/GameChat)
- [2023-05-13] 新增[PDF阅读-多文件版本] & 新增可选择对照多源文件功能。[点击体验](https://funai.vip/#/ChatWithFile)
- [2023-05-13] 新增[PDF阅读-单文件]可对照源文件功能
- [2023-05-07] 优化文生图模型MJ-V4，支持中文大白话描述。[点击体验](https://funai.vip/#/ImgGenerate)
- [2023-05-06] 新增文生图模型(不支持中文)

### 本开源仓库

- [2023-05-10] 修复文件名低于3个字符出现的服务错误问题
- [2023-05-05] 修改apiKey轮询bug
- [2023-05-03] 新增对openai免费Key限制所做的优化的轮询调度算法

## ➰ FunAi线上项目亮点



✅ ChatGPT聊天

  - 集成OpenAi API (ChatGPT3.5 + GPT4 + embedding)
  
  - 支持markdown格式，代码高亮，代码复制，公式和图表展示

  - 无限轮聊天 + 带上下文逻辑 （Guava Cache 优化响应时间）

  - 流式输出 / 普通输出

  - 多会话 + 记录存储 + 记录管理（新增/清空/删除）

  - 数据导出
  
  - 语音聊天 + 语音播放

  - 接入文生图模型（MidJourney / Stable Diffusion Model）

  - 大量多语言支持的AI助手




✅ PDF智能阅读（论文 / 简历 / 知识文档等）

  - 接入OpenAI的Embedding API，使用Pinecone/Milvus向量库存储向量。

  - PDF解析 + 递归分词文段抽取 + 文本向量化 + 向量语义匹配 + 召回知识库相似文本匹配

  - 大文件上传（目前线上项目暂时最多支持80页，实际可支持上千页）

  - 多会话 + 文件保存 + 记录存储 + 记录管理
  
  - 对照源文件阅读 + ChatGPT互动

  - 多源PDF上传，集成庞大知识库

  - 支持各种文件类型（Word、Excel、PPT、PDF等）



✅ AI客服

- 多语言支持
- 24/7全天候服务：无论是白天还是夜晚，AI客服都可以提供不间断的服务，满足用户随时随地的需求。
- 拟人化交互，擅长用表情符号或动画来表达情感
- 转接人工客服 TODO




✅ 智能画图

  - 接入MJ-V4和Stable Diffusion

  - 普通模式：用户输入端支持中文大白话描述，ChatGPT优化描述

  - 专业模式：插件式提供海量更适合模型的英文prompt



✅ 文字冒险游戏

  - 多主题设置

  - 游戏记录存储 + 按键互动

  - AI生成场景描述图片

  - TODO: 背景音乐 + 多游戏选择



✅ 语言专家

  - 多语翻译

  - 语法修改

    

✅ 账号管理

  - 游客登录（IP / 浏览器指纹）

  - 登录注册（手机号 / 账号密码 / 微信TODO）

  - 用户API-Key管理（用户可上传自己的API-Key，不受限制。否则使用系统的API-Key，会根据用户等级不同设置当日聊天限制）

  - 用户等级管理（普通用户、vip用户、管理员、游客）




✅ 提示库

  - 实时修改提示，动态影响系统相关内置功能

  - 全局多条件分页搜索

  - 提示库管理（权限管理 + 提示增删查改）



✅ 其它

  - 系统API-Key池管理（优化多账号轮询调度算法，突破免费账号5次/分钟限制）

  - API-Key额度查询 + 定时失效检测

  - 异步优化请求响应时间

  - 定时任务刷新缓存

  - OpenAI多模型选择 + 参数自定义



✅ 智能简历 TODO

  - ChatGPT智能分析简历


✅ 口语练习 TODO

  - 多种真人语音对话

✅ 魔鬼社区 TODO

  - 跟AI魔鬼无限互动吧



## 🔰 快速开始

🎈第零步：科学上网（全局模式） 或 海外服务器

🎈第一步：MySQL导入funai.sql文件

🎈第二步：必配项目 （在项目中全局搜 TODO关键字找到必配条目）

- 配置application.properties
  - MySQL
  - Redis
  - 梦网云短信服务（无需手机注册功能可不填）

- 配置向量库（二选一，推荐选Pinecone; 注意：openai embedding api的向量维度为1536）

  - **Pinecone向量库（第三方）**

    1. 去Pinecone申请apikey
    
        1.1 [Pinecone官网](https://app.pinecone.io/) 进入官网注册你的账号 
      
        1.2 创建Index, 其中Pod Type可以自己选择
        
        <img src="mdImg/pinecone-1.png" width="85%" height="85%">
      
        1.3 在API Keys中可以找到你的API 密钥，还可以从仪表板检索您的环境和索引名称。（用户构建PINECONE_API_URL）
      
        1.4 [Pinecone Http请求学习文档](https://docs.pinecone.io/reference/describe_index_stats_post)

    2. 在`PineconeApi.java`中完善以下信息

       ```java
       private static final String PINECONE_API_URL = "https://xxxxxx.pinecone.io";
       ```

    3. 在MySQL数据库表admin_apikey中插入一条记录，type为4，name为Pinecone的apikey
      
        ```mysql
        INSERT INTO `funai`.`admin_apikey` (`type`, `name`) VALUES (4, 'your pinecone apikey');
        ```

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
2. 在MySQL数据库admin_apikey中插入一条记录，type为0，name为OpenAI的apikey；若该key为免费账号则is_free字段需要填写为1
```mysql
INSERT INTO `funai`.`admin_apikey` (`type`, `name`, `is_free`) VALUES ('0', 'your openai apikey', '1');
```


🎈第四步：启动 FunAiApplication  或  在测试类TestChatService中测试chatOneShot方法



## :zap: 技术栈

### 前端

[FunAi前端仓库](https://github.com/huangPengL/ChatGPT-Vue-FunAi) 暂时未开放，敬请期待~

### 后端

- 主语言：Java（JDK 1.8）
- 开发框架：SpringBoot
- 核心技术：
  - 本地缓存Caffeine LoadingCache
  - SSE服务器发送事件
  - 算法（双端队列 + 滑动窗口 + 轮询负载均衡等）
  - Stream流
  - JUC
  - WebSocket
  - 锁机制
  - 定时任务
  - 拦截器（登录拦截/管理员权限/限流/功能限制）
  - 过滤器（跨域/全局日志）
  - 全局异常处理器 
  - JWT用户鉴权
- 数据库：MySQL 5.7、Pinecone、Milvus 2.2.5
- 中间件：Redis 7.0.11, MyBatis-Plus
- 对象存储： 阿里云OSS
- 第三方API：Openai-ChatGPT、Openai-Embedding、梦网云短信服务、百度语音识别

### 部署

- web 服务：Nginx
- 海外服务器\本地全局科学上网



## 🤖 功能展示

✅ ChatGPT聊天

<img src="mdImg/chat-4.png" width="85%" height="85%">

<img src="mdImg/chat-3.png" width="85%" height="85%">

<img src="mdImg/chat-1.png" width="85%" height="85%">

<img src="mdImg/chat-2.png" width="85%" height="85%">

✅ 文生图  

<img src="mdImg/text2Img-1.png" width="85%" height="85%">

<img src="mdImg/text2Img-2.png" width="85%" height="85%">

<img src="mdImg/text2Img-3.png" width="85%" height="85%">

✅ PDF智能阅读（论文 / 简历 / 知识文档等）  

<img src="mdImg/pdf-1.png" width="85%" height="85%">

<img src="mdImg/pdf-3.png" width="85%" height="85%">

<img src="mdImg/pdf-2.png" width="85%" height="85%">

✅ 文字冒险游戏

<img src="mdImg/game-2.png" width="85%" height="85%">

<img src="mdImg/game-4.png" width="85%" height="85%">

✅ AI客服

<img src="mdImg/assistant-1.png" width="85%" height="85%">

<img src="mdImg/assistant-2.png" width="85%" height="85%">

✅ 智能语言学家（更像人类的翻译官）

<img src="mdImg/trans-1.png" width="85%" height="85%">

<img src="mdImg/trans-2.png" width="85%" height="85%">


✅ 提示库

<img src="mdImg/prompt-1.png" width="85%" height="85%">


✅ 账号管理

<img src="mdImg/user-1.png" width="85%" height="85%">

<img src="mdImg/user-4.png" width="85%" height="85%">

<img src="mdImg/user-2.png" width="85%" height="85%">

<img src="mdImg/user-3.png" width="85%" height="85%">




✅ 智能简历 （TODO）


✅ 口语练习 （TODO）



## 🖋 参与贡献

<a href="https://github.com/huangPengL/ChatGPT-Java-FunAi/graphs/contributors">

  <img src="https://contrib.rocks/image?repo=huangPengL/ChatGPT-Java-FunAi" />

</a>





## 🍺 赞助 & 合作

如果你认为我的项目对你很有帮助，而且情况允许的话，那么请考虑支持我的项目。我将非常感激任何的支持，哪怕只是一点点的资助，也能激励我持续开发和改进这个项目。

您可以通过以下几种方式支持我的项目：

- 赞助我：您可以通过贡献资金来支持我的项目，这将帮助我支付服务器、工具和其他开发成本。您可以在下方找到资助方式。

- 分享项目：如果您不能贡献资金，但是您认为我的项目非常有价值，那么请考虑分享项目链接给您的朋友和同事。这将有助于我的项目得到更多的关注和支持。如果可以请给一个小小的star！

- 提供反馈：您可以通过提交Issues或者Pull Requests来帮助改进我的项目。如果您发现了任何错误或者您认为我的项目可以改进的地方，欢迎随时向我提供反馈。

- 与我合作：如果您对该项目感兴趣，想加入我们或有定制化需，欢迎随时与我们联系。

总之，非常感谢您对我的项目的支持，我将努力不懈地改进和提高这个项目的质量，让它更好地为您和其他用户服务。

WeChat Pay:

<img src="mdImg/wechat-pay.png" width="35%" height="35%">

Contact WeChat:

<img src="mdImg/mmexport1684888774983.jpg" width="35%" height="35%">

## ⏰ Star History

[![Star History Chart](https://api.star-history.com/svg?repos=huangPengL/ChatGPT-Java-FunAi&type=Timeline)](https://star-history.com/#huangPengL/ChatGPT-Java-FunAi&Timeline)


## 📄 License

FunAi is licensed under the Apache-2.0 License. See the [LICENSE](https://github.com/huangPengL/ChatGPT-Java-FunAi/blob/master/LICENSE) file for more information.


## 免责声明 Disclaimers

The code is for demo and testing only. 代码仅用于演示和测试。用于商业用途，请联系授权并注明来源。

⚠⚠⚠请勿将本系统代码用于商业用途！



