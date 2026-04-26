package site.codemonster.comon.domain.fcm.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.entity.Alarm;
import site.codemonster.comon.domain.alarm.service.AlarmLowService;
import site.codemonster.comon.domain.article.entity.Article;
import site.codemonster.comon.domain.article.entity.ArticleComment;
import site.codemonster.comon.domain.auth.entity.Member;
import site.codemonster.comon.domain.auth.service.MemberLowService;
import site.codemonster.comon.domain.fcm.dto.DeviceTokenRequest;
import site.codemonster.comon.domain.fcm.entity.DeviceToken;
import site.codemonster.comon.global.error.fcm.DuplicateDeviceTokenException;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenLowService deviceTokenLowService;
    private final AlarmLowService alarmLowService;
    private final MemberLowService memberLowService;

    public DeviceToken addDeviceToken(Member member, DeviceTokenRequest deviceTokenRequest) {

        if (deviceTokenLowService.existsByMemberIdAndToken(member.getId(), deviceTokenRequest.token()))
            throw new DuplicateDeviceTokenException();

        return deviceTokenLowService.save(new DeviceToken(member, deviceTokenRequest.token()));

    }

    @Async("sendAlarm")
    public void sendArticleAlarm(Long memberId, String articleTitle, String articleComment) {

        List<DeviceToken> deviceTokens = deviceTokenLowService.findByMemberId(memberId);

        Member articleOwner = memberLowService.getReferenceById(memberId);

        String title = articleTitle + "에 댓글이 달렸습니다.";

        alarmLowService.save(new Alarm(title,articleComment, articleOwner));

        deviceTokens.forEach(deviceToken -> {
            sendMessageTo(deviceToken.getToken(), title, articleComment);
        });

    }

    private void sendMessageTo(String targetToken, String title, String body) {

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .build();

        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        ApiFuture<String> future = firebaseMessaging.sendAsync(message);

        future.addListener(() -> {
            try {
                future.get();
            } catch (Exception ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof FirebaseMessagingException fme) {
                    if (fme.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                        deviceTokenLowService.deleteByToken(targetToken);
                    }
                }
            }
        }, Runnable::run);
    }
}
