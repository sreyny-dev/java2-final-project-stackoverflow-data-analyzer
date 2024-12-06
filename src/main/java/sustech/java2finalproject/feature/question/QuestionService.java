package sustech.java2finalproject.feature.question;

import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;

import java.util.List;


public interface QuestionService {

    List<TopNResponse> getTopNQuestionsByTag(int topN);
    List<TopEngagementResponse> getTopEngagementTag(int topN);
    List<TopEngagementResponse> getTopEngagementTagByTopUser(int topN, int reputation);
}
