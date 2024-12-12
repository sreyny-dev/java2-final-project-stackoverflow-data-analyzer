package sustech.java2finalproject.feature.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import sustech.java2finalproject.domain.Answer;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.repository.AnswerRepository;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;
import sustech.java2finalproject.init.StackExchangeResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class RethreiveData {
    private final OwnerRepository ownerRepository;
    private final QuestionRepository questionRepository;
    private final TagRepository tagRepository;
    private final AnswerRepository answerRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(RethreiveData.class);

    @GetMapping("/data")
    public void init() {
        logger.info("Starting to fetch data from Stack Overflow API...");

        int totalQuestions = 1000; // Set desired number
        List<StackExchangeResponse.QuestionItem> questions = fetchQuestions(totalQuestions);

//        logger.info("Fetched {} questions.", questions.size());



        for (StackExchangeResponse.QuestionItem item : questions) {

                System.out.println(item.getAnswers());

            try {
                Owner owner = saveOwner(item.getOwner());
                Question question = saveQuestion(item, owner);
                saveTags(item.getTags(), question);

                // Check if answers exist before calling saveAnswer
                if (item.getAnswers() != null) {
                    saveAnswer(item.getAnswers(), item, question);
                } else {
//                    logger.info("No answers available for Question ID {}", item.getQuestionId());
                }
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

    private void saveAnswer(List<StackExchangeResponse.Answer> answers, StackExchangeResponse.QuestionItem questionItem, Question question) {
        if (answers == null || answers.isEmpty()) {
            logger.info("No answers found for Question ID {}", questionItem.getQuestionId());
            return;
        }

        for (StackExchangeResponse.Answer apiAnswer : answers) {
            try {
                // Log the response for the answer
                logger.info("Processing Answer ID {} for Question ID {}", apiAnswer.getAnswerId(), questionItem.getQuestionId());


                // Create a new Answer object if it doesn't exist
                Answer newAnswer = new Answer();
                newAnswer.setAnswerId(apiAnswer.getAnswerId());
                newAnswer.setQuestionStackId(questionItem.getQuestionId());
                newAnswer.setScore(apiAnswer.getScore());
                newAnswer.setIsAccepted(apiAnswer.getIsAccepted());
                newAnswer.setCreatedDate(convertToLocalDateTime(apiAnswer.getCreatedDate()));

                // Extract owner details from the API response
                if (apiAnswer.getAnswerOwner() != null) {
                    newAnswer.setAccountId(apiAnswer.getAnswerOwner().getAccountId());
                    newAnswer.setUserId(Long.valueOf(apiAnswer.getAnswerOwner().getUserId())); // Ensure correct data type
                    newAnswer.setOwnerReputation(apiAnswer.getAnswerOwner().getReputation());
                } else {
                    logger.warn("No owner information for Answer ID {}", apiAnswer.getAnswerId());
                }

                // Associate the question
                newAnswer.setQuestion(question);

                // Save the new answer to the database
                answerRepository.save(newAnswer);
                logger.info("Answer with ID {} saved for Question ID {}", apiAnswer.getAnswerId(), questionItem.getQuestionId());
            } catch (Exception e) {
                logger.error("Error saving answer with ID {} for Question ID {}", apiAnswer.getAnswerId(), questionItem.getQuestionId(), e);
            }
        }

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

        // Correct base URL without the 'page=1' parameter
        String baseUrl = "https://api.stackexchange.com/2.3/questions?pagesize=100&order=desc&sort=activity&tagged=java&site=stackoverflow&filter=!4)7wd.G6sgmI5NUcy";

        for (int page = 1; page <= totalPages; page++) {
            // Append the correct page number to the URL
            String url = baseUrl + "&page=" + page;
            logger.debug("Fetching questions from URL: " + url);

            try {
                // Make the API request and capture the response
                String response = restTemplate.getForObject(url, String.class);
                logger.debug("API Response for page " + page + ": " + response);

                // Deserialize the response into a StackExchangeResponse object
                StackExchangeResponse stackExchangeResponse = objectMapper.readValue(response, StackExchangeResponse.class);
                logger.debug("Deserialized Response: " + stackExchangeResponse);

                // Add all items from the current page to the complete list
                allQuestions.addAll(stackExchangeResponse.getItems());
            } catch (Exception e) {
                // Log the error if something goes wrong
                logger.error("Error fetching questions on page " + page, e);
            }

            // Optional: Add a delay to avoid rate-limiting issues
            try {
                Thread.sleep(1000); // Wait for 1 second between requests
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        return allQuestions;
    }

}
