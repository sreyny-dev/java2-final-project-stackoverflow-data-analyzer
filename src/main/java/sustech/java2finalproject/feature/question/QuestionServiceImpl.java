package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.dto.TopNResponse;
import sustech.java2finalproject.feature.question.repository.TagRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final TagRepository tagRepository;

    @Override
    public List<TopNResponse> getTopNQuestionsByTag(int topN) {
        // Fetch the top N results from the repository (with limit applied in the query)
        List<Object[]> topNResults = tagRepository.findTopNTags(topN);

        // Create a list to store the TopNResponse
        List<TopNResponse> topNResponses = new ArrayList<>();

        // Iterate through the results and map them to TopNResponse
        for (int i = 0; i < Math.min(topN, topNResults.size()); i++) {
            Object[] result = topNResults.get(i);  // Get the i-th result
            Tag tag = (Tag) result[0];  // Tag
            Long frequency = (Long) result[1];  // Frequency of questions

            // Map to TopNResponse
            topNResponses.add(new TopNResponse(tag.getName(), frequency.intValue()));
        }

        return topNResponses;
    }
}
