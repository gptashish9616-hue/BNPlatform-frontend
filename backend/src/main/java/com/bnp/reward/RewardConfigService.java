package com.bnp.reward;

import com.bnp.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardConfigService {

    private final RewardConfigRepository rewardConfigRepository;

    /** Returns the single reward-config row, creating the default one on first access. */
    @Transactional
    public RewardConfig getConfig() {
        return rewardConfigRepository.findAll().stream().findFirst()
                .orElseGet(() -> rewardConfigRepository.save(RewardConfig.builder().build()));
    }

    @Transactional
    public RewardConfig updateConfig(RewardConfig update) {
        if (update.getFreeMonthCap() == null || update.getFreeMonthCap() < 0
                || update.getPointsWithFreeMonth() == null || update.getPointsWithFreeMonth() < 0
                || update.getPointsAfterCap() == null || update.getPointsAfterCap() < 0) {
            throw new BadRequestException("Reward values must be zero or positive numbers");
        }

        RewardConfig config = getConfig();
        config.setFreeMonthCap(update.getFreeMonthCap());
        config.setPointsWithFreeMonth(update.getPointsWithFreeMonth());
        config.setPointsAfterCap(update.getPointsAfterCap());
        return rewardConfigRepository.save(config);
    }
}
