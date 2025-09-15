package site.codemonster.comon.domain.problem.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProblemResponseEnum {

    // 문제 체크 관련
    PROBLEM_CHECK_SUCCESS("문제 정보 조회에 성공했습니다.", HttpStatus.OK.value()),
    PROBLEM_CHECK_DUPLICATE("이미 등록된 문제입니다.", HttpStatus.OK.value()),

    // 문제 등록 관련
    PROBLEM_BATCH_REGISTER_SUCCESS("문제 일괄 등록에 성공했습니다.", HttpStatus.CREATED.value()),

    // 문제 수정 관련
    PROBLEM_UPDATE_SUCCESS("문제 정보 수정에 성공했습니다.", HttpStatus.OK.value()),

    // 문제 삭제 관련
    PROBLEM_DELETE_SUCCESS("문제 삭제에 성공했습니다.", HttpStatus.OK.value()),

    // 문제 조회 관련
    PROBLEM_LIST_GET_SUCCESS("문제 목록 조회에 성공했습니다.", HttpStatus.OK.value()),
    PROBLEM_STATISTICS_GET_SUCCESS("문제 통계 조회에 성공했습니다.", HttpStatus.OK.value());

    private final String message;
    private final int statusCode;
}
