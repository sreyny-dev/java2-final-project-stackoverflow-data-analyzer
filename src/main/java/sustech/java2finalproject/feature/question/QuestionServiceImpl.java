package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final OwnerRepository ownerRepository;

    @Override
    public List<TopNResponse> getTopNQuestionsByTag(int topN) {
        // Fetch the top N results from the repository (with limit applied in the query)
        List<Object[]> topNResults = tagRepository.findTopNTags(topN);

        // Create a list to store the TopNResponse
        List<TopNResponse> topNResponses = new ArrayList<>();

        topNResults.stream()
                .skip(1)
                .limit(topN)
                .forEach(result -> {
                    Tag tag = (Tag) result[0];
                    Long frequency = (Long) result[1];  // Frequency of questions

                    // Map to TopNResponse
                    topNResponses.add(new TopNResponse(tag.getName(), frequency.intValue()));
                });

        return topNResponses;
    }

    @Override
    public List<TopEngagementResponse> getTopEngagementTag(int topN) {

        List<Question> allQuestions = questionRepository.findAll();
        Map<Tag, Long> tagEngagementMap = allQuestions.stream()
                .flatMap(question -> question.getTags().stream())
                .collect(Collectors.toMap(
                        tag -> tag,
                        tag -> allQuestions.stream()
                                .filter(q->q.getTags().contains(tag))
                                .mapToLong(q->q.getScore() + q.getViewCount())
                                .sum(),
                        Long::sum
                ));

        return tagEngagementMap.entrySet().stream()
                .map(entry-> new TopEngagementResponse(entry.getKey().getName(), entry.getValue()))
                .sorted((entry1, entry2)-> entry2.totalEngagement().compareTo(entry1.totalEngagement()))
                .skip(1)
                .limit(topN)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopEngagementResponse> getTopEngagementTagByTopUser(int topN, int reputation) {

        List<Owner> topOwners = ownerRepository.findTopOwnersByReputation(reputation);

        // Get all questions from high-reputation users
        List<Question> highReputationQuestions = topOwners.stream()
                .flatMap(owner -> owner.getQuestions().stream())
                .collect(Collectors.toList());

        // Create a map of tags and their total engagement (score + viewCount)
        Map<Tag, Long> tagEngagementMap = highReputationQuestions.stream()
                .flatMap(question -> question.getTags().stream())
                .collect(Collectors.toMap(
                        tag -> tag,
                        tag -> highReputationQuestions.stream()
                                .filter(q -> q.getTags().contains(tag))
                                .mapToLong(q -> q.getScore() + q.getViewCount())
                                .sum(),
                        Long::sum
                ));

        // Return the top N tags sorted by total engagement, skipping 1 to adjust for the 1-based index of 'topN'
        return tagEngagementMap.entrySet().stream()
                .map(entry -> new TopEngagementResponse(entry.getKey().getName(), entry.getValue()))
                .sorted((entry1, entry2) -> entry2.totalEngagement().compareTo(entry1.totalEngagement()))
                .skip(1)
                .limit(topN)
                .collect(Collectors.toList());
    }

}
