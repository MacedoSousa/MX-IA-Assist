package com.macedxs.mx.api.v1.run;

public record ApprovalRejectionV1Request(String reason) {

    public ApprovalRejectionV1Request {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        reason = reason.trim();
    }
}
