package sustech.java2finalproject.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sustech.java2finalproject.domain.Owner;
import sustech.java2finalproject.domain.Question;
import sustech.java2finalproject.domain.Tag;
import sustech.java2finalproject.feature.question.repository.OwnerRepository;
import sustech.java2finalproject.feature.question.repository.QuestionRepository;
import sustech.java2finalproject.feature.question.repository.TagRepository;


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
            } catch (Exception e) {
                logger.error("Error processing question with ID: " + item.getQuestionId(), e);
            }
        }

        logger.info("Data initialization completed.");
    }

    private Owner saveOwner(StackExchangeResponse.Owner apiOwner) {
        // Check if owner exists, if not, save it
        Owner owner = ownerRepository.findByAccountId(apiOwner.getAccountId());
        if (owner == null) {
            owner = new Owner();
            owner.setAccountId(apiOwner.getAccountId());
            owner.setReputation(apiOwner.getReputation());
            owner.setUserId(apiOwner.getUserId());
            owner.setUserType(apiOwner.getUserType());
            owner.setAcceptRate(apiOwner.getAcceptRate());
            owner.setProfileImage(apiOwner.getProfileImage());
            owner.setDisplayName(apiOwner.getDisplayName());
            owner.setLink(apiOwner.getLink());
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
        question.setLastActivityDate(convertToLocalDateTime(item.getLastActivityDate()));
        question.setCreationDate(convertToLocalDateTime(item.getCreationDate()));
        question.setQuestionId(item.getQuestionId());
        question.setContentLicense(item.getContentLicense());
        question.setLink(item.getLink());
        question.setTitle(item.getTitle());
        question.setOwner(owner);


        if(item.getAcceptedAnswerId() != null){
            question.setAcceptedAnswerId(item.getAcceptedAnswerId());
        }

        if(item.getClosedDate() != null){
            question.setClosedDate(convertToLocalDateTime(item.getClosedDate()));
        }
        if(item.getClosedReason() != null){
            question.setClosedReason(item.getClosedReason());
        }

        if(item.getCommunityOwnedDate() != null){
            question.setCommunityOwnedDate(convertToLocalDateTime(item.getCommunityOwnedDate()));
        }

        if(item.getProtectedDate() != null){
            question.setProtectedDate(convertToLocalDateTime(item.getProtectedDate()));
        }

        if(item.getLockedDate() != null){
            question.setLockedDate(convertToLocalDateTime(item.getLockedDate()));
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

        for (int page = 1; page <= totalPages; page++) {
            String url = String.format("https://api.stackexchange.com/2.3/questions?page=%d&pagesize=100&order=asc&sort=activity&tagged=java&site=stackoverflow", page);
            String response = restTemplate.getForObject(url, String.class);
            try {
                StackExchangeResponse stackExchangeResponse = objectMapper.readValue(response, StackExchangeResponse.class);
                allQuestions.addAll(stackExchangeResponse.getItems());
            } catch (Exception e) {
                e.printStackTrace(); // You can log this for debugging
            }
        }
        return allQuestions;
    }


}


