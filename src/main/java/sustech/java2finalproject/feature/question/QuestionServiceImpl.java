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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        return calculateTopEngagementTag(ownerRepository.findAll(), topN);
    }

    @Override
    public List<TopEngagementResponse> getTopEngagementTagByTopUser(int topN, int reputation) {
        List<Owner> topOwners = ownerRepository.findTopOwnersByReputation(reputation);
        return calculateTopEngagementTag(topOwners, topN);
    }

    private List<TopEngagementResponse> calculateTopEngagementTag(List<Owner> owners, int topN) {
        // Get all questions from owners
        List<Question> questions = owners.stream()
                .flatMap(owner -> owner.getQuestions().stream())
                .collect(Collectors.toList());

        // Define weights for each component of engagement
        double scoreWeight = 0.3;
        double reputationWeight = 0.05;
        double viewCountWeight = 0.3;
        double answerCountWeight = 0.35;

        // Calculate min and max for normalization
        double minScore = questions.stream().mapToDouble(q -> q.getScore() != null ? q.getScore() : 0).min().orElse(0);
        double maxScore = questions.stream().mapToDouble(q -> q.getScore() != null ? q.getScore() : 0).max().orElse(1);
        double minView = questions.stream().mapToDouble(q -> q.getViewCount() != null ? q.getViewCount() : 0).min().orElse(0);
        double maxView = questions.stream().mapToDouble(q -> q.getViewCount() != null ? q.getViewCount() : 0).max().orElse(1);
        double minAnswer = questions.stream().mapToDouble(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0).min().orElse(0);
        double maxAnswer = questions.stream().mapToDouble(q -> q.getAnswerCount() != null ? q.getAnswerCount() : 0).max().orElse(1);
        double minReputation = owners.stream().mapToDouble(o -> o.getReputation() != null ? o.getReputation() : 0).min().orElse(0);
        double maxReputation = owners.stream().mapToDouble(o -> o.getReputation() != null ? o.getReputation() : 0).max().orElse(1);

        // Create a map of tags and their total engagement and question count
        Map<Tag, double[]> tagEngagementMap = new HashMap<>();

        for (Question question : questions) {
            for (Tag tag : question.getTags()) {
                // Normalize each component
                double normalizedScore = normalize(question.getScore(), minScore, maxScore);
                double normalizedViewCount = normalize(question.getViewCount(), minView, maxView);
                double normalizedAnswerCount = normalize(question.getAnswerCount(), minAnswer, maxAnswer);
                double normalizedReputation = normalize(question.getOwner().getReputation(), minReputation, maxReputation);

                // Calculate engagement for the current question and tag
                double engagement = (
                        normalizedScore * scoreWeight +
                                normalizedViewCount * viewCountWeight +
                                normalizedAnswerCount * answerCountWeight +
                                normalizedReputation * reputationWeight
                );

                // Update the map with the total engagement and question count for this tag
                tagEngagementMap.compute(tag, (t, values) -> {
                    if (values == null) {
                        return new double[]{engagement, 1}; // New tag with the first question
                    } else {
                        values[0] += engagement; // Add to total engagement
                        values[1] += 1;           // Increment question count
                        return values;
                    }
                });
            }
        }

        // Calculate average engagement for each tag and round to 6 decimal places
        Map<Tag, Double> averageEngagementByTag = tagEngagementMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> round(entry.getValue()[0] / entry.getValue()[1], 6)
                ));

        // Sort tags by average engagement in descending order and limit to top N
        return averageEngagementByTag.entrySet()
                .stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(entry -> new TopEngagementResponse(entry.getKey().getName(), entry.getValue()))
                .collect(Collectors.toList());
    }

    // Helper method to normalize values
    private double normalize(Long value, double min, double max) {
        if (value == null) return 0;
        return (value - min) / (max - min);
    }

    // Helper method to round values to a specified number of decimal places
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
    public List<AnswerResponse> overallAnswerQuality(Integer topN) {
        List<Answer> answers = answerRepository.findAll();

        // Define weights for each criterion
        double isAcceptedWeight = 0.4;
        double elapsedTimeWeight = 0.2;
        double reputationWeight = 0.2;
        double scoreWeight = 0.2;

        // Process answers and map to responses with quality scores
        List<AnswerResponse> processedAnswers = answers.stream()
                .map(answer -> new AnswerResponse(
                        calculateQualityScore(answer, isAcceptedWeight, elapsedTimeWeight, reputationWeight, scoreWeight),
                        calculateElapseTime(answer),
                        answer.getOwnerReputation(),
                        answer.getScore(),
                        answer.getIsAccepted(),  // Pass isAccepted from Answer to AnswerResponse
                        answer.getAnswerLength(),
                        answer.getAnswerId()
                ))
                .sorted((a1, a2) -> Double.compare(a2.qualityScore(), a1.qualityScore())) // Sort by qualityScore descending
                .limit(topN)
                .collect(Collectors.toList());

        // Return the processed answers
        return processedAnswers;
    }


    private double calculateElapseTime(Answer answer) {
        if (answer.getCreatedDate() == null || answer.getQuestion() == null) {
            return Double.MAX_VALUE; // Use a large value for missing data
        }
        long elapsedTime = Math.abs(
                java.time.Duration.between(answer.getQuestion().getCreationDate(), answer.getCreatedDate()).toHours()
        );
        return elapsedTime > 0 ? elapsedTime : 1.0; // Avoid division by zero
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
        double qualityScore = (isAcceptedWeight * acceptedScore) +
                (elapsedTimeWeight * elapsedTimeScore) +
                (reputationWeight * reputationScore) +
                (scoreWeight * score);

        // Round to 2 decimal places using BigDecimal
        return BigDecimal.valueOf(qualityScore)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
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
