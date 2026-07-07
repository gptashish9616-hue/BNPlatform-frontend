package com.bnp.common.enums;

/** Small shared enums used across modules, grouped to keep the tree tidy. */
public final class Enums {

    private Enums() {}

    public enum AccountStatus { ACTIVE, SUSPENDED, PENDING, DEACTIVATED }

    public enum ReferralType { APP_REFERRAL, DEAL_REFERRAL }

    public enum ReferralStatus { PENDING, ACCEPTED, COMPLETED, REJECTED, EXPIRED }

    public enum InviteStatus { SENT, ACCEPTED, EXPIRED }

    public enum RequirementStatus { OPEN, IN_PROGRESS, FULFILLED, CLOSED }

    public enum SubscriptionStatus { ACTIVE, EXPIRED, CANCELLED, PENDING }

    public enum BillingCycle { MONTHLY, QUARTERLY, YEARLY }

    public enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }

    public enum NotificationType { SYSTEM, REFERRAL, REQUIREMENT, CHAT, SUBSCRIPTION, POINTS, REVIEW }

    public enum NotificationChannel { IN_APP, EMAIL, SMS, WHATSAPP }

    public enum PointsReason { REFERRAL_COMPLETED, REQUIREMENT_FULFILLED, PROFILE_COMPLETED, REVIEW_RECEIVED, INVITE_ACCEPTED }

    public enum DocumentStatus { PENDING, VERIFIED, REJECTED }

    public enum GroupType   { CITY, CATEGORY }

    public enum GroupStatus { ACTIVE, INACTIVE }
}
