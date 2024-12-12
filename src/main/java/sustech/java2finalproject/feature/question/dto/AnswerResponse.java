package sustech.java2finalproject.feature.question.dto;

public record AnswerResponse(
        Long answerId,
        Double qualityScore,
        Long accountId,
        Long questionId,
        Double timeElapse,
        Long ownerReputation,
        Integer score,
        Boolean isAccepted
) {

}
