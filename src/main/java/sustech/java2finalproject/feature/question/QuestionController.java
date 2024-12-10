package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sustech.java2finalproject.feature.question.dto.MistakeResponse;
import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/questions")
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/top-tags/{topN}")
    @CrossOrigin
    public List<TopNResponse> getTopTags(@PathVariable Integer topN){
        return questionService.getTopNQuestionsByTag(topN);
    }

    @GetMapping("/frequency/{tagName}")
    @CrossOrigin
    public Long frequencyOfQuestion(@PathVariable String tagName){
        return questionService.getFrequencyOfTopic(tagName);
    }

    @GetMapping("/top-engagement-tags/{topN}")
    @CrossOrigin
    public List<TopEngagementResponse> getTopEngagementTag(@PathVariable Integer topN){
        return questionService.getTopEngagementTag(topN);
    }
    @GetMapping("/top-engagement-tags-top-users/{topN}/{reputation}")
    @CrossOrigin
    public List<TopEngagementResponse> getTopEngagementTag(@PathVariable Integer topN,
                                                           @PathVariable Integer reputation){
        return questionService.getTopEngagementTagByTopUser(topN, reputation);
    }
    @GetMapping("/common-error/{topN}")
    @CrossOrigin
    public List<MistakeResponse> commonError(@PathVariable Integer topN){
        return questionService.ErrorAnalysis(topN);
    }


}
