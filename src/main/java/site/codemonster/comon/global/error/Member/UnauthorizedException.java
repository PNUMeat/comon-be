package site.codemonster.comon.global.error.Member;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class UnauthorizedException extends ComonException {
  public UnauthorizedException() {
    super(ErrorCode.UNAUTHORIZED_MEMBER_ERROR);
  }
}