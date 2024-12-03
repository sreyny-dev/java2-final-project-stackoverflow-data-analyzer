package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sustech.java2finalproject.feature.question.dto.TopNResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/questions")
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/top-tags/{topN}")
    public List<TopNResponse> getTopTags(@PathVariable Integer topN){
        return questionService.getTopNQuestionsByTag(topN);
    }

}
