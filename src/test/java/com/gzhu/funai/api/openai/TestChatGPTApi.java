package com.gzhu.funai.api.openai;

import com.gzhu.funai.api.openai.resp.BillingUsage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @Author: huangpenglong
 * @Date: 2023/4/3 19:49
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestChatGPTApi {


    private final String encodeExample1 = "😀Hello 😀my name 😀is Kevin😀😀😀.";
    private final String encodeExample2 = "😀你好呀😀，😀我是ChatGPT😀";
    private final String encodeExample3 = "No, 人民军人后勤部 is the appellant (上诉人) in this case, not the respondent (被上诉人).";
    private final String encodeExample4 = "两数之和是一道经典的算法问题，可以使用多种方法来实现。下面是Java实现两数之和的代码：\n" +
            " \n" +
            " 方法1：暴力枚举\n" +
            " \n" +
            " ```java\n" +
            " public int[] twoSum(int[] nums, int target) {\n" +
            "     for (int i = 0; i < nums.length; i++) {\n" +
            "         for (int j = i + 1; j < nums.length; j++) {\n" +
            "             if (nums[i] + nums[j] == target) {\n" +
            "                 return new int[] {i, j};\n" +
            "             }\n" +
            "         }\n" +
            "     }\n" +
            "     return null;\n" +
            " }\n" +
            " ```\n" +
            " \n" +
            " 在这个实现中，我们使用了两层循环来枚举所有可能的组合。时间复杂度为O(n^2)，空间复杂度为O(1)。\n" +
            " \n" +
            " 方法2：哈希表\n" +
            " \n" +
            " ```java\n" +
            " public int[] twoSum(int[] nums, int target) {\n" +
            "     Map<Integer, Integer> map = new HashMap<>();\n" +
            "     for (int i = 0; i < nums.length; i++) {\n" +
            "         int complement = target - nums[i];\n" +
            "         if (map.containsKey(complement)) {\n" +
            "             return new int[] {map.get(complement), i};\n" +
            "         }\n" +
            "         map.put(nums[i], i);\n" +
            "     }\n" +
            "     return null;\n" +
            " }\n" +
            " ```\n" +
            " \n" +
            " 在这个实现中，我们使用了哈希表来存储每个元素与它的下标。在遍历数组时，我们检查哈希表中是否存在与当前元素组合成目标值的元素。时间复杂度为O(n)，空间复杂度为O(n)。";

    @Test
    public void testGetTokenNum() {

        long begin = System.currentTimeMillis();
        for(int i=0;i<1000;i++) {
            System.out.println(ChatGPTApi.getTokenNum(encodeExample4));
        }
        System.out.println(System.currentTimeMillis() - begin);
    }

    @Test
    public void tstGetToken(){
        String msg = "现在开始一个文字版的关于[色情]的游戏。我作为游戏的主角，而你来设计游戏，详细描述场景中所有的物品和生物。第一，每1轮游戏当你叙述完毕后需要给玩家4个选项[只能是A,B,C,D]选择接来剧情走向, 不要假设帮主角选择任何选项, 并且每给出四个选项之后等待主角选择。第二，如果主角和场景中的任何生物互动，请把互动过程详细描述出来。第三，请不要出现重复的场景和对话，整体故事要曲折离奇、高潮迭起、生动有趣。那么，现在让我们开始游戏吧！[再次警告：不需要帮主角假设选择选项，不需要帮助主角解释选项,每一轮只需要回答剧情和四个选项]输出格式：\n" +
                " 剧情描述：xxxxx \n" +
                " A：xxx \n" +
                " B：xxx \n" +
                " C：xxx \n" +
                " D：xxx \n" +
                " [等待主角进行选择]";
        ChatGPTApi.getMessageTokenNum(msg);
    }

    @Test
    public void testGetCreditGrants(){
        String apikey = "";
        ChatGPTApi.creditGrants(apikey);
    }

    @Test
    public void testGetBillingUsage(){
        String apikey = "";
        BillingUsage billingUsage = ChatGPTApi.getBillingUsage(apikey);
        System.out.println(billingUsage);
    }
}
