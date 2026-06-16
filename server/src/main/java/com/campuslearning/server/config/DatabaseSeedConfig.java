package com.campuslearning.server.config;

import com.campuslearning.server.model.User;
import com.campuslearning.server.model.UserRole;
import com.campuslearning.server.model.QuestionBankItem;
import com.campuslearning.server.repository.QuestionBankItemRepository;
import com.campuslearning.server.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseSeedConfig {

    @Bean
    public CommandLineRunner seedBaseUsers(UserRepository userRepository,
                                           PasswordEncoder passwordEncoder,
                                           QuestionBankItemRepository questionBankItemRepository) {
        return (args) -> {
            ensureUser(userRepository, passwordEncoder, "teacher01", "张老师", "123456", UserRole.TEACHER);
            ensureUser(userRepository, passwordEncoder, "student01", "李同学", "123456", UserRole.STUDENT);
            ensureUser(userRepository, passwordEncoder, "test", "测试用户", "1234", UserRole.STUDENT);
            seedQuestionBank(questionBankItemRepository);
        };
    }

    private void ensureUser(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            String username,
                            String realName,
                            String rawPassword,
                            UserRole role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setRealName(realName);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }

    private void seedQuestionBank(QuestionBankItemRepository repository) {
        if (repository.count() > 0) {
            return;
        }

        addQuestion(repository, "cn_paper_1", "小学语文基础卷一", "语文", "single", "下列词语中，哪一个表示颜色？",
                "[{\"key\":\"A\",\"label\":\"开心\"},{\"key\":\"B\",\"label\":\"红色\"},{\"key\":\"C\",\"label\":\"奔跑\"},{\"key\":\"D\",\"label\":\"安静\"}]",
                "B", 10, 1);
        addQuestion(repository, "cn_paper_1", "小学语文基础卷一", "语文", "single", "“春眠不觉晓”这句诗出自哪首诗？",
                "[{\"key\":\"A\",\"label\":\"《春晓》\"},{\"key\":\"B\",\"label\":\"《静夜思》\"},{\"key\":\"C\",\"label\":\"《登鹳雀楼》\"},{\"key\":\"D\",\"label\":\"《咏柳》\"}]",
                "A", 10, 2);
        addQuestion(repository, "cn_paper_1", "小学语文基础卷一", "语文", "judge", "“大海”属于自然景物。", "[{\"key\":\"A\",\"label\":\"正确\"},{\"key\":\"B\",\"label\":\"错误\"}]", "A", 10, 3);
        addQuestion(repository, "cn_paper_1", "小学语文基础卷一", "语文", "single", "下面哪个字的部首是“氵”？",
                "[{\"key\":\"A\",\"label\":\"河\"},{\"key\":\"B\",\"label\":\"树\"},{\"key\":\"C\",\"label\":\"花\"},{\"key\":\"D\",\"label\":\"鸟\"}]",
                "A", 10, 4);
        addQuestion(repository, "cn_paper_1", "小学语文基础卷一", "语文", "multiple", "下面哪些词语表示动作？",
                "[{\"key\":\"A\",\"label\":\"跳\"},{\"key\":\"B\",\"label\":\"笑\"},{\"key\":\"C\",\"label\":\"蓝\"},{\"key\":\"D\",\"label\":\"跑\"}]",
                "A,B,D", 20, 5);

        addQuestion(repository, "cn_paper_2", "小学语文阅读卷二", "语文", "single", "“窗前明月光”中的“明月”指什么？",
                "[{\"key\":\"A\",\"label\":\"太阳\"},{\"key\":\"B\",\"label\":\"月亮\"},{\"key\":\"C\",\"label\":\"星星\"},{\"key\":\"D\",\"label\":\"灯光\"}]",
                "B", 10, 1);
        addQuestion(repository, "cn_paper_2", "小学语文阅读卷二", "语文", "single", "下列哪个词语最适合形容小草？",
                "[{\"key\":\"A\",\"label\":\"翠绿\"},{\"key\":\"B\",\"label\":\"香甜\"},{\"key\":\"C\",\"label\":\"高大\"},{\"key\":\"D\",\"label\":\"寒冷\"}]",
                "A", 10, 2);
        addQuestion(repository, "cn_paper_2", "小学语文阅读卷二", "语文", "judge", "“书”是左右结构的字。", "[{\"key\":\"A\",\"label\":\"正确\"},{\"key\":\"B\",\"label\":\"错误\"}]", "B", 10, 3);
        addQuestion(repository, "cn_paper_2", "小学语文阅读卷二", "语文", "multiple", "下面哪些是标点符号？",
                "[{\"key\":\"A\",\"label\":\"。\"},{\"key\":\"B\",\"label\":\"，\"},{\"key\":\"C\",\"label\":\"山\"},{\"key\":\"D\",\"label\":\"？\"}]",
                "A,B,D", 20, 4);
        addQuestion(repository, "cn_paper_2", "小学语文阅读卷二", "语文", "single", "“同学”一词通常指什么？",
                "[{\"key\":\"A\",\"label\":\"一起学习的人\"},{\"key\":\"B\",\"label\":\"老师\"},{\"key\":\"C\",\"label\":\"家长\"},{\"key\":\"D\",\"label\":\"医生\"}]",
                "A", 10, 5);

        addQuestion(repository, "math_paper_1", "小学数学计算卷一", "数学", "single", "7 + 5 = ?",
                "[{\"key\":\"A\",\"label\":\"10\"},{\"key\":\"B\",\"label\":\"11\"},{\"key\":\"C\",\"label\":\"12\"},{\"key\":\"D\",\"label\":\"13\"}]",
                "C", 10, 1);
        addQuestion(repository, "math_paper_1", "小学数学计算卷一", "数学", "single", "9 × 3 = ?",
                "[{\"key\":\"A\",\"label\":\"27\"},{\"key\":\"B\",\"label\":\"18\"},{\"key\":\"C\",\"label\":\"21\"},{\"key\":\"D\",\"label\":\"24\"}]",
                "A", 10, 2);
        addQuestion(repository, "math_paper_1", "小学数学计算卷一", "数学", "judge", "20 比 18 大。", "[{\"key\":\"A\",\"label\":\"正确\"},{\"key\":\"B\",\"label\":\"错误\"}]", "A", 10, 3);
        addQuestion(repository, "math_paper_1", "小学数学计算卷一", "数学", "multiple", "下面哪些数是偶数？",
                "[{\"key\":\"A\",\"label\":\"2\"},{\"key\":\"B\",\"label\":\"5\"},{\"key\":\"C\",\"label\":\"8\"},{\"key\":\"D\",\"label\":\"11\"}]",
                "A,C", 20, 4);
        addQuestion(repository, "math_paper_1", "小学数学计算卷一", "数学", "single", "15 - 6 = ?",
                "[{\"key\":\"A\",\"label\":\"7\"},{\"key\":\"B\",\"label\":\"8\"},{\"key\":\"C\",\"label\":\"9\"},{\"key\":\"D\",\"label\":\"10\"}]",
                "C", 10, 5);

        addQuestion(repository, "math_paper_2", "小学数学应用卷二", "数学", "single", "小明有 4 个苹果，又买了 3 个，一共有几个？",
                "[{\"key\":\"A\",\"label\":\"5\"},{\"key\":\"B\",\"label\":\"6\"},{\"key\":\"C\",\"label\":\"7\"},{\"key\":\"D\",\"label\":\"8\"}]",
                "C", 10, 1);
        addQuestion(repository, "math_paper_2", "小学数学应用卷二", "数学", "single", "一个星期有几天？",
                "[{\"key\":\"A\",\"label\":\"5\"},{\"key\":\"B\",\"label\":\"6\"},{\"key\":\"C\",\"label\":\"7\"},{\"key\":\"D\",\"label\":\"8\"}]",
                "C", 10, 2);
        addQuestion(repository, "math_paper_2", "小学数学应用卷二", "数学", "judge", "1 米 = 100 厘米。", "[{\"key\":\"A\",\"label\":\"正确\"},{\"key\":\"B\",\"label\":\"错误\"}]", "A", 10, 3);
        addQuestion(repository, "math_paper_2", "小学数学应用卷二", "数学", "multiple", "下面哪些图形有角？",
                "[{\"key\":\"A\",\"label\":\"三角形\"},{\"key\":\"B\",\"label\":\"正方形\"},{\"key\":\"C\",\"label\":\"圆形\"},{\"key\":\"D\",\"label\":\"长方形\"}]",
                "A,B,D", 20, 4);
        addQuestion(repository, "math_paper_2", "小学数学应用卷二", "数学", "single", "钟面上短针表示什么？",
                "[{\"key\":\"A\",\"label\":\"分\"},{\"key\":\"B\",\"label\":\"时\"},{\"key\":\"C\",\"label\":\"秒\"},{\"key\":\"D\",\"label\":\"日期\"}]",
                "B", 10, 5);

        addQuestion(repository, "en_paper_1", "小学英语词汇卷一", "英语", "single", "“apple” 的中文意思是？",
                "[{\"key\":\"A\",\"label\":\"苹果\"},{\"key\":\"B\",\"label\":\"香蕉\"},{\"key\":\"C\",\"label\":\"橘子\"},{\"key\":\"D\",\"label\":\"葡萄\"}]",
                "A", 10, 1);
        addQuestion(repository, "en_paper_1", "小学英语词汇卷一", "英语", "single", "下列哪个是颜色单词？",
                "[{\"key\":\"A\",\"label\":\"blue\"},{\"key\":\"B\",\"label\":\"run\"},{\"key\":\"C\",\"label\":\"cat\"},{\"key\":\"D\",\"label\":\"milk\"}]",
                "A", 10, 2);
        addQuestion(repository, "en_paper_1", "小学英语词汇卷一", "英语", "judge", "“dog” 表示“狗”。", "[{\"key\":\"A\",\"label\":\"正确\"},{\"key\":\"B\",\"label\":\"错误\"}]", "A", 10, 3);
        addQuestion(repository, "en_paper_1", "小学英语词汇卷一", "英语", "multiple", "下面哪些是学校用品？",
                "[{\"key\":\"A\",\"label\":\"book\"},{\"key\":\"B\",\"label\":\"pen\"},{\"key\":\"C\",\"label\":\"banana\"},{\"key\":\"D\",\"label\":\"ruler\"}]",
                "A,B,D", 20, 4);
        addQuestion(repository, "en_paper_1", "小学英语词汇卷一", "英语", "single", "“Good morning” 通常在什么时候说？",
                "[{\"key\":\"A\",\"label\":\"早上\"},{\"key\":\"B\",\"label\":\"中午\"},{\"key\":\"C\",\"label\":\"晚上\"},{\"key\":\"D\",\"label\":\"睡觉前\"}]",
                "A", 10, 5);
    }

    private void addQuestion(QuestionBankItemRepository repository,
                             String presetKey,
                             String presetName,
                             String subject,
                             String type,
                             String content,
                             String optionsJson,
                             String correctAnswer,
                             Integer score,
                             Integer sortOrder) {
        QuestionBankItem item = new QuestionBankItem();
        item.setPresetKey(presetKey);
        item.setPresetName(presetName);
        item.setSubject(subject);
        item.setType(type);
        item.setContent(content);
        item.setOptionsJson(optionsJson);
        item.setCorrectAnswer(correctAnswer);
        item.setScore(score);
        item.setSortOrder(sortOrder);
        repository.save(item);
    }
}
