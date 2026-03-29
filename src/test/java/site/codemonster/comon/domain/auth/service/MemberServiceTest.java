package site.codemonster.comon.domain.auth.service;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.repository.MemberRepository;
import site.codemonster.comon.domain.team.entity.Team;
import site.codemonster.comon.domain.team.repository.TeamRepository;
import site.codemonster.comon.domain.teamMember.repository.TeamMemberRepository;
import site.codemonster.comon.domain.util.TestUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void deleteMemberConcurrencyTest() throws InterruptedException {

        Member member1 = memberRepository.save(TestUtil.createMember());
        Member member2 = memberRepository.save(TestUtil.createOtherMember());

        List<Member> members = Arrays.asList(member1, member2);

        Team team = teamRepository.save(TestUtil.createTeam());

        teamMemberRepository.save(TestUtil.createTeamManager(team, member1));
        teamMemberRepository.save(TestUtil.createTeamManager(team, member2));

        int threadCount = 2;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (Member member : members) {
            executorService.submit(() -> {
                try{
                    memberService.deleteMember(member.getId());

                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(teamRepository.existsById(team.getTeamId())).isFalse();
        });
    }
}
