package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.dto.MistakeResponse;
import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                    topNResponses.add(new TopNResponse(tag.getName(), frequency));
                });

        return topNResponses;
    }

    @Override
    public Long getFrequencyOfTopic(String tag) {
        List<Question> questions = questionRepository.findAll();

        // Use Streams to filter questions by tag and count the frequency
        return questions.stream()
                .filter(question -> question.getTags()
                        .stream()
                        .anyMatch(tagName -> tagName.getName().equalsIgnoreCase(tag))) // Check if the question contains the tag
                .count(); // Count the number of questions that match
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

    // List of common Java exceptions
    private static final String[] COMMON_EXCEPTIONS = {
            // Exceptions
            "ClassNotFoundException",
            "CloneNotSupportedException",
            "IllegalAccessException",
            "InstantiationException",
            "InterruptedException",
            "NoSuchFieldException",
            "NoSuchMethodException",
            "ArithmeticException",
            "ArrayStoreException",
            "ClassCastException",
            "IllegalArgumentException",
            "IllegalMonitorStateException",
            "IllegalStateException",
            "IndexOutOfBoundsException",
            "NegativeArraySizeException",
            "NullPointerException",
            "SecurityException",
            "UnsupportedOperationException",
            "ArrayIndexOutOfBoundsException",
            "StringIndexOutOfBoundsException",
            "NumberFormatException",

            // Errors
            "AssertionError",
            "ClassCircularityError",
            "ClassFormatError",
            "ExceptionInInitializerError",
            "IncompatibleClassChangeError",
            "NoClassDefFoundError",
            "UnsatisfiedLinkError",
            "VerifyError",
            "InternalError",
            "OutOfMemoryError",
            "StackOverflowError",
            "UnknownError",
            "AbstractMethodError",
            "IllegalAccessError",
            "InstantiationError",
            "NoSuchFieldError",
            "NoSuchMethodError"
    };


    private static final String EXCEPTION_PATTERN =
            String.join("|", COMMON_EXCEPTIONS); // "NullPointerException|ArrayIndexOutOfBoundsException|..."

    @Override
    public List<MistakeResponse> ErrorAnalysis(Integer topN) {
        List<Question> questions = questionRepository.findAll();
        Map<String, Integer> exceptionCountMap = new HashMap<>();

        // Compile the regex pattern with case-insensitivity
        Pattern pattern = Pattern.compile(EXCEPTION_PATTERN, Pattern.CASE_INSENSITIVE);

        // Analyze questions and their answers for exception mentions
        for (Question question : questions) {
            // Analyze the question title for exception mentions
            matchExceptionInText(question.getTitle(), pattern, exceptionCountMap);

            // Analyze the question body for exception mentions
            matchExceptionInText(question.getBody(), pattern, exceptionCountMap);
        }

        // Convert the exception count map to a list of MistakeResponse objects
        List<MistakeResponse> errorResponses = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : exceptionCountMap.entrySet()) {
            errorResponses.add(new MistakeResponse(entry.getKey(), entry.getValue()));
        }

        // Sort by frequency in descending order and return the top N
        errorResponses.sort((e1, e2) -> e2.frequency().compareTo(e1.frequency()));
        return errorResponses.size() > topN ? errorResponses.subList(0, topN) : errorResponses;
    }

    private void matchExceptionInText(String text, Pattern pattern, Map<String, Integer> exceptionCountMap) {
        if (text != null) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String matchedException = matcher.group().toLowerCase(); // Normalize to lowercase
                exceptionCountMap.put(matchedException, exceptionCountMap.getOrDefault(matchedException, 0) + 1);
            }
        }
    }


}
