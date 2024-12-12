package sustech.java2finalproject.feature.question.dto;

public record AnswerResponse(
        Long answerId,
        Double qualityScore,
        Long accountId

) {

}
