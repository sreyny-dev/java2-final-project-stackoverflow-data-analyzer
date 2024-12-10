package sustech.java2finalproject.init;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackExchangeAnswersResponse {

    // The 'items' field in the response JSON
    @JsonProperty("items")
    private List<AnswerItem> items;

    // Nested class to represent each question item
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AnswerItem {
        // The owner of the question
        private Owner owner;

        // Various metadata related to the question
        @JsonProperty("answer_id")
        private Long answerId;

        @JsonProperty("question_id")
        private Long questionStackId;

        @JsonProperty("is_accepted")
        private Boolean isAccepted;

        @JsonProperty("answer_count")
        private Long answerCount;

        private Integer score;

        @JsonProperty("creation_date")
        private Long creationDate;

        @JsonProperty("owner_id")
        private Long ownerId;

    }

    // Nested class to represent the owner of a question
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Owner {
        private Long reputation;
    }
}
