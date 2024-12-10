package sustech.java2finalproject.feature.question;

import sustech.java2finalproject.feature.question.dto.MistakeResponse;
import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;

import java.util.List;


public interface QuestionService {

    List<TopNResponse> getTopNQuestionsByTag(int topN);
    Long getFrequencyOfTopic(String tag);
    List<TopEngagementResponse> getTopEngagementTag(int topN);
    List<TopEngagementResponse> getTopEngagementTagByTopUser(int topN, int reputation);

    List<MistakeResponse> ErrorAnalysis(Integer topN);
}
