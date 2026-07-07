package com.bnp.points;

import com.bnp.common.enums.Enums.PointsReason;
import com.bnp.user.User;
import com.bnp.user.UserDtos.UserCard;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsTransactionRepository repository;
    private final UserRepository userRepository;

    /** Award (or deduct, if negative) credibility points and record the transaction. */
    @Transactional
    public void award(Long userId, int amount, PointsReason reason, String description) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        int newBalance = (user.getPoints() == null ? 0 : user.getPoints()) + amount;
        user.setPoints(newBalance);
        userRepository.save(user);

        repository.save(PointsTransaction.builder()
                .userId(userId)
                .amount(amount)
                .reason(reason)
                .description(description)
                .balanceAfter(newBalance)
                .build());
    }

    public List<PointsTransaction> history(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ---------- Leaderboards ----------
    public List<UserCard> cityLeaderboard(String city) {
        return userRepository.findTop50ByCityIgnoreCaseOrderByPointsDesc(city)
                .stream().map(UserCard::from).toList();
    }

    public List<UserCard> stateLeaderboard(String state) {
        return userRepository.findTop50ByStateIgnoreCaseOrderByPointsDesc(state)
                .stream().map(UserCard::from).toList();
    }

    public List<UserCard> globalLeaderboard() {
        return userRepository.findTop50ByOrderByPointsDesc()
                .stream().map(UserCard::from).toList();
    }
}
