package com.bnp.requirement;

import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Enums.PointsReason;
import com.bnp.common.enums.Enums.RequirementStatus;
import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import com.bnp.notification.NotificationService;
import com.bnp.points.PointsService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequirementService {

    private static final int REQUIREMENT_FULFILLED_POINTS = 75;

    private final RequirementRepository requirementRepository;
    private final RequirementResponseRepository responseRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional
    public Requirement create(Long posterId, Requirement req) {
        req.setId(null);
        req.setPosterId(posterId);
        req.setStatus(RequirementStatus.OPEN);
        req.setResponseCount(0);
        req.setAcceptedResponderId(null);
        User poster = userRepository.findById(posterId).orElse(null);
        if (poster != null) {
            req.setLatitude(poster.getLatitude());
            req.setLongitude(poster.getLongitude());
        }
        Requirement saved = requirementRepository.save(req);

        notificationService.notifyAdmins(NotificationType.REQUIREMENT,
                "New requirement posted",
                (poster != null ? poster.getFullName() : "A member") + " posted \"" + saved.getTitle() + "\".",
                "/pages/dashboard/admin-dashboard.html", posterId);

        return saved;
    }

    public List<Requirement> all() {
        return requirementRepository.findByStatusOrderByCreatedAtDesc(RequirementStatus.OPEN);
    }

    public List<Requirement> mine(Long posterId) {
        return requirementRepository.findByPosterIdOrderByCreatedAtDesc(posterId);
    }

    public Requirement get(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", id));
    }

    public List<RequirementResponse> responses(Long requirementId) {
        return responseRepository.findByRequirementIdOrderByCreatedAtDesc(requirementId);
    }

    @Transactional
    public RequirementResponse respond(Long responderId, Long requirementId, String message, String quote) {
        Requirement requirement = get(requirementId);
        if (requirement.getPosterId().equals(responderId)) {
            throw new BadRequestException("You cannot respond to your own requirement");
        }
        if (requirement.getStatus() != RequirementStatus.OPEN) {
            throw new BadRequestException("This requirement is no longer open");
        }

        RequirementResponse response = RequirementResponse.builder()
                .requirementId(requirementId)
                .responderId(responderId)
                .message(message)
                .quote(quote)
                .build();
        response = responseRepository.save(response);

        requirement.setResponseCount(requirement.getResponseCount() + 1);
        requirementRepository.save(requirement);

        notificationService.notify(requirement.getPosterId(), NotificationType.REQUIREMENT,
                "New response to your requirement",
                "A professional responded to \"" + requirement.getTitle() + "\".",
                "/pages/requirements/requirement-details.html?id=" + requirementId,
                responderId);

        return response;
    }

    /** Poster accepts a response → requirement moves to IN_PROGRESS. */
    @Transactional
    public Requirement acceptResponse(Long posterId, Long requirementId, Long responseId) {
        Requirement requirement = get(requirementId);
        if (!requirement.getPosterId().equals(posterId)) {
            throw new BadRequestException("Only the poster can accept a response");
        }
        RequirementResponse response = responseRepository.findById(responseId)
                .filter(r -> r.getRequirementId().equals(requirementId))
                .orElseThrow(() -> new ResourceNotFoundException("Response", responseId));

        response.setAccepted(true);
        responseRepository.save(response);

        requirement.setStatus(RequirementStatus.IN_PROGRESS);
        requirement.setAcceptedResponderId(response.getResponderId());
        requirementRepository.save(requirement);

        notificationService.notify(response.getResponderId(), NotificationType.REQUIREMENT,
                "Your response was accepted",
                "The poster accepted your response to \"" + requirement.getTitle() + "\".",
                "/pages/requirements/requirement-details.html?id=" + requirementId,
                posterId);

        return requirement;
    }

    /** Poster marks the requirement fulfilled → poster earns credibility points. */
    @Transactional
    public Requirement markFulfilled(Long posterId, Long requirementId) {
        Requirement requirement = get(requirementId);
        if (!requirement.getPosterId().equals(posterId)) {
            throw new BadRequestException("Only the poster can mark this requirement fulfilled");
        }
        if (requirement.getStatus() == RequirementStatus.FULFILLED) {
            return requirement;
        }

        requirement.setStatus(RequirementStatus.FULFILLED);
        requirementRepository.save(requirement);

        pointsService.award(posterId, REQUIREMENT_FULFILLED_POINTS,
                PointsReason.REQUIREMENT_FULFILLED, "Requirement #" + requirementId + " fulfilled");
        notificationService.notify(posterId, NotificationType.POINTS,
                "Requirement fulfilled",
                "You earned " + REQUIREMENT_FULFILLED_POINTS + " points.",
                "/pages/requirements/my-requirements.html");

        return requirement;
    }

    /** Admin edit — updates any provided fields, including status, bypassing the poster-only checks. */
    @Transactional
    public Requirement adminUpdate(Long id, Requirement patch) {
        Requirement requirement = get(id);
        if (patch.getTitle() != null) requirement.setTitle(patch.getTitle());
        if (patch.getCategory() != null) requirement.setCategory(patch.getCategory());
        if (patch.getDescription() != null) requirement.setDescription(patch.getDescription());
        if (patch.getBudget() != null) requirement.setBudget(patch.getBudget());
        if (patch.getCity() != null) requirement.setCity(patch.getCity());
        if (patch.getStatus() != null) requirement.setStatus(patch.getStatus());
        return requirementRepository.save(requirement);
    }

    /** Admin status override — e.g. force close/reopen without going through the poster-only flow. */
    @Transactional
    public Requirement adminUpdateStatus(Long id, RequirementStatus status) {
        Requirement requirement = get(id);
        requirement.setStatus(status);
        return requirementRepository.save(requirement);
    }

    /** Admin delete — removes the requirement and any responses posted to it. */
    @Transactional
    public void adminDelete(Long id) {
        Requirement requirement = get(id);
        responseRepository.deleteAll(responseRepository.findByRequirementIdOrderByCreatedAtDesc(id));
        requirementRepository.delete(requirement);
    }
}
