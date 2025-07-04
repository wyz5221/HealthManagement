package com.jyx.healthsys.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jyx.Data_unification.Unification;
import com.jyx.healthsys.entity.Paper;
import com.jyx.healthsys.entity.Question;
import com.jyx.healthsys.service.IPaperService;
import com.jyx.healthsys.service.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/paper")
public class PaperController {
    @Autowired
    private IPaperService paperService;
    @Autowired
    private IQuestionService questionService;

    @GetMapping("/list")
    public Unification<List<Paper>> list() {
        List<Paper> list = paperService.list(new QueryWrapper<Paper>().orderByDesc("create_time"));
        return Unification.success(list, "查询成功");
    }

    @PostMapping("/add")
    public Unification<?> add(@RequestBody Paper paper) {
        paper.setCreateTime(new Date());
        paper.setStatus(1);
        boolean result = paperService.save(paper);
        if (result) {
            return Unification.success(paper.getId(), "新增成功");
        }
        return Unification.fail("新增失败");
    }

    @PostMapping("/update")
    public Unification<?> update(@RequestBody Paper paper) {
        boolean result = paperService.updateById(paper);
        return result ? Unification.success("修改成功") : Unification.fail("修改失败");
    }

    @PostMapping("/delete/{id}")
    public Unification<?> delete(@PathVariable Long id) {
        boolean result = paperService.removeById(id);
        return result ? Unification.success("删除成功") : Unification.fail("删除失败");
    }

    @PostMapping("/status/{id}")
    public Unification<?> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        Paper paper = paperService.getById(id);
        if (paper != null) {
            paper.setStatus(status);
            boolean result = paperService.updateById(paper);
            return result ? Unification.success("状态更新成功") : Unification.fail("状态更新失败");
        }
        return Unification.fail("未找到试卷");
    }

    @PostMapping("/addQuestions/{paperId}")
    public Unification<?> addQuestions(@PathVariable Long paperId, @RequestBody List<Question> questions) {
        for (int i = 0; i < questions.size(); i++) {
            questions.get(i).setPaperId(paperId);
            questions.get(i).setSort(i + 1);
        }
        boolean result = questionService.saveBatch(questions);
        return result ? Unification.success("题目添加成功") : Unification.fail("题目添加失败");
    }

    @GetMapping("/questions/{paperId}")
    public Unification<List<Question>> getQuestions(@PathVariable Long paperId) {
        List<Question> list = questionService.list(new QueryWrapper<Question>().eq("paper_id", paperId).orderByAsc("sort"));
        return Unification.success(list, "查询成功");
    }
} 