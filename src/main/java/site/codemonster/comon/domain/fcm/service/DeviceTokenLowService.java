package site.codemonster.comon.domain.fcm.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codemonster.comon.domain.fcm.entity.DeviceToken;
import site.codemonster.comon.domain.fcm.repository.DeviceTokenRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceTokenLowService {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceToken save(DeviceToken deviceToken) {
        return deviceTokenRepository.save(deviceToken);
    }

    public void deleteByMemberId(Long memberId) {
        deviceTokenRepository.deleteByMemberId(memberId);
    }

    public List<DeviceToken> findByMemberId(Long memberId) {
        return deviceTokenRepository.findByMemberId(memberId);
    }

    public boolean existsByMemberIdAndToken(Long memberId, String token) {
        Optional<DeviceToken> findToken = deviceTokenRepository.findByMemberIdAndToken(memberId, token);

        if (findToken.isPresent()) return true;
        return false;
    }

    public void deleteByToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
