package sustech.java2finalproject.feature.question.dto;

public record AnswerResponse(
        Double qualityScore,
        Double timeElapse,
        Long ownerReputation,
        Integer score,
        Boolean isAccepted,
        Long answerLength,
        Long answerId
) {

}
