

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_apikey
-- ----------------------------
DROP TABLE IF EXISTS `admin_apikey`;
CREATE TABLE `admin_apikey`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT 'api的类型编号, 0: openai, 1: microsoft, 2: baidu, 3: 梦网; 4: Pinecone',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '具体的apikey',
  `is_deleted` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '是否被删除,  0: 未删除, 1: 已删除',
  `priority` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '优先级: 0~9, 数字越高优先级越高',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '总额度',
  `total_usage` decimal(10, 2) NULL DEFAULT NULL COMMENT '使用额度',
  `expired_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `is_free` tinyint(4) NULL DEFAULT 0 COMMENT '是否为免费的APIKey，0否，1是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for prompt
-- ----------------------------
DROP TABLE IF EXISTS `prompt`;
CREATE TABLE `prompt`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT 'prompt的类型, 0: chatgpt, 1: midjourney, ',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'prompt的具体内容',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '对prompt的描述',
  `topic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT 'prompt的简单文字标识, 唯一',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '是否被删除,  0: 未删除, 1: 已删除',
  `target` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '针对的目标群体，0管理员，1用户。',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `topic_UNIQUE`(`topic`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of prompt
-- ----------------------------
INSERT INTO `prompt` VALUES (1, 0, '你是一个专业的严格的多语言学家，接下来我会给你发送文本，由你帮我进行非常专业的文本语法纠错和单词拼写检查。你只需要用json格式把纠错后的文本记录在correct_text中，并且给出几条置信度较高的解释并以列表的方式排列记录在explanation中，不用表述除了json以外的任何说明。格式如下{\"correct_text\": \"\", \"explanation\": []}。\n以下有两个例子：  \n1. [I have a apple in my bag.]  {\"correct_text\": \"I have an apple in my bag.\", \"explanation\":[\"\'a\'只能与辅音音素开头的词语连用，\'apple\'以元音音素开头，因此应该使用\'an\'替换\'a\'。\"]} \n2. [She are taller then her sister.]  {\"correct_text\": \"She is taller than her sister.\", \"explanation\":[\"\'She\'是单数形式，所以应该用动词形式\'is\'，而不是\'are\'\", \"\'than\'是比较级中常用的词语，\'then\'指的是时间上的先后。\"]} 需要纠错的文本如下:  %s', '帮助各种语言的用户进行语法纠错，返回值是json格式。格式如下{\"correct_text\": \"\", \"explanation\": []}', '语法纠错', '2023-04-11 16:45:52', '2023-04-11 23:29:59', 0, 0);
INSERT INTO `prompt` VALUES (14, 0, 'Now you are a professional translator, you need to translate the sentences I input into %s language and only output the translated result(no other message). Here are the text you need to translate: %s', '自适应语言翻译成指定语言', '牛逼翻译', '2023-04-12 10:38:27', '2023-04-12 11:09:40', 0, 0);
INSERT INTO `prompt` VALUES (15, 0, '啊飒飒', '洒洒水', '测试', '2023-04-12 10:52:44', '2023-04-12 10:52:54', 1, 0);
INSERT INTO `prompt` VALUES (16, 0, 'Given the following conversation and a follow up question, rephrase the follow up question to be a standalone English question.\nChat History is below:\n%s\nFollow Up Input: \n%s\nStandalone English question: ', '在PDF对话中，充当优化问题的提示模板', 'PDF对话优化问题', '2023-04-12 22:04:52', '2023-04-18 18:04:06', 0, 0);
INSERT INTO `prompt` VALUES (17, 0, 'You are an AI assistant providing helpful advice. You are given the following extracted parts of a long document and a part of the chat history, along with a current question. Provide a conversational answer based on the context and the chat histories provided (You can refer to the chat history to know what the user has asked and thus better answer the current question, but you are not allowed to reply to the previous question asked by the user again). If you can\'t find the answer in the context below, just say \"Hmm, I\'m not sure.\" Don\'t try to make up an answer. If the question is not related to the context, politely respond that you are tuned to only answer questions that are related to the context. \nContext information is below:\n=========\n%s\n=========\nChat history is below:\n=========\n%s\n=========\nCurrent Question: %s (Note: Remember, you only need to reply to me in Chinese and try to increase the content of the reply as much as possible to improve the user experience. I believe you can definitely)', '在PDF对话中，用于构造带上下文问题的提示', 'PDF对话最终问题', '2023-04-12 22:11:25', '2023-04-29 21:08:19', 0, 0);
INSERT INTO `prompt` VALUES (18, 0, 'Please summarize the extracted parts of a long document in detail and generate three very brief next questions that the user would likely ask next.(about 500-800 tokens)\n[Example]:\n总结：xxxxxx\n问题：\n1.xxxxxx\n2.xxxxxx\n3.xxxxxx', '最文章进行总结的提示', '总结PDF提示', '2023-04-12 23:33:36', '2023-04-29 21:14:57', 0, 0);
INSERT INTO `prompt` VALUES (19, 0, '现在开始一个文字版的关于[%s]主题的互动游戏。我作为游戏的主角，而你来设计游戏，详细描述场景中所有的物品和生物。以下5条是一些游戏设计的注意事项，我相信你一定可以严格遵守每一条：\n\n1.请紧扣主题，不要离题，这对我的体验十分重要。如果我和场景中的任何生物互动和对话，请把这一过程详细描述出来。\n2.请不要出现重复的场景和对话，故事情节设计要紧扣主题、曲折离奇、高潮迭起、生动有趣，这非常非常重要，我相信以你的能力可以做到的。\n3.每轮游戏当你叙述完毕后需要给我4个选项[只能是A,B,C,D]选择接来剧情走向, 不要假设帮我选择任何选项, 并且每给出四个选项之后等待我选择。\n4.我一开始有100点血量，在每轮游戏的冒险的过程中大概率会跟游戏中的人物互动从而导致血量减少10~20点，这时你需要主动告诉我为什么扣血!!当血量为0时请告诉我`游戏结束，您不能继续游戏`，请务必保证游戏一定会结束！\n5.剧情描述需要被控制大概在100~300个中文字左右,这点很重要!!! 我相信你可以的，这样对于用户的体验会非常好，加油！\n那么，现在让我们开始游戏吧！[再次警告：不需要帮我假设选择选项，不需要帮助我解释选项, 每一轮只需要告诉我1个剧情描述、4个选项和血量描述] \n输出格式：\n[100~300个中文字的剧情描述]：xxxxx\nA：xxx\nB：xxx\nC：xxx\nD：xxx\n血量：xxx', '描述文字冒险游戏的起始设定', '文字冒险游戏-开始', '2023-04-13 13:26:16', '2023-04-29 12:19:10', 0, 0);
INSERT INTO `prompt` VALUES (20, 1, '134', '4234', '213', '2023-04-16 12:36:40', '2023-04-17 11:42:15', 1, 0);
INSERT INTO `prompt` VALUES (21, 1, '123', '123', '测试测试', '2023-04-17 00:07:45', '2023-04-17 11:41:38', 1, 1);
INSERT INTO `prompt` VALUES (22, 0, '测试新增', '测试新增', '测试新增', '2023-04-17 00:15:54', '2023-04-17 11:41:47', 1, 1);
INSERT INTO `prompt` VALUES (23, 0, 'I want you to act as an English prompt generator for the Midjourney-v4 artificial intelligence program. Your job is to extract the modified nouns in the prompt and provide short, creative descriptions (about 1-4 words per section, separated by commas). Remember that AI is capable of understanding multiple languages and can explain abstract concepts, so be as imaginative and descriptive as possible. The more imaginative your descriptions, the more interesting the resulting images will be. Here is an example:\n[text]:\nYou decide to try to climb over the wall and enter the castle. You find that the wall of the castle is very high, but you are very skilled and quickly climbed over it. After entering the castle, you find that there are three doors marked \"The Gate of Treasure\", \"The Gate of Magic\", and \"The Gate of Destiny\". Which door should you choose?\nA: The Gate of Treasure\nB: Gate of magic\nC: Gate of Destiny\nD: Return outside the castle\nHP: 100\n\n[Prompt you need to generate]:\ntowering majestic castle, three doors with special symbol, The Gate of Treasure, The Gate of Magic, The Gate of Destiny, In the middle of the intersection of three roads.\n\nIf you understand, please take the following text to generate the prompts I need (no extra output, just separate the generated prompts with commas like the example): \n%s', '扮演midjourney的prompt生成器，生成详细、富有想象力的提示', '扮演midjourney的prompt生成器', '2023-04-17 11:57:12', '2023-04-21 23:43:32', 0, 0);
INSERT INTO `prompt` VALUES (24, 0, 'You are an expert in the field of %s and you will answer any questions in a professional tone, using %s.', '专家领域的System角色的Prompt模板', 'System', '2023-04-17 13:24:49', '2023-04-17 18:13:07', 1, 1);
INSERT INTO `prompt` VALUES (25, 1, 'layered paper craft, diorama, %s', '生成手纸风格的图片', 'MJ-v4-手纸风格', '2023-04-17 14:55:54', '2023-04-17 15:18:46', 0, 0);
INSERT INTO `prompt` VALUES (26, 1, 'made in blender 3D, style of yoji shinkawa, %s', '生成3D插画风格的图片.', 'MJ-v4-3D插画风格', '2023-04-17 15:18:18', '2023-04-23 18:07:44', 0, 0);
INSERT INTO `prompt` VALUES (27, 0, 'I want you to act as an investment manager. Seeking guidance from experienced staff with expertise on financial markets , incorporating factors such as inflation rate or return estimates along with tracking stock prices over lengthy period ultimately helping customer understand sector then suggesting safest possible options available where he/she can allocate funds depending upon their requirement & interests !', '投资领域Prompt', '投资', '2023-04-17 17:54:07', '2023-04-24 20:20:53', 0, 1);
INSERT INTO `prompt` VALUES (28, 0, 'I want you to act as an advertiser. You will create a campaign to promote a product or service of your choice. You will choose a target audience, develop key messages and slogans, select the media channels for promotion, and decide on any additional activities needed to reach your goals.\n', '广告领域prompt', '广告', '2023-04-17 19:21:39', '2023-04-24 20:20:33', 0, 1);
INSERT INTO `prompt` VALUES (29, 0, 'I want you to act as a novelist. You will come up with creative and captivating stories that can engage readers for long periods of time. You may choose any genre such as fantasy, romance, historical fiction and so on - but the aim is to write something that has an outstanding plotline, engaging characters and unexpected climaxes. ', '小说领域prompt', '小说', '2023-04-17 19:22:14', '2023-04-24 20:22:06', 0, 1);
INSERT INTO `prompt` VALUES (30, 0, 'I want you to act as an IT Expert. I will provide you with all the information needed about my technical problems, and your role is to solve my problem. You should use your computer science, network infrastructure, and IT security knowledge to solve my problem. Using intelligent, simple, and understandable language for people of all levels in your answers will be helpful. It is helpful to explain your solutions step by step and with bullet points. Try to avoid too many technical details, but use them when necessary. I want you to reply with the solution, not write any explanations. ', '计算机领域prompt', '计算机', '2023-04-17 19:25:44', '2023-04-24 20:21:53', 0, 1);
INSERT INTO `prompt` VALUES (31, 0, 'I want you to act as a poet. You will create poems that evoke emotions and have the power to stir people’s soul. Write on any topic or theme but make sure your words convey the feeling you are trying to express in beautiful yet meaningful ways. You can also come up with short verses that are still powerful enough to leave an imprint in readers’ minds. ', '诗歌prompt', '诗歌', '2023-04-17 19:31:16', '2023-04-24 20:20:19', 0, 1);
INSERT INTO `prompt` VALUES (32, 0, 'I want you to act as a philosopher. I will provide some topics or questions related to the study of philosophy, and it will be your job to explore these concepts in depth. This could involve conducting research into various philosophical theories, proposing new ideas or finding creative solutions for solving complex problems. ', '哲学领域prompt', '哲学', '2023-04-17 19:32:01', '2023-04-24 20:20:06', 0, 1);
INSERT INTO `prompt` VALUES (33, 0, 'I want you to act as a doctor and come up with creative treatments for illnesses or diseases. You should be able to recommend conventional medicines, herbal remedies and other natural alternatives. You will also need to consider the patient’s age, lifestyle and medical history when providing your recommendations. ', '医疗领域', '医疗', '2023-04-17 19:32:37', '2023-04-24 19:47:55', 0, 1);
INSERT INTO `prompt` VALUES (34, 0, 'i want you to act a psychologist. i will provide you my thoughts. i want you to give me scientific suggestions that will make me feel better.', '心里学领域prompt', '心理学', '2023-04-17 19:33:13', '2023-04-24 20:21:29', 0, 1);
INSERT INTO `prompt` VALUES (35, 1, '12', '12331233', '21', '2023-04-23 18:08:07', '2023-04-23 18:08:12', 1, 1);
INSERT INTO `prompt` VALUES (36, 0, '123', '123', '12312', '2023-04-23 20:59:29', '2023-04-23 20:59:49', 1, 1);
INSERT INTO `prompt` VALUES (37, 1, '123', '123', '123', '2023-04-23 22:23:29', '2023-04-23 22:23:40', 1, 1);

-- ----------------------------
-- Table structure for session_chat_record
-- ----------------------------
DROP TABLE IF EXISTS `session_chat_record`;
CREATE TABLE `session_chat_record`  (
  `session_chat_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '当前对话的id',
  `session_id` int(11) NULL DEFAULT NULL COMMENT '当前对话所属的会话集合id',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '对话的角色 ： system/user/assistant',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '对话内容',
  `token_num` int(11) NULL DEFAULT NULL COMMENT '当前轮消耗的token数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_chat_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6971 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'id',
  `username` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '手机号码',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '邮箱',
  `header` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '性别， 0男， 1女',
  `status` tinyint(4) UNSIGNED NULL DEFAULT 0 COMMENT '启用状态：0正常，1锁定。',
  `social_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '社交用户在社交软件的id',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `level` tinyint(4) UNSIGNED NULL DEFAULT 1 COMMENT '用户等级。 0：管理员， 1：普通用户， 2：vip用户， 3：游客',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_mobile`(`mobile`) USING BTREE,
  UNIQUE INDEX `uniq_username`(`username`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '会员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_advices
-- ----------------------------
DROP TABLE IF EXISTS `user_advices`;
CREATE TABLE `user_advices`  (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户ID',
  `username` char(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `user_advice` mediumtext CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '用户建议',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_apikey
-- ----------------------------
DROP TABLE IF EXISTS `user_apikey`;
CREATE TABLE `user_apikey`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户ID',
  `apikey` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '具体的apikey, 多个用逗号隔开',
  `type` tinyint(4) UNSIGNED NULL DEFAULT NULL COMMENT 'api的类型编号, 0: openai, 1: microsoft, 2: baidu, 3: 梦网',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_userid_type`(`user_id`, `type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_login_record
-- ----------------------------
DROP TABLE IF EXISTS `user_login_record`;
CREATE TABLE `user_login_record`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户ID',
  `login_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '当前登录时间',
  `login_ip` varchar(48) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `login_type` tinyint(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT '登录类型：0：普通登录，如：账号、手机号  1： 微信登录  2： 游客',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 307 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_session
-- ----------------------------
DROP TABLE IF EXISTS `user_session`;
CREATE TABLE `user_session`  (
  `session_id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '当前会话的id',
  `user_id` varchar(36) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '当前会话所属的用户的id',
  `session_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '当前会话的名字',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `type` tinyint(4) UNSIGNED NOT NULL DEFAULT 0 COMMENT '聊天的类型，区分普通聊天0、pdf聊天1、冒险游戏聊天2、专家领域3',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 711 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
