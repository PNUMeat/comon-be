package site.codemonster.comon.global.error.Member;


import site.codemonster.comon.global.error.ComonException;
import site.codemonster.comon.global.error.ErrorCode;

public class MemberNotFoundException extends ComonException {
  public MemberNotFoundException() {
    super(ErrorCode.MEMBER_NOT_FOUND_ERROR);
  }

}