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
    public static class QuestionItem {

        // The owner of the question
        private Owner owner;

        // List of tags associated with the question
        private List<String> tags;

        // Various metadata related to the question
        @JsonProperty("is_answered")
        private Boolean isAnswered;

        @JsonProperty("view_count")
        private Long viewCount;

        @JsonProperty("accepted_answer_id")
        private Integer acceptedAnswerId;

        @JsonProperty("answer_count")
        private Long answerCount;

        @JsonProperty("closed_date")
        private Long closedDate;

        @JsonProperty("closed_reason")
        private String closedReason;

        private Long score;

        @JsonProperty("last_activity_date")
        private Long lastActivityDate;

        @JsonProperty("creation_date")
        private Long creationDate;

        @JsonProperty("question_id")
        private Integer questionId;

        @JsonProperty("content_license")
        private String contentLicense;

        private String link;

        private String title;

        @JsonProperty("last_edit_date")
        private Long lastEditDate;

        @JsonProperty("protected_date")
        private Long protectedDate;

        @JsonProperty("community_owned_date")
        private Long communityOwnedDate;

        @JsonProperty("locked_date")
        private Long lockedDate;

    }

    // Nested class to represent the owner of a question
    @Getter
    @Setter
    public static class Owner {

        @JsonProperty("account_id")
        private Integer accountId;

        private Integer reputation;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("user_type")
        private String userType;

        @JsonProperty("accept_rate")
        private Integer acceptRate;

        @JsonProperty("profile_image")
        private String profileImage;

        @JsonProperty("display_name")
        private String displayName;

        private String link;
    }
}

