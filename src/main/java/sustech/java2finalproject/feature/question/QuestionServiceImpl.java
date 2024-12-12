package sustech.java2finalproject.feature.question;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sustech.java2finalproject.domain.Answer;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.dto.AnswerResponse;
import sustech.java2finalproject.feature.question.dto.MistakeResponse;
import sustech.java2finalproject.feature.question.dto.TopEngagementResponse;
import sustech.java2finalproject.feature.question.dto.TopNResponse;
import sustech.java2finalproject.feature.question.repository.AnswerRepository;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final OwnerRepository ownerRepository;
    private final AnswerRepository answerRepository;

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
                String matchedException = matcher.group(); // Use the matched exception as it is
                exceptionCountMap.put(matchedException, exceptionCountMap.getOrDefault(matchedException, 0) + 1);
            }
        }
    }

    @Override
    public MistakeResponse getExceptionFrequency(String exceptionName) {
        // Normalize the exception name (so we can compare case-insensitively)
        String normalizedExceptionName = exceptionName.trim();
        Map<String, Integer> exceptionCountMap = analyzeErrors();

        // Check if the exception exists in the map
        Integer frequency = exceptionCountMap.getOrDefault(normalizedExceptionName, 0);

        return new MistakeResponse(normalizedExceptionName, frequency);
    }

    @Override
    public List<AnswerResponse> answerQuality(Long questionStackId) {

        List<Answer> answers = answerRepository.findByQuestionStackId(questionStackId);

        // Define weights for each criterion
        double isAcceptedWeight = 0.4;
        double elapsedTimeWeight = 0.2;
        double reputationWeight = 0.2;
        double scoreWeight = 0.2;

        // Map Answer to AnswerResponse and return
        return answers.stream()
                .map(answer -> new AnswerResponse(
                        answer.getAnswerId(),
                        Double.parseDouble(String.format("%.2f", calculateQualityScore(answer, isAcceptedWeight, elapsedTimeWeight, reputationWeight, scoreWeight))),
                        answer.getAccountId()
                ))
                .sorted((a1, a2) -> Double.compare(a2.qualityScore(), a1.qualityScore()))  // Sort by qualityScore descending
                .collect(Collectors.toList());
    }

    @Override
    public List<AnswerResponse> overallAnswerQuality(Integer topN) {

        List<Answer> answers = answerRepository.findAll();

        // Define weights for each criterion
        double isAcceptedWeight = 0.4;
        double elapsedTimeWeight = 0.2;
        double reputationWeight = 0.2;
        double scoreWeight = 0.2;

        // Map Answer to AnswerResponse and return
        return answers.stream()
                .map(answer -> new AnswerResponse(
                        answer.getAnswerId(),
                        Double.parseDouble(String.format("%.2f", calculateQualityScore(answer, isAcceptedWeight, elapsedTimeWeight, reputationWeight, scoreWeight))),
                        answer.getAccountId()
                ))
                .sorted((a1, a2) -> Double.compare(a2.qualityScore(), a1.qualityScore()))  // Sort by qualityScore descending
                .limit(topN)
                .collect(Collectors.toList());
    }


    private double calculateQualityScore(Answer answer, double isAcceptedWeight, double elapsedTimeWeight,
                                         double reputationWeight, double scoreWeight) {

        // Calculate elapsed time in hours
        long elapsedTime = answer.getCreatedDate() != null && answer.getQuestion() != null
                ? Math.abs(java.time.Duration.between(answer.getQuestion().getCreationDate(), answer.getCreatedDate()).toHours())
                : Long.MAX_VALUE;

        // Calculate individual scores
        double acceptedScore = answer.getIsAccepted() ? 1.0 : 0.0;
        double elapsedTimeScore = elapsedTime > 0 ? 1.0 / elapsedTime : 0.0; // Normalize inverse of elapsed time
        double reputationScore = answer.getOwnerReputation() != null ? Math.log10(answer.getOwnerReputation() + 1) : 0.0; // Log scale
        double score = answer.getScore() != null ? answer.getScore() : 0;

        // Combine scores using weights
        return (isAcceptedWeight * acceptedScore) +
                (elapsedTimeWeight * elapsedTimeScore) +
                (reputationWeight * reputationScore) +
                (scoreWeight * score);
    }



    private Map<String, Integer> analyzeErrors() {

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

        return exceptionCountMap;
    }



}
