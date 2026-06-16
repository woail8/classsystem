package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import com.campuslearning.server.dto.ExamAnswerPayload;
import com.campuslearning.server.model.Exam;
import com.campuslearning.server.model.ExamQuestion;
import com.campuslearning.server.model.ExamRecord;
import com.campuslearning.server.model.QuestionBankItem;
import com.campuslearning.server.model.StudentAnswer;
import com.campuslearning.server.model.User;
import com.campuslearning.server.repository.ExamQuestionRepository;
import com.campuslearning.server.repository.ExamRecordRepository;
import com.campuslearning.server.repository.ExamRepository;
import com.campuslearning.server.repository.QuestionBankItemRepository;
import com.campuslearning.server.repository.StudentAnswerRepository;
import com.campuslearning.server.security.CurrentUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CourseService courseService;
    private final UserService userService;
    private final QuestionBankItemRepository questionBankItemRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRecordRepository examRecordRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ObjectMapper objectMapper;

    public ExamService(CourseService courseService,
                       UserService userService,
                       QuestionBankItemRepository questionBankItemRepository,
                       ExamRepository examRepository,
                       ExamQuestionRepository examQuestionRepository,
                       ExamRecordRepository examRecordRepository,
                       StudentAnswerRepository studentAnswerRepository,
                       ObjectMapper objectMapper) {
        this.courseService = courseService;
        this.userService = userService;
        this.questionBankItemRepository = questionBankItemRepository;
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examRecordRepository = examRecordRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> listPresets() {
        Map<String, Map<String, Object>> presetMap = new LinkedHashMap<>();
        for (QuestionBankItem item : questionBankItemRepository.findAllByOrderByPresetKeyAscSortOrderAsc()) {
            Map<String, Object> preset = presetMap.computeIfAbsent(item.getPresetKey(), key -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("presetKey", item.getPresetKey());
                data.put("presetName", item.getPresetName());
                data.put("subject", item.getSubject());
                data.put("questionCount", 0);
                data.put("totalScore", 0);
                return data;
            });
            preset.put("questionCount", ((Integer) preset.get("questionCount")) + 1);
            preset.put("totalScore", ((Integer) preset.get("totalScore")) + item.getScore());
        }
        return new ArrayList<>(presetMap.values());
    }

    @Transactional
    public Map<String, Object> createExam(Long classId,
                                          String title,
                                          String presetKey,
                                          Integer duration,
                                          String startTimeText,
                                          String endTimeText) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureClassAdmin(classId, currentUser.getUserId());

        List<QuestionBankItem> bankItems = questionBankItemRepository.findByPresetKeyOrderBySortOrderAsc(presetKey);
        if (bankItems.isEmpty()) {
            throw new BusinessException(400, "未找到对应的预置题卷");
        }

        LocalDateTime startTime = LocalDateTime.parse(startTimeText, FORMATTER);
        LocalDateTime endTime = LocalDateTime.parse(endTimeText, FORMATTER);
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }

        Exam exam = new Exam();
        exam.setClassId(classId);
        exam.setTeacherId(currentUser.getUserId());
        exam.setTitle(title);
        exam.setPresetKey(presetKey);
        exam.setSubject(bankItems.get(0).getSubject());
        exam.setDuration(duration);
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);
        exam.setPublishedAt(LocalDateTime.now());
        exam = examRepository.save(exam);

        for (QuestionBankItem bankItem : bankItems) {
            ExamQuestion question = new ExamQuestion();
            question.setExamId(exam.getId());
            question.setType(bankItem.getType());
            question.setContent(bankItem.getContent());
            question.setOptionsJson(bankItem.getOptionsJson());
            question.setCorrectAnswer(bankItem.getCorrectAnswer());
            question.setScore(bankItem.getScore());
            question.setSortOrder(bankItem.getSortOrder());
            examQuestionRepository.save(question);
        }

        return toExamItem(exam);
    }

    public List<Map<String, Object>> listExamsByClass(Long classId) {
        CurrentUser currentUser = userService.getCurrentUser();
        courseService.ensureMember(classId, currentUser.getUserId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam exam : examRepository.findByClassIdOrderByPublishedAtDesc(classId)) {
            result.add(toExamItem(exam));
        }
        return result;
    }

    @Transactional
    public Map<String, Object> startExam(Long examId) {
        CurrentUser currentUser = userService.getCurrentUser();
        Exam exam = getExam(examId);
        courseService.ensureMember(exam.getClassId(), currentUser.getUserId());

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new BusinessException(400, "考试尚未开始");
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException(400, "考试已结束");
        }

        ExamRecord record = examRecordRepository.findByExamIdAndStudentId(examId, currentUser.getUserId()).orElse(null);
        if (record == null) {
            record = new ExamRecord();
            record.setExamId(examId);
            record.setStudentId(currentUser.getUserId());
            record.setStatus("ongoing");
            record.setStartTime(now);
            record.setScreenSwitchCount(0);
            record = examRecordRepository.save(record);
        } else if ("submitted".equalsIgnoreCase(record.getStatus())) {
            throw new BusinessException(400, "试卷已提交，不能重复作答");
        }

        LocalDateTime allowedEnd = record.getStartTime().plusMinutes(exam.getDuration());
        if (allowedEnd.isAfter(exam.getEndTime())) {
            allowedEnd = exam.getEndTime();
        }
        long remainSeconds = Math.max(0, Duration.between(now, allowedEnd).getSeconds());

        Map<String, Object> examData = new LinkedHashMap<>();
        examData.put("id", exam.getId());
        examData.put("title", exam.getTitle());
        examData.put("questions", examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId).stream()
                .map(this::toQuestionMap)
                .collect(Collectors.toList()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("exam", examData);
        result.put("remainSeconds", remainSeconds);
        result.put("recordId", record.getId());
        return result;
    }

    @Transactional
    public Map<String, Object> submitExam(Long examId, List<ExamAnswerPayload> answerPayloads) {
        CurrentUser currentUser = userService.getCurrentUser();
        Exam exam = getExam(examId);
        courseService.ensureMember(exam.getClassId(), currentUser.getUserId());

        ExamRecord record = examRecordRepository.findByExamIdAndStudentId(examId, currentUser.getUserId())
                .orElseThrow(() -> new BusinessException(400, "请先开始考试"));
        if ("submitted".equalsIgnoreCase(record.getStatus())) {
            throw new BusinessException(400, "试卷已提交");
        }

        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId);
        Map<Long, String> answerMap = new HashMap<>();
        for (ExamAnswerPayload payload : answerPayloads) {
            answerMap.put(payload.getQuestionId(), payload.getAnswer());
        }

        List<StudentAnswer> oldAnswers = studentAnswerRepository.findByRecordIdOrderByQuestionIdAsc(record.getId());
        if (!oldAnswers.isEmpty()) {
            studentAnswerRepository.deleteByRecordId(record.getId());
        }

        double totalScore = 0D;
        for (ExamQuestion question : questions) {
            String answerText = normalizeAnswer(answerMap.get(question.getId()));
            boolean correct = normalizeAnswer(question.getCorrectAnswer()).equals(answerText);
            double questionScore = correct ? question.getScore() : 0D;

            StudentAnswer answer = new StudentAnswer();
            answer.setRecordId(record.getId());
            answer.setQuestionId(question.getId());
            answer.setAnswerText(answerText);
            answer.setCorrect(correct);
            answer.setScore(questionScore);
            studentAnswerRepository.save(answer);
            totalScore += questionScore;
        }

        record.setStatus("submitted");
        record.setSubmittedAt(LocalDateTime.now());
        record.setScore(totalScore);
        examRecordRepository.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("score", totalScore);
        result.put("submittedAt", record.getSubmittedAt().format(FORMATTER));
        return result;
    }

    @Transactional
    public Map<String, Object> reportCheat(Long examId, String action) {
        CurrentUser currentUser = userService.getCurrentUser();
        ExamRecord record = examRecordRepository.findByExamIdAndStudentId(examId, currentUser.getUserId())
                .orElseThrow(() -> new BusinessException(400, "考试尚未开始"));
        record.setScreenSwitchCount((record.getScreenSwitchCount() == null ? 0 : record.getScreenSwitchCount()) + 1);
        examRecordRepository.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("action", action);
        result.put("screenSwitchCount", record.getScreenSwitchCount());
        return result;
    }

    public List<Map<String, Object>> listExamRecords(Long examId) {
        CurrentUser currentUser = userService.getCurrentUser();
        Exam exam = getExam(examId);
        if (!exam.getTeacherId().equals(currentUser.getUserId())) {
            throw new BusinessException(403, "仅管理员可查看成员作答详情");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        List<ExamQuestion> questions = examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId);
        for (ExamRecord record : examRecordRepository.findByExamIdOrderBySubmittedAtDesc(examId)) {
            User student = userService.getById(record.getStudentId());
            Map<Long, StudentAnswer> answerMap = studentAnswerRepository.findByRecordIdOrderByQuestionIdAsc(record.getId())
                    .stream()
                    .collect(Collectors.toMap(StudentAnswer::getQuestionId, item -> item));

            List<Map<String, Object>> answerDetails = new ArrayList<>();
            for (ExamQuestion question : questions) {
                StudentAnswer answer = answerMap.get(question.getId());
                Map<String, Object> answerDetail = new LinkedHashMap<>();
                answerDetail.put("questionId", question.getId());
                answerDetail.put("content", question.getContent());
                answerDetail.put("type", question.getType());
                answerDetail.put("correctAnswer", question.getCorrectAnswer());
                answerDetail.put("studentAnswer", answer == null ? "" : answer.getAnswerText());
                answerDetail.put("correct", answer != null && Boolean.TRUE.equals(answer.getCorrect()));
                answerDetail.put("score", answer == null ? 0 : answer.getScore());
                answerDetails.add(answerDetail);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("recordId", record.getId());
            item.put("studentId", student.getId());
            item.put("studentName", student.getRealName());
            item.put("username", student.getUsername());
            item.put("score", record.getScore());
            item.put("status", record.getStatus());
            item.put("screenSwitchCount", record.getScreenSwitchCount());
            item.put("submittedAt", record.getSubmittedAt() == null ? null : record.getSubmittedAt().format(FORMATTER));
            item.put("answers", answerDetails);
            result.add(item);
        }
        result.sort(Comparator.comparing(item -> String.valueOf(item.get("studentName"))));
        return result;
    }

    public Exam getExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new BusinessException(404, "考试不存在"));
    }

    private Map<String, Object> toExamItem(Exam exam) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", exam.getId());
        result.put("title", exam.getTitle());
        result.put("subject", exam.getSubject());
        result.put("startTime", exam.getStartTime().format(FORMATTER));
        result.put("endTime", exam.getEndTime().format(FORMATTER));
        result.put("duration", exam.getDuration());

        LocalDateTime now = LocalDateTime.now();
        String status = now.isBefore(exam.getStartTime()) ? "未开始" : now.isAfter(exam.getEndTime()) ? "已结束" : "进行中";
        result.put("status", status);
        return result;
    }

    private Map<String, Object> toQuestionMap(ExamQuestion question) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("type", question.getType());
        result.put("content", question.getContent());
        result.put("score", question.getScore());
        result.put("options", parseOptions(question.getOptionsJson()));
        return result;
    }

    private List<Map<String, String>> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<Map<String, String>>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private String normalizeAnswer(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String trimmed = value.trim().toUpperCase();
        if (!trimmed.contains(",")) {
            return trimmed;
        }
        List<String> parts = new ArrayList<>();
        for (String part : trimmed.split(",")) {
            if (!part.trim().isEmpty()) {
                parts.add(part.trim());
            }
        }
        Collections.sort(parts);
        return String.join(",", parts);
    }
}
