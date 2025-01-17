package PlayMakers.SportsIT.domain;

import PlayMakers.SportsIT.enums.CompetitionType;
import PlayMakers.SportsIT.enums.Subscribe;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
//@Primary  // 가장 우선 적용
@Qualifier("mainCompetitionPolicy")
public class CompetitionPolicyImpl_v1 implements CompetitionPolicy{

    @Override
    public CompetitionType getCompetitionType(Member host) {
        if (host.getSubscription() == Subscribe.BASIC_HOST || host.getSubscription() == Subscribe.FREE) return CompetitionType.FREE;
        else if (host.getSubscription() == Subscribe.PREMIUM_HOST) return CompetitionType.PREMIUM;
        else if (host.getSubscription() == Subscribe.VIP_HOST) return CompetitionType.VIP;
        else throw new IllegalArgumentException("회원의 구독 정보가 올바르지 않습니다. 회원 타입 : " + host.getMemberType());
    }

    @Override
    public CompetitionState getCompetitionState(Competition competition) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(competition.getRecruitingStart())) return CompetitionState.PLANNING;
        else if (now.isBefore(competition.getRecruitingEnd())) return CompetitionState.RECRUITING;
        else if (now.isBefore(competition.getStartDate())) return CompetitionState.RECRUITING_END;
        else if (now.isAfter(competition.getStartDate()) || now.isEqual(competition.getStartDate())) return CompetitionState.IN_PROGRESS;
        else throw new IllegalArgumentException("모집 및 대회 일정이 정확한지 확인해주세요.");
    }
}
