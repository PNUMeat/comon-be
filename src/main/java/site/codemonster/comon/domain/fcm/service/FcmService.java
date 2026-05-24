package site.codemonster.comon.domain.fcm.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.alarm.entity.AlarmType;
import site.codemonster.comon.domain.fcm.dto.DeviceTokenRequest;
import site.codemonster.comon.domain.fcm.entity.DeviceToken;
import site.codemonster.comon.domain.auth.entity.Member;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenLowService deviceTokenLowService;

    public DeviceToken addDeviceToken(Member member, DeviceTokenRequest deviceTokenRequest) {
        Optional<DeviceToken> deviceToken = deviceTokenLowService.findByToken(deviceTokenRequest.token());

        if (deviceToken.isPresent()) {
            deviceToken.get().updateMember(member);
            return deviceToken.get();
        }

        return deviceTokenLowService.save(new DeviceToken(member, deviceTokenRequest.token()));
    }

    @Async("sendAlarm")
    public void sendArticleAlarm(Long memberId, String articleTitle, String articleComment, String deepLink, Long routingId) {

        List<DeviceToken> deviceTokens = deviceTokenLowService.findByMemberId(memberId);


        deviceTokens.forEach(deviceToken -> {
            sendMessageTo(deviceToken.getToken(), articleTitle, articleComment, deepLink, routingId, AlarmType.ARTICLE_COMMENT_ALARM);
        });

    }

    private void sendMessageTo(String targetToken, String title, String body, String deepLink, Long routingId, AlarmType alarmType) {

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putData("deepLink", deepLink)
                .putData("routingId", String.valueOf(routingId))
                .putData("alarmType", alarmType.name())
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
