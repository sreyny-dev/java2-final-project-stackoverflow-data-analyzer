package sustech.java2finalproject.feature.question;

import sustech.java2finalproject.feature.question.dto.TopNResponse;

import java.util.List;


public interface QuestionService {

    List<TopNResponse> getTopNQuestionsByTag(int topN);

}
