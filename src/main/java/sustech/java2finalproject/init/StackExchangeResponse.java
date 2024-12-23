package sustech.java2finalproject.init;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackExchangeResponse {

    // The 'items' field in the response JSON
    @JsonProperty("items")
    private List<QuestionItem> items;

    // Nested class to represent each question item
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestionItem {

        // The owner of the question
        private Owner owner;

        // List of tags associated with the question
        private List<String> tags;

        @JsonProperty("answers")
        private List<Answer> answers;

        // Various metadata related to the question
        @JsonProperty("is_answered")
        private Boolean isAnswered;

        @JsonProperty("view_count")
        private Long viewCount;

        @JsonProperty("accepted_answer_id")
        private Long acceptedAnswerId;

        @JsonProperty("answer_count")
        private Long answerCount;

        private Long score;

        @JsonProperty("creation_date")
        private Long creationDate;

        @JsonProperty("question_id")
        private Long questionId;

        private String link;

        private String title;

        @JsonProperty("body")
        private String body;

    }

    // Nested class to represent the owner of a question
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {

        @JsonProperty("account_id")
        private Long accountId;

        private Long reputation;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("user_type")
        private String userType;

        @JsonProperty("display_name")
        private String displayName;

    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Answer {

        @JsonProperty("is_accepted")
        private Boolean isAccepted;
        private Integer score;
        @JsonProperty("creation_date")
        private Long createdDate;
        @JsonProperty("answer_id")
        private Long answerId;
        @JsonProperty("question_id")
        private Long questionStackId;

        @JsonProperty("reputation")
        private Long ownerReputation;

        private String body;

        @JsonProperty("owner")
        private AnswerOwner answerOwner;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)

    public static class AnswerOwner {

        @JsonProperty("account_id")
        private Long accountId;

        private Long reputation;

        @JsonProperty("user_id")
        private String userId;
    }



}

