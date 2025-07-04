package com.jyx.healthsys.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jyx.healthsys.entity.Question;
import com.jyx.healthsys.mapper.QuestionMapper;
import com.jyx.healthsys.service.IQuestionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {
} 