package sustech.java2finalproject.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sustech.java2finalproject.domain.Answer;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.repository.AnswerRepository;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class DataInit {

    private final OwnerRepository ownerRepository;
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final AnswerRepository answerRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(DataInit.class);

//    @PostConstruct
    public void init() {
        logger.info("Starting to fetch data from Stack Overflow API...");

        int totalQuestions = 1000; // Set desired number
        List<StackExchangeResponse.QuestionItem> questions = fetchQuestions(totalQuestions);

        logger.info("Fetched {} questions.", questions.size());

        for (StackExchangeResponse.QuestionItem item : questions) {
            try {
                Owner owner = saveOwner(item.getOwner());
                Question question = saveQuestion(item, owner);
                saveTags(item.getTags(), question);
                fetchAndSaveAnswers(question);
            } catch (Exception e) {
                logger.error("Error processing question with ID: " + item.getQuestionId(), e);
            }
        }

        logger.info("Data initialization completed.");
    }

    private Owner saveOwner(StackExchangeResponse.Owner apiOwner) {
        // Check if owner exists, if not, save it
        Owner owner = null;

        if (apiOwner.getAccountId() != null) {
            owner = ownerRepository.findByAccountId(Math.toIntExact(apiOwner.getAccountId()));
        }

        if (owner == null) {
            owner = new Owner();
            owner.setAccountId(apiOwner.getAccountId());
            owner.setReputation(apiOwner.getReputation());

            // Check if userId is not null before parsing
            if (apiOwner.getUserId() != null) {
                owner.setUserId(Long.valueOf(apiOwner.getUserId()));
            } else {
                // Handle the case where userId is null, e.g., set a default value or skip saving
                owner.setUserId(0L); // Set default value or handle accordingly
            }

            owner.setDisplayName(apiOwner.getDisplayName());
            owner = ownerRepository.save(owner);
        }
        return owner;
    }


    private Question saveQuestion(StackExchangeResponse.QuestionItem item, Owner owner) {
        // Create and save the Question
        Question question = new Question();
        question.setIsAnswer(item.getIsAnswered());
        question.setViewCount(item.getViewCount());
        question.setAnswerCount(item.getAnswerCount());
        question.setScore(item.getScore());
        question.setCreationDate(convertToLocalDateTime(item.getCreationDate()));
        question.setQuestionStackId(item.getQuestionId());

        question.setTitle(item.getTitle());
        question.setBody(item.getBody());
        question.setOwner(owner);


        if(item.getAcceptedAnswerId() != null){
            question.setAcceptedAnswerId(item.getAcceptedAnswerId());
        }


        return questionRepository.save(question);
    }

    private void saveTags(List<String> tagNames, Question question) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            // Check if tag exists, if not, create and save it
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tag = tagRepository.save(tag);
            }
            tags.add(tag);
        }
        question.setTags(tags);
        questionRepository.save(question);
    }


    private LocalDateTime convertToLocalDateTime(Long timestamp) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneOffset.UTC) // If you want to store in UTC
                .toLocalDateTime();
    }


    private List<StackExchangeResponse.QuestionItem> fetchQuestions(int totalQuestions) {
        List<StackExchangeResponse.QuestionItem> allQuestions = new ArrayList<>();
        int totalPages = (totalQuestions + 99) / 100; // Calculate pages required

        // The new URL you want to use
        String baseUrl = "https://api.stackexchange.com/2.3/questions?order=desc&sort=activity&site=stackoverflow&filter=withbody&pagesize=100";

        for (int page = 1; page <= totalPages; page++) {
            // Append page number to the URL
            String url = baseUrl + "&page=" + page;
            String response = restTemplate.getForObject(url, String.class);

            try {
                StackExchangeResponse stackExchangeResponse = objectMapper.readValue(response, StackExchangeResponse.class);
                allQuestions.addAll(stackExchangeResponse.getItems());
            } catch (Exception e) {
                logger.error("Error fetching questions on page " + page, e);
            }
        }

        return allQuestions;
    }

    private void fetchAndSaveAnswers(Question question) {
        String url = "https://api.stackexchange.com/2.3/questions/" + question.getQuestionStackId() + "/answers?order=desc&sort=activity&site=stackoverflow";
        int retryCount = 0;
        int maxRetries = 5;  // Maximum retry attempts
        long backoffTime = 2000;  // Initial backoff time in milliseconds (2 seconds)

        while (retryCount < maxRetries) {
            try {
                // Fetch the response from Stack Exchange API
                String response = restTemplate.getForObject(url, String.class);

                // Deserialize the response into Java objects
                StackExchangeAnswersResponse stackExchangeAnswersResponse = objectMapper.readValue(response, StackExchangeAnswersResponse.class);

                // Process each answer in the response
                for (StackExchangeAnswersResponse.AnswerItem answerItem : stackExchangeAnswersResponse.getItems()) {
                    // Extract the owner's reputation from the answer response
                    Long ownerReputation = answerItem.getOwner().getReputation();

                    // Create and populate the Answer object
                    Answer answer = new Answer();
                    answer.setAnswerId(answerItem.getAnswerId());
                    answer.setScore(answerItem.getScore());
                    answer.setIsAccepted(answerItem.getIsAccepted());
                    answer.setCreatedDate(convertToLocalDateTime(answerItem.getCreationDate()));
                    answer.setQuestionStackId(answerItem.getQuestionStackId());
                    answer.setQuestion(question);
                    answer.setOwnerReputation(ownerReputation);  // Set the reputation directly

                    // Save the answer to the repository
                    answerRepository.save(answer);
                }

                // If the request is successful, exit the loop
                return;
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    // Handle the 429 Too Many Requests error
                    logger.warn("Too many requests, retrying... (Attempt " + (retryCount + 1) + ")");
                    retryCount++;

                    // Wait before retrying with exponential backoff
                    try {
                        Thread.sleep(backoffTime);
                        backoffTime *= 2;  // Exponential backoff: double the wait time after each retry
                    } catch (InterruptedException ex) {
                        // Handle interruption during sleep
                        Thread.currentThread().interrupt();
                        break;  // Exit the loop if interrupted
                    }
                } else {
                    // Handle other errors (e.g., 4xx, 5xx)
                    logger.error("Error fetching answers for question ID: " + question.getQuestionStackId(), e);
                    break;  // Exit the loop for other error types
                }
            } catch (Exception e) {
                // Handle any other exceptions that may occur
                logger.error("Unexpected error fetching answers for question ID: " + question.getQuestionStackId(), e);
                break;
            }
        }

        // If max retries are reached, log an error
        if (retryCount >= maxRetries) {
            logger.error("Max retry attempts reached for question ID: " + question.getQuestionStackId());
        }
    }


}



